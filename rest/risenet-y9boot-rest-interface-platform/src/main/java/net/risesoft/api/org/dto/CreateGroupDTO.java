package net.risesoft.api.org.dto;

import lombok.Getter;
import lombok.Setter;

import net.risesoft.enums.GroupTypeEnum;

/**
 * @author shidaobang
 * @date 2023/11/06
 * @since 9.6.3
 */
@Getter
@Setter
public class CreateGroupDTO extends CreateOrgUnitBaseDTO {

    private static final long serialVersionUID = 2284204062603906615L;

    /** 父节点id */
    private String parentId;

    /**
     * 岗位组或者用户组
     *
     * {@link net.risesoft.enums.GroupTypeEnum}
     */
    private String type = GroupTypeEnum.PERSON.getName();

}
