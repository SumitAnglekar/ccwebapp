package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, UUID> {
    Optional<Recipe> findRecipesById(UUID uuid);

    public List<Recipe> findTop1ByOrderByCreatedtsDesc();

    public List<Recipe> findAllByAuthorId(UUID authorId);

}
