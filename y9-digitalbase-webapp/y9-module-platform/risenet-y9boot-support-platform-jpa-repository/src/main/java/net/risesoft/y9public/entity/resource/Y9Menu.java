package net.risesoft.y9public.entity.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import net.risesoft.enums.platform.ResourceTypeEnum;

/**
 * 应用的菜单表
 * 
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Entity
@Table(name = "Y9_COMMON_MENU")
@DynamicUpdate
@org.hibernate.annotations.Table(comment = "应用的菜单表", appliesTo = "Y9_COMMON_MENU")
@Data
@SuperBuilder
public class Y9Menu extends Y9ResourceBase {

    private static final long serialVersionUID = 7952871346132443097L;

    /** 应用id */
    @Column(name = "APP_ID", length = 38, nullable = false)
    @Comment("应用id")
    private String appId;

    /** 父节点ID */
    @Comment("父节点ID")
    @Column(name = "PARENT_ID", length = 38, nullable = false)
    private String parentId;

    /** 打开模式 */
    @Column(name = "TARGET")
    @Comment("打开模式")
    private String target;

    /** 菜单部件 */
    @Column(name = "COMPONENT", length = 50)
    @Comment("菜单部件")
    private String component;

    /** 重定向 */
    @Column(name = "REDIRECT", length = 50)
    @Comment("重定向")
    private String redirect;

    /** 元信息 */
    @Column(name = "META", length = 500)
    @Comment("元信息")
    private String meta;

    public Y9Menu() {
        super.setResourceType(ResourceTypeEnum.MENU);
    }

    @Override
    public String getAppId() {
        return this.appId;
    }
}
