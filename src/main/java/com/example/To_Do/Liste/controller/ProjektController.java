package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.dto.TodoDto;
import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Projekt;
import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.PersonRepository;
import com.example.To_Do.Liste.repository.ProjektRepository;
import com.example.To_Do.Liste.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/projekt")
public class ProjektController {

    @Autowired
    private ProjektRepository projektRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private PersonRepository personRepository;

    @GetMapping
    public String showProjects(Model model, Principal principal) {
        String username = principal.getName(); // Get the current user's username
        Person person = personRepository.findByUsername(username); // Get the user from the DB

        if (person != null) {
            // Find projects owned by the user and projects where the user is a participant
            List<Projekt> ownedProjects = projektRepository.findByOwnerid(person.getPersonid());
            List<Projekt> participantProjects = projektRepository.findByPersonenPersonid(person.getPersonid());

            model.addAttribute("ownedProjects", ownedProjects);
            model.addAttribute("participantProjects", participantProjects);
        }

        return "projekt";
    }

    @GetMapping("/todos")
    @ResponseBody
    public List<Todo> getTodosByProjektId(@RequestParam Long projektId) {
        List<Todo> todos = todoRepository.findByProjektId(projektId);

        // Für jedes To-do die zugewiesene Person laden
        for (Todo todo : todos) {
            if (todo.getPersonid() != null) {
                Optional<Person> person = personRepository.findById(todo.getPersonid());
                person.ifPresent(value -> todo.setPersonName(value.getUsername())); // Name setzen
            } else {
                todo.setPersonName("Keine Person zugewiesen");
            }
        }
        return todos;
    }


    @PostMapping("/create")
    public String createProject(@RequestParam("titel") String titel, @RequestParam("beschreibung") String beschreibung, Principal principal) {
        // Hole den aktuell angemeldeten Benutzer basierend auf dem Principal
        Person currentUser = personRepository.findByUsername(principal.getName());    // Prüfe, ob der Benutzer existiert, bevor fortgefahren wird
        if (currentUser == null) {        // Handle error (z.B., weiterleiten oder Fehlermeldung anzeigen)
            return "redirect:/error";
        }    // Erstelle ein neues Projekt und setze die Felder

        Projekt projekt = new Projekt();
        projekt.setTitel(titel);
        projekt.setBeschreibung(beschreibung);
        projekt.setOwnerid(currentUser.getPersonid()); // Setze die personid als Besitzer
        projekt.setTodos(new ArrayList<>()); // Initialisiert die To-do-Liste als leere Liste

        // Speichere das Projekt
        projektRepository.save(projekt);    // Nach dem Erstellen des Projekts zur Projektseite zurückkehren
        return "redirect:/projekt";}

        @PostMapping("/addMember")
        public String addMember (@RequestParam Long projektid, @RequestParam String username){
            Optional<Projekt> projektOpt = projektRepository.findById(projektid);
            if (projektOpt.isPresent()) {
                Projekt projekt = projektOpt.get();
                Person person = personRepository.findByUsername(username); // Directly get Person object

                if (person != null) {
                    projekt.getPersonen().add(person); // Add person to the project's members
                    projektRepository.save(projekt); // Save the updated project
                }
            }
            return "redirect:/projekt";
        }

        @PostMapping("/removeMember")
        public String removeMember (@RequestParam Long projektid, @RequestParam Long memberid){
            Optional<Projekt> projektOpt = projektRepository.findById(projektid);
            if (projektOpt.isPresent()) {
                Projekt projekt = projektOpt.get();
                Optional<Person> personOpt = personRepository.findById(memberid);
                if (personOpt.isPresent()) {
                    Person person = personOpt.get();
                    projekt.getPersonen().remove(person); // Entferne Person aus den Projektmitgliedern
                    projektRepository.save(projekt); // Speichere das Projekt
                }
            }
            return "redirect:/projekt";
        }

        @PostMapping("/delete")
        public String deleteProject (@RequestParam Long projektid){
            projektRepository.deleteById(projektid);
            return "redirect:/projekt";
        }

        @PostMapping("/updateTodoPerson")
        public String updateTodoPerson (@RequestParam Long todoId, @RequestParam String personName, RedirectAttributes
        redirectAttributes){
            Optional<Todo> todoOptional = todoRepository.findById(todoId);
            Person person = personRepository.findByUsername(personName); // Direkt die Person abrufen

            if (todoOptional.isPresent() && person != null) {
                Todo todo = todoOptional.get();
                todo.setPersonid(person.getPersonid()); // Die personid des Todos aktualisieren

                todoRepository.save(todo); // Todo speichern
                redirectAttributes.addFlashAttribute("successMessage", "Person für das To-do erfolgreich aktualisiert!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "To-do oder Benutzer nicht gefunden!");
            }

            return "redirect:/projekt"; // Zielseite anpassen
        }

        @GetMapping("/details")
        public String getProjektDetails (@RequestParam("projektid") Long projektId, Model model){
            // Todos mit Personennamen laden
            List<Object[]> rawTodos = todoRepository.findTodosWithPersonNameByProjektId(projektId);

            // Liste aufbereiten
            List<TodoDto> todos = rawTodos.stream().map(row -> {
                Todo todo = (Todo) row[0];
                String personName = (String) row[1];
                return new TodoDto(
                        todo.getTodoId(),
                        todo.getTitel(),
                        todo.getBeschreibung(),
                        todo.getStatus(),
                        personName != null ? personName : "Nicht zugewiesen"
                );
            }).toList();

            model.addAttribute("todos", todos);
            return "projekt-details";
        }

        @PostMapping("/removeParticipant")
        public String removeParticipant (@RequestParam Long projektid, Principal principal, RedirectAttributes
        redirectAttributes){
            // Hole den aktuellen Benutzer basierend auf dem angemeldeten Principal
            String username = principal.getName();
            Person person = personRepository.findByUsername(username);

            if (person != null) {
                Optional<Projekt> projektOpt = projektRepository.findById(projektid);
                if (projektOpt.isPresent()) {
                    Projekt projekt = projektOpt.get();

                    // Entferne den Benutzer aus der Liste der Teilnehmer
                    if (projekt.getPersonen().contains(person)) {
                        projekt.getPersonen().remove(person);
                        projektRepository.save(projekt); // Speichere das aktualisierte Projekt
                        redirectAttributes.addFlashAttribute("successMessage", "Sie wurden erfolgreich aus dem Projekt entfernt.");
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "Sie sind kein Teilnehmer dieses Projekts.");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Projekt nicht gefunden.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Benutzer nicht gefunden.");
            }

            return "redirect:/projekt"; // Zurück zur Projektübersicht
        }

    }
