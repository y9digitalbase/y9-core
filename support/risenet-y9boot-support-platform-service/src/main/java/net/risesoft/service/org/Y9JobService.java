package net.risesoft.service.org;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import net.risesoft.entity.Y9Job;

/**
 * @author sdb
 * @author ls
 * @date 2022/9/22
 */
public interface Y9JobService {

    /**
     * 计数
     *
     * @return long
     */
    long count();

    /**
     * 根据id数组删除
     *
     * @param ids id数组
     */
    void delete(List<String> ids);

    /**
     * 根据id删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id获取职位
     *
     * @param id id
     * @return {@link Y9Job}
     */
    Optional<Y9Job> findById(String id);

    /**
     * 根据id获取职位
     *
     * @param id id
     * @return {@link Y9Job}
     */
    Y9Job getById(String id);

    /**
     * 获取所有职位
     *
     * @return {@link List}<{@link Y9Job}>
     */
    List<Y9Job> listAll();

    /**
     * 根据职位名获取职位
     *
     * @param name 名字
     * @return {@link List}<{@link Y9Job}>
     */
    List<Y9Job> listByName(String name);

    /**
     * 按照tabindexs的顺序重新排序职位列表
     *
     * @param jobIds 岗位id
     * @return {@link List}<{@link Y9Job}>
     */
    List<Y9Job> order(List<String> jobIds);

    /**
     * 分页查询职位
     *
     * @param page 页数
     * @param limit 每页的行数
     * @return {@link Page}<{@link Y9Job}>
     */
    Page<Y9Job> page(Integer page, Integer limit);

    /**
     * 保存或更新职位
     *
     * @param job 职位
     * @return {@link Y9Job}
     */
    Y9Job saveOrUpdate(Y9Job job);

    Y9Job create(String name, String code);
}
