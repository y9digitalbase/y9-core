package net.risesoft.y9public.repository.custom;

import java.util.List;

/**
 *
 * @author guoweijun
 * @author shidaobang
 * @author mengjuhua
 *
 */
public interface Y9CommonAppForPersonCustomRepository {

    List<String> getAppNamesByPersonId(String personId);

    long getCount();

}
