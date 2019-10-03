package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.exception.UserAlreadyPresentException;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/user")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class UserController {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> addUser(@RequestBody User user) throws Exception {

        // check if user is present
        ResponseEntity<User> responseEntity;
        if (user.getId() == null && user.getAccount_created() == null && user.getAccount_updated() == null) {

            User user1 = userService.saveUser(user);
            responseEntity = new ResponseEntity<User>(user1, HttpStatus.CREATED);

        } else {
            throw new UserAlreadyPresentException("Given user already present in the db");
        }
        return responseEntity;
    }

    @GetMapping("/self")
    public User getUser(Authentication authentication) {
        return userRepository.findUserByEmailaddress(authentication.getName()).get();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/self")
    public ResponseEntity<User> updateUser(@RequestBody User user, Authentication authentication) throws Exception {
        return userService.updateUser(user, authentication);
    }

}
