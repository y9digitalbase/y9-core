package net.risesoft.service.org.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import net.risesoft.consts.OrgLevelConsts;
import net.risesoft.entity.Y9Department;
import net.risesoft.entity.Y9DepartmentProp;
import net.risesoft.entity.Y9OrgBase;
import net.risesoft.entity.Y9Organization;
import net.risesoft.enums.platform.AuthorizationPrincipalTypeEnum;
import net.risesoft.enums.platform.DepartmentPropCategoryEnum;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.exception.OrgUnitErrorCodeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.manager.org.CompositeOrgBaseManager;
import net.risesoft.manager.org.Y9DepartmentManager;
import net.risesoft.model.platform.Department;
import net.risesoft.repository.Y9DepartmentPropRepository;
import net.risesoft.repository.Y9DepartmentRepository;
import net.risesoft.repository.permission.Y9AuthorizationRepository;
import net.risesoft.repository.relation.Y9OrgBasesToRolesRepository;
import net.risesoft.service.org.Y9DepartmentService;
import net.risesoft.util.Y9OrgUtil;
import net.risesoft.util.Y9PublishServiceUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.exception.Y9BusinessException;
import net.risesoft.y9.pubsub.constant.Y9OrgEventConst;
import net.risesoft.y9.pubsub.event.Y9EntityDeletedEvent;
import net.risesoft.y9.pubsub.event.Y9EntityUpdatedEvent;
import net.risesoft.y9.pubsub.message.Y9MessageOrg;
import net.risesoft.y9.util.Y9BeanUtil;
import net.risesoft.y9.util.Y9ModelConvertUtil;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@Service
@RequiredArgsConstructor
public class Y9DepartmentServiceImpl implements Y9DepartmentService {

    private final EntityManagerFactory entityManagerFactory;
    private final Y9DepartmentRepository y9DepartmentRepository;
    private final Y9DepartmentPropRepository y9DepartmentPropRepository;
    private final Y9OrgBasesToRolesRepository y9OrgBasesToRolesRepository;
    private final Y9AuthorizationRepository y9AuthorizationRepository;

    private final CompositeOrgBaseManager compositeOrgBaseManager;
    private final Y9DepartmentManager y9DepartmentManager;

    @Override
    @Transactional(readOnly = false)
    public Y9Department changeDisable(String id) {

        // 检查所有子节点是否都禁用了，只有所有子节点都禁用了，当前部门才能禁用
        compositeOrgBaseManager.checkAllDecendantsDisabled(id);

        Y9Department dept = this.getById(id);
        Boolean isDisabled = !dept.getDisabled();
        dept.setDisabled(isDisabled);
        final Y9Department savedDepartment = y9DepartmentManager.save(dept);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String event = isDisabled ? "禁用" : "启用";
                Y9MessageOrg<Department> msg =
                    new Y9MessageOrg<>(Y9ModelConvertUtil.convert(savedDepartment, Department.class),
                        Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_DEPARTMENT, Y9LoginUserHolder.getTenantId());
                Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, event + "部门", event + savedDepartment.getName());
            }
        });

        return savedDepartment;
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String id) {
        Y9Department y9Department = this.getById(id);

        // 删除部门关联数据
        y9OrgBasesToRolesRepository.deleteByOrgId(id);
        y9DepartmentPropRepository.deleteByDeptId(id);
        y9AuthorizationRepository.deleteByPrincipalIdAndPrincipalType(id, AuthorizationPrincipalTypeEnum.DEPARTMENT);

        y9DepartmentManager.delete(y9Department);

        // 发布事件，程序内部监听处理相关业务
        Y9Context.publishEvent(new Y9EntityDeletedEvent<>(y9Department));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Y9MessageOrg<Department> msg =
                    new Y9MessageOrg<>(Y9ModelConvertUtil.convert(y9Department, Department.class),
                        Y9OrgEventConst.RISEORGEVENT_TYPE_DELETE_DEPARTMENT, Y9LoginUserHolder.getTenantId());
                Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "删除部门", "删除 " + y9Department.getName());
            }
        });
    }

    @Override
    public boolean existsById(String id) {
        return y9DepartmentRepository.existsById(id);
    }

    @Override
    public Optional<Y9Department> findById(String id) {
        return y9DepartmentManager.findById(id);
    }

    @Override
    public Y9Department getById(String id) {
        return y9DepartmentManager.getById(id);
    }

    @Override
    public List<Y9OrgBase> getDeptSecretarys(String deptId) {
        List<Y9OrgBase> y9OrgBaseList = new ArrayList<>();
        List<Y9DepartmentProp> propList = y9DepartmentPropRepository.findByDeptIdAndCategoryOrderByTabIndex(deptId,
            DepartmentPropCategoryEnum.SECRETARY);
        for (Y9DepartmentProp prop : propList) {
            Y9OrgBase y9OrgBase = compositeOrgBaseManager.getOrgUnit(prop.getOrgBaseId());
            if (y9OrgBase != null) {
                y9OrgBaseList.add(y9OrgBase);
            }
        }
        return y9OrgBaseList;
    }

    @Override
    public List<Y9Department> listRecursivelyByParentId(String orgUnitId, Boolean disabled) {
        List<Y9Department> deptList = new ArrayList<>();
        getDeptTrees(orgUnitId, deptList, disabled);
        return deptList;
    }

    @Override
    public List<Y9Department> list() {
        return y9DepartmentRepository.findAll();
    }

    @Override
    public List<Y9Department> listBureau(String organizationId, Boolean disabled) {
        if (disabled == null) {
            return y9DepartmentRepository.findByBureauAndGuidPathContainingOrderByTabIndexAsc(true, organizationId);
        } else {
            return y9DepartmentRepository.findByBureauAndGuidPathContainingAndDisabledOrderByTabIndexAsc(true,
                organizationId, disabled);
        }
    }

    @Override
    public List<Y9Department> listByDn(String dn, Boolean disabled) {
        if (disabled == null) {
            return y9DepartmentRepository.findByDn(dn);
        } else {
            return y9DepartmentRepository.findByDnAndDisabled(dn, disabled);
        }
    }

    @Override
    public List<Y9Department> listByName(String name, Boolean disabled) {
        if (disabled == null) {
            return y9DepartmentRepository.findByNameContainingOrderByTabIndexAsc(name);
        } else {
            return y9DepartmentRepository.findByNameContainingAndDisabledOrderByTabIndexAsc(name, disabled);
        }
    }

    @Override
    public List<Y9Department> listByParentId(String parentId, Boolean disabled) {
        if (disabled == null) {
            return y9DepartmentRepository.findByParentIdOrderByTabIndexAsc(parentId);
        } else {
            return y9DepartmentRepository.findByParentIdAndDisabledOrderByTabIndexAsc(parentId, disabled);
        }
    }

    @Override
    public List<Y9OrgBase> listLeaders(String deptId, Boolean disabled) {
        List<Y9OrgBase> y9OrgBaseList = new ArrayList<>();
        List<Y9DepartmentProp> propList = y9DepartmentPropRepository.findByDeptIdAndCategoryOrderByTabIndex(deptId,
            DepartmentPropCategoryEnum.LEADER);
        for (Y9DepartmentProp prop : propList) {
            Y9OrgBase y9OrgBase = compositeOrgBaseManager.getOrgUnit(prop.getOrgBaseId());
            if (disabled == null) {
                y9OrgBaseList.add(y9OrgBase);
            } else if (disabled.equals(y9OrgBase.getDisabled())) {
                y9OrgBaseList.add(y9OrgBase);
            }
        }
        return y9OrgBaseList;
    }

    @Override
    public List<Y9OrgBase> listManagers(String deptId, Boolean disabled) {
        List<Y9OrgBase> y9OrgBaseList = new ArrayList<>();
        List<Y9DepartmentProp> propList = y9DepartmentPropRepository.findByDeptIdAndCategoryOrderByTabIndex(deptId,
            DepartmentPropCategoryEnum.MANAGER);
        for (Y9DepartmentProp prop : propList) {
            Y9OrgBase y9OrgBase = compositeOrgBaseManager.getOrgUnit(prop.getOrgBaseId());
            if (disabled == null) {
                y9OrgBaseList.add(y9OrgBase);
            } else if (disabled.equals(y9OrgBase.getDisabled())) {
                y9OrgBaseList.add(y9OrgBase);
            }
        }
        return y9OrgBaseList;
    }

    @Override
    public List<Y9OrgBase> listViceLeaders(String deptId) {
        List<Y9OrgBase> y9OrgBaseList = new ArrayList<>();
        List<Y9DepartmentProp> propList = y9DepartmentPropRepository.findByDeptIdAndCategoryOrderByTabIndex(deptId,
            DepartmentPropCategoryEnum.VICE_LEADER);
        for (Y9DepartmentProp prop : propList) {
            Y9OrgBase y9OrgBase = compositeOrgBaseManager.getOrgUnit(prop.getOrgBaseId());
            if (y9OrgBase != null) {
                y9OrgBaseList.add(y9OrgBase);
            }
        }
        return y9OrgBaseList;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Department move(String deptId, String parentId) {
        Y9Department originDepartment = new Y9Department();
        Y9Department updatedDepartment = getById(deptId);
        Y9BeanUtil.copyProperties(updatedDepartment, originDepartment);

        checkMoveTarget(updatedDepartment, parentId);

        Y9OrgBase parent = compositeOrgBaseManager.getOrgUnitAsParent(parentId);
        updatedDepartment.setParentId(parent.getId());
        updatedDepartment.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.DEPARTMENT) + updatedDepartment.getName()
            + OrgLevelConsts.SEPARATOR + parent.getDn());
        updatedDepartment.setGuidPath(parent.getGuidPath() + OrgLevelConsts.SEPARATOR + updatedDepartment.getId());
        updatedDepartment.setTabIndex(compositeOrgBaseManager.getMaxSubTabIndex(parentId));
        final Y9Department savedDepartment = y9DepartmentManager.save(updatedDepartment);

        Y9Context.publishEvent(new Y9EntityUpdatedEvent<>(originDepartment, savedDepartment));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Y9MessageOrg<Department> msg =
                    new Y9MessageOrg<>(Y9ModelConvertUtil.convert(savedDepartment, Department.class),
                        Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_DEPARTMENT, Y9LoginUserHolder.getTenantId());
                Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "移动部门", "移动" + originDepartment.getName());
            }
        });

        return savedDepartment;
    }

    @Override
    @Transactional(readOnly = false)
    public void removeLeader(String deptId, String orgBaseId) {
        Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
            .findByDeptIdAndOrgBaseIdAndCategory(deptId, orgBaseId, DepartmentPropCategoryEnum.LEADER);
        if (optionalY9DepartmentProp.isPresent()) {
            y9DepartmentPropRepository.delete(optionalY9DepartmentProp.get());
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void removeManager(String deptId, String orgBaseId) {
        Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
            .findByDeptIdAndOrgBaseIdAndCategory(deptId, orgBaseId, DepartmentPropCategoryEnum.MANAGER);
        if (optionalY9DepartmentProp.isPresent()) {
            y9DepartmentPropRepository.delete(optionalY9DepartmentProp.get());
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void removeSecretary(String deptId, String personId) {
        Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
            .findByDeptIdAndOrgBaseIdAndCategory(deptId, personId, DepartmentPropCategoryEnum.SECRETARY);
        if (optionalY9DepartmentProp.isPresent()) {
            y9DepartmentPropRepository.delete(optionalY9DepartmentProp.get());
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void removeViceLeader(String deptId, String personId) {
        Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
            .findByDeptIdAndOrgBaseIdAndCategory(deptId, personId, DepartmentPropCategoryEnum.VICE_LEADER);
        if (optionalY9DepartmentProp.isPresent()) {
            y9DepartmentPropRepository.delete(optionalY9DepartmentProp.get());
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<Y9Department> saveOrder(List<String> deptIds) {
        List<Y9Department> orgDeptList = new ArrayList<>();

        int tabIndex = 0;
        for (String deptId : deptIds) {
            orgDeptList.add(this.saveOrder(deptId, tabIndex++));
        }

        return orgDeptList;
    }

    @Transactional(readOnly = false)
    public Y9Department saveOrder(String deptId, int i) {
        Y9Department dept = this.getById(deptId);
        dept.setTabIndex(i);
        return y9DepartmentManager.save(dept);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Department saveOrUpdate(Y9Department dept) {
        Y9OrgBase parent = compositeOrgBaseManager.getOrgUnitAsParent(dept.getParentId());

        if (StringUtils.isNotEmpty(dept.getId())) {
            Optional<Y9Department> y9DepartmentOptional = y9DepartmentManager.findById(dept.getId());
            if (y9DepartmentOptional.isPresent()) {
                Y9Department originDepartment = new Y9Department();
                Y9Department updatedDepartment = y9DepartmentOptional.get();
                Y9BeanUtil.copyProperties(updatedDepartment, originDepartment);

                Y9BeanUtil.copyProperties(dept, updatedDepartment);

                updatedDepartment.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.DEPARTMENT) + dept.getName()
                    + OrgLevelConsts.SEPARATOR + parent.getDn());
                updatedDepartment.setParentId(parent.getId());
                updatedDepartment.setGuidPath(parent.getGuidPath() + OrgLevelConsts.SEPARATOR + dept.getId());

                final Y9Department savedDepartment = y9DepartmentManager.save(updatedDepartment);

                Y9Context.publishEvent(new Y9EntityUpdatedEvent<>(originDepartment, savedDepartment));

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        Y9MessageOrg<Department> msg =
                            new Y9MessageOrg<>(Y9ModelConvertUtil.convert(savedDepartment, Department.class),
                                Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_DEPARTMENT, Y9LoginUserHolder.getTenantId());
                        Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "修改部门",
                            "修改 " + originDepartment.getName());
                    }
                });

                return savedDepartment;
            }
        } else {
            dept.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        }
        Integer maxTabIndex = compositeOrgBaseManager.getMaxSubTabIndex(parent.getId());
        dept.setTabIndex(maxTabIndex);
        dept.setVersion(OrgTypeEnum.Y9_VERSION);
        dept.setDn(OrgLevelConsts.getOrgLevel(OrgTypeEnum.DEPARTMENT) + dept.getName() + OrgLevelConsts.SEPARATOR
            + parent.getDn());
        dept.setDisabled(false);
        dept.setParentId(parent.getId());
        dept.setTenantId(Y9LoginUserHolder.getTenantId());
        dept.setGuidPath(parent.getGuidPath() + OrgLevelConsts.SEPARATOR + dept.getId());

        final Y9Department savedDepartment = y9DepartmentManager.save(dept);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Y9MessageOrg<Department> msg =
                    new Y9MessageOrg<>(Y9ModelConvertUtil.convert(savedDepartment, Department.class),
                        Y9OrgEventConst.RISEORGEVENT_TYPE_ADD_DEPARTMENT, Y9LoginUserHolder.getTenantId());
                Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "新增部门", "新增" + savedDepartment.getName());
            }
        });

        return savedDepartment;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Department saveProperties(String id, String properties) {
        Y9Department dept = this.getById(id);
        dept.setProperties(properties);
        final Y9Department savedDepartment = y9DepartmentManager.save(dept);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Y9MessageOrg<Department> msg =
                    new Y9MessageOrg<>(Y9ModelConvertUtil.convert(savedDepartment, Department.class),
                        Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_DEPARTMENT, Y9LoginUserHolder.getTenantId());
                Y9PublishServiceUtil.publishMessageOrg(msg);
            }
        });

        return savedDepartment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Y9Department> search(String whereClause) {
        EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        Query query = null;
        if (null != em) {
            if (whereClause == null || "".equals(whereClause.trim())) {
                query = em.createNativeQuery(" SELECT * FROM Y9_ORG_DEPARTMENT ", Y9Department.class);

            } else {
                query =
                    em.createNativeQuery(" SELECT * FROM Y9_ORG_DEPARTMENT where " + whereClause, Y9Department.class);
            }
            return query.getResultList();
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = false)
    public void setDeptLeaders(String deptId, List<String> orgBaseIds) {
        for (String orgBaseId : orgBaseIds) {
            Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
                .findByDeptIdAndOrgBaseIdAndCategory(deptId, orgBaseId, DepartmentPropCategoryEnum.LEADER);
            if (optionalY9DepartmentProp.isEmpty()) {
                Y9DepartmentProp y9DepartmentProp = new Y9DepartmentProp();
                y9DepartmentProp.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                y9DepartmentProp.setDeptId(deptId);
                y9DepartmentProp.setOrgBaseId(orgBaseId);
                y9DepartmentProp.setCategory(DepartmentPropCategoryEnum.LEADER);

                Integer tabIndex = y9DepartmentPropRepository.getMaxTabIndex(deptId, DepartmentPropCategoryEnum.LEADER);
                if (null == tabIndex) {
                    tabIndex = 1;
                } else {
                    tabIndex++;
                }
                y9DepartmentProp.setTabIndex(tabIndex);
                y9DepartmentPropRepository.save(y9DepartmentProp);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void setDeptManagers(String deptId, List<String> orgBaseIds) {
        for (String orgBaseId : orgBaseIds) {
            Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
                .findByDeptIdAndOrgBaseIdAndCategory(deptId, orgBaseId, DepartmentPropCategoryEnum.MANAGER);
            if (optionalY9DepartmentProp.isEmpty()) {
                Y9DepartmentProp y9DepartmentProp = new Y9DepartmentProp();
                y9DepartmentProp.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                y9DepartmentProp.setDeptId(deptId);
                y9DepartmentProp.setOrgBaseId(orgBaseId);
                y9DepartmentProp.setCategory(DepartmentPropCategoryEnum.MANAGER);

                Integer tabIndex =
                    y9DepartmentPropRepository.getMaxTabIndex(deptId, DepartmentPropCategoryEnum.MANAGER);
                if (null == tabIndex) {
                    tabIndex = 1;
                } else {
                    tabIndex++;
                }
                y9DepartmentProp.setTabIndex(tabIndex);
                y9DepartmentPropRepository.save(y9DepartmentProp);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void setDeptSecretarys(String deptId, List<String> orgBaseIds) {
        for (String orgBaseId : orgBaseIds) {
            Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
                .findByDeptIdAndOrgBaseIdAndCategory(deptId, orgBaseId, DepartmentPropCategoryEnum.SECRETARY);
            if (optionalY9DepartmentProp.isEmpty()) {
                Y9DepartmentProp y9DepartmentProp = new Y9DepartmentProp();
                y9DepartmentProp.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                y9DepartmentProp.setDeptId(deptId);
                y9DepartmentProp.setOrgBaseId(orgBaseId);
                y9DepartmentProp.setCategory(DepartmentPropCategoryEnum.SECRETARY);

                Integer tabIndex =
                    y9DepartmentPropRepository.getMaxTabIndex(deptId, DepartmentPropCategoryEnum.SECRETARY);
                if (null == tabIndex) {
                    tabIndex = 1;
                } else {
                    tabIndex++;
                }
                y9DepartmentProp.setTabIndex(tabIndex);
                y9DepartmentPropRepository.save(y9DepartmentProp);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void setDeptViceLeaders(String deptId, List<String> orgBaseIds) {
        for (String orgBaseId : orgBaseIds) {
            Optional<Y9DepartmentProp> optionalY9DepartmentProp = y9DepartmentPropRepository
                .findByDeptIdAndOrgBaseIdAndCategory(deptId, orgBaseId, DepartmentPropCategoryEnum.VICE_LEADER);
            if (optionalY9DepartmentProp.isEmpty()) {
                Y9DepartmentProp y9DepartmentProp = new Y9DepartmentProp();
                y9DepartmentProp.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                y9DepartmentProp.setDeptId(deptId);
                y9DepartmentProp.setOrgBaseId(orgBaseId);
                y9DepartmentProp.setCategory(DepartmentPropCategoryEnum.VICE_LEADER);

                Integer tabIndex =
                    y9DepartmentPropRepository.getMaxTabIndex(deptId, DepartmentPropCategoryEnum.VICE_LEADER);
                if (null == tabIndex) {
                    tabIndex = 1;
                } else {
                    tabIndex++;
                }
                y9DepartmentProp.setTabIndex(tabIndex);
                y9DepartmentPropRepository.save(y9DepartmentProp);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Department updateTabIndex(String id, int tabIndex) {
        Y9Department department = this.getById(id);
        department.setTabIndex(tabIndex);
        final Y9Department savedDepartment = y9DepartmentManager.save(department);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Y9MessageOrg<Department> msg =
                    new Y9MessageOrg<>(Y9ModelConvertUtil.convert(savedDepartment, Department.class),
                        Y9OrgEventConst.RISEORGEVENT_TYPE_UPDATE_DEPARTMENT_TABINDEX, Y9LoginUserHolder.getTenantId());
                Y9PublishServiceUtil.persistAndPublishMessageOrg(msg, "更新部门排序号",
                    savedDepartment.getName() + "的排序号更新为" + tabIndex);
            }
        });

        return savedDepartment;
    }

    @Override
    public List<Y9Department> list(List<String> ids) {
        List<Y9Department> y9DepartmentList = new ArrayList<>();
        for (String id : ids) {
            Optional<Y9Department> y9DepartmentOptional = findById(id);
            y9DepartmentOptional.ifPresent(y9DepartmentList::add);
        }
        return y9DepartmentList;
    }

    /**
     * 判断是否移动到自己，或者自己的子节点里面，这种情况要排除，不让移动
     *
     * @param dept
     * @param parentId
     */
    private void checkMoveTarget(Y9Department dept, String parentId) {
        List<Y9OrgBase> parentList = new ArrayList<>();
        recursionParent(parentId, parentList);
        if (parentList.contains(dept)) {
            throw new Y9BusinessException(OrgUnitErrorCodeEnum.MOVE_TO_SUB_DEPARTMENT_NOT_PERMITTED.getCode(),
                OrgUnitErrorCodeEnum.MOVE_TO_SUB_DEPARTMENT_NOT_PERMITTED.getDescription());
        }
    }

    private void getDeptTrees(String orgBaseId, List<Y9Department> deptList, Boolean disabled) {
        List<Y9Department> childrenList = this.listByParentIdAndDisabled(orgBaseId, disabled);
        if (childrenList.isEmpty()) {
            return;
        }
        deptList.addAll(childrenList);
        for (Y9Department orgDept : childrenList) {
            getDeptTrees(orgDept.getId(), deptList, disabled);
        }
    }

    private List<Y9Department> listByParentIdAndDisabled(String orgBaseId, boolean disabled) {
        return y9DepartmentRepository.findByParentIdAndDisabled(orgBaseId, disabled);
    }

    @EventListener
    @Transactional(readOnly = false)
    public void onParentDepartmentDeleted(Y9EntityDeletedEvent<Y9Department> event) {
        Y9Department parentDepartment = event.getEntity();
        // 删除部门时其下部门也要删除
        removeByParentId(parentDepartment.getId());
    }

    @Transactional(readOnly = false)
    public void removeByParentId(String parentId) {
        List<Y9Department> y9DepartmentList = this.listByParentId(parentId, Boolean.FALSE);
        for (Y9Department y9Department : y9DepartmentList) {
            this.delete(y9Department.getId());
        }
    }

    @EventListener
    @Transactional(readOnly = false)
    public void onParentOrganizationDeleted(Y9EntityDeletedEvent<Y9Organization> event) {
        Y9Organization y9Organization = event.getEntity();
        // 删除组织时其下部门也要删除
        removeByParentId(y9Organization.getId());
    }

    @EventListener
    @Transactional(readOnly = false)
    public void onParentOrganizationUpdated(Y9EntityUpdatedEvent<Y9Organization> event) {
        Y9Organization originOrganization = event.getOriginEntity();
        Y9Organization updatedOrganization = event.getUpdatedEntity();

        if (Y9OrgUtil.isRenamed(originOrganization, updatedOrganization)) {
            List<Y9Department> deptList = y9DepartmentRepository.findByParentId(updatedOrganization.getId());
            for (Y9Department dept : deptList) {
                this.saveOrUpdate(dept);
            }
        }
    }

    @EventListener
    @Transactional(readOnly = false)
    public void onParentDepartmentUpdated(Y9EntityUpdatedEvent<Y9Department> event) {
        Y9Department originDepartment = event.getOriginEntity();
        Y9Department updatedDepartment = event.getUpdatedEntity();

        if (Y9OrgUtil.isAncestorChanged(originDepartment, updatedDepartment)) {
            List<Y9Department> deptList = y9DepartmentRepository.findByParentId(updatedDepartment.getId());
            for (Y9Department dept : deptList) {
                this.saveOrUpdate(dept);
            }
        }
    }

    private void recursionParent(String parentId, List<Y9OrgBase> parentList) {
        Y9OrgBase parent = compositeOrgBaseManager.getOrgUnitAsParent(parentId);
        parentList.add(parent);
        if (parent.getOrgType().equals(OrgTypeEnum.DEPARTMENT)) {
            Y9Department dept = (Y9Department)parent;
            recursionParent(dept.getParentId(), parentList);
        }
    }

}
