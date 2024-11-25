package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private CustomUserDetailsService userService;

    // zeigt die Seite an
    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new Person()); // // Model attribute must match the form's th:object
        return "registration";
    }

    // ruft die Methode zum Speichern des Users in der Datenbank auf
    @PostMapping("/registration")
    public String registerUser(@ModelAttribute("user") Person user) {
        System.out.println("Registering user: " + user.getUsername());  // Debugging to check if the registerUser() method is being triggered
        userService.saveUser(user); // Save user to the database
        return "redirect:/login"; // Redirect to login page after successful registration
    }

    @GetMapping("/user")
    public String showUserPage(Model model) {
        // Aktuell eingeloggten Benutzer abrufen
        Person currentUser = userService.getCurrentUser();

        // Benutzerdaten an das Modell anhängen
        model.addAttribute("user", currentUser);

        return "user"; // Diese Seite zeigt die Benutzerinformationen an

    }
    @PostMapping("/user/edit")
    public String editUser(@ModelAttribute("user") Person user) {
        Person currentUser = userService.getCurrentUser();

        // Обновление данных текущего пользователя
        if (!currentUser.getUsername().equals(user.getUsername())) {
            currentUser.setUsername(user.getUsername());
            userService.saveUser(currentUser);
            return "redirect:/login";
        }
        if (!currentUser.getPasswort().equals(user.getPasswort())){
            currentUser.setPasswort((user.getPasswort()));
            userService.saveUser(currentUser);
            return "redirect:/login";
        }

        currentUser.setEmail(user.getEmail());
        currentUser.setName(user.getName());

        // Сохранение обновленных данных
        userService.saveUser(currentUser);

        return "redirect:/user"; // Перенаправление обратно на страницу пользователя
    }
}