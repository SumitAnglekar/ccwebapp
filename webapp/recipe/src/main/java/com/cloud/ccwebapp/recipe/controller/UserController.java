package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(method= RequestMethod.POST, value="/user")
    public User addUser(@RequestBody User user, HttpServletResponse response) {
        // check if user is present
        try {
            User user1 = userService.saveUser(user);
            return user;
        } catch(Exception e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
    }

    @GetMapping("/user/self")
    public User getUser(Authentication authentication ) {
        return userRepository.findUserByEmailaddress(authentication.getName()).get();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/self")
    public ResponseEntity<User> updateUser(@RequestBody User user, Authentication authentication) {
        return userService.updateUser(user, authentication);
    }

}
