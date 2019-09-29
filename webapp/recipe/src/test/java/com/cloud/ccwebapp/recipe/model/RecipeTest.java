package com.cloud.ccwebapp.recipe.model;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RecipeTest {

    private static Validator validator;

    @BeforeClass
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public Recipe getDefaultRecipeObject() {
        Recipe defaultRecipeObject = new Recipe();
        defaultRecipeObject.setCook_time_in_min(1);
        defaultRecipeObject.setPrep_time_in_min(1);
        defaultRecipeObject.setTitle("Test Recipe");
        defaultRecipeObject.setCuisine("Test Cuisine");
        defaultRecipeObject.setServings(1);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        defaultRecipeObject.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        defaultRecipeObject.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        defaultRecipeObject.setIngredients(testIngredients);

        return defaultRecipeObject;
    }

    @Test
    public void whenRecipeHasNoConstraintViolations() {
        // Default recipe values
        Recipe recipeWithNoViolations = getDefaultRecipeObject();

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoViolations);
        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void whenRecipeHasCookTimeViolation() {

        //Violation
        Recipe recipeWithViolation = getDefaultRecipeObject();
        recipeWithViolation.setCook_time_in_min(0);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasPrepTimeViolation() {

        // Violation
        Recipe recipeWithViolation = getDefaultRecipeObject();
        recipeWithViolation.setPrep_time_in_min(0);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasTitleViolation() {

        // Violation - no title
        Recipe recipeWithViolation = getDefaultRecipeObject();
        recipeWithViolation.setTitle(null);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasCuisineViolation() {
        // Violation - no cuisine
        Recipe recipeWithViolation = getDefaultRecipeObject();
        recipeWithViolation.setCuisine(null);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasServingsViolation() {

        // Violation - negative servings
        Recipe recipeWithViolation = getDefaultRecipeObject();
        recipeWithViolation.setServings(-2);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasStepsViolation() {
        // Violation - no steps
        Recipe recipeWithViolation = getDefaultRecipeObject();
        List<OrderedList> testSteps = new ArrayList<>();
        recipeWithViolation.setSteps(testSteps);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasNutritionalInformationViolation() {

        // Violation - Null Nutritional Information
        Recipe recipeWithViolation = getDefaultRecipeObject();
        recipeWithViolation.setNutrition_information(null);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenNullIngredients_thenConstraintViolation() {
        // Empty Ingredients
        Recipe recipeWithViolation = getDefaultRecipeObject();
        List<String> testIngredients = new ArrayList<>();
        recipeWithViolation.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithViolation);
        assertThat(violations.size()).isEqualTo(1);
    }
}
