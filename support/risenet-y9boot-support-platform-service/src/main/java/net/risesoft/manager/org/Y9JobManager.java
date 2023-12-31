package net.risesoft.manager.org;

import java.util.Optional;

import net.risesoft.entity.Y9Job;

/**
 * 职位 manager
 * 
 * @author shidaobang
 * @date 2023/07/26
 * @since 9.6.3
 */
public interface Y9JobManager {

    Optional<Y9Job> findById(String id);

    Y9Job getById(String id);

    Y9Job save(Y9Job y9Job);

    void delete(Y9Job y9Job);
}
