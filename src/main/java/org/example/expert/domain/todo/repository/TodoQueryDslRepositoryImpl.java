package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TodoQueryDslRepositoryImpl implements TodoQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo todo = queryFactory
                .selectFrom(QTodo.todo)
                .leftJoin(QTodo.todo.user, QUser.user).fetchJoin()
                .where(QTodo.todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(todo);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String nickname,
            Pageable pageable
    ) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;

        List<TodoSearchResponse> results = queryFactory
                .select(new QTodoSearchResponse(
                        todo.title,
                        manager.countDistinct(),
                        comment.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(
                        title != null ? todo.title.containsIgnoreCase(title) : null,
                        startDate != null ? todo.createdAt.goe(startDate) : null,
                        endDate != null ? todo.createdAt.loe(endDate) : null,
                        nickname != null ? user.nickname.containsIgnoreCase(nickname) : null
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(
                        title != null ? todo.title.containsIgnoreCase(title) : null,
                        startDate != null ? todo.createdAt.goe(startDate) : null,
                        endDate != null ? todo.createdAt.loe(endDate) : null,
                        nickname != null ? user.nickname.containsIgnoreCase(nickname) : null
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }
}