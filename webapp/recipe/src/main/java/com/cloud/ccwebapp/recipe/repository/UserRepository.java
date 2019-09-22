package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public interface UserRepository extends CrudRepository<User, Integer> {

}
