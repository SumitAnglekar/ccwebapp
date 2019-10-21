package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.ImageService;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/recipe/{recipeId}/image")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class ImageController {

        @Autowired
        ImageService imageService;

        @Autowired
        ImageRepository imageRepository;

        //Get Recipe
        @GetMapping(value = "/{imageId}")
        public void getImage(@PathVariable UUID imageId , @PathVariable UUID recipeId, MultipartFile multipartFile) throws Exception {
                System.out.println(recipeId);
                System.out.println(imageId);
        }

        @PostMapping
        public ResponseEntity<Recipe> saveImage(@RequestBody Image image, Authentication authentication) throws Exception {
                // check if user is present
                ResponseEntity<Image> imageResponseEntity ;

               return null;
        }


}
