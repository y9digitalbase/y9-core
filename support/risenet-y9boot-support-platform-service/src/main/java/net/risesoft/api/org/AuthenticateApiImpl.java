package net.risesoft.api.org;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.model.Message;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.org.Y9PersonService;
import net.risesoft.util.Y9PlatformUtil;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 用户身份验证服务组件
 *
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 * @since 9.6.0
 */
@Primary
@Validated
@RestController
@RequestMapping(value = "/services/rest/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticateApiImpl implements AuthenticateApi {

    private final Y9PersonService y9PersonService;

    /**
     * 根据用户名和密码认证
     *
     * @param tenantShortName 租户英文名称
     * @param loginName 登录名称
     * @param password 登录密码
     * @return Message
     * @since 9.6.0
     */
    @Override
    public Y9Result<Message> authenticate3(@RequestParam("tenantShortName") @NotBlank String tenantShortName,
        @RequestParam("loginName") @NotBlank String loginName, @RequestParam("password") @NotBlank String password) {
        List<String> tenantIds = Y9PlatformUtil.getTenantByLoginName(tenantShortName);
        if (!tenantIds.isEmpty()) {
            String tenantId = tenantIds.get(0);
            Y9LoginUserHolder.setTenantId(tenantId);
        }
        return Y9Result.success(y9PersonService.authenticate3(tenantShortName, loginName, password));
    }

    /**
     * 根据手机号和密码认证
     *
     * @param tenantShortName 租户英文名称
     * @param mobile 手机号
     * @param password 密码
     * @return Message
     * @since 9.6.0
     */
    @Override
    public Y9Result<Message> authenticate5(@RequestParam("tenantShortName") @NotBlank String tenantShortName,
        @RequestParam("mobile") @NotBlank String mobile, @RequestParam("password") @NotBlank String password) {
        List<String> tenantIds = Y9PlatformUtil.getTenantByLoginName(tenantShortName);
        if (!tenantIds.isEmpty()) {
            String tenantId = tenantIds.get(0);
            Y9LoginUserHolder.setTenantId(tenantId);
        }
        return Y9Result.success(y9PersonService.authenticate5(tenantShortName, mobile, password));
    }
}