package com.cloud.ccwebapp.recipe.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.cloud.ccwebapp.recipe.controller.RecipeController;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.UUID;

import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.Logger;
import com.timgroup.statsd.StatsDClient;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(ImageService.class.getName());
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
    @Autowired
    StatsDClient statsDClient;

    public ResponseEntity<Image> getImage(UUID imageId, Recipe recipe) {
        if (recipe.getImage() != null) {
            if (recipe.getImage().getId().equals(imageId)) {
                String fileName = recipe.getImage().getUrl().split("/")[2];
                statsDClient.time("dbquery.get.image", (System.currentTimeMillis() - start));
                long start = System.currentTimeMillis();
                S3Object s3object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
                long end = System.currentTimeMillis();
                long result = end-start;
                statsDClient.recordExecutionTime("s3.get.image",result);
                if (s3object != null) {
                    LOGGER.info("Image created...");
                    return new ResponseEntity<Image>(recipe.getImage(), HttpStatus.CREATED);
                } else {
                    recipe.setImage(null);
                    LOGGER.error("Image with imageId "+imageId+" not found!!!");
                    throw new ImageNotFoundException("Image not found!!!");
                }

            } else {
                LOGGER.error("Wrong image ID for the recipe!!!");
                throw new ImageNotFoundException("Wrong image ID for the recipe!!!");
            }

        } else {
            LOGGER.error("No image found!!!");
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
            long start1 = System.currentTimeMillis();
            amazonS3.putObject(putObjectRequest);
            long end1 = System.currentTimeMillis();
            long result1 = end1-start1;
            statsDClient.recordExecutionTime("s3.save.image",result1);
            String fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            System.out.println("******************************file URL *****************************" +fileUrl);
            Image image = new Image();
            image.setUrl(fileUrl);
            image.setMd5(meta.getContentMD5());
            image.setContentLength(meta.getContentLength());
            recipe.setImage(image);

            System.out.println("########################After recipe setimage##################################");
            long start = System.currentTimeMillis();
            imageRepository.save(image);
            recipeRepository.save(recipe);
            statsDClient.time("dbquery.save.image", (System.currentTimeMillis() - start));
            LOGGER.info("Image created!!!");
            return new ResponseEntity<Image>(image, HttpStatus.CREATED);
        } else {
            LOGGER.error("The image is already present....");
            throw new ImageAlreadyExistsException("The Image is already present!!!");
        }
    }


    public ResponseEntity<Image> deleteImage(UUID imageId, Recipe recipe) {
        long start1 = System.currentTimeMillis();
        if (recipe.getImage() != null) {
            if (recipe.getImage().getId().equals(imageId)) {
                String fileName = recipe.getImage().getUrl().split("/")[2];
                S3Object s3object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
                if (s3object != null) {
                    Image image = recipe.getImage();
                    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
                    long end1 = System.currentTimeMillis();
                    long result1 = end1-start1;
                    statsDClient.recordExecutionTime("s3.delete.image",result1);
                    recipe.setImage(null);
                    long start = System.currentTimeMillis();
                    recipeRepository.save(recipe);
                    imageRepository.delete(image);
                    statsDClient.time("dbquery.delete.image", (System.currentTimeMillis() - start));
                    LOGGER.info("Deleting image with imageId "+imageId);
                    return new ResponseEntity<Image>(HttpStatus.NO_CONTENT);
                } else {
                    LOGGER.error("Image not found with imageId "+imageId);
                    recipe.setImage(null);
                    throw new ImageNotFoundException("Image not found!!!");
                }

            } else {
                LOGGER.error("Wrong image ID for the recipe!!!");
                throw new ImageNotFoundException("Wrong image ID for the recipe!!!");
            }

        } else {
            LOGGER.error("There is no image found with imageId "+imageId);
            throw new ImageNotFoundException("There is no image found!!!");
        }
    }
}
