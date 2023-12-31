package net.risesoft.controller.authorization.vo;

import lombok.Data;

import net.risesoft.enums.platform.AuthorityEnum;

/**
 * 权限类型
 *
 * @author shidaobang
 * @date 2022/3/17
 */
@Data
public class AuthorityVO {

    /** 权限名称 */
    private String name;
    /** 权限码 */
    private Integer code;

    public AuthorityVO(AuthorityEnum typeEnum) {
        this.name = typeEnum.getName();
        this.code = typeEnum.getValue();
    }
}
