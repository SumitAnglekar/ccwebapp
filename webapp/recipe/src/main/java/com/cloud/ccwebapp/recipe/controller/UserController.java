package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(method= RequestMethod.POST, value="/user")
    public User addUser(@RequestBody User user, HttpServletResponse response) {
       return userRepository.save(user);
    }

    @GetMapping("/user/self")

    public User getUser() {
        return userRepository.findUserByEmailaddress("sumit@gmail.com").get();
    }

}
