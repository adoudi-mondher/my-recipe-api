package com.myrecipe.myrecipeapi.Controllers;

import com.myrecipe.myrecipeapi.Models.AppUser;
import com.myrecipe.myrecipeapi.Models.Recipe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final com.myrecipe.myrecipeapi.DAO.RecipeDao recipeDao;
    private final com.myrecipe.myrecipeapi.DAO.UserDao userDao;

    // GET /api/recipes — toutes les recettes
    @GetMapping
    public ResponseEntity<List<Recipe>> getAll() {
        return ResponseEntity.ok(recipeDao.findAll());
    }

    // GET /api/recipes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return recipeDao.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/recipes/user/{userId} — recettes d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        if (!userDao.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recipeDao.findByAuthorId(userId));
    }

    // GET /api/recipes/search?title=... — recherche par titre
    @GetMapping("/search")
    public ResponseEntity<List<Recipe>> search(@RequestParam String title) {
        return ResponseEntity.ok(recipeDao.findByTitleContainingIgnoreCase(title));
    }

    // POST /api/recipes — créer une recette
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Recipe recipe,
                                    @RequestParam Long userId) {
        Optional<AppUser> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur introuvable"));
        }
        recipe.setAuthor(userOpt.get());
        Recipe saved = recipeDao.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/recipes/{id} — mettre à jour
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody Recipe updated,
                                    @RequestParam Long userId) {
        return recipeDao.findById(id)
                .map(recipe -> {
                    if (!recipe.getAuthor().getId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Vous n'êtes pas l'auteur de cette recette"));
                    }
                    recipe.setTitle(updated.getTitle());
                    recipe.setDescription(updated.getDescription());
                    recipe.setIngredients(updated.getIngredients());
                    recipe.setInstructions(updated.getInstructions());
                    return ResponseEntity.ok((Object) recipeDao.save(recipe));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/recipes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam Long userId) {
        return recipeDao.findById(id)
                .map(recipe -> {
                    if (!recipe.getAuthor().getId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Vous n'êtes pas l'auteur de cette recette"));
                    }
                    recipeDao.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
