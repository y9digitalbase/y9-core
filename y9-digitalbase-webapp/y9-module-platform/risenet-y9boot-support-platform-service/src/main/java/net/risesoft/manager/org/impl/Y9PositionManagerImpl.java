package net.risesoft.manager.org.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import net.risesoft.consts.CacheNameConsts;
import net.risesoft.consts.OrgLevelConsts;
import net.risesoft.entity.Y9Job;
import net.risesoft.entity.Y9OrgBase;
import net.risesoft.entity.Y9Person;
import net.risesoft.entity.Y9Position;
import net.risesoft.entity.relation.Y9PersonsToPositions;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.exception.OrgUnitErrorCodeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.manager.org.CompositeOrgBaseManager;
import net.risesoft.manager.org.Y9JobManager;
import net.risesoft.manager.org.Y9PersonManager;
import net.risesoft.manager.org.Y9PositionManager;
import net.risesoft.model.platform.Position;
import net.risesoft.repository.Y9PositionRepository;
import net.risesoft.repository.relation.Y9PersonsToPositionsRepository;
import net.risesoft.util.ModelConvertUtil;
import net.risesoft.util.Y9PublishServiceUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.configuration.Y9Properties;
import net.risesoft.y9.exception.util.Y9ExceptionUtil;
import net.risesoft.y9.pubsub.constant.Y9OrgEventConst;
import net.risesoft.y9.pubsub.event.Y9EntityCreatedEvent;
import net.risesoft.y9.pubsub.event.Y9EntityUpdatedEvent;
import net.risesoft.y9.pubsub.message.Y9MessageOrg;
import net.risesoft.y9.util.Y9Assert;
import net.risesoft.y9.util.Y9BeanUtil;
import net.risesoft.y9.util.Y9ModelConvertUtil;

@Service
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@CacheConfig(cacheNames = CacheNameConsts.ORG_POSITION)
@RequiredArgsConstructor
public class Y9PositionManagerImpl implements Y9PositionManager {

    private final Y9PersonsToPositionsRepository y9PersonsToPositionsRepository;
    private final Y9PositionRepository y9PositionRepository;

    private final Y9JobManager y9JobManager;
    private final Y9PersonManager y9PersonManager;
    private final CompositeOrgBaseManager compositeOrgBaseManager;

    private final Y9Properties y9Properties;

    @Override
    public String buildName(Y9Job y9Job, List<Y9PersonsToPositions> personsToPositionsList) {
        String name;
        String pattern = y9Properties.getApp().getPlatform().getPositionNamePattern();

        if (personsToPositionsList.isEmpty()) {
            name = MessageFormat.format(pattern, y9Job.getName(), "空缺");
        } else {
            List<Y9Person> personList = new ArrayList<>();
            for (Y9PersonsToPositions y9PersonsToPositions : personsToPositionsList) {
                Y9Person person = y9PersonManager.getById(y9PersonsToPositions.getPersonId());
                personList.add(person);
            }
            String personNames = personList.stream().sorted((Comparator.comparing(Y9Person::getOrderedPath)))
                .map(Y9OrgBase::getName).collect(Collectors.joining("，"));
            name = MessageFormat.format(pattern, y9Job.getName(), personNames);
        }
        return name;
    }

    @Override
    @Cacheable(key = "#id", condition = "#id!=null", unless = "#result==null")
    public Y9Position getById(String id) {
        return y9PositionRepository.findById(id)
            .orElseThrow(() -> Y9ExceptionUtil.notFoundException(OrgUnitErrorCodeEnum.POSITION_NOT_FOUND, id));
    }

    @Override
    @Cacheable(key = "#id", condition = "#id!=null", unless = "#result==null")
    public Optional<Y9Position> findById(String id) {
        return y9PositionRepository.findById(id);
    }

    private void checkHeadCountAvailability(Y9Position position) {
        Integer personCount = y9PersonsToPositionsRepository.countByPositionId(position.getId());
        Y9Assert.lessThanOrEqualTo(personCount, position.getCapacity(), OrgUnitErrorCodeEnum.POSITION_IS_FULL,
            position.getName());
    }

    @Override
    @Transactional(readOnly = false)
    @CacheEvict(key = "#position.id")
    public Y9Position saveOrUpdate(Y9Position position) {
        Y9OrgBase parent = compositeOrgBaseManager.getOrgUnitAsParent(position.getParentId());
        Y9Job y9Job = y9JobManager.getById(position.getJobId());

        if (StringUtils.isNotEmpty(position.getId())) {
            Optional<Y9Position> updatedY9PositionOptional = this.findById(position.getId());
            if (updatedY9PositionOptional.isPresent()) {
                Y9Position updatedY9Position = updatedY9PositionOptional.get();
                // 修改的岗位容量不能小于当前岗位人数
                checkHeadCountAvailability(position);

                Y9Position originY9Position = new Y9Position();
                Y9BeanUtil.copyProperties(updatedY9Position, originY9Position);
                Y9BeanUtil.copyProperties(position, updatedY9Position);

                updatedY9Position.setParentId(parent.getId());
                updatedY9Position
                    .setGuidPath(parent.getGuidPath() + OrgLevelConsts.SEPARATOR + updatedY9Position.getId());
                updatedY9Position.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.POSITION) + updatedY9Position.getName()
                    + OrgLevelConsts.SEPARATOR + parent.getDn());

                final Y9Position savedY9Position = save(updatedY9Position);

                Y9Context.publishEvent(new Y9EntityUpdatedEvent<>(originY9Position, savedY9Position));

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        Y9MessageOrg<Position> msg =
                            new Y9MessageOrg<>(ModelConvertUtil.convert(savedY9Position, Position.class),
                                Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_POSITION, Y9LoginUserHolder.getTenantId());
                        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "更新岗位信息",
                            "更新" + originY9Position.getName());
                    }
                });

                return savedY9Position;
            }
        }
        if (StringUtils.isEmpty(position.getId())) {
            position.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        }
        position.setTenantId(Y9LoginUserHolder.getTenantId());
        position.setName(this.buildName(y9Job, Collections.emptyList()));
        position.setJobName(y9Job.getName());
        position.setTabIndex(compositeOrgBaseManager.getMaxSubTabIndex(parent.getId()));
        position.setDisabled(false);
        position.setParentId(parent.getId());
        position.setGuidPath(parent.getGuidPath() + OrgLevelConsts.SEPARATOR + position.getId());
        position.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.POSITION) + position.getName() + OrgLevelConsts.SEPARATOR
            + parent.getDn());
        Y9Position returnPosition = save(position);

        Y9MessageOrg<Position> msg = new Y9MessageOrg<>(ModelConvertUtil.convert(returnPosition, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_ADD_POSITION, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "新增岗位信息", "新增" + position.getName());

        Y9Context.publishEvent(new Y9EntityCreatedEvent<>(returnPosition));

        return returnPosition;
    }

    @Override
    @Transactional(readOnly = false)
    @CacheEvict(key = "#position.id")
    public Y9Position save(Y9Position position) {
        return y9PositionRepository.save(position);
    }

    @Override
    @Transactional(readOnly = false)
    @CacheEvict(key = "#y9Position.id")
    public void delete(Y9Position y9Position) {
        y9PositionRepository.delete(y9Position);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Position updateTabIndex(String id, int tabIndex) {
        Y9Position position = this.getById(id);
        position.setTabIndex(tabIndex);
        position = this.save(position);

        Y9MessageOrg<Position> msg = new Y9MessageOrg<>(Y9ModelConvertUtil.convert(position, Position.class),
            Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_POSITION_TABINDEX, Y9LoginUserHolder.getTenantId());
        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "更新岗位排序号", position.getName() + "的排序号更新为" + tabIndex);

        return position;
    }
}
