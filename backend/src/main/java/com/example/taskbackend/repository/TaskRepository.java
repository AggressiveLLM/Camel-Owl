package com.example.taskbackend.repository;

import com.example.taskbackend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository 层（DAO 层）
 *
 * 使用 Spring Data JPA 的 JpaRepository 实现 MySQL 持久化操作
 * 自动提供常用方法：
 *   - findAll()      查询所有任务
 *   - findById(id)   按 ID 查询
 *   - save(task)     保存/更新任务
 *   - deleteById(id) 按 ID 删除
 *
 * 自定义方法：
 *   - findByTitle(String title)           精确查询
 *   - findByTitleContaining(String keyword) 模糊查询
 *   - findByIdGreaterThan(Long id)       查询 ID 大于某值
 *   - findFirstByOrderByIdDesc()         查询最新任务
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 按标题精确查询任务
     */
    List<Task> findByTitle(String title);

    /**
     * 按标题模糊查询任务
     */
    List<Task> findByTitleContaining(String keyword);

    /**
     * 查询 ID 大于指定值的任务
     */
    List<Task> findByIdGreaterThan(Long id);

    /**
     * 查询最新插入的任务（ID 最大）
     */
    Task findFirstByOrderByIdDesc();


}
