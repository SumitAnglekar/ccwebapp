package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.controller.ImageController;
import com.cloud.ccwebapp.recipe.helper.ImageHelper;
import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.model.Recipe;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;

import java.io.File;

@Service
public class ImageService {

        @Autowired
        private ImageRepository imageRepository;
        @Autowired
        ImageHelper imageHelper;

        public ResponseEntity<Image> saveImage(Recipe recipe, Authentication authentication, File imageFile) {
                return null;
        }
}
