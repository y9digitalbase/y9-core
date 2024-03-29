package net.risesoft.enums.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.risesoft.enums.ValuedEnum;

/**
 * 操作按钮展示类型
 *
 * @author shidaobang
 * @date 2022/09/19
 */
@Getter
@AllArgsConstructor
public enum OperationDisplayTypeEnum implements ValuedEnum<Integer> {
    /** 图标文本都展示 */
    ICON_TEXT(0),
    /** 只展示图标 */
    ICON(1),
    /** 只展示文本 */
    TEXT(2);

    private final Integer value;
}
