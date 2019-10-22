package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.services.s3.AmazonS3;
import com.cloud.ccwebapp.recipe.exception.ImageAlreadyExistsException;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.helper.RecipeHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ImageService {

        String bucketName;
        String endpointUrl;

        @Autowired
        AmazonS3 amazonS3;

        @Autowired
        private ImageRepository imageRepository;
        @Autowired
        ImageHelper imageHelper;

        @Autowired
        UserRepository userRepository;

        @Autowired
        RecipeHelper  recipeHelper;

        public ResponseEntity<Image> saveImage(Recipe recipe, File imageFile) throws Exception {
            String fileName= imageHelper.generateFileName(imageFile);
            amazonS3.putObject(bucketName,fileName,imageFile);
            String fileUrl = endpointUrl+"/"+bucketName+"/"+fileName;
            Image image = new Image();
            image.setUrl(fileUrl);
            if(recipe.getImage()==null){
            recipe.setImage(image);
            return new ResponseEntity<Image>(image, HttpStatus.CREATED);
            }else{
                throw new ImageAlreadyExistsException("The Image is already present!!!");
            }
        }
}
