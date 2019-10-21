package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.Image;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends CrudRepository<Image, UUID> {
}
