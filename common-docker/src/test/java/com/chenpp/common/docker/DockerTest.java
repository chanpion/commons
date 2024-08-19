package com.chenpp.common.docker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ContainerSpec;
import com.github.dockerjava.api.model.Driver;
import com.github.dockerjava.api.model.EndpointResolutionMode;
import com.github.dockerjava.api.model.EndpointSpec;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortConfig;
import com.github.dockerjava.api.model.PortConfigProtocol;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceRestartPolicy;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.api.model.SwarmNode;
import com.github.dockerjava.api.model.SwarmNodeManagerStatus;
import com.github.dockerjava.api.model.Task;
import com.github.dockerjava.api.model.TaskSpec;
import com.github.dockerjava.api.model.VolumeOptions;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author April.Chen
 * @date 2023/9/28 10:14 上午
 **/
public class DockerTest {

    private String dockerHost = "tcp://10.58.16.154:2375";
    //    private String dockerHost = "tcp://enki-hadoop-d-010057017244.te.td:2375";
//    private String dockerHost = "tcp://docker.swarm:2378";
    private DockerProperties dockerProperties;

    private DockerClient dockerClient;

    private String imageName = "containous/whoami";
    private String serviceName = "whoami";

    @Before
    public void init() {
        dockerProperties = new DockerProperties();
        dockerProperties.setHost(dockerHost);
        dockerProperties.setApiVersion("1.43");
        dockerProperties.setTlsVerify(true);
        dockerProperties.setTlsCertPath("/Users/chenpp/mingmo/docker/tls");
        dockerClient = DockerClientUtils.buildDockerClient(dockerProperties);
        Info info = dockerClient.infoCmd().exec();
        System.out.println("connect to docker, info: " + JSON.toJSONString(info));
    }

    @Test
    public void testCreateService() {
        DockerContainerProperties containerProperties = new DockerContainerProperties();
        containerProperties.setCpuCount(1L);
        containerProperties.setMemory(128L);
        containerProperties.setImageName(imageName);
        containerProperties.setEnvList(new ArrayList<>());


        Map<Integer, Integer> portMap = new HashMap<>();
        portMap.put(18091, 80);
        containerProperties.setPortBindMap(portMap);
        DockerServiceProperties serviceProperties = new DockerServiceProperties();
        serviceProperties.setReplicas(1);
        serviceProperties.setContainerProperties(containerProperties);
        serviceProperties.setServiceName(serviceName + System.currentTimeMillis());
        serviceProperties.setEndpointMode("vip");
        serviceProperties.setEndpointTargetPort(18091);
        String serviceId = DockerClientUtils.createService(dockerClient, serviceProperties);
        System.out.println(serviceId);
    }

    @Test
    public void testListService() {
        List<Service> serviceList = dockerClient.listServicesCmd().exec();
        serviceList.forEach(service -> System.out.println(JSONObject.toJSONString(service)));
    }

    @Test
    public void testRemoveService() {
        DockerClientUtils.removeService(dockerClient, serviceName);
    }

    @Test
    public void testInspectService() {
        Service service = DockerClientUtils.inspectService(dockerClient, "serviceName");
        System.out.println(JSONObject.toJSONString(service));
    }

    @Test
    public void testCreteNetwork() {
        //设置network
        String networkId = dockerClient.createNetworkCmd().withName("networkName")
                .withDriver("overlay")
                .withIpam(new Network.Ipam()
                        .withDriver("default"))
                .exec().getId();
        System.out.println(networkId);
    }

    @Test
    public void testSize() {
        System.out.println(StandardCharsets.UTF_8.name());
        long size = 1 << 20;
        System.out.println(size);
    }


    @Test
    public void testListImages() {
        List<Image> images = DockerClientUtils.imageList(dockerClient);
        images.forEach(System.out::println);
    }

    @Test
    public void testService() {
        testListService();
        testInspectService();
        testRemoveService();
        testCreateService();
        testInspectService();
    }

    @Test
    public void testStats() {

        dockerClient.statsCmd("");
    }

    @Test
    public void listTasks() {
        String serviceName = "whoami";
        List<Task> tasks = dockerClient.listTasksCmd().withServiceFilter(serviceName).exec();
        tasks.forEach(task -> {
            System.out.println(JSONObject.toJSONString(task));
            System.out.println("state: " + task.getStatus().getState());
            System.out.println("container " + task.getStatus().getContainerStatus());
        });
    }

    @Test
    public void testIsServiceRunning() {
        boolean running = DockerClientUtils.isServiceRunning(dockerClient, "service-holmes-python-qiye");
        System.out.println("service-holmes-python-qiye: " + running);
        running = DockerClientUtils.isServiceRunning(dockerClient, "service-whoami");
        System.out.println("service-whoami: " + running);
    }

    @Test
    public void testGetServiceStatus() {
        String status = DockerClientUtils.getServiceStatus(dockerClient, "service-holmes-python-qiye");
        System.out.println("service-holmes-python-qiye status: " + status);
        status = DockerClientUtils.getServiceStatus(dockerClient, "service-whoami");
        System.out.println("service-whoami status: " + status);
    }

    @Test
    public void testCreateServiceWithMount() {
        String imageName = "10.57.17.244:5000/ml/configurable-http-proxy:1.0.0";
        String serviceName = "service-http-proxy-nfs";

        Map<String, String> options = new HashMap<>();
        options.put("type", "nfs");
        options.put("device", "10.57.17.244:/data01/nfs/notebook");
        options.put("o", "addr=10.57.17.244,rw,async");
        Mount mount = new Mount()
                .withType(MountType.VOLUME)
                .withSource("nfsvolume")
                .withTarget("/tmp")
                .withVolumeOptions(new VolumeOptions()
                        .withDriverConfig(new Driver().withName("local")
                                .withOptions(options)));


        DockerContainerProperties containerProperties = new DockerContainerProperties();
        containerProperties.setCpuCount(1L);
        containerProperties.setMemory(128L);
        containerProperties.setImageName(imageName);
        containerProperties.setEnvList(Arrays.asList("CONFIGURABLE_PROXY_REDIS_URI=redis://:12345@10.57.34.196:6379"));
        containerProperties.setMounts(Arrays.asList(mount));

        Map<Integer, Integer> portMap = new HashMap<>();
        portMap.put(18091, 80);
//        containerProperties.setPortBindMap(portMap);
        DockerServiceProperties serviceProperties = new DockerServiceProperties();
        serviceProperties.setReplicas(1);
        serviceProperties.setContainerProperties(containerProperties);
        serviceProperties.setServiceName(serviceName);
        serviceProperties.setEndpointMode("vip");
        serviceProperties.setEndpointTargetPort(80);
        String serviceId = DockerClientUtils.createService(dockerClient, serviceProperties);
        System.out.println(serviceId);

    }

    @Test
    public void testNode() {
        List<SwarmNode> nodes = dockerClient.listSwarmNodesCmd().exec();
        nodes.forEach(node -> {
            String nodeId = node.getId();
            String addr = node.getStatus().getAddress();
            if (node.getManagerStatus() != null && node.getManagerStatus().isLeader()) {
                String managerAddr = node.getManagerStatus().getAddr();
                addr = managerAddr.split(":")[0];
            }
            String managerAddr = Optional.ofNullable(node.getManagerStatus()).orElse(new SwarmNodeManagerStatus()).getAddr();
            System.out.println(String.format("nodeId: %s, addr: %s, managerAddr: %s", nodeId, addr, managerAddr));
        });

        Map<String, String> nodeMap = DockerClientUtils.getSwarmNodes(dockerClient);
        System.out.println(nodeMap);
    }

    @Test
    public void getServiceTaskIp() {
        Service service = DockerClientUtils.inspectService(dockerClient, "whoami-v1");
        System.out.println(JSON.toJSONString(service));
        System.out.println(service.getEndpoint().getPorts()[0].getPublishedPort());
        Map<String, String> nodeMap = DockerClientUtils.getSwarmNodes(dockerClient);
        System.out.println("====================getServiceTaskIp====================");
        List<Task> tasks = dockerClient.listTasksCmd().withServiceFilter("whoami-v1").exec();
        tasks.stream().filter(DockerClientUtils::isRunningState).map(task -> {
            JSONObject instance = new JSONObject();
            instance.put("nodeId", task.getNodeId());
            instance.put("containerId", task.getStatus().getContainerStatus().getContainerID());
            instance.put("ip", nodeMap.get(task.getNodeId()));
            return instance;
        }).forEach(task -> System.out.println(JSON.toJSONString(task)));
    }


    @Test
    public void getServicePort() {
        Integer port = DockerClientUtils.getServicePublishedPort(dockerClient, "whoami-v12");
        System.out.println(port);
    }

    @Test
    public void testGetContainerState() {
        InspectContainerResponse response = dockerClient.inspectContainerCmd("3fa40177a903").exec();
        System.out.println("===================");
        String status = DockerClientUtils.getContainerStatus(dockerClient, "3fa40177a903111");
        System.out.println("status:" + status);

    }

    @Test
    public void testString() throws IOException {
        String str = "apiVersion: v1\nkind: Pod\nmetadata:\n  name: pod-runtime-resource-instance-1\n  labels: {appName: pod-runtime-resource-instance-1}\nnamespace: apps-2324\nspec:\n  containers:\n  - name: pod-runtime-resource-instance-1\n    image: 10.58.16.154:5000/whoami:1.0\n    ports:\n    - {containerPort: 8080}\n    env:\n    - {name: INSTANCE_GROUP, value: 76}\n    - {name: NACOS_SERVER, value: '10.57.16.13:8848'}\n    - {name: ROOT_PATH, value: /TD/MINGMO/PROD}\n    - {name: NACOS_USERNAME, value: nacos}\n    - {name: S3_URL, value: 'http://10.58.16.154:9000'}\n    - {name: INSTANCE_ID, value: '1'}\n    - {name: PYTHON_VERSION, value: '37'}\n    - {name: MODELS_PATH_KEY, value: /TD/MINGMO/PROD/application-repo/models}\n    - {name: APP_IP, value: 127.0.0.1}\n    - {name: APPLICATION_NAME, value: holmes-python-qiye}\n    - {name: S3_BUCKET, value: mingmo}\n    - {name: CONDA_PATH, value: /TD/MINGMO/PROD/anaconda}\n    - {name: NACOS_MODEL_SERVICE, value: holmes-manager}\n    - {name: NACOS_PASSWORD, value: nacos}\n    - {name: S3_ACCESS_KEY, value: '4kBXCEQGRM9LCBUjsAKj:'}\n    - {name: SERVER_LOGGING, value: error}\n    - {name: CONSUMER_CONF, value: '''{ \"thread.num\": 1, \"bootstrap.servers\": \"localhost:9092\",\n        \"group.id\": \"holmes-python\", \"auto.offset.reset\": \"latest\" }'''}\n    - {name: ENV_PATH, value: /TD/MINGMO/PROD/runtime-resource/76}\n    - {name: CPU_NUM, value: '1'}\n    - {name: MODELS_PATH, value: /TD/MINGMO/PROD/application-repo/models}\n    - {name: S3_SECRET_KEY, value: NeDmNsdjfKng6bRfgnzUJxkQyoQZIAce5N5cEdO3}\n    - {name: NACOS_NAMESPACE, value: ce594d88-be3f-4e8a-83d0-f9921ed8316b}\n    - {name: SERVER_LOG_FILE, value: logs/server.log}\n    - {name: NFS_URL, value: 10.58.12.6}\n    - {name: APP_PORT, value: '8080'}\n    - {name: NACOS_GROUP, value: wy}\n";
        File file = new File("/Users/chenpp/mingmo/2324/k8s.yaml");
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        System.out.println(content);
    }

    @Test
    public void testUpdateService() {
        String serviceName = "runtime-resource-220-184";
        List<String> envList = Arrays.asList("APPLICATION_NAME=holmes-python-qiye",
                "APP_IP=10.58.16.195",
                "APP_PORT=8080",
                "CONDA_PATH=/TD/MINGMO/PROD/anaconda",
                "CONSUMER_CONF={ \"thread.num\": 1, \"bootstrap.servers\": \"10.57.16.13:9092\", \"group.id\": \"holmes-python\", \"auto.offset.reset\": \"latest\" }",
                "CPU_NUM=1",
                "ENV_PATH=/TD/MINGMO/PROD/runtime-resource/${env_id}",
                "IMAGE=10.58.12.6:4001/mingmo/mingmo-base:v1.0.1",
                "INSTANCE_GROUP=220",
                "INSTANCE_ID=184",
                "MEMORY=1024",
                "MODELS_PATH=/TD/MINGMO/PROD/application-repo/models",
                "MODELS_PATH_KEY=/TD/MINGMO/PROD/application-repo/models",
                "NACOS_GROUP=dev",
                "NACOS_MODEL_SERVICE=holmes-manager",
                "NACOS_NAMESPACE=ce594d88-be3f-4e8a-83d0-f9921ed8316b",
                "NACOS_PASSWORD=nacos",
                "NACOS_SERVER=10.57.16.13:8848",
                "NACOS_USERNAME=nacos",
                "NFS_DIR=/data01/nfs1",
                "NFS_URL=10.58.12.6",
                "PYTHON_VERSION=38",
                "ROOT_PATH=/TD/MINGMO/PROD",
                "S3_ACCESS_KEY=4kBXCEQGRM9LCBUjsAKj:",
                "S3_BUCKET=mingmo",
                "S3_SECRET_KEY=NeDmNsdjfKng6bRfgnzUJxkQyoQZIAce5N5cEdO3",
                "S3_URL=http://10.58.16.154:9000",
                "SERVER_LOGGING=error");

        List<PortConfig> portConfigs = new ArrayList<>();
        portConfigs.add(new PortConfig()
                .withPublishMode(PortConfig.PublishMode.ingress)
                .withPublishedPort(30108)
                //设置目标端口号
                .withTargetPort(8080)
                .withProtocol(PortConfigProtocol.TCP));

        //创建服务
        ServiceSpec serviceSpec = new ServiceSpec()
                //服务名称（serviceName）
                .withName(serviceName)
                //环境变量(env）
                .withTaskTemplate(new TaskSpec()
                        .withRestartPolicy(new ServiceRestartPolicy().withMaxAttempts(1L))
                        .withContainerSpec(new ContainerSpec()
                                .withEnv(envList)
                                .withCapAdd(Capability.CAP_SYS_ADMIN, Capability.CAP_NET_ADMIN)
                                .withImage("10.58.12.6:4001/mingmo/mingmo-base:v1.0.1")
                                .withInit(false)
                        ))
                .withEndpointSpec(new EndpointSpec()
                        .withMode(EndpointResolutionMode.VIP)

                        .withPorts(portConfigs)
                );

        Service service = dockerClient.inspectServiceCmd(serviceName).exec();
        ServiceSpec spec = service.getSpec();

        spec.getTaskTemplate().getContainerSpec().withEnv(envList);

        dockerClient.updateServiceCmd(serviceName, spec)
                .withVersion(service.getVersion().getIndex())
                .exec();
    }
}
