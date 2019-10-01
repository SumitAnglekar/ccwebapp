package com.cloud.ccwebapp.recipe.helper;

import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.model.OrderedList;
import com.cloud.ccwebapp.recipe.model.Recipe;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class RecipeHelper {

    public void isRecipeValid(Recipe recipe){
        if (!(recipe.getCook_time_in_min() > 0 && recipe.getCook_time_in_min() % 5 == 0)){
            throw new InvalidInputException("Cook time should be greater than 0 and multiple of 5");
        }

        if(!(recipe.getPrep_time_in_min() > 0 && recipe.getPrep_time_in_min() % 5 == 0)) {
            throw new InvalidInputException("Prep time should be greater than 0 and multiple of 5");
        }

        if(recipe.getTitle().isEmpty()) {
            throw new InvalidInputException("Recipe Title cannot be null!!!");
        }

        if(recipe.getCuisine().isEmpty()) {
            throw new InvalidInputException("Cuisine cannot be null!!!");
        }

        if(recipe.getServings()<1 || recipe.getServings()>5) {
            throw new InvalidInputException("Servings must be between 1 and 5!!!");
        }

        if(recipe.getIngredients().size()==0)
            throw new InvalidInputException("List of Ingredients cannot be null!!!");

        if(recipe.getSteps().size()==0)
            throw new InvalidInputException("List of Steps cannot be null!!!");
        else {
            for(OrderedList orderedList : recipe.getSteps()) {
                if(orderedList.getItems()== null || orderedList.getItems().isEmpty())
                    throw new InvalidInputException("Items of Steps cannot be null!!!");
                if(orderedList.getPosition()==0)
                    throw new InvalidInputException("Position of Steps must be greater than 0 && it is a mandatory field!!!");
            }
        }

        if(recipe.getNutrition_information() == null) {
            throw new InvalidInputException("Nutrition_Information cannot be null!!");
        }
            else{

            }


    }
}
