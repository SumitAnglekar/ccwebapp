package com.cloud.ccwebapp.recipe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Validated
public class NutritionalInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    @Min(value = 0, message = "calories cannot be negative")
    @NotNull(message = "calories should be present")
    private int calories;

    @DecimalMin(value = "0.0", message = "cholesterol cannot be negative")
    @NotNull(message = "cholesterol should be present")
    private float cholesterol_in_mg;

    @Min(value = 0, message = "sodium cannot be negative")
    @NotNull(message = "sodium should be present")
    private int sodium_in_mg;

    @DecimalMin(value = "0.0", message = "carbohydrates cannot be negative")
    @NotNull(message = "carbohydrates should be present")
    private float carbohydrates_in_grams;

    @DecimalMin(value = "0.0", message = "protein cannot be negative")
    @NotNull(message = "proteins should be present")
    private float protein_in_grams;

    public NutritionalInformation() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public float getCholesterol_in_mg() {
        return cholesterol_in_mg;
    }

    public void setCholesterol_in_mg(float cholesterol_in_mg) {
        this.cholesterol_in_mg = cholesterol_in_mg;
    }

    public int getSodium_in_mg() {
        return sodium_in_mg;
    }

    public void setSodium_in_mg(int sodium_in_mg) {
        this.sodium_in_mg = sodium_in_mg;
    }

    public float getCarbohydrates_in_grams() {
        return carbohydrates_in_grams;
    }

    public void setCarbohydrates_in_grams(float carbohydrates_in_grams) {
        this.carbohydrates_in_grams = carbohydrates_in_grams;
    }

    public float getProtein_in_grams() {
        return protein_in_grams;
    }

    public void setProtein_in_grams(float protein_in_grams) {
        this.protein_in_grams = protein_in_grams;
    }
}
