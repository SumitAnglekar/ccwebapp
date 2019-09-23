package com.cloud.ccwebapp.recipe.controller;

import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;


@RestController
@RequestMapping("/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(method= RequestMethod.POST, value="/user")
    public User addUser(@RequestBody User user, HttpServletResponse response) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @GetMapping("/user/self")
    public User getUser(Authentication authentication ) {
        return userRepository.findUserByEmailaddress(authentication.getName()).get();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/self")
    public User updateUser(@RequestBody User user, Authentication authentication, HttpServletResponse response) {
        // check if user is updating his own record
        if (!user.getEmailaddress().equals(authentication.getName())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return user;
        }

        // check if user is present
        Optional dbRecord = userRepository.findUserByEmailaddress(user.getEmailaddress());
        if (!dbRecord.isPresent()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return user;
        }
        User dbUser = (User) dbRecord.get();

        // check if user is unmodified

        if (dbUser.equals(user)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return user;
        }

        // check if valid fields are updated
        if (dbUser.getFirst_name().equals(user.getFirst_name())
                || dbUser.getLast_name().equals(user.getLast_name())
                || dbUser.getPassword().equals(user.getPassword())) {
            // ok to update

            // is password strong?

            dbUser.setFirst_name(user.getFirst_name());
            dbUser.setLast_name(user.getLast_name());
            dbUser.setPassword(user.getPassword());
            user = userRepository.save(dbUser);
            response.setStatus(HttpServletResponse.SC_OK);
            return user;
        }

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return user;
    }

}
