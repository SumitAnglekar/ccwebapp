package com.cloud.ccwebapp.recipe.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.cloud.ccwebapp.recipe.configuration.MetricsConfiguration;
import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.exception.InvalidImageFormatException;
import com.cloud.ccwebapp.recipe.exception.RecipeNotFoundException;
import com.cloud.ccwebapp.recipe.exception.UserNotAuthorizedException;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.ImageService;
import com.cloud.ccwebapp.recipe.service.RecipeService;
import com.timgroup.statsd.StatsDClient;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/v1/recipe/{recipeId}/image")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class ImageController {

  private static final Logger LOGGER = (Logger) LogManager.getLogger(ImageController.class.getName());

  @Autowired AmazonS3 amazonS3;

  @Autowired ImageService imageService;

  @Autowired ImageRepository imageRepository;

  @Autowired RecipeService recipeService;

  @Autowired ImageHelper imageHelper;

  @Autowired UserRepository userRepository;

  @Autowired RecipeRepository recipeRepository;

  @Autowired
  StatsDClient statsDClient;

  // Get Recipe
  @GetMapping(value = "/{imageId}")
  public ResponseEntity<Image> getImage(
      @PathVariable UUID imageId, @PathVariable UUID recipeId)
      throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.image.http.get");
    // check if recipe is present and if user is authenticated
    Recipe recipe = recipeService.getRecipe(recipeId).getBody();
    if (recipe != null) {
      LOGGER.info("Recipe found for id = "+ recipeId+". Searching image for imageID" + imageId);
      Object object = imageService.getImage(imageId, recipe);
      long end = System.currentTimeMillis();
      long result = end-start;
      statsDClient.recordExecutionTime("endpoint.image.http.get",result);
      return (ResponseEntity<Image>) object;
    }
    LOGGER.error("Recipe not found for recipeId:"+recipeId);
    throw new RecipeNotFoundException("The Recipe is not present!!!");
  }

  @PostMapping
  public ResponseEntity<Image> saveImage(
      @PathVariable UUID recipeId,
      @RequestPart(value = "file") MultipartFile file,
      Authentication authentication)
      throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.image.http.post");
    if (file == null) {
      LOGGER.error("Image cannot be null!!!");
      throw new InvalidImageFormatException("Image cannot be null!!");
    }
    // check if recipe is present and if user is authenticated
    Recipe recipe = recipeService.getRecipe(recipeId).getBody();
    if (recipe != null) {
      LOGGER.info("Recipe found for id = "+ recipeId+". Creating an  image....");
      Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
      File convertedFile = imageHelper.convertMultiPartToFile(file);
      String fileExtension = convertedFile.getName().substring(convertedFile.getName().lastIndexOf(".") + 1);
      if (fileExtension.equalsIgnoreCase("jpeg")
          || fileExtension.equalsIgnoreCase("jpg")
          || fileExtension.equalsIgnoreCase("png")) {
        LOGGER.info("Image has been created for recipeId "+ recipeId);
        Object object = imageService.saveImage(recipe, convertedFile);
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("endpoint.image.http.post",result);
        return (ResponseEntity<Image>)object;
      } else {
        LOGGER.error("Invalid Image Format");
        throw new InvalidImageFormatException("Invalid Image Format");
      }
    }
    LOGGER.error("Recipe not found for recipeId:"+recipeId);
    throw new RecipeNotFoundException("The Recipe is not present!!!");
  }

  @DeleteMapping(value = "/{imageId}")
  public ResponseEntity<Image> deleteImage(
      @PathVariable UUID imageId, @PathVariable UUID recipeId, Authentication authentication)
      throws Exception {
    long start = System.currentTimeMillis();
    statsDClient.incrementCounter("endpoint.image.http.delete");
    // check if recipe is present and if user is authenticated
    Recipe recipe = recipeService.getRecipe(recipeId).getBody();
    if (recipe != null) {
      Optional<User> dbRecord = userRepository.findUserByEmailaddress(authentication.getName());
      if (dbRecord.get().getId().equals(recipe.getAuthor_id())) {
        LOGGER.info("Deleting image id "+imageId+" for recipeId "+recipeId);
        Object object = imageService.deleteImage(imageId, recipe);
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("endpoint.image.http.delete",result);
        return (ResponseEntity)object;
      } else {
        LOGGER.error("User is not authorized to post an image!!!");
        throw new UserNotAuthorizedException("User is not authorized to post an image");
      }
    }
    LOGGER.error("Recipe not found for recipeId:"+recipeId);
    throw new RecipeNotFoundException("The Recipe is not present!!!");
  }
}
