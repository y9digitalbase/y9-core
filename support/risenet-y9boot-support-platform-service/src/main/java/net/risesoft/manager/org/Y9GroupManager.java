package net.risesoft.manager.org;

import java.util.Optional;

import net.risesoft.entity.Y9Group;
import net.risesoft.y9.exception.Y9NotFoundException;

public interface Y9GroupManager {

    /**
     * 根据主键id获取用户组实例
     *
     * @param id 唯一标识
     * @return 用户组对象
     * @throws Y9NotFoundException id 对应的记录不存在的情况
     */
    Y9Group getById(String id);

    Optional<Y9Group> findById(String id);

    Y9Group save(Y9Group y9Group);

    void delete(Y9Group y9Group);

    Y9Group updateTabIndex(String id, int tabIndex);
}
