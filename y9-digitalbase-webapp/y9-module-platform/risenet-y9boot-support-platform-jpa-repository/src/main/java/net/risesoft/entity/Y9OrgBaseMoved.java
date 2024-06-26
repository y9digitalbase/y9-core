package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.persistence.EnumConverter;

/**
 * 移动的组织表
 * 
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Entity
@Table(name = "Y9_ORGBASE_MOVED")
@DynamicUpdate
@org.hibernate.annotations.Table(comment = "移动的组织表", appliesTo = "Y9_ORGBASE_MOVED")
@NoArgsConstructor
@Data
public class Y9OrgBaseMoved extends BaseEntity {

    private static final long serialVersionUID = -5165264734456244849L;

    /** 主键 */
    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment("主键")
    protected String id;

    /** 组织id */
    @Column(name = "ORG_ID", length = 38)
    @Comment("组织id")
    protected String orgId;

    /** 组织类型 */
    @Column(name = "ORG_TYPE", length = 255, nullable = false)
    @Comment("组织类型")
    @Convert(converter = EnumConverter.OrgTypeEnumConverter.class)
    protected OrgTypeEnum orgType;

    /** 操作者 */
    @Column(name = "OPERATOR", length = 30)
    @Comment("操作者")
    protected String operator;

    /** 原来的父节点id */
    @Column(name = "PARENT_ID_FROM", length = 38)
    @Comment("原来的父节点Id")
    protected String parentIdFrom;

    /** 目标父节点id */
    @Column(name = "PARENT_ID_TO", length = 38)
    @Comment("目标父节点Id")
    protected String parentIdTo;

    /** 工作交接是否完成 */
    @Type(type = "numeric_boolean")
    @Column(name = "FINISHED", nullable = false)
    @Comment("工作交接是否完成")
    @ColumnDefault("0")
    protected Boolean finished = false;

}