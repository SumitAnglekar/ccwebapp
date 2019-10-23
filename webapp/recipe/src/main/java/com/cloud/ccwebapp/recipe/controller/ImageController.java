package com.cloud.ccwebapp.recipe.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.exception.InvalidImageFormatException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.ImageService;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;
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

    @Autowired
    UserRepository userRepository;

        //Get Recipe
        @GetMapping(value = "/{imageId}")
        public ResponseEntity<Image> getImage(@PathVariable UUID imageId, @PathVariable UUID recipeId, Authentication authentication) throws Exception {
                System.out.println(recipeId);
                System.out.println(imageId);
            // check if recipe is present and if user is authenticated
            Recipe recipe = recipeService.getRecipe(recipeId).getBody();
            if (recipe != null) {
                Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
                if (dbRecord.get().getId().equals(recipe.getAuthor_id())) {
                    return imageService.getImage(imageId);
                } else {
                    throw new UserNotAuthorizedException("User is not authorized to post an image");
                }

            }
            throw new RecipeNotFoundException("The Recipe is not present!!!");
        }

            @PostMapping
        public ResponseEntity<Image> saveImage
            (@PathVariable UUID recipeId,
                 @RequestPart(value = "file") MultipartFile file,
                 Authentication authentication) throws Exception {
                // check if recipe is present and if user is authenticated
                Recipe recipe = recipeService.getRecipe(recipeId).getBody();
                if(recipe!=null){
                    Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
                    if (dbRecord.get().getId().equals(recipe.getAuthor_id())) {
                        File convertedFile = imageHelper.convertMultiPartToFile(file);
                        String fileExtension = convertedFile.getName().substring(convertedFile.getName().lastIndexOf(".") + 1);
                        System.out.println(fileExtension);
                        if (fileExtension.equalsIgnoreCase("jpeg") || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("png")) {
                            return imageService.saveImage(recipe, convertedFile);
                        } else {
                            throw new InvalidImageFormatException("Invalid Image Format");
                        }
                    } else {
                        throw new UserNotAuthorizedException("User is not authorized to post an image");
                    }
                }
               throw  new RecipeNotFoundException("The Recipe is not present!!!");
        }


}
