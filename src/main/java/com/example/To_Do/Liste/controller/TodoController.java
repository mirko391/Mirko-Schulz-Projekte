package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.CustomUserDetails;
import com.example.To_Do.Liste.model.Lernplan;
import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Projekt;
import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.LernplanRepository;
import com.example.To_Do.Liste.repository.ProjektRepository;
import com.example.To_Do.Liste.repository.TodoRepository;
import com.example.To_Do.Liste.repository.UserRepository;
import com.example.To_Do.Liste.service.TodoService;
import com.example.To_Do.Liste.repository.uebersichtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LernplanRepository lernplanRepository;

    @Autowired
    private ProjektRepository projektRepository;

    @Autowired
    private uebersichtRepository uebersichtRepository;


    @GetMapping
    public List<Map<String, Object>> getTodos(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long personId = userDetails.getPersonid();

        List<Todo> todos = todoRepository.findByPersonid(personId);

        List<Map<String, Object>> events = new ArrayList<>();
        for (Todo todo : todos) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", todo.getTodoId());  // Die ID des Todos
            event.put("title", todo.getTitel());  // Der Titel des Todos (was auf dem Kalender angezeigt wird)
            event.put("start", todo.getStart().toString());  // Das Startdatum
            event.put("beschreibung", todo.getBeschreibung());
            event.put("end", todo.getEnde() != null ? todo.getEnde().toString() : todo.getStart().toString());  // Das Enddatum (oder Startdatum falls Enddatum nicht gesetzt)
            events.add(event);
        }
        return events;
    }

    @GetMapping("/userdata/projects-and-plans")
    public ResponseEntity<Map<String, Object>> getProjectsAndPlans(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long personId = userDetails.getPersonid();

        List<Projekt> userProjects = projektRepository.findByOwnerid(personId);
        List<Lernplan> userLernplaene = lernplanRepository.findByPersonid(personId);

        // Projekte und Lernpläne in eine Map übertragen
        Map<String, Object> response = new HashMap<>();
        response.put("projekte", userProjects.stream().map(p -> Map.of("projektId", p.getProjektId(), "titel", p.getTitel())).collect(Collectors.toList()));
        response.put("lernplaene", userLernplaene.stream().map(l -> Map.of("lernplanId", l.getLernplanId(), "titel", l.getTitel())).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTodo(@RequestBody Map<String, String> requestData) {
        String titel = requestData.get("titel");
        titel = Character.toUpperCase(titel.charAt(0)) + titel.substring(1);
        Long personId = Long.parseLong(requestData.get("person"));
        LocalDate startDate = LocalDate.parse(requestData.get("start"));

        LocalDate endDate = null;
        if (requestData.containsKey("ende") && !requestData.get("ende").isEmpty()) {
            endDate = LocalDate.parse(requestData.get("ende"));
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Startdatum darf nicht nach dem Enddatum liegen"));
            }
        }

        Long projektId = requestData.containsKey("projektid") && !requestData.get("projektid").isEmpty() ? Long.parseLong(requestData.get("projektid")) : null;
        Long lernplanId = requestData.containsKey("lernplanid") && !requestData.get("lernplanid").isEmpty() ? Long.parseLong(requestData.get("lernplanid")) : null;

        Todo todo = new Todo();
        todo.setTitel(titel);
        todo.setStart(startDate);
        todo.setEnde(endDate);
        todo.setProjektId(projektId);
        todo.setLehrplanId(lernplanId);

        Optional<Person> person = userRepository.findById(personId);
        if (person.isPresent()) {
            todo.setPersonid(person.get().getPersonid());
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Person not found"));
        }

        if (requestData.containsKey("status") && requestData.get("status") != null) {
            todo.setStatus(requestData.get("status"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Status not provided"));
        }

        todoRepository.save(todo);

        Map<String, Object> response = new HashMap<>();
        response.put("titel", todo.getTitel());
        response.put("start", todo.getStart().toString());
        response.put("ende", todo.getEnde() != null ? todo.getEnde().toString() : null);
        response.put("todoid", todo.getTodoId());
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        try {
            todoService.deleteTodo(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTodoDates(@PathVariable Long id, @RequestBody Map<String, String> requestData) {
        Optional<Todo> optionalTodo = todoRepository.findById(id);

        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();

            // Setze das neue Startdatum
            if (requestData.containsKey("start")) {
                todo.setStart(LocalDate.parse(requestData.get("start")));
            }

            // Setze das neue Enddatum; prüfe auf "null"-Wert
            if (requestData.containsKey("ende") && requestData.get("ende") != null) {
                todo.setEnde(LocalDate.parse(requestData.get("ende")));
            } else {
                todo.setEnde(null);  // Setze Enddatum auf null
            }

            todoRepository.save(todo);
            return ResponseEntity.ok(Map.of("message", "Todo erfolgreich aktualisiert"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo nicht gefunden");
        }
    }

    @GetMapping("/api/todos")
    @ResponseBody
    public List<Map<String, Object>> getTodosForCalendar(Authentication authentication) {
        String username = authentication.getName();
        Person user = userRepository.findByUsernameIgnoreCase(username);

        if (user == null) {
            throw new UsernameNotFoundException("Benutzer nicht gefunden: " + username);
        }

        Long userId = user.getPersonid();
        List<Todo> todos = uebersichtRepository.findAllByPersonid(userId);

        return todos.stream().map(todo -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", todo.getTodoId());
            event.put("title", todo.getTitel());
            event.put("start", todo.getStart().toString()); // ISO-Format
            event.put("end", todo.getEnde() != null ? todo.getEnde().toString() : null);
            event.put("beschreibung", todo.getBeschreibung());
            event.put("status", todo.getStatus());
            return event;
        }).collect(Collectors.toList());
    }
}