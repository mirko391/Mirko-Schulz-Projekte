package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.CustomUserDetails;
import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.DashboardRepository;
import com.example.To_Do.Liste.repository.PersonRepository;
import com.example.To_Do.Liste.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final TodoRepository todoRepository;
    private final DashboardRepository dashboardRepository;
    private final PersonRepository personRepository;

    @Autowired
    public DashboardController(TodoRepository todoRepository, DashboardRepository dashboardRepository, PersonRepository personRepository) {
        this.todoRepository = todoRepository;
        this.dashboardRepository = dashboardRepository;
        this.personRepository = personRepository;
    }

    @GetMapping("/dashboard")
    public String getTodosForTodayAndWeek(Authentication authentication, Model model) {

        // `CustomUserDetails` aus `authentication.getPrincipal()` beziehen
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long personid = userDetails.getPerson().getPersonid();  // `getPerson()` Methode von `CustomUserDetails`

        // To-Dos des eingeloggten Nutzers abrufen
        List<Todo> todos = todoRepository.findByPersonid(personid);
        List<Todo> todayTodos = new ArrayList<>();
        Map<String, List<Todo>> weeklyTodosMap = new HashMap<>();

        // Initialisiere die Tage der Woche
        for (DayOfWeek day : DayOfWeek.values()) {
            weeklyTodosMap.put(day.toString(), new ArrayList<>());
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        // To-Dos für heute und die Woche filtern
        for (Todo todo : todos) {
            LocalDate startDate = todo.getStart();
            LocalDate endDate = todo.getEnde();

            // To-Dos für "Heute" hinzufügen, wenn das heutige Datum entweder das Start- oder Enddatum ist
            if ((startDate != null && startDate.isEqual(today)) || (endDate != null && endDate.isEqual(today))) {
                todayTodos.add(todo);
            }

            // To-Dos für die Woche hinzufügen
            if (startDate != null && !startDate.isBefore(startOfWeek) && !startDate.isAfter(endOfWeek)) {
                String dayOfWeek = startDate.getDayOfWeek().toString();
                weeklyTodosMap.get(dayOfWeek).add(todo);
            }
        }

        // Formatieren des Startdatums für jedes To-Do
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        for (Todo todo : todayTodos) {
            if (todo.getStart() != null) {
                String formattedDate = todo.getStart().format(formatter);
                todo.setFormattedStartDate(formattedDate); // Füge das formatierte Datum als neues Attribut hinzu
            }
        }

        // Gebe die To-Dos und die wöchentliche Todo-Liste an Thymeleaf weiter
        model.addAttribute("todayTodos", todayTodos);
        model.addAttribute("todosMap", weeklyTodosMap);
        return "dashboard"; // Dein Thymeleaf-Template
    }

}
