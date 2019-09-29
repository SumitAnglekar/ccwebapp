package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, UUID> {
    public Optional<Recipe> findRecipesById(UUID uuid);

}
