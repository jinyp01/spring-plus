package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoCustomRepositoryImpl implements TodoCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(
        String title,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        String managerNickname,
        Pageable pageable
    ) {
        BooleanBuilder conditions = searchConditions(title, createdFrom, createdTo, managerNickname);

        List<TodoSearchResponse> contents = queryFactory
            .select(Projections.constructor(
                TodoSearchResponse.class,
                todo.title,
                JPAExpressions
                    .select(manager.id.count())
                    .from(manager)
                    .where(manager.todo.eq(todo)),
                JPAExpressions
                    .select(comment.id.count())
                    .from(comment)
                    .where(comment.todo.eq(todo))
            ))
            .from(todo)
            .where(conditions)
            .orderBy(todo.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(todo.id.count())
            .from(todo)
            .where(conditions)
            .fetchOne();

        return new PageImpl<>(contents, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder searchConditions(
        String title,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        String managerNickname
    ) {
        BooleanBuilder conditions = new BooleanBuilder();

        if (StringUtils.hasText(title)) {
            conditions.and(todo.title.containsIgnoreCase(title));
        }

        if (createdFrom != null) {
            conditions.and(todo.createdAt.goe(createdFrom));
        }

        if (createdTo != null) {
            conditions.and(todo.createdAt.loe(createdTo));
        }

        if (StringUtils.hasText(managerNickname)) {

            conditions.and(JPAExpressions
                .selectOne()
                .from(manager)
                .join(manager.user, user)
                .where(
                    manager.todo.eq(todo),
                    user.username.containsIgnoreCase(managerNickname)
                )
                .exists());
        }

        return conditions;
    }

}
