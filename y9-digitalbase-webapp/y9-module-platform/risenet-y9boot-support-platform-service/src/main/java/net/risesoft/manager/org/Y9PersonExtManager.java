package net.risesoft.manager.org;

import java.util.List;
import java.util.Optional;

import net.risesoft.entity.Y9Person;
import net.risesoft.entity.Y9PersonExt;

/**
 * 人员扩展管理类
 *
 * @author shidaobang
 * @date 2022/10/19
 */
public interface Y9PersonExtManager {

    Optional<Y9PersonExt> findByPersonId(String personId);

    /**
     * 根据idType和idNum查询
     *
     * @param idType 证件类型
     * @param idNum 证件号
     * @return {@link List}<{@link Y9PersonExt}>
     */
    List<Y9PersonExt> listByIdTypeAndIdNum(String idType, String idNum);

    Y9PersonExt saveOrUpdate(Y9PersonExt personExt, Y9Person person);
}
