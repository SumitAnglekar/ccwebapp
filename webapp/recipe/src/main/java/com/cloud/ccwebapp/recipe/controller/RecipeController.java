package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

}
