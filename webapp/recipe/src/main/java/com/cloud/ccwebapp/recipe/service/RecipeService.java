package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<Recipe> saveRecipe(Recipe recipe, Authentication authentication) {
        //get user's id
        Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
        if (dbRecord.isPresent()) {
            User user = dbRecord.get();
            recipe.setAuthor(user);
            recipe.setTotal_time_in_min(recipe.getCook_time_in_min() + recipe.getPrep_time_in_min());
            recipeRepository.save(recipe);
            return new ResponseEntity<Recipe>(recipe, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Recipe> updateRecipe(Recipe recipe, Authentication authentication, String recipeId) throws Exception {
        //get user's id
        Optional<Recipe> dbRecipe = recipeRepository.findById(UUID.fromString(recipeId));
        if(!dbRecipe.isPresent())
            throw new RecipeNotFoundException("Recipe is not present!!");
        if(!dbRecipe.get().getAuthor().getEmailaddress().equals(authentication.getName()))
            throw new UserNotAuthorizedException("You are not authorized to make changes!!");

        if(dbRecipe.get()!=null) {

            if (!(recipe.getCook_time_in_min() > 0 && !(recipe.getCook_time_in_min() % 5 == 0))){
                dbRecipe.get().setCook_time_in_min(recipe.getCook_time_in_min());
            }else{
                throw new InvalidInputException("Cook time should be greater than 0 and multiple of 5");
            }

            if(!(recipe.getPrep_time_in_min() > 0 && !(recipe.getPrep_time_in_min() % 5 == 0))) {
                dbRecipe.get().setPrep_time_in_min(recipe.getPrep_time_in_min());
            }else {
                throw new InvalidInputException("Prep time should be greater than 0 and multiple of 5");
            }
            dbRecipe.get().setTotal_time_in_min(dbRecipe.get().getCook_time_in_min()+dbRecipe.get().getPrep_time_in_min());

            if(recipe.getIngredients() != null && recipe.getIngredients().size()>0) {
                dbRecipe.get().setIngredients(recipe.getIngredients());
            }

            if(recipe.getNutrition_information() != null) {
                dbRecipe.get().setNutrition_information(recipe.getNutrition_information());
            } else {
                throw new InvalidInputException("Nutritional Information cannot be null");
            }

            if(recipe.getSteps() != null && recipe.getSteps().size()>0) {
                dbRecipe.get().setSteps(recipe.getSteps());
            }
        }
        Recipe rc = dbRecipe.get();
        recipeRepository.save(rc);
        return new ResponseEntity<Recipe>(rc,HttpStatus.OK);
    }

}
