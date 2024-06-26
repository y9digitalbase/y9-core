package net.risesoft.y9.configuration.feature.log;

import lombok.Getter;
import lombok.Setter;

/**
 * 日志配置
 *
 * @author shidaobang
 * @date 2022/09/30
 */
@Getter
@Setter
public class Y9LogProperties {

    /**
     * 是否启用
     */
    private boolean enabled;

    /** 日志信息保存方式 ：kafka,api */
    private String logSaveTarget = "kafka";

}
