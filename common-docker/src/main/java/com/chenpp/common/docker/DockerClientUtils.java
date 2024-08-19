package com.chenpp.common.docker;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.CreateServiceResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.Endpoint;
import com.github.dockerjava.api.model.EndpointResolutionMode;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfigProtocol;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.ResourceRequirements;
import com.github.dockerjava.api.model.ResourceSpecs;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceModeConfig;
import com.github.dockerjava.api.model.ServiceReplicatedModeOptions;
import com.github.dockerjava.api.model.ServiceRestartPolicy;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.SwarmNode;
import com.github.dockerjava.api.model.Task;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.api.model.TaskState;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @date 2023/9/27 7:18 下午
 **/
@Slf4j
public class DockerClientUtils {

    /**
     * 连接Docker服务器
     */
    public static DockerClient buildDockerClient(DockerProperties dockerProperties) {
        // 配置docker CLI的一些选项
        DefaultDockerClientConfig dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerProperties.getHost())
                // 与docker版本对应，参考https://docs.docker.com/engine/api/#api-version-matrix
                // 或者通过docker version指令查看api version
                .withApiVersion(dockerProperties.getApiVersion())
                //下载源地址（docker镜像存放的地址）
                .withRegistryUrl(dockerProperties.getRegistryUrl())
                .withRegistryUsername(dockerProperties.getRegistryUsername())
                .withRegistryPassword(dockerProperties.getRegistryPassword())
                .withRegistryEmail(dockerProperties.getRegistryEmail())
                .withDockerTlsVerify(dockerProperties.getTlsVerify())
                // Tls压缩包解压的路径
                .withDockerCertPath(dockerProperties.getTlsCertPath())
                .build();

        // 创建DockerHttpClient
        DockerHttpClient httpClient = new ApacheDockerHttpClient
                .Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(dockerClientConfig)
                .withDockerHttpClient(httpClient)
                .build();
        Info info = dockerClient.infoCmd().exec();
        log.info("connect to docker, info: {}", info);
        return dockerClient;
    }

    /**
     * 创建网络
     *
     * @param client      DockerClient
     * @param networkName 网络名称
     * @param driver      driver
     */
    public static void createNetwork(DockerClient client, String networkName, String driver) {
        CreateNetworkResponse networkResponse = client.createNetworkCmd()
                .withName(networkName)
                .withDriver(driver)
                .exec();
        log.info("create network response: {}", networkResponse);
    }

    /**
     * 创建容器
     *
     * @param client DockerClient
     * @return 容器id
     */
    public static String createContainer(DockerClient client, DockerContainerProperties containerProperties) {

        // 端口绑定
        Map<Integer, Integer> portMap = Optional.ofNullable(containerProperties.getPortBindMap()).orElse(new HashMap<>());
        Iterator<Map.Entry<Integer, Integer>> iterator = portMap.entrySet().iterator();
        List<PortBinding> portBindingList = new ArrayList<>();
        List<ExposedPort> exposedPortList = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            ExposedPort tcp = ExposedPort.tcp(entry.getKey());
            Ports.Binding binding = Ports.Binding.bindPort(entry.getValue());
            PortBinding ports = new PortBinding(binding, tcp);
            portBindingList.add(ports);
            exposedPortList.add(tcp);
        }

        List<Bind> binds = new ArrayList<>();
        containerProperties.getPathMountMap().forEach((k, v) -> {
            Bind bind = new Bind(k, new Volume(v));
            binds.add(bind);
        });

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(binds)
                .withPortBindings(portBindingList)
                .withMemory(containerProperties.getMemoryOfBytes())
                .withCpuCount(containerProperties.getCpuCount())
                .withCapAdd(Capability.SYS_ADMIN, Capability.NET_ADMIN);

        CreateContainerResponse container = client.createContainerCmd(containerProperties.getImageName())
                .withName(containerProperties.getContainerName())
                //端口映射
                .withHostConfig(hostConfig)
                //对外暴露端口
                .withExposedPorts(exposedPortList)
                // 执行命令，注意命令和参数不能进行组合，必须都用逗号隔开,也就是空格全部换成这里的,分割
                // .withCmd("python", "/root/scripts/test.py", "-t", "999")
                .exec();
        log.info("create container response: {}", container);
        return container.getId();
    }

    /**
     * 启动容器
     *
     * @param client      DockerClient
     * @param containerId 容器id
     */
    public static void startContainer(DockerClient client, String containerId) {
        client.startContainerCmd(containerId).exec();
    }

    /**
     * 停止容器
     *
     * @param client      DockerClient
     * @param containerId 容器id
     */
    public static void stopContainer(DockerClient client, String containerId) {
        client.stopContainerCmd(containerId).exec();
    }

    /**
     * 删除容器
     *
     * @param client      DockerClient
     * @param containerId 容器id
     */
    public static void deleteContainer(DockerClient client, String containerId) {
        client.stopContainerCmd(containerId).exec();
        client.removeContainerCmd(containerId).exec();
    }

    /**
     * 停止容器
     *
     * @param client      DockerClient
     * @param containerId 容器id
     */
    public static void restartContainer(DockerClient client, String containerId) {
        client.restartContainerCmd(containerId).exec();
        log.info("restart container: {}", containerId);
    }

    /**
     * 删除容器
     *
     * @param client      DockerClient
     * @param containerId 容器id
     */
    public static void removeContainer(DockerClient client, String containerId) {
        client.removeContainerCmd(containerId).exec();
        log.info("remove container: {}", containerId);
    }

    public static String inspectContainer(DockerClient client, String containerId) {
        InspectContainerResponse response = client.inspectContainerCmd(containerId).exec();
        log.info("container: {}", response);
        return JSONObject.toJSONString(response);
    }

    public static String getContainerStatus(DockerClient client, String containerId) {
        InspectContainerResponse response = client.inspectContainerCmd(containerId).exec();
        log.info("container: {}", response);
        return response.getState().getStatus();
    }


    public static void listContainers(DockerClient client, List<String> statusList) {
        //获取所有运行的容器
        List<Container> containers = client.listContainersCmd().withStatusFilter(statusList).exec();
        for (Container container : containers) {
            log.info(container.getId() + ": " + container.getNames()[0]);
        }
    }

    /**
     * 删除镜像
     *
     * @param client  DockerClient
     * @param imageId 镜像id
     */
    public static void removeImage(DockerClient client, String imageId) {
        client.removeImageCmd(imageId).exec();
    }

    /**
     * 查看镜像详细信息
     *
     * @param client  DockerClient
     * @param imageId 镜像id
     * @return 镜像详细信息
     */
    public static String inspectImage(DockerClient client, String imageId) {
        InspectImageResponse response = client.inspectImageCmd(imageId).exec();
        return JSONObject.toJSONString(response);
    }

    /**
     * 从文件加载镜像
     *
     * @param client   DockerClient
     * @param filePath 文件路径
     */
    public static void loadImage(DockerClient client, String filePath) {
        try {
            client.loadImageCmd(new FileInputStream(filePath)).exec();
        } catch (FileNotFoundException e) {
            log.info("load image error", e);
        }
    }

    /**
     * repository 镜像名称:tag名称
     **/
    public static boolean pullImage(DockerClient client, String repository) {
        PullImageCmd pullImageCmd = client.pullImageCmd(repository);
        boolean isSuccess;
        try {
            isSuccess = client.pullImageCmd(repository)
                    .start()
                    .awaitCompletion(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return isSuccess;
    }

    /**
     * 构建镜像
     *
     * @param client          DockerClient
     * @param imageProperties 镜像配置
     * @return 镜像id
     */
    public static String buildImage(DockerClient client, DockerImageProperties imageProperties) {
        ImmutableSet<String> tag = ImmutableSet.of(imageProperties.getImageName() + ":" + imageProperties.getImageTag());
        String imageId = client.buildImageCmd(new File(imageProperties.getDockerfilePath()))
                .withTags(tag)
                .start()
                .awaitImageId();
        log.info("build image id: {}", imageId);
        return imageId;
    }

    /**
     * 获取镜像列表
     *
     * @param client DockerClient
     * @return 镜像列表
     */
    public static List<Image> imageList(DockerClient client) {
        List<Image> imageList = client.listImagesCmd().withShowAll(true).exec();
        log.info("image list: {}", imageList);
        return imageList;
    }

    /**
     * 创建swarm服务
     *
     * @param dockerClient      DockerClient
     * @param serviceProperties 服务配置
     * @return 服务id
     */
    public static String createService(DockerClient dockerClient, DockerServiceProperties serviceProperties) {
        List<PortConfig> portConfigs = new ArrayList<>();
        if (serviceProperties.getEndpointTargetPort() != null) {
            portConfigs.add(new PortConfig()
                    .withPublishMode(PortConfig.PublishMode.ingress)
                    //设置目标端口号
                    .withTargetPort(serviceProperties.getEndpointTargetPort())
                    .withProtocol(PortConfigProtocol.TCP));
        }
        if (serviceProperties.getContainerProperties().getPortBindMap() != null) {
            serviceProperties.getContainerProperties().getPortBindMap().forEach((k, v) -> {
                PortConfig portConfig = new PortConfig()
                        //设置主机模式
                        .withPublishMode(PortConfig.PublishMode.ingress)
                        //设置目标端口号
                        .withTargetPort(v)
                        //外部端口
                        .withPublishedPort(k)
                        .withProtocol(PortConfigProtocol.TCP);
                portConfigs.add(portConfig);
            });
        }
        if (serviceProperties.getContainerProperties().getTargetPorts() != null) {
            serviceProperties.getContainerProperties().getTargetPorts().forEach(k -> {
                PortConfig portConfig = new PortConfig()
                        //设置主机模式
                        .withPublishMode(PortConfig.PublishMode.ingress)
                        //内部端口
                        .withTargetPort(k)
                        .withProtocol(PortConfigProtocol.TCP);
                portConfigs.add(portConfig);
            });
        }

        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Long cpu = serviceProperties.getContainerProperties().getCpuCount();
        Long memory = serviceProperties.getContainerProperties().getMemoryOfBytes();
        if (cpu != null && cpu > 0 && memory != null && memory > 0) {
            resourceRequirements.withLimits(new ResourceSpecs().withNanoCPUs(cpu * 1000000000).withMemoryBytes(memory));
        }
        //创建服务
        ServiceSpec spec = new ServiceSpec()
                //服务名称（serviceName）
                .withName(serviceProperties.getServiceName())
                //环境变量(env）
                .withTaskTemplate(new TaskSpec()
                                .withForceUpdate(0)
                                .withResources(resourceRequirements)
                                .withRestartPolicy(new ServiceRestartPolicy().withMaxAttempts(1L))
                                .withContainerSpec(new ContainerSpec()
                                        .withEnv(serviceProperties.getContainerProperties().getEnvList())
                                        .withInit(false)
                                        .withMounts(serviceProperties.getContainerProperties().getMounts())
                                        .withCapAdd(Capability.CAP_SYS_ADMIN, Capability.CAP_NET_ADMIN)
//                                .withHealthCheck(healthCheck)
                                        //镜像名称
                                        .withImage(serviceProperties.getContainerProperties().getImageName()))
                )
                // 标签
                .withLabels(serviceProperties.getLabels())
                //启动副本数量
                .withMode(new ServiceModeConfig().withReplicated(
                        new ServiceReplicatedModeOptions()
                                .withReplicas(serviceProperties.getReplicas())
                ))
                .withEndpointSpec(new EndpointSpec()
                        .withMode(EndpointResolutionMode.valueOf(serviceProperties.getEndpointMode().toUpperCase()))
                        .withPorts(portConfigs)
                );
        //执行服务创建指令

        CreateServiceResponse response = dockerClient.createServiceCmd(spec).exec();
        log.info("create service response: {}", response);
        return response.getId();
    }

    /**
     * 查看服务信息
     *
     * @param dockerClient DockerClient
     * @param serviceName  容器服务名
     * @return 服务信息
     */
    public static Service inspectService(DockerClient dockerClient, String serviceName) {
        Service service = dockerClient.inspectServiceCmd(serviceName).exec();
        log.info("service {} inspect: {}", serviceName, service);
        return service;
    }

    /**
     * 删除服务
     *
     * @param dockerClient DockerClient
     * @param serviceName  容器服务名
     * @return 删除结果
     */
    public static boolean removeService(DockerClient dockerClient, String serviceName) {
        try {
            dockerClient.removeServiceCmd(serviceName).exec();
            log.info("remove service: {}", serviceName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 更新服务
     *
     * @param dockerClient DockerClient
     * @param serviceName  容器服务名
     * @param port         端口号
     * @return 删除结果
     */
    public static void updateServiceEnv(DockerClient dockerClient, String serviceName, List<String> envs, Integer port) {
        Service service = dockerClient.inspectServiceCmd(serviceName).exec();
        ServiceSpec spec = service.getSpec();
        spec.getTaskTemplate().getContainerSpec().withEnv(envs);

        PortConfig portConfig = new PortConfig()
                //设置主机模式
                .withPublishMode(PortConfig.PublishMode.ingress)
                //设置目标端口号
                .withTargetPort(port)
                //外部端口
                .withPublishedPort(port)
                .withProtocol(PortConfigProtocol.TCP);

        spec.withEndpointSpec(new EndpointSpec()
                .withMode(EndpointResolutionMode.VIP)
                .withPorts(Arrays.asList(portConfig))
        );
        //执行服务更新指令
        dockerClient.updateServiceCmd(serviceName, spec)
                .withVersion(service.getVersion().getIndex())
                .exec();
        log.info("update service {}", serviceName);
    }

    /**
     * 查看 docker service任务列表
     *
     * @param dockerClient DockerClient
     * @return 任务列表
     */
    public static List<Task> listTasks(DockerClient dockerClient) {
        return dockerClient.listTasksCmd().exec();
    }

    /**
     * 获取docker service状态
     *
     * @param dockerClient DockerClient
     * @param serviceName  容器服务名
     * @return 状态
     */
    public static String getServiceStatus(DockerClient dockerClient, String serviceName) {
        List<Task> tasks = dockerClient.listTasksCmd().withServiceFilter(serviceName).exec();
        if (CollectionUtils.isEmpty(tasks)) {
            throw new NotFoundException(String.format("The task of service %s not found.", serviceName));
        }
        boolean isRunning = tasks.stream().anyMatch(DockerClientUtils::isRunningState);
        if (isRunning) {
            return TaskState.RUNNING.getValue();
        }
        Task task = tasks.get(0);
        if (StringUtils.isNotBlank(task.getStatus().getErr())) {
            return TaskState.FAILED.getValue();
        }
        return task.getStatus().getState().getValue();
    }

    /**
     * 查看docker service是否运行中
     *
     * @param dockerClient DockerClient
     * @param serviceName  容器服务名
     * @return 是否运行，true or false
     */
    public static boolean isServiceRunning(DockerClient dockerClient, String serviceName) {
        try {
            List<Task> tasks = dockerClient.listTasksCmd().withServiceFilter(serviceName).exec();
            if (CollectionUtils.isEmpty(tasks)) {
                return false;
            }
            boolean isRunning = tasks.stream().anyMatch(DockerClientUtils::isRunningState);
            log.info("service is running: {}", isRunning);
            return isRunning;
        } catch (Exception e) {
            log.info("isServiceRunning", e);
            return false;
        }
    }

    public static boolean isRunningState(Task task) {
        return task.getDesiredState().equals(TaskState.RUNNING)
                && task.getStatus().getState().equals(TaskState.RUNNING)
                && task.getStatus().getContainerStatus() != null
                && task.getStatus().getContainerStatus().getContainerID() != null;
    }

    /**
     * 获取docker swarm节点列表，key：节点id，value：节点IP
     *
     * @param dockerClient DockerClient
     * @return key：节点id，value：节点IP
     */
    public static Map<String, String> getSwarmNodes(DockerClient dockerClient) {
        List<SwarmNode> nodes = dockerClient.listSwarmNodesCmd().exec();
        Map<String, String> nodeMap = nodes.stream().collect(Collectors.toMap(SwarmNode::getId, node -> {
            String addr = Objects.requireNonNull(node.getStatus()).getAddress();
            if (node.getManagerStatus() != null && node.getManagerStatus().isLeader()) {
                String managerAddr = node.getManagerStatus().getAddr();
                addr = Objects.requireNonNull(managerAddr).split(":")[0];
            }
            return addr;
        }));
        log.info("swarm nodes: {}", nodeMap);
        return nodeMap;
    }

    public static List<ServiceContainerInfo> getServiceContainers(DockerClient dockerClient, String serviceName) {
        Integer port = getServicePublishedPort(dockerClient, serviceName);
        Map<String, String> nodeMap = DockerClientUtils.getSwarmNodes(dockerClient);
        List<Task> tasks = dockerClient.listTasksCmd().withServiceFilter(serviceName).exec();
        if (CollectionUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        return tasks.stream().filter(DockerClientUtils::isRunningState).map(task ->
                        ServiceContainerInfo.builder()
                                .serviceName(serviceName)
                                .serviceId(task.getServiceId())
                                .nodeId(task.getNodeId())
                                .nodeIp(nodeMap.get(task.getNodeId()))
                                .containerId(task.getStatus().getContainerStatus().getContainerID())
                                .state(task.getStatus().getState().getValue())
                                .port(port)
                                .build())
                .collect(Collectors.toList());
    }

    public static Integer getServicePublishedPort(DockerClient dockerClient, String serviceName) {
        Service service = inspectService(dockerClient, serviceName);
        return Optional.ofNullable(service.getEndpoint())
                .map(Endpoint::getPorts)
                .map(portConfigs -> portConfigs[0].getPublishedPort())
                .orElse(null);
    }

    public static List<String> listServiceNames(DockerClient dockerClient) {
        List<Service> serviceList = dockerClient.listServicesCmd().exec();
        return serviceList.stream().map(service -> service.getSpec().getName()).collect(Collectors.toList());
    }
}