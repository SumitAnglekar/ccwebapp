package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.NutritionalInformation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NutritionalInformationRepository extends CrudRepository<NutritionalInformation, UUID> {

}
