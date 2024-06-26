package net.risesoft.y9public.manager.resource;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import net.risesoft.y9public.entity.resource.Y9App;

/**
 * 应用 Manager
 *
 * @author shidaobang
 * @date 2023/06/14
 * @since 9.6.2
 */
public interface Y9AppManager {
    void delete(String id);

    void deleteBySystemId(String systemId);

    void deleteTenantRelatedByAppId(String appId);

    Optional<Y9App> findById(String id);

    Y9App getById(String id);

    Y9App save(Y9App y9App);

    @Transactional(readOnly = false)
    Y9App updateTabIndex(String id, int index);
}
