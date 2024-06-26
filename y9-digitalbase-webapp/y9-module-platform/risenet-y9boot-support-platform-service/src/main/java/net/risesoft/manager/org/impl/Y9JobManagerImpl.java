package net.risesoft.manager.org.impl;

import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.consts.CacheNameConsts;
import net.risesoft.entity.Y9Job;
import net.risesoft.exception.OrgUnitErrorCodeEnum;
import net.risesoft.manager.org.Y9JobManager;
import net.risesoft.repository.Y9JobRepository;
import net.risesoft.y9.exception.util.Y9ExceptionUtil;

/**
 * 职位 manager 实现类
 *
 * @author shidaobang
 * @date 2023/07/26
 * @since 9.6.3
 */
@Service
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@CacheConfig(cacheNames = CacheNameConsts.ORG_JOB)
@RequiredArgsConstructor
public class Y9JobManagerImpl implements Y9JobManager {

    private final Y9JobRepository y9JobRepository;

    @Override
    @CacheEvict(key = "#y9Job.id")
    @Transactional(readOnly = false)
    public void delete(Y9Job y9Job) {
        y9JobRepository.delete(y9Job);
    }

    @Override
    @Cacheable(key = "#id", condition = "#id!=null", unless = "#result==null")
    public Optional<Y9Job> findById(String id) {
        return y9JobRepository.findById(id);
    }

    @Override
    public Optional<Y9Job> findByIdNotCache(String id) {
        return y9JobRepository.findById(id);
    }

    @Override
    public Y9Job getByIdNotCache(String id) {
        return y9JobRepository.findById(id)
            .orElseThrow(() -> Y9ExceptionUtil.notFoundException(OrgUnitErrorCodeEnum.JOB_NOT_FOUND, id));
    }

    @Override
    @Cacheable(key = "#id", condition = "#id!=null", unless = "#result==null")
    public Y9Job getById(String id) {
        return y9JobRepository.findById(id)
            .orElseThrow(() -> Y9ExceptionUtil.notFoundException(OrgUnitErrorCodeEnum.JOB_NOT_FOUND, id));
    }

    @Override
    @CacheEvict(key = "#y9Job.id", condition = "#y9Job.id!=null")
    @Transactional(readOnly = false)
    public Y9Job save(Y9Job y9Job) {
        return y9JobRepository.save(y9Job);
    }

    @Override
    @CacheEvict(key = "#id")
    @Transactional(readOnly = false)
    public Y9Job updateTabIndex(String id, int tabIndex) {
        Y9Job y9Job = this.getById(id);
        y9Job.setTabIndex(tabIndex);
        return save(y9Job);
    }

}
