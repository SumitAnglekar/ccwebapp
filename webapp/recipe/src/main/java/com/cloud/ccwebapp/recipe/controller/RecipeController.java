package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/recipe")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class RecipeController {

    @Autowired
    RecipeService recipeService;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    UserRepository userRepository;

    //Get Recipe
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable UUID id) throws Exception {

        return recipeService.getRecipe(id);
    }

    //Delete Recipe
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity<Recipe> deleteRecipe(@PathVariable UUID id, Authentication authentication) throws Exception {
        return recipeService.deleteRecipe(id, authentication);
    }

    /**
     * Posts new recipe
     */
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public ResponseEntity<Recipe> saveRecipe(@RequestBody Recipe recipe, Authentication authentication) throws Exception {
        return recipeService.saveRecipe(recipe, authentication);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public ResponseEntity<Recipe> updateUser(@RequestBody Recipe recipe, Authentication authentication, @PathVariable String id) throws Exception {
        return recipeService.updateRecipe(recipe, authentication, id);
    }

}
