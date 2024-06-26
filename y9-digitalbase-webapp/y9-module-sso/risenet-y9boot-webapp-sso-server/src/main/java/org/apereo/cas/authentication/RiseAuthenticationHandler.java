package org.apereo.cas.authentication;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.Y9User;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.dao.DataAccessException;

import lombok.extern.slf4j.Slf4j;

import y9.service.Y9LoginUserService;
import y9.service.Y9UserService;
import y9.util.Y9Context;
import y9.util.Y9MessageDigest;
import y9.util.common.Base64Util;
import y9.util.common.RSAUtil;

@Slf4j
public class RiseAuthenticationHandler extends AbstractAuthenticationHandler {
    private Y9UserService y9UserService;
    private Y9LoginUserService y9LoginUserService;

    public RiseAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory,
        Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(Credential credential, Service service)
        throws GeneralSecurityException, PreventedException {
        if (y9UserService == null) {
            y9UserService = Y9Context.getBean(Y9UserService.class);
            y9LoginUserService = Y9Context.getBean(Y9LoginUserService.class);
        }
        RememberMeUsernamePasswordCredential riseCredential = (RememberMeUsernamePasswordCredential)credential;
        String loginType = riseCredential.getLoginType();
        String tenantShortName = riseCredential.getTenantShortName();
        String deptId = riseCredential.getDeptId();
        String username = riseCredential.getUsername();
        String password = riseCredential.toPassword();
        String pwdEcodeType = riseCredential.getPwdEcodeType();
        // 特殊登录处理
        String realTenantname = tenantShortName;
        String realUsername = username;
        try {
            username = Base64Util.decode(username, "Unicode");
            if (StringUtils.isNotBlank(pwdEcodeType)) {
                // Object obj = redisTemplate.opsForValue().get(pwdEcodeType);
                String rsaPrivateKey = Y9Context.getProperty("y9.login.encryptionRsaPrivateKey");
                if (null != rsaPrivateKey) {
                    password = RSAUtil.privateDecrypt(password, rsaPrivateKey);
                } else {
                    throw new FailedLoginException("认证失败：随机数已过期，请重新登录!");
                }
            }
            password = Base64Util.decode(password, "Unicode");

            if (username.contains("&")) {
                realUsername = username;
                String agentTenantShortName = "operation";
                String agentUserName = username.substring(username.indexOf("&") + 1);
                username = agentUserName;
                tenantShortName = agentTenantShortName;

                List<Y9User> agentUsers = null;
                if (StringUtils.isNotBlank(deptId)) {
                    agentUsers = y9UserService.findByTenantShortNameAndMobileAndParentId(agentTenantShortName,
                        agentUserName, deptId);
                } else {
                    agentUsers = y9UserService.findByTenantShortNameAndLoginNameAndOriginal(agentTenantShortName,
                        agentUserName, Boolean.TRUE);
                }
                if (agentUsers == null || agentUsers.isEmpty()) {
                    throw new FailedLoginException("没有找到这个【代理】用户。");
                }
            }
            riseCredential.setUsername(username);
            riseCredential.assignPassword(password);

            HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            if (null != request) {
                String systemName = request.getParameter("systemName");
                if (StringUtils.isNotBlank(systemName)) {
                    riseCredential.setSystemName(systemName);
                }
                riseCredential.setUserAgent(request.getHeader("User-Agent"));
                riseCredential.setClientIp(Y9Context.getIpAddr(request));
            }
        } catch (Exception e) {
            throw new FailedLoginException(e.getMessage());
        }

        try {
            if ("caid".equals(loginType)) {
                AuthenticationHandlerExecutionResult hr = new DefaultAuthenticationHandlerExecutionResult(this,
                    new BasicCredentialMetaData(riseCredential), principalFactory.createPrincipal(username));
                return hr;
            } else {
                List<Y9User> users = null;
                if ("mobile".equals(loginType)) {
                    if (StringUtils.isNotBlank(deptId)) {
                        users =
                            y9UserService.findByTenantShortNameAndMobileAndParentId(tenantShortName, username, deptId);
                    } else {
                        users = y9UserService.findByTenantShortNameAndMobileAndOriginal(tenantShortName, username,
                            Boolean.TRUE);
                    }
                } else if ("loginMobileName".equals(loginType)) {
                    if (StringUtils.isNotBlank(deptId)) {
                        users = y9UserService.findByTenantShortNameAndLoginNameAndParentId(tenantShortName, username,
                            deptId);
                    } else {
                        users = y9UserService.findByTenantShortNameAndLoginNameAndOriginal(tenantShortName, username,
                            Boolean.TRUE);
                    }
                } else {
                    if (StringUtils.isNotBlank(deptId)) {
                        users = y9UserService.findByTenantShortNameAndLoginNameAndParentId(tenantShortName, username,
                            deptId);
                    } else {
                        users = y9UserService.findByTenantShortNameAndLoginNameAndOriginal(tenantShortName, username,
                            Boolean.TRUE);
                    }
                }

                if (users != null && !users.isEmpty()) {
                    Y9User y9User = users.get(0);
                    String hashed = y9User.getPassword();
                    if (!Y9MessageDigest.bcryptMatch(password, hashed)) {
                        throw new FailedLoginException("密码错误。");
                    }
                    // 特殊处理登录成功后还原登录账号
                    if (realUsername.contains("&")) {
                        username = realUsername.substring(0, realUsername.indexOf("&"));
                        riseCredential.setUsername(username);
                        riseCredential.setTenantShortName(realTenantname);
                    }
                    try {
                        y9LoginUserService.save(riseCredential, "true", "登录成功");
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    AuthenticationHandlerExecutionResult hr = new DefaultAuthenticationHandlerExecutionResult(this,
                        new BasicCredentialMetaData(riseCredential), principalFactory.createPrincipal(username));
                    return hr;
                } else {
                    throw new FailedLoginException("没有找到这个用户。");
                }
            }
        } catch (DataAccessException e) {
            throw new FailedLoginException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<? extends Credential> clazz) {
        return RememberMeUsernamePasswordCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeUsernamePasswordCredential;
    }

}
