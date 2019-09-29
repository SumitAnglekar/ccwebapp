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

    @Test
    public void whenRecipeHasNoConstraintViolations() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void whenRecipeHasCookTimeViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        //Violation
        recipeWithNoIngredients.setCook_time_in_min(0);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasPrepTimeViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        // Violation
        recipeWithNoIngredients.setPrep_time_in_min(0);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasTitleViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        // Violation - no title
        //recipeWithNoIngredients.setTitle("Test Recipe");

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasCuisineViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setServings(1);

        // Violation - no cuisine
        // recipeWithNoIngredients.setCuisine("Test Cuisine");

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasServingsViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");

        // Violation - negative servings
        recipeWithNoIngredients.setServings(-2);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasStepsViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        // Violation - no steps
        List<OrderedList> testSteps = new ArrayList<>();
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenRecipeHasNutritionalInformationViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Violation - Null Nutritional Information
        recipeWithNoIngredients.setNutrition_information(null);

        // Default Ingredients
        List<String> testIngredients = new ArrayList<>();
        testIngredients.add("test Ingredient");
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void whenNullIngredients_thenConstraintViolation() {
        // Default recipe values
        Recipe recipeWithNoIngredients = new Recipe();
        recipeWithNoIngredients.setCook_time_in_min(1);
        recipeWithNoIngredients.setPrep_time_in_min(1);
        recipeWithNoIngredients.setTitle("Test Recipe");
        recipeWithNoIngredients.setCuisine("Test Cuisine");
        recipeWithNoIngredients.setServings(1);

        // Default OrderedList value
        List<OrderedList> testSteps = new ArrayList<>();
        OrderedList testStep = new OrderedList();
        testStep.setPosition(1);
        testStep.setItems("Test Step");
        testSteps.add(testStep);
        recipeWithNoIngredients.setSteps(testSteps);

        // Default Nutritional Information
        NutritionalInformation testNutrition = new NutritionalInformation();
        testNutrition.setCalories(0);
        testNutrition.setCholesterol_in_mg(0);
        testNutrition.setSodium_in_mg(0);
        testNutrition.setCarbohydrates_in_grams(0);
        testNutrition.setProtein_in_grams(0);
        recipeWithNoIngredients.setNutrition_information(testNutrition);

        // Empty Ingredients
        List<String> testIngredients = new ArrayList<>();
        recipeWithNoIngredients.setIngredients(testIngredients);

        Set<ConstraintViolation<Recipe>> violations = validator.validate(recipeWithNoIngredients);
        assertThat(violations.size()).isEqualTo(1);
    }
}
