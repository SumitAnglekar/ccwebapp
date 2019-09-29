package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/recipe")
public class RecipeController {

    @Autowired
    RecipeService recipeService;

    //Get Recipe
//    @RequestMapping("/recipe/{id}")
//    public Recipe getTopic(@PathVariable String id) {
//        return recipeService.getTopic(id);
//    }
//
//    //Delete Recipe
//    @RequestMapping(method=RequestMethod.DELETE, value="/recipe/{id}")
//    public void deleteTopic(@PathVariable String id) {
//        recipeService.deleteTopic(id);
//    }

    /**
     * Posts new recipe
     */
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public ResponseEntity<Recipe> saveRecipe(@Valid @RequestBody Recipe recipe, Authentication authentication) {
        return recipeService.saveRecipe(recipe, authentication);
    }

}
