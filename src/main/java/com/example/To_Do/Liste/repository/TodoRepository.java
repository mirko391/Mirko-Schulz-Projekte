package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByPersonid(Long personid);
    List<Todo> findByProjektId(Long projektId);

    @Query("SELECT t FROM Todo t WHERE t.personid = :personid ORDER BY t.titel")
    List<Todo> findAllByPersonId(@Param("personid") Long personid);

    @Query("SELECT t, p.name FROM Todo t LEFT JOIN Person p ON t.personid = p.personid WHERE t.projektId = :projektId")
    List<Object[]> findTodosWithPersonNameByProjektId(@Param("projektId") Long projektId);

}

