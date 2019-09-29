package com.cloud.ccwebapp.recipe.service;

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

}
