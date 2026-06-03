package com.myrecipe.myrecipeapi.DAO;

import com.myrecipe.myrecipeapi.Models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeDao extends JpaRepository<Recipe, Long> {

    // Toutes les recettes d'un utilisateur
    List<Recipe> findByAuthorId(Long authorId);

    // Recherche par titre (insensible à la casse)
    List<Recipe> findByTitleContainingIgnoreCase(String title);
}
