package y9.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.services.Y9LoginUser;
import org.apereo.cas.services.Y9User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import y9.service.Y9LoginUserService;
import y9.service.Y9UserService;
import y9.util.InetAddressUtil;
import y9.util.common.UserAgentUtil;
import y9.util.json.Y9JacksonUtil;

import cz.mallat.uasparser.UserAgentInfo;

@Service
@Slf4j
public class Y9LoginUserServiceImpl implements Y9LoginUserService {

    public static String SSO_SERVER_IP = InetAddressUtil.getLocalAddress().getHostAddress();

    @Autowired
    private Y9UserService y9UserService;

    @Autowired
    @Qualifier("jdbcServiceRegistryTransactionTemplate")
    private TransactionOperations transactionTemplate;

    @PersistenceContext(unitName = "jpaServiceRegistryContext")
    private EntityManager entityManager;

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void save(RememberMeUsernamePasswordCredential credential, String success, String logMessage) {
        try {
            String deptId = credential.getDeptId();
            String loginType = credential.getLoginType();
            String tenantShortName = credential.getTenantShortName();
            String userLoginName = credential.getUsername();
            String userHostIP = credential.getClientIp();
            String userAgent = credential.getUserAgent();
            String screenResolution = credential.getScreenDimension();
            String userHostMAC = credential.getClientMac();
            String userHostName = credential.getClientHostName();
            String userHostDiskId = credential.getClientDiskId();

            String tenantName = "";
            String personId = "";
            String tenantId = "";
            String userName = "";
            String managerLevel = "0";
            if (!"127.0.0.1".equals(userHostIP)) {
                UserAgentInfo uaInfo = UserAgentUtil.getUserAgentInfo(userAgent);
                String browser = UserAgentUtil.getBrowserName(userAgent);

                List<Y9User> users = null;
                if ("mobile".equals(loginType)) {
                    if (StringUtils.hasText(deptId)) {
                        users = y9UserService.findByTenantShortNameAndMobileAndParentId(tenantShortName, userLoginName,
                            deptId);
                    } else {
                        users = y9UserService.findByTenantShortNameAndMobileAndOriginal(tenantShortName, userLoginName,
                            Boolean.TRUE);
                    }
                } else {
                    if (StringUtils.hasText(deptId)) {
                        users = y9UserService.findByTenantShortNameAndLoginNameAndParentId(tenantShortName,
                            userLoginName, deptId);
                    } else {
                        users = y9UserService.findByTenantShortNameAndLoginNameAndOriginal(tenantShortName,
                            userLoginName, Boolean.TRUE);
                    }
                }
                if (users != null && users.size() > 0) {
                    Y9User user = users.get(0);
                    tenantName = user.getTenantName();
                    personId = user.getPersonId();
                    tenantId = user.getTenantId();
                    userName = user.getName();
                    managerLevel = String.valueOf(user.getManagerLevel());
                }

                Y9LoginUser user = new Y9LoginUser();
                user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                user.setLoginTime(new Date());
                user.setLoginType(loginType);
                user.setUserId(personId);
                user.setUserLoginName(userLoginName);
                user.setUserName(userName);
                user.setUserHostIp(userHostIP);
                user.setUserHostMac(userHostMAC);
                user.setUserHostName(userHostName);
                user.setUserHostDiskId(userHostDiskId);
                user.setTenantId(tenantId);
                user.setTenantName(tenantName);
                user.setServerIp(SSO_SERVER_IP);
                user.setSuccess(success);
                user.setLogMessage(logMessage);
                user.setBrowserName(browser);
                user.setBrowserVersion(uaInfo.getBrowserVersionInfo());
                user.setScreenResolution(screenResolution);
                user.setOsName(uaInfo.getOsName());
                user.setManagerLevel(managerLevel);
                Environment environment = applicationContext.getEnvironment();
                String loginInfoSaveTarget = environment.getProperty("y9.login.loginInfoSaveTarget", "jpa");
                if ("jpa".equals(loginInfoSaveTarget)) {
                    transactionTemplate.execute(status -> {
                        entityManager.persist(user);
                        LOGGER.info("保存登录日志成功");
                        return null;
                    });
                } else {
                    String jsonString = Y9JacksonUtil.writeValueAsString(user);
                    kafkaTemplate.send("y9_userLoginInfo_message", jsonString);
                    LOGGER.info("保存登录日志成功至Kafka成功");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
