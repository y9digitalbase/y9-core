package net.risesoft.api.org.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import net.risesoft.model.Person;
import net.risesoft.model.Position;

/**
 * @author shidaobang
 * @date 2023/11/06
 * @since 9.6.3
 */
@Getter
@Setter
public class PersonInfoDTO implements Serializable {

    private Person person;

    private List<Position> positionList;

}
