package com.example.To_Do.Liste.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "projekt")
public class Projekt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projektid")
    private Long projektId;

    @Column(name = "ownerid")
    private Long ownerid;

    @Column(name = "titel", nullable = false)
    private String titel;

    @Column(name = "beschreibung", nullable = false)
    private String beschreibung;

    @OneToMany(mappedBy = "projektId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Todo> todos;

    @ManyToMany
    @JoinTable(name = "projekt_person", joinColumns = @JoinColumn(name = "projektid"), inverseJoinColumns = @JoinColumn(name = "personid"))
    private List<Person> personen;

    /**
     * Entfernt eine Person aus der Teilnehmerliste dieses Projekts, falls vorhanden.
     *
     * @param person die zu entfernende Person
     * @return true, falls die Person erfolgreich entfernt wurde; false, wenn die Person nicht gefunden wurde
     */
    public boolean removeParticipant(Person person) {
        return personen.remove(person);
    }
}