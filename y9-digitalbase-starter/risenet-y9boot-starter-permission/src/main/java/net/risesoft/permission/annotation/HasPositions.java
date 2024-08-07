package net.risesoft.permission.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.risesoft.enums.LogicalEnum;

/**
 * 是否拥有相应的岗位 只有拥有相应权限，方法才能继续调用
 * 
 * @author shidaobang
 * @date 2022/11/10
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasPositions {

    /**
     * 岗位 id 数组
     * 
     * @return {@code String[] } 岗位Id数组
     */
    String[] value();

    /**
     * 检查岗位的逻辑操作 与 和 或，默认是 与
     * 
     * @return {@code LogicalEnum } 岗位的逻辑操作
     */
    LogicalEnum logical() default LogicalEnum.AND;
}
