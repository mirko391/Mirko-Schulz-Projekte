package com.example.To_Do.Liste;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.example.To_Do.Liste.model.Todo;
import com.example.To_Do.Liste.repository.TodoRepository;
import com.example.To_Do.Liste.repository.uebersichtRepository;
import com.example.To_Do.Liste.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
public class TodoServiceTest {

    @Mock
    private uebersichtRepository uebersichtRepository;

    @Mock
    private TodoRepository todoRepository; // Mock des Datenbank-Repositories

    @InjectMocks
    private TodoService todoService; // Service, der getestet wird

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialisiert Mocks vor jedem Test
    }

    @Test
    void testCreateTodoAndSaveToDatabase() {
        // Arrange: Testdaten erstellen
        Todo newTodo = new Todo();
        newTodo.setTodoId(1L);
        newTodo.setTitel("Test To-do");
        newTodo.setBeschreibung("This is a test To-do");
        newTodo.setProjektId(1907L);

        // Simulierte Rückgabe des Repositories beim Speichern
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simuliere das Verhalten von uebersichtRepository.findByTodoId
        when(uebersichtRepository.findByTodoId(1L)).thenReturn(newTodo);

        // Act: Service-Methode aufrufen
        todoService.createAndSaveTodo(newTodo);
        Todo savedTodo = uebersichtRepository.findByTodoId(newTodo.getTodoId());

        // Assert: Überprüfen, ob das Ergebnis nicht null ist und korrekt gespeichert wurde
        assertNotNull(savedTodo);
        assertNotNull(savedTodo.getTodoId()); //
        verify(todoRepository, times(1)).save(newTodo); // Sicherstellen, dass save() genau einmal aufgerufen wurde
    }
}