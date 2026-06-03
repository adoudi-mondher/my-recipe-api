package com.myrecipe.myrecipeapi.Controllers;

import com.myrecipe.myrecipeapi.Models.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final com.myrecipe.myrecipeapi.DAO.UserDao userDao;

    // POST /api/users/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AppUser user) {
        if (userDao.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username déjà utilisé"));
        }
        if (userDao.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email déjà utilisé"));
        }
        // TODO : hasher le mot de passe avec BCrypt avant de persister
        AppUser saved = userDao.save(user);
        saved.setPassword(null); // ne pas renvoyer le mot de passe
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // POST /api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<AppUser> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Identifiants incorrects"));
        }
        AppUser user = userOpt.get();
        user.setPassword(null);
        return ResponseEntity.ok(Map.of("message", "Connexion réussie", "user", user));
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userDao.findAll());
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userDao.findById(id)
                .map(u -> { u.setPassword(null); return ResponseEntity.ok(u); })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody AppUser updated) {
        return userDao.findById(id)
                .map(user -> {
                    user.setUsername(updated.getUsername());
                    user.setEmail(updated.getEmail());
                    if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                        user.setPassword(updated.getPassword()); // TODO : hasher
                    }
                    AppUser saved = userDao.save(user);
                    saved.setPassword(null);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
