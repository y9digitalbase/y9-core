package net.risesoft.api.v0.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.identity.Y9IdentityToResourceAndAuthorityBase;
import net.risesoft.entity.identity.person.Y9PersonToResourceAndAuthority;
import net.risesoft.enums.platform.AuthorityEnum;
import net.risesoft.enums.platform.ResourceTypeEnum;
import net.risesoft.model.platform.VueButton;
import net.risesoft.model.platform.VueMenu;
import net.risesoft.service.identity.Y9PersonToResourceAndAuthorityService;
import net.risesoft.y9public.entity.resource.Y9Menu;
import net.risesoft.y9public.entity.resource.Y9Operation;
import net.risesoft.y9public.service.resource.Y9MenuService;
import net.risesoft.y9public.service.resource.Y9OperationService;

/**
 * 根据人员权限列表构建 vue 菜单组件
 * 
 * @author shidaobang
 * @date 2023/07/11
 * @since 9.6.2
 */
@Component(value = "v0VueMenuBuilder")
@RequiredArgsConstructor
@Deprecated
public class VueMenuBuilder {

    private final Y9MenuService y9MenuService;
    private final Y9OperationService y9OperationService;
    private final Y9PersonToResourceAndAuthorityService y9PersonToResourceAndAuthorityService;

    private VueButton buildVueButton(Y9Operation y9Operation) {
        VueButton button = new VueButton();
        button.setName(y9Operation.getName());
        button.setIcon(y9Operation.getIconUrl());
        button.setButtonId(y9Operation.getCustomId());
        button.setDisplayType(y9Operation.getDisplayType());
        button.setUrl(y9Operation.getUrl());
        button.setEventName(y9Operation.getEventName());
        return button;
    }

    private List<VueButton> buildVueButtons(String personId, AuthorityEnum authority, String menuId) {
        List<VueButton> buttonList = new ArrayList<>();
        List<Y9PersonToResourceAndAuthority> authorizedButtonList =
            y9PersonToResourceAndAuthorityService.list(personId, menuId, ResourceTypeEnum.OPERATION, authority);
        List<Y9Operation> y9OperationList =
            authorizedButtonList.stream().map(Y9IdentityToResourceAndAuthorityBase::getResourceId).distinct()
                .map(y9OperationService::getById).sorted().collect(Collectors.toList());
        for (Y9Operation y9Operation : y9OperationList) {
            if (y9Operation.getEnabled()) {
                buttonList.add(buildVueButton(y9Operation));
            }
        }
        return buttonList;
    }

    private VueMenu buildVueMenu(String personId, AuthorityEnum authority, String menuId, Y9Menu y9Menu) {
        VueMenu vueMenu = new VueMenu();
        vueMenu.setName(y9Menu.getName());
        vueMenu.setPath(y9Menu.getUrl());
        vueMenu.setRedirect(y9Menu.getRedirect());
        vueMenu.setComponent(y9Menu.getComponent());
        vueMenu.setMeta(y9Menu.getMeta());
        vueMenu.setTarget(y9Menu.getTarget());

        List<VueMenu> subVueMenuList = new ArrayList<>();
        buildVueMenus(personId, authority, menuId, subVueMenuList);
        vueMenu.setChildren(subVueMenuList);

        List<VueButton> buttonList = buildVueButtons(personId, authority, menuId);
        vueMenu.setButtons(buttonList);
        return vueMenu;
    }

    public void buildVueMenus(String personId, AuthorityEnum authority, String resourceId, List<VueMenu> vueMenuList) {
        List<Y9PersonToResourceAndAuthority> authorizedMenuList =
            y9PersonToResourceAndAuthorityService.list(personId, resourceId, ResourceTypeEnum.MENU, authority);
        List<Y9Menu> menuList = authorizedMenuList.stream().map(Y9IdentityToResourceAndAuthorityBase::getResourceId)
            .distinct().map(y9MenuService::getById).sorted().collect(Collectors.toList());
        for (Y9Menu y9Menu : menuList) {
            if (y9Menu.getEnabled()) {
                VueMenu vueMenu = buildVueMenu(personId, authority, y9Menu.getId(), y9Menu);
                vueMenuList.add(vueMenu);
            }
        }
    }
}
