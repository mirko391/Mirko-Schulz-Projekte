package com.example.To_Do.Liste.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "todo")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todoid")
    private Long todoId; // ID des Todos

    @Column(name = "personid", nullable = false)
    private Long personid;

    @ManyToOne
    @JoinColumn(name = "personid", insertable = false, updatable = false)
    private Person person;

    @Column(name = "lernplanid")
    private Long lehrplanId;

    @Column(name = "start", nullable = true)
    private LocalDate start;

    @Column(name = "ende", nullable = true)
    private LocalDate ende; // Fälligkeitsdatum des Todos

    @Column(name = "projektid", nullable = true)
    private Long projektId;

    @Column(nullable = false)
    private String titel; //    Titel des Todos

    @Column(nullable = true) // Beschreibung kann optional sein
    private String beschreibung;

    @Column(nullable = false)
    private String status; // Status des Todos

    @Transient
    private Long ActiveDaysCount;

    @Transient
    private List<String> activeDays;

    @Transient
    private String formattedStartDate;

    // Neue Methode für Großschreibung
    @PrePersist
    @PreUpdate
    public void capitalizeTitle() {
        // Großschreibung des ersten Buchstabens
        titel = Character.toUpperCase(titel.charAt(0)) + titel.substring(1);
    }

    @Transient
    private String personName;

    @PostLoad
    public void setPersonNameFromPerson() {
        if (this.person != null) {
            this.personName = this.person.getUsername();
        }
    }

    public String getFormattedStartDate() {
        return formattedStartDate;
    }

    public void setFormattedStartDate(String formattedStartDate) {
        this.formattedStartDate = formattedStartDate;
    }
}

