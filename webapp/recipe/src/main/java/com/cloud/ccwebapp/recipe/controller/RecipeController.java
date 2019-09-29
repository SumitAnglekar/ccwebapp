package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

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
    @RequestMapping(method=RequestMethod.GET, value ="/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable UUID id) throws Exception {
        Optional<Recipe> dbRecord = recipeRepository.findRecipesById(id);
        if (dbRecord.isPresent()) {
            return new ResponseEntity<>(dbRecord.get(), HttpStatus.OK);
        }
        else {
            throw new Exception("Id is invalid");
        }
    }

    //Delete Recipe
    @RequestMapping(method=RequestMethod.DELETE, value="/{id}")
    public void deleteRecipe(@PathVariable UUID id, Authentication authentication) throws Exception {
        Optional<Recipe> dbRecordRecipe = recipeRepository.findRecipesById(id);
        Optional<User> dbRecordUser = userRepository.findUserByEmailaddress(authentication.getName());
        if (dbRecordRecipe.isPresent()) {
            Recipe recipeDb = (Recipe) dbRecordRecipe.get();
            User userDb = (User) dbRecordUser.get();

            if (recipeDb.getAuthor().getId().equals(userDb.getId())) {
                recipeRepository.delete(recipeDb);
            } else {
                throw new Exception("User is invalid");
            }
        }
        else{
                throw new Exception("Recipe Id is invalid");
            }

    }

    /**
     * Posts new recipe
     */
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public ResponseEntity<Recipe> saveRecipe(@Valid @RequestBody Recipe recipe, Authentication authentication) {
        return recipeService.saveRecipe(recipe, authentication);
    }

}
