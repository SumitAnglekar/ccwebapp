package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    public Optional<User> findUserByEmailaddress(String email);
}
