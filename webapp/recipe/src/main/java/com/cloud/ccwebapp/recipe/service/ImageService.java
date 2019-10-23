package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.services.s3.AmazonS3;
import com.cloud.ccwebapp.recipe.exception.ImageAlreadyExistsException;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.helper.RecipeHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${aws.s3.bucketname}")
    String bucketName;

    @Value("${aws.s3.endpointURL}")
    String endpointUrl;

    @Autowired
    AmazonS3 amazonS3;
    @Autowired
    ImageHelper imageHelper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RecipeHelper recipeHelper;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    public ResponseEntity<Image> saveImage(Recipe recipe, File imageFile) throws Exception {
        if (recipe.getImage() == null) {
            String fileName = imageHelper.generateFileName(imageFile);
            amazonS3.putObject(bucketName, fileName, imageFile);
            String fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            Image image = new Image();
            image.setUrl(fileUrl);
            recipe.setImage(image);
            imageRepository.save(image);
            recipeRepository.save(recipe);
            return new ResponseEntity<Image>(image, HttpStatus.CREATED);
        } else {
            throw new ImageAlreadyExistsException("The Image is already present!!!");
        }
    }

    public ResponseEntity<Image> getImage(UUID imageId) {
        //        Optional<Image> image = recipeRepository.findRecipesById(id);
        return new ResponseEntity<Image>((Image) null, HttpStatus.OK);
    }
}
