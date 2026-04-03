package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long todoId;
    private Long requestUserId;
    private Long targetUserId;
    private String message;
    private LocalDateTime createdAt;

    public Log(Long todoId, Long requestUserId, Long targetUserId, String message) {
        this.todoId = todoId;
        this.requestUserId = requestUserId;
        this.targetUserId = targetUserId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}