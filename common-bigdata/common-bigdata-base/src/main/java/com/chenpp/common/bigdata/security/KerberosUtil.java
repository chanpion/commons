package com.chenpp.common.bigdata.security;


import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author April.Chen
 * @date 2023/11/6 10:37 上午
 */
public class KerberosUtil {
    public static final String JAVA_VENDER = "java.vendor";

    public static final String IBM_FLAG = "IBM";

    public static final String CONFIG_CLASS_FOR_IBM = "com.ibm.security.krb5.internal.Config";

    public static final String CONFIG_CLASS_FOR_SUN = "sun.security.krb5.Config";

    public static final String METHOD_GET_INSTANCE = "getInstance";

    public static final String METHOD_GET_DEFAULT_REALM = "getDefaultRealm";

    public static final String DEFAULT_REALM = "HADOOP.COM";

    private static final Logger LOG = LoggerFactory.getLogger(KerberosUtil.class);

    public static String getKrb5DomainRealm() {
        Class<?> krb5ConfClass;
        String peerRealm;
        try {
            if (System.getProperty(JAVA_VENDER).contains(IBM_FLAG)) {
                krb5ConfClass = Class.forName(CONFIG_CLASS_FOR_IBM);
            } else {
                krb5ConfClass = Class.forName(CONFIG_CLASS_FOR_SUN);
            }

            Method getInstanceMethod = krb5ConfClass.getMethod(METHOD_GET_INSTANCE);
            Object kerbConf = getInstanceMethod.invoke(krb5ConfClass);

            Method getDefaultRealmMethod = krb5ConfClass.getDeclaredMethod(METHOD_GET_DEFAULT_REALM);
            peerRealm = (String) getDefaultRealmMethod.invoke(kerbConf);
            LOG.info("Get default realm successfully, the realm is : " + peerRealm);

        } catch (Exception e) {
            peerRealm = DEFAULT_REALM;
            LOG.warn("Get default realm failed, use default value : " + DEFAULT_REALM);
        }

        return peerRealm;
    }


    /**
     * 检查kerberos认证-通用
     *
     * @param krb5Path
     * @param keytabPath
     * @param principal
     * @return
     */
    public static UserGroupInformation checkKerberos(String krb5Path, String keytabPath, String principal) {
        if (StringUtils.isBlank(krb5Path) || StringUtils.isBlank(keytabPath) || StringUtils.isBlank(principal)) {
            return null;
        }
        System.setProperty("java.security.krb5.conf", krb5Path);
        Configuration configuration = new Configuration();
        UserGroupInformation userGroupInformation = null;
        try {
//            sun.security.krb5.Config.refresh();
            configuration.set("hadoop.security.authentication", "kerberos");
            configuration.set("hadoop.security.auth_to_local", "RULE:[1:$1]\n" +
                    "RULE:[2:$1]\n" +
                    "DEFAULT");
            UserGroupInformation.setConfiguration(configuration);
            LoginUtil.setJaasFile(principal, keytabPath);
            userGroupInformation = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytabPath);
            if (LOG.isInfoEnabled()) {
                LOG.info("kerberos authentication was successful");
            }
            return userGroupInformation;
        } catch (Exception e) {
            LOG.error(String.format("kerberos auth error: principal=%s, keytabPath=%s, krb5Path=%s", principal, keytabPath, krb5Path), e);
            throw new RuntimeException("kerberos authentication exception", e);
        }
    }


    public static void resetUserGroupInformation() {
        // hadoop 3.x use UserGroupInformation.reset()
        UserGroupInformation.setLoginUser(null);
        UserGroupInformation.setConfiguration(new Configuration());
    }
}
