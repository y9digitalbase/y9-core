package net.risesoft.service.org.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Y9CustomGroup;
import net.risesoft.exception.OrgUnitErrorCodeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.manager.relation.Y9CustomGroupMembersManager;
import net.risesoft.pojo.Y9PageQuery;
import net.risesoft.repository.Y9CustomGroupRepository;
import net.risesoft.repository.relation.Y9CustomGroupMembersRepository;
import net.risesoft.service.org.Y9CustomGroupService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.exception.util.Y9ExceptionUtil;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Service
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@RequiredArgsConstructor
public class Y9CustomGroupServiceImpl implements Y9CustomGroupService {

    private final Y9CustomGroupRepository customGroupRepository;
    private final Y9CustomGroupMembersRepository customGroupMembersRepository;

    private final Y9CustomGroupMembersManager y9CustomGroupMembersManager;

    @Override
    @Transactional(readOnly = false)
    public void delete(List<String> idList) {
        for (String id : idList) {
            customGroupRepository.deleteById(id);
            customGroupMembersRepository.deleteByGroupId(id);
        }
    }

    @Override
    public Optional<Y9CustomGroup> findByCustomId(String customId) {
        return customGroupRepository.findByCustomId(customId);
    }

    @Override
    public Optional<Y9CustomGroup> findById(String id) {
        return customGroupRepository.findById(id);
    }

    @Override
    public Y9CustomGroup getById(String id) {
        return customGroupRepository.findById(id)
            .orElseThrow(() -> Y9ExceptionUtil.notFoundException(OrgUnitErrorCodeEnum.CUSTOM_GROUP_NOT_FOUND, id));
    }

    @Override
    public List<Y9CustomGroup> listByPersonId(String personId) {
        return customGroupRepository.findByPersonIdOrderByTabIndexAsc(personId);
    }

    @Override
    public Page<Y9CustomGroup> pageByPersonId(String personId, Y9PageQuery pageQuery) {
        PageRequest pageable =
            PageRequest.of(pageQuery.getPage4Db(), pageQuery.getSize(), Sort.by(Sort.Direction.ASC, "tabIndex"));
        return customGroupRepository.findByPersonId(personId, pageable);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9CustomGroup save(Y9CustomGroup y9CustomGroup) {
        if (StringUtils.isBlank(y9CustomGroup.getId())) {
            y9CustomGroup.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        }
        y9CustomGroup.setTenantId(Y9LoginUserHolder.getTenantId());
        return customGroupRepository.save(y9CustomGroup);
    }

    @Override
    @Transactional(readOnly = false)
    public boolean saveCustomGroupOrder(List<String> sortIdList) {
        for (int i = 0; i < sortIdList.size(); i++) {
            Y9CustomGroup customGroup = this.getById(sortIdList.get(i));
            customGroup.setTabIndex(i);
            this.save(customGroup);
        }
        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9CustomGroup saveOrUpdate(String personId, List<String> personIdList, String groupId, String groupName) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        Y9CustomGroup group = new Y9CustomGroup();
        if (StringUtils.isBlank(groupId)) {
            String guid = Y9IdGenerator.genId(IdType.SNOWFLAKE);
            group.setId(guid);
            group.setTenantId(tenantId);
            group.setGroupName(groupName);
            Integer tabIndex = customGroupRepository.getMaxTabIndex(personId).map(index -> index + 1).orElse(1);
            group.setTabIndex(tabIndex);
            group.setPersonId(personId);
            group = this.save(group);
        } else {
            group = this.getById(groupId);
            group.setGroupName(groupName);
            group = this.save(group);
        }
        y9CustomGroupMembersManager.save(personIdList, group.getId());
        return group;
    }

    @Override
    @Transactional(readOnly = false)
    public boolean share(List<String> personIds, List<String> groupIds) {
        for (String personId : personIds) {
            for (String groupId : groupIds) {
                Y9CustomGroup y9CustomGroup = this.share(personId, groupId);
                y9CustomGroupMembersManager.share(groupId, y9CustomGroup.getId());
            }
        }
        return true;
    }

    @Transactional(readOnly = false)
    public Y9CustomGroup share(String personId, String groupId) {
        Y9CustomGroup customGroup = this.getById(groupId);
        Y9CustomGroup group = new Y9CustomGroup();
        String id = Y9IdGenerator.genId(IdType.SNOWFLAKE);
        group.setId(id);
        group.setGroupName(customGroup.getGroupName());
        Integer tabIndex = customGroupRepository.getMaxTabIndex(personId).map(index -> index + 1).orElse(1);
        group.setTabIndex(tabIndex);
        group.setTenantId(customGroup.getTenantId());
        group.setShareId(customGroup.getPersonId());
        group.setPersonId(personId);
        return this.save(group);
    }

}
