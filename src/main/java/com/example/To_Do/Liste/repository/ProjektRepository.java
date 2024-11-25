package com.example.To_Do.Liste.repository;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Projekt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjektRepository extends JpaRepository<Projekt, Long> {

    // Projekte basierend auf der OwnerID abfragen
    List<Projekt> findByOwnerid(Long ownerid);
    List<Projekt> findByPersonenPersonid(Long personid);


}