package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/v1")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class RecipeController {

  private static final Logger LOGGER = (Logger) LogManager.getLogger(RecipeController.class.getName());
  @Autowired RecipeService recipeService;

  @Autowired RecipeRepository recipeRepository;

  @Autowired UserRepository userRepository;

  @Autowired
  StatsDClient statsDClient;
  // Get Recipe
  @RequestMapping(method = RequestMethod.GET, value = "/recipe/{id}")
  public ResponseEntity<Recipe> getRecipe(@PathVariable UUID id) throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.recipe.http.get");
    LOGGER.info("Checking if the recipe is present for recipeId "+ id);
    Object object = recipeService.getRecipe(id);
    long end = System.currentTimeMillis();
    long result = end-start;
    statsDClient.recordExecutionTime("timer.recipe.get",result);
    return (ResponseEntity<Recipe>) object;
  }

  // Delete Recipe
  @RequestMapping(method = RequestMethod.DELETE, value = "/recipe/{id}")
  public ResponseEntity<Recipe> deleteRecipe(@PathVariable UUID id, Authentication authentication)
      throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.recipe.http.delete");
    LOGGER.info("Checking if the recipe is present for recipeId "+ id);
    Object object = recipeService.deleteRecipe(id, authentication);
    long end = System.currentTimeMillis();
    long result = end-start;
    statsDClient.recordExecutionTime("timer.recipe.delete",result);
    return (ResponseEntity<Recipe>) object;
  }

  /** Posts new recipe */
  @RequestMapping(method = RequestMethod.POST, value = "/recipe/")
  public ResponseEntity<Recipe> saveRecipe(
      @RequestBody Recipe recipe, Authentication authentication) throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.recipe.http.post");
    LOGGER.info("Saving recipe");
    Object object = recipeService.saveRecipe(recipe, authentication);
    long end = System.currentTimeMillis();
    long result = end-start;
    statsDClient.recordExecutionTime("timer.recipe.post",result);
    return (ResponseEntity<Recipe>) object;
  }

  // Update a recipe
  @RequestMapping(method = RequestMethod.PUT, value = "/recipe/{id}")
  public ResponseEntity<Recipe> updateUser(
      @RequestBody Recipe recipe, Authentication authentication, @PathVariable String id)
      throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.recipe.http.put");
    LOGGER.info("Checking if the recipe is present for recipeId "+ id);
    Object object = recipeService.updateRecipe(recipe, authentication, id);
    long end = System.currentTimeMillis();
    long result = end-start;
    statsDClient.recordExecutionTime("timer.recipe.put",result);
    return (ResponseEntity<Recipe>) object;
  }

  //Get all recipes
  @GetMapping("/recipes")
  public ResponseEntity<Recipe> getLatestRecipie(){
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.recipe.http.get");
    LOGGER.info("Checking the information of latest recipe ");
    Object object = recipeService.getLatestRecipe();
    long end = System.currentTimeMillis();
    long result = end-start;
    statsDClient.recordExecutionTime("timer.recipe.getall",result);
    return (ResponseEntity<Recipe>)object;
  }
}
