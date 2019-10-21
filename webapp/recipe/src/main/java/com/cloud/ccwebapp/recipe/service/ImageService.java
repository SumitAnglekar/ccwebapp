package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.model.Image;
import com.cloud.ccwebapp.recipe.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

        @Autowired
        private ImageRepository imageRepository;

        public ResponseEntity<Image> saveImage(Image image, Authentication authentication) {
                return null;
        }
}
