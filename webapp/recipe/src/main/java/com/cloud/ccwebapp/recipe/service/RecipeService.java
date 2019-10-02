package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.helper.RecipeHelper;
import com.cloud.ccwebapp.recipe.model.NutritionalInformation;
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

    public ResponseEntity<Recipe> saveRecipe(Recipe recipe, Authentication authentication) throws Exception {
        //get user's id
        Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
        if (dbRecord.isPresent()) {

            // Ensure recipe is valid
            // Helper will throw Exception if the recipe is invalid
            recipeHelper.isRecipeValid(recipe);

            User user = dbRecord.get();
            recipe.setAuthor(user);
            recipe.setTotal_time_in_min(recipe.getCook_time_in_min() + recipe.getPrep_time_in_min());
            recipeRepository.save(recipe);
            return new ResponseEntity<Recipe>(recipe, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Recipe> updateRecipe(@Valid Recipe recipe, Authentication authentication, String recipeId) throws Exception {

        recipeHelper.isRecipeValid(recipe);

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
                NutritionalInformation nutritionalInformation = recipe.getNutrition_information();
                NutritionalInformation dbNutritionalInformation = dbRecipe.get().getNutrition_information();

                if(nutritionalInformation.getCholesterol_in_mg()<0
                        || nutritionalInformation.getCalories()<0
                        || nutritionalInformation.getCarbohydrates_in_grams()<0
                        || nutritionalInformation.getProtein_in_grams()<0
                        || nutritionalInformation.getSodium_in_mg()<0) {
                    throw new InvalidInputException("Values for Nutritional Information Attributs cannot be less than 0");
                }
                dbNutritionalInformation.setCalories(nutritionalInformation.getCalories() > 0
                        ? nutritionalInformation.getCalories() : dbNutritionalInformation.getCalories());
                dbNutritionalInformation.setCarbohydrates_in_grams(nutritionalInformation.getCarbohydrates_in_grams() > 0
                        ? nutritionalInformation.getCarbohydrates_in_grams() : dbNutritionalInformation.getCarbohydrates_in_grams());
                dbNutritionalInformation.setCholesterol_in_mg(nutritionalInformation.getCholesterol_in_mg() > 0
                        ? nutritionalInformation.getCholesterol_in_mg() : dbNutritionalInformation.getCholesterol_in_mg());
                dbNutritionalInformation.setSodium_in_mg(nutritionalInformation.getSodium_in_mg() > 0
                        ? nutritionalInformation.getSodium_in_mg() : dbNutritionalInformation.getSodium_in_mg());
                dbNutritionalInformation.setProtein_in_grams(nutritionalInformation.getProtein_in_grams() > 0
                        ? nutritionalInformation.getProtein_in_grams() : dbNutritionalInformation.getProtein_in_grams());

                try {
                    nutritionalInformationRepository.save(dbNutritionalInformation);
                } catch (Exception ex) {
                    throw new InvalidInputException("Nutritional Information cannot be null");
                }
//                dbRecipe.get().setNutrition_information(recipe.getNutrition_information());
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
