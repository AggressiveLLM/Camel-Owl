package com.example.taskbackend.service;

import com.example.taskbackend.model.Task;
import com.example.taskbackend.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service 层：封装业务逻辑，调用 JPA Repository（MySQL 持久化）
 */
@Service
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    /**
     * 查询所有任务
     */
    public List<Task> findAll() {
        return repo.findAll(); // 直接调用 JPA 的 findAll()
    }

    /**
     * 保存新任务
     */
    public Task create(Task t) {
        return repo.save(t); // JPA 会自动生成 INSERT
    }

    /**
     * 按标题查询任务（依赖 TaskRepository 的方法定义）
     */
    public List<Task> findByTitle(String title) {
        return repo.findByTitle(title); // Spring Data JPA 自动解析方法名生成 SQL
    }

    public void deleteById(Long id){
        repo.deleteById(id);
    }
}
