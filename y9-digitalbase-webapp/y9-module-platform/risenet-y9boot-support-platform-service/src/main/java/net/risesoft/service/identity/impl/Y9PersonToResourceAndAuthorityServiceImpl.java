package net.risesoft.service.identity.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Y9Person;
import net.risesoft.entity.identity.Y9IdentityToResourceAndAuthorityBase;
import net.risesoft.entity.identity.person.Y9PersonToResourceAndAuthority;
import net.risesoft.entity.permission.Y9Authorization;
import net.risesoft.enums.platform.AuthorityEnum;
import net.risesoft.enums.platform.ResourceTypeEnum;
import net.risesoft.manager.identity.Y9PersonToResourceAndAuthorityManager;
import net.risesoft.manager.org.CompositeOrgBaseManager;
import net.risesoft.repository.identity.person.Y9PersonToResourceAndAuthorityRepository;
import net.risesoft.service.identity.Y9PersonToResourceAndAuthorityService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.entity.resource.Y9App;
import net.risesoft.y9public.entity.resource.Y9Menu;
import net.risesoft.y9public.entity.resource.Y9ResourceBase;
import net.risesoft.y9public.entity.tenant.Y9TenantApp;
import net.risesoft.y9public.manager.resource.Y9AppManager;
import net.risesoft.y9public.manager.resource.Y9MenuManager;
import net.risesoft.y9public.manager.tenant.Y9TenantAppManager;
import net.risesoft.y9public.service.resource.CompositeResourceService;

/**
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
@Service
@RequiredArgsConstructor
public class Y9PersonToResourceAndAuthorityServiceImpl implements Y9PersonToResourceAndAuthorityService {

    private final Y9PersonToResourceAndAuthorityRepository y9PersonToResourceAndAuthorityRepository;

    private final CompositeOrgBaseManager compositeOrgBaseManager;
    private final Y9PersonToResourceAndAuthorityManager y9PersonToResourceAndAuthorityManager;
    private final CompositeResourceService compositeResourceService;
    private final Y9AppManager y9AppManager;
    private final Y9MenuManager y9MenuManager;
    private final Y9TenantAppManager y9TenantAppManager;

    @Override
    public void deleteByAuthorizationIdAndOrgUnitId(String authorizationId, String orgUnitId) {
        List<Y9Person> allPersons = compositeOrgBaseManager.listAllPersonsRecursionDownward(orgUnitId);
        for (Y9Person y9Person : allPersons) {
            this.deleteByAuthorizationIdAndPersonId(authorizationId, y9Person.getId());
        }
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteByAuthorizationIdAndPersonId(String authorizationId, String personId) {
        y9PersonToResourceAndAuthorityRepository.deleteByAuthorizationIdAndPersonId(authorizationId, personId);
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteByOrgUnitId(String orgUnitId) {
        List<Y9Person> personList = compositeOrgBaseManager.listAllPersonsRecursionDownward(orgUnitId);
        for (Y9Person y9Person : personList) {
            deleteByPersonId(y9Person.getId());
        }
    }

    @Transactional(readOnly = false)
    @Override
    public void deleteByPersonId(String personId) {
        y9PersonToResourceAndAuthorityRepository.deleteByPersonId(personId);
    }

    @Override
    public boolean hasPermission(String personId, String resourceId, AuthorityEnum authority) {
        return !y9PersonToResourceAndAuthorityRepository
            .findByPersonIdAndResourceIdAndAuthority(personId, resourceId, authority).isEmpty();
    }

    @Override
    public boolean hasPermissionByCustomId(String personId, String resourceCustomId, AuthorityEnum authority) {
        return !y9PersonToResourceAndAuthorityRepository
            .findByPersonIdAndResourceCustomIdAndAuthority(personId, resourceCustomId, authority).isEmpty();
    }

    @Override
    public List<Y9PersonToResourceAndAuthority> list(String personId) {
        return y9PersonToResourceAndAuthorityRepository.findByPersonId(personId);
    }

    @Override
    public List<Y9PersonToResourceAndAuthority> list(String personId, String parentResourceId,
        AuthorityEnum authority) {
        return y9PersonToResourceAndAuthorityRepository
            .findByPersonIdAndParentResourceIdAndAuthorityOrderByResourceTabIndex(personId, parentResourceId,
                authority);
    }

    @Override
    public List<Y9PersonToResourceAndAuthority> list(String personId, String parentResourceId,
        ResourceTypeEnum resourceType, AuthorityEnum authority) {
        return y9PersonToResourceAndAuthorityRepository
            .findByPersonIdAndParentResourceIdAndAuthorityAndResourceTypeOrderByResourceTabIndex(personId,
                parentResourceId, authority, resourceType);
    }

    @Override
    public List<Y9App> listAppsByAuthority(String personId, AuthorityEnum authority) {
        List<Y9PersonToResourceAndAuthority> resourceList =
            y9PersonToResourceAndAuthorityRepository.findByPersonIdAndAuthorityAndResourceTypeOrderByResourceTabIndex(
                personId, authority, ResourceTypeEnum.APP);
        List<Y9App> appList = new ArrayList<>();
        for (Y9PersonToResourceAndAuthority r : resourceList) {
            Optional<Y9TenantApp> y9TenantAppOptional =
                y9TenantAppManager.getByTenantIdAndAppIdAndTenancy(Y9LoginUserHolder.getTenantId(), r.getAppId(), true);
            if (y9TenantAppOptional.isPresent()) {
                Y9App y9App = y9AppManager.getById(r.getAppId());
                if (!appList.contains(y9App)) {
                    appList.add(y9App);
                }
            }
        }
        return appList;
    }

    @Transactional(readOnly = false)
    @Override
    public void saveOrUpdate(Y9ResourceBase y9ResourceBase, Y9Person person, Y9Authorization y9Authorization,
        Boolean inherit) {
        y9PersonToResourceAndAuthorityManager.saveOrUpdate(y9ResourceBase, person, y9Authorization, inherit);
    }

    @Transactional(readOnly = false)
    @Override
    public void update(String resourceId, String resourceName, String systemName, String systemCnName,
        String description) {
        List<Y9PersonToResourceAndAuthority> list =
            y9PersonToResourceAndAuthorityRepository.findByResourceId(resourceId);
        List<String> ids = list.stream().map(Y9PersonToResourceAndAuthority::getId).collect(Collectors.toList());
        for (String id : ids) {
            Optional<Y9PersonToResourceAndAuthority> y9PersonToResourceAndAuthorityOptional =
                y9PersonToResourceAndAuthorityRepository.findById(id);
            if (y9PersonToResourceAndAuthorityOptional.isPresent()) {
                Y9PersonToResourceAndAuthority personResourceMapping = y9PersonToResourceAndAuthorityOptional.get();
                personResourceMapping.setResourceName(resourceName);
                personResourceMapping.setSystemCnName(systemCnName);
                personResourceMapping.setSystemName(systemName);
                personResourceMapping.setResourceDescription(description);
                y9PersonToResourceAndAuthorityRepository.save(personResourceMapping);
            }
        }
    }

    @Override
    public List<Y9ResourceBase> listSubResources(String personId, String resourceId, AuthorityEnum authority) {
        List<Y9ResourceBase> returnResourceList = new ArrayList<>();
        List<Y9PersonToResourceAndAuthority> y9PersonToResourceAndAuthorityList =
            this.list(personId, resourceId, authority);

        for (Y9PersonToResourceAndAuthority personResource : y9PersonToResourceAndAuthorityList) {
            Y9ResourceBase y9ResourceBase = compositeResourceService
                .findByIdAndResourceType(personResource.getResourceId(), personResource.getResourceType());
            if (y9ResourceBase != null && !returnResourceList.contains(y9ResourceBase)) {
                returnResourceList.add(y9ResourceBase);
            }
        }
        return returnResourceList;
    }

    @Override
    public List<Y9Menu> listSubMenus(String personId, String resourceId, AuthorityEnum authority) {
        List<Y9PersonToResourceAndAuthority> y9PersonToResourceAndAuthorityList =
            this.list(personId, resourceId, ResourceTypeEnum.MENU, authority);
        List<String> menuIdList = y9PersonToResourceAndAuthorityList.stream()
            .map(Y9IdentityToResourceAndAuthorityBase::getResourceId).distinct().collect(Collectors.toList());
        List<Y9Menu> y9MenuList = new ArrayList<>();
        for (String menuId : menuIdList) {
            Y9Menu y9Menu = y9MenuManager.getById(menuId);
            y9MenuList.add(y9Menu);
        }
        return y9MenuList;
    }

}
