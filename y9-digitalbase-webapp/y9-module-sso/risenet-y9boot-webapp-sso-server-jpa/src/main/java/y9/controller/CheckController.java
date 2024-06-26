package y9.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.services.Y9User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import y9.service.Y9UserService;
import y9.util.Y9Context;
import y9.util.Y9MessageDigest;
import y9.util.Y9Result;
import y9.util.common.Base64Util;
import y9.util.common.CheckPassWord;
import y9.util.common.RSAUtil;

@Controller
@RequestMapping(value = "/api")
@Slf4j
@RequiredArgsConstructor
public class CheckController {

    private final Y9UserService y9UserService;

    private void changeSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // We need to migrate to a new session
            String originalSessionId = session.getId();
            HashMap<String, Object> attributesToMigrate = createMigratedAttributeMap(session);

            session.invalidate();
            session = request.getSession(true); // we now have a new session
            String newSessionId = session.getId();
            LOGGER.debug("Started new session: " + newSessionId);

            // Copy attributes to new session
            if (attributesToMigrate != null) {
                for (Map.Entry<String, Object> entry : attributesToMigrate.entrySet()) {
                    session.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            if (originalSessionId.equals(newSessionId)) {
                LOGGER.warn(
                    "Your servlet container did not change the session ID when a new session was created. You will not be adequately protected against session-fixation attacks");
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/checkSsoLoginInfo", method = RequestMethod.POST)
    public Map<String, Object> checkSsoLoginInfo(final RememberMeUsernamePasswordCredential riseCredential,
        final HttpServletRequest request, final HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("msg", "认证成功!");
        changeSessionId(request);
        try {
            String tenantShortName = riseCredential.getTenantShortName();
            String username = riseCredential.getUsername();
            String password = riseCredential.toPassword();
            String pwdEcodeType = riseCredential.getPwdEcodeType();

            username = Base64Util.decode(username, "Unicode");
            if (StringUtils.isNotBlank(pwdEcodeType)) {
                String privateKey = Y9Context.getProperty("y9.login.encryptionRsaPrivateKey");
                // Object obj = redisTemplate.opsForValue().get(pwdEcodeType);
                password = RSAUtil.privateDecrypt(password, privateKey);
            }
            password = Base64Util.decode(password, "Unicode");
            if (username.contains("&")) {
                username = username.substring(username.indexOf("&") + 1);
                tenantShortName = "operation";
            }
            riseCredential.setUsername(username);
            String loginType = riseCredential.getLoginType();

            List<Y9User> users = null;
            if ("mobile".equals(loginType)) {
                users = y9UserService.findByTenantShortNameAndMobile(tenantShortName, username);
            } else {
                users = y9UserService.findByTenantShortNameAndLoginName(tenantShortName, username);
            }

            if (users.isEmpty()) {
                map.put("msg", "该账号不存在，请检查账号输入是否正确！");
                map.put("success", false);
                return map;
            }

            Y9User y9User = users.get(0);
            String hashed = y9User.getPassword();
            if (!Y9MessageDigest.bcryptMatch(password, hashed)) {
                map.put("msg", "密码错误!");
                map.put("success", false);
                return map;
            }
            boolean isSimplePassWord = CheckPassWord.isSimplePassWord(password);
            if (isSimplePassWord) {
                map.put("msg", "密码过于简单,请重新设置密码！");
            }

        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", "认证失败!");
            LOGGER.warn(e.getMessage(), e);
        }
        return map;
    }

    private HashMap<String, Object> createMigratedAttributeMap(HttpSession session) {
        HashMap<String, Object> attributesToMigrate = new HashMap<String, Object>();
        Enumeration<String> enumer = session.getAttributeNames();
        while (enumer.hasMoreElements()) {
            String key = enumer.nextElement();
            attributesToMigrate.put(key, session.getAttribute(key));
        }
        return attributesToMigrate;
    }

    @ResponseBody
    @GetMapping(value = "/getRandom")
    public Y9Result<Object> getRandom() {
        try {
            // String[] rsaArr = RSAUtil.genKeyPair();
            // redisTemplate.opsForValue().set(rsaArr[0], rsaArr[1], 120, TimeUnit.SECONDS);
            return Y9Result.success(Y9Context.getProperty("y9.login.encryptionRsaPublicKey"), "获取成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("获取失败");
        }
    }

}
