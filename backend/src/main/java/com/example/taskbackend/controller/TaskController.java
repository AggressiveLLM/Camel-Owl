package com.example.taskbackend.controller;

import com.example.taskbackend.model.Task;
import com.example.taskbackend.service.TaskService;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.taskbackend.repository.TaskRepository;

import java.util.List;

/**
 * Controller 层：负责 HTTP 请求映射（/api/tasks）
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {



    private final TaskService service;    // service 层定义为service

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Task>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task t) {
        Task created = service.create(t);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }
}




