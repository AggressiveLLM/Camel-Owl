package com.example.taskbackend.model;

import jakarta.persistence.*;  // JPA 注解
import java.io.Serializable;

@Entity                      // 声明这是一个 JPA 实体类（映射到数据库表）
@Table(name = "taskdb")       // 指定表名为 taskdb
public class Task implements Serializable {

    @Id                     // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;         // 自增 ID

    @Column(nullable = false, length = 255)
    private String title;    // 任务标题

    // === 构造方法 ===
    public Task() {}

    public Task(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    // === Getter / Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
