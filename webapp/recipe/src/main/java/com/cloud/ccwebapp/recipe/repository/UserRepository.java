package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    long start = System.currentTimeMillis();
    Optional<User> findUserByEmailaddress(String email);
}
