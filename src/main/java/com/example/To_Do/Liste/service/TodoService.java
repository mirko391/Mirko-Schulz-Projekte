
package com.example.To_Do.Liste.service;

import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.TodoRepository;
import com.example.To_Do.Liste.repository.uebersichtRepository; // Achte auf die Großschreibung
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public void fixTitles() {
        List<Todo> todos = todoRepository.findAll();
        for (Todo todo : todos) {
            String titel = todo.getTitel();
            if (Character.isLowerCase(titel.charAt(0))) {
                // Großbuchstaben anwenden
                todo.setTitel(Character.toUpperCase(titel.charAt(0)) + titel.substring(1));
                todoRepository.save(todo); // Speichert die Änderung in der DB
            }
        }
    }

    @Autowired
    private uebersichtRepository uebersichtRepository; // Richtig importieren

    // CREATE
    public Todo createTodo(Todo todo) {
        return uebersichtRepository.save(todo);
    }

    public void createAndSaveTodo(Todo todo) {
        todoRepository.save(todo); // Hier wird das Todo gespeichert
    }

    // READ
    public Todo getTodoById(Long id) {
        return uebersichtRepository.findById(id).orElse(null);
    }

    public List<Todo> getAllTodos() {
        return uebersichtRepository.findAll();
    }

    // UPDATE
    public Todo updateTodo(Long id, Todo todoDetails) {
        Todo todo = getTodoById(id);
        if (todo != null) {
            todo.setTitel(todoDetails.getTitel());
            todo.setBeschreibung(todoDetails.getBeschreibung());
            todo.setEnde(todoDetails.getEnde());
            todo.setStatus(todoDetails.getStatus());
            todo.setLehrplanId(todoDetails.getLehrplanId());
            todo.setProjektId(todoDetails.getProjektId());
            return uebersichtRepository.save(todo);
        }
        return null; // Hier könnte man eine Exception werfen, falls Todo nicht gefunden wird
    }

    // DELETE
    public void deleteTodo(Long id) {
        if (uebersichtRepository.existsById(id)) {
            uebersichtRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Todo mit ID " + id + " existiert nicht.");
        }
    }
}