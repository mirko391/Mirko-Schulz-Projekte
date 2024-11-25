package com.example.To_Do.Liste.controller;

import com.example.To_Do.Liste.model.Person;
import com.example.To_Do.Liste.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Random;

@Controller
public class PasswordController {

    @Autowired
    private PersonRepository personRepository;

    @PostMapping("/password-reset")
    public String resetPassword(
            @RequestParam String username,
            @RequestParam String name,
            @RequestParam String email,
            Model model) {

        // Suche die Person basierend auf Benutzername, Name und E-Mail
        Person person = personRepository.findByUsernameAndNameAndEmail(username, name, email);

        // Überprüfen, ob eine passende Person gefunden wurde
        if (person != null) {

            Random random = new Random();
            int number = random.nextInt(1000000);

            // Format the number with leading zeros to ensure 6 digits
            String formattedNumber = String.format("%06d", number + 1);

            person.setPasswort(formattedNumber); // Temporäres Passwort setzen
            personRepository.save(person);
            model.addAttribute("resetSuccess", true);
            model.addAttribute("newPassword", formattedNumber);
        } else {
            model.addAttribute("resetError", true);
        }

        return "password"; // Rückkehr zur Passwort-Seite mit Erfolg oder Fehler
    }
}