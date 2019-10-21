package com.cloud.ccwebapp.recipe.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.exception.InvalidImageFormatException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.service.ImageService;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/v1/recipe/{recipeId}/image")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class ImageController {

        @Autowired
        AmazonS3 amazonS3;

        @Autowired
        ImageService imageService;

        @Autowired
        ImageRepository imageRepository;

        @Autowired
        RecipeService recipeService;

        @Autowired
        ImageHelper imageHelper;

        //Get Recipe
        @GetMapping(value = "/{imageId}")
        public void getImage(@PathVariable UUID imageId , @PathVariable UUID recipeId) throws Exception {
                System.out.println(recipeId);
                System.out.println(imageId);
        }

        @PostMapping
        public ResponseEntity<Recipe> saveImage
                (@PathVariable UUID recipeId, @RequestBody Image image,
                 @RequestPart(value = "file") MultipartFile file,
                 Authentication authentication) throws Exception {
                // check if recipe is present and if user is authenticated
                Recipe recipe = recipeService.getRecipe(recipeId).getBody();
                if(recipe!=null){
                        ResponseEntity<Image> imageResponseEntity ;
                }
                File convertedFile = imageHelper.convertMultiPartToFile(file);
                String fileExtension = convertedFile.getName().substring(convertedFile.getName().lastIndexOf("."));
                if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                        imageService.saveImage(recipe,authentication,convertedFile);
                }else{
                        throw new InvalidImageFormatException("Invalid Image Format");
                }
               throw  new RecipeNotFoundException("The Recipe is not present!!!");
        }



}
