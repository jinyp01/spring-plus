package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("""
    SELECT t
    FROM Todo t
    WHERE (:weather IS NULL OR t.weather = :weather)
      AND (:modifiedFrom IS NULL OR t.modifiedAt >= :modifiedFrom)
      AND (:modifiedTo IS NULL OR t.modifiedAt <= :modifiedTo)
    ORDER BY t.modifiedAt DESC
""")
    Page<Todo> searchTodos(
            @Param("weather") String weather,
            @Param("modifiedFrom") LocalDateTime modifiedFrom,
            @Param("modifiedTo") LocalDateTime modifiedTo,
            Pageable pageable
    );

}
