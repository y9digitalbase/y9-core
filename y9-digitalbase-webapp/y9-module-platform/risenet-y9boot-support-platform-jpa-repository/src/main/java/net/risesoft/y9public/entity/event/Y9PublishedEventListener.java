package net.risesoft.y9public.entity.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.base.BaseEntity;

/**
 * 事件监听信息表
 * 
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
@Entity
@Table(name = "Y9_PUBLISHED_EVENT_LISTENER")
@DynamicUpdate
@org.hibernate.annotations.Table(comment = "事件监听信息表", appliesTo = "Y9_PUBLISHED_EVENT_LISTENER")
@NoArgsConstructor
@Data
public class Y9PublishedEventListener extends BaseEntity {

    private static final long serialVersionUID = 6579436882467488027L;

    /** 主键 */
    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment("主键")
    private String id;

    /** 租户id */
    @Column(name = "TENANT_ID", length = 38, nullable = true)
    @Comment("租户id")
    private String tenantId;

    /** 事件类型 */
    @Column(name = "EVENT_TYPE", length = 255, nullable = false)
    @Comment("事件类型")
    private String eventType;

    /** 事件消费者ip */
    @Column(name = "CLIENT_IP", nullable = false)
    @Comment("事件消费者ip")
    private String clientIp;

    /** 应用名称 */
    @Column(name = "SYSTEM_NAME", nullable = false)
    @Comment("应用名称")
    private String systemName;

}
