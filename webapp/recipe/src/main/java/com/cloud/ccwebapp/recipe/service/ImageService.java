package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.cloud.ccwebapp.recipe.exception.ImageAlreadyExistsException;
import com.cloud.ccwebapp.recipe.exception.ImageNotFoundException;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.helper.RecipeHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import com.cloud.ccwebapp.recipe.repository.RecipeRepository;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import java.io.File;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<Image> getImage(UUID imageId, Recipe recipe) {
        if (recipe.getImage() != null) {
            if (recipe.getImage().getId().equals(imageId)) {
                String fileName = recipe.getImage().getUrl().split("/")[2];
                S3Object s3object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
                if (s3object != null) {
                    return new ResponseEntity<Image>(recipe.getImage(), HttpStatus.CREATED);
                } else {
                    recipe.setImage(null);
                    throw new ImageNotFoundException("Image not found!!!");
                }

            } else {
                throw new ImageNotFoundException("Wrong image ID for the recipe!!!");
            }

        } else {
            throw new ImageNotFoundException("There is no image found!!!");
        }
    }


    public ResponseEntity<Image> saveImage(Recipe recipe, File imageFile) throws Exception {
        if (recipe.getImage() == null) {
            String fileName = imageHelper.generateFileName(imageFile);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, imageFile);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(imageFile.length());
            putObjectRequest.setMetadata(meta);
            amazonS3.putObject(putObjectRequest);
            String fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            System.out.println("******************************file URL *****************************" +fileUrl);
            Image image = new Image();
            image.setUrl(fileUrl);
            image.setContentLength(meta.getContentLength());
            recipe.setImage(image);

            System.out.println("########################After recipe setimage##################################");
            imageRepository.save(image);
            recipeRepository.save(recipe);
            return new ResponseEntity<Image>(image, HttpStatus.CREATED);
        } else {
            throw new ImageAlreadyExistsException("The Image is already present!!!");
        }
    }


    public ResponseEntity<Image> deleteImage(UUID imageId, Recipe recipe) {
        if (recipe.getImage() != null) {
            if (recipe.getImage().getId().equals(imageId)) {
                String fileName = recipe.getImage().getUrl().split("/")[2];
                S3Object s3object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
                if (s3object != null) {
                    Image image = recipe.getImage();
                    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
                    recipe.setImage(null);
                    recipeRepository.save(recipe);
                    imageRepository.delete(image);
                    return new ResponseEntity<Image>(HttpStatus.NO_CONTENT);
                } else {
                    recipe.setImage(null);
                    throw new ImageNotFoundException("Image not found!!!");
                }

            } else {
                throw new ImageNotFoundException("Wrong image ID for the recipe!!!");
            }

        } else {
            throw new ImageNotFoundException("There is no image found!!!");
        }
    }
}
