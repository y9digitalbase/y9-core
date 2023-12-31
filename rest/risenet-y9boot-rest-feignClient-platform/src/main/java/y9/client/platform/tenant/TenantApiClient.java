package y9.client.platform.tenant;

import org.springframework.cloud.openfeign.FeignClient;

import net.risesoft.api.tenant.TenantApi;

/**
 * 租户管理员
 *
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 * @since 9.6.0
 */
@FeignClient(contextId = "TenantApiClient", name = "y9platform", url = "${y9.common.orgBaseUrl}",
    path = "/services/rest/v1/tenant", primary = false)
public interface TenantApiClient extends TenantApi {

}