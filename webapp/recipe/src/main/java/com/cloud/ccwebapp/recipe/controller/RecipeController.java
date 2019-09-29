package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/recipe")
public class RecipeController {

    @Autowired
    RecipeService recipeService;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    UserRepository userRepository;

    //Get Recipe
    @RequestMapping("/recipe/{id}")
    public Optional<Recipe> getRecipe(@PathVariable UUID id) throws Exception {
        Optional<Recipe> dbRecord = recipeRepository.findRecipesById(id);
        if (dbRecord.isPresent()) {
            return recipeRepository.findRecipesById(id);
        }
        else {
            throw new Exception("Id is invalid");
        }
    }

    //Delete Recipe
    @RequestMapping(method=RequestMethod.DELETE, value="/recipe/{id}")
    public void deleteRecipe(@PathVariable UUID id, Authentication authentication) throws Exception {
        Optional<Recipe> dbRecordRecipe = recipeRepository.findRecipesById(id);
        Optional<User> dbRecordUser = userRepository.findUserByEmailaddress(authentication.getName());
        if (dbRecordRecipe.isPresent()) {
            Recipe recipeDb = (Recipe) dbRecordRecipe.get();
            User userDb = (User)dbRecordUser.get();

            if (recipeDb.getId().equals(userDb.))

             recipe.getAuthor().getId();
        }
            return recipeRepository.findRecipesById(id);
        }
        else {
            throw new Exception("Id is invalid");

    }

    /**
     * Posts new recipe
     */
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public ResponseEntity<Recipe> saveRecipe(@RequestBody Recipe recipe, Authentication authentication) {
        return recipeService.saveRecipe(recipe, authentication);
    }

}
