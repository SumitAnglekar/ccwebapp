package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.Response;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.helper.RecipeHelper;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.NutritionalInformationRepository;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Service

public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NutritionalInformationRepository nutritionalInformationRepository;
    @Autowired
    private RecipeHelper recipeHelper;

    public ResponseEntity<Recipe> getRecipe(UUID id){
        Optional<Recipe> dbRecord = recipeRepository.findRecipesById(id);
        if (dbRecord.isPresent()) {
            return new ResponseEntity<>(dbRecord.get(), HttpStatus.OK);
        } else {
            throw new RecipeNotFoundException("Recipe Id is invalid");
        }
    }

    public ResponseEntity<Recipe> deleteRecipe(UUID id, Authentication authentication) throws Exception {
        Optional<Recipe> dbRecordRecipe = recipeRepository.findById(id);
        if (!dbRecordRecipe.isPresent())
            throw new RecipeNotFoundException("Recipe is not present!!");
        Recipe recipeDb = dbRecordRecipe.get();
        Optional<User> dbUser = userRepository.findById(recipeDb.getAuthor_id());
        if (!dbUser.isPresent())
            throw new InvalidInputException("Invalid user id");
        if (!dbUser.get().getEmailaddress().equals(authentication.getName()))
            throw new UserNotAuthorizedException("You are not authorized to make changes!!");
        recipeRepository.delete(recipeDb);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Recipe> saveRecipe(Recipe recipe, Authentication authentication) {
        //get user's id
        Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
        if (dbRecord.isPresent()) {

            // Ensure recipe is valid
            // Helper will throw Exception if the recipe is invalid
            recipeHelper.isRecipeValid(recipe);

            User user = dbRecord.get();
            recipe.setAuthor_id(user.getId());
            recipe.setTotal_time_in_min(recipe.getCook_time_in_min() + recipe.getPrep_time_in_min());
            recipeRepository.save(recipe);
            return new ResponseEntity<Recipe>(recipe, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Recipe> updateRecipe(@Valid Recipe recipe, Authentication authentication, String recipeId) throws Exception {
        //get user's id
        Optional<Recipe> dbRecipe = recipeRepository.findById(UUID.fromString(recipeId));
        if (!dbRecipe.isPresent())
            throw new RecipeNotFoundException("Recipe is not present!!");
        Optional<User> dbUser = userRepository.findById(dbRecipe.get().getAuthor_id());
        if (!dbUser.isPresent())
            throw new InvalidInputException("Unknown error");
        if (!dbUser.get().getEmailaddress().equals(authentication.getName()))
            throw new UserNotAuthorizedException("You are not authorized to make changes!!");
        recipeHelper.isRecipeValid(recipe);
        if (dbRecipe.get() != null) {
            dbRecipe.get().setCook_time_in_min(recipe.getCook_time_in_min());
            dbRecipe.get().setPrep_time_in_min(recipe.getPrep_time_in_min());
            dbRecipe.get().setTotal_time_in_min(dbRecipe.get().getCook_time_in_min() + dbRecipe.get().getPrep_time_in_min());
            dbRecipe.get().setIngredients(recipe.getIngredients());
            dbRecipe.get().setSteps(recipe.getSteps());
            dbRecipe.get().setNutrition_information(recipe.getNutrition_information());
        }
        Recipe rc = dbRecipe.get();
        recipeRepository.save(rc);
        return new ResponseEntity<Recipe>(rc, HttpStatus.OK);
    }

}
