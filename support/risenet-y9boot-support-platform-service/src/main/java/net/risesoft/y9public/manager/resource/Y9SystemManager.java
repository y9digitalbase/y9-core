package net.risesoft.y9public.manager.resource;

import org.springframework.transaction.annotation.Transactional;

import net.risesoft.y9public.entity.resource.Y9System;

/**
 * 系统 Manager
 * 
 * @author shidaobang
 * @date 2023/08/22
 * @since 9.6.3
 */
public interface Y9SystemManager {

    @Transactional(readOnly = false)
    Y9System save(Y9System y9System);

    Y9System getById(String id);

    Y9System findById(String id);

    @Transactional(readOnly = false)
    void delete(String id);
}
