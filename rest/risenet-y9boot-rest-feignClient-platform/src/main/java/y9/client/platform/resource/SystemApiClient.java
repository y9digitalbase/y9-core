package y9.client.platform.resource;

import org.springframework.cloud.openfeign.FeignClient;

import net.risesoft.api.resource.SystemApi;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 * @since 9.6.0
 */
@FeignClient(contextId = "SystemApiClient", name = "y9platform", url = "${y9.common.orgBaseUrl}", path = "/services/rest/system")
public interface SystemApiClient extends SystemApi {

}