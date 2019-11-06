package com.cloud.ccwebapp.recipe.controller;

import com.amazonaws.services.cloudwatch.model.Metric;
import com.cloud.ccwebapp.recipe.exception.CustomizedResponseEntityExceptionHandler;
import com.cloud.ccwebapp.recipe.exception.UserAlreadyPresentException;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import com.cloud.ccwebapp.recipe.service.UserService;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import com.timgroup.statsd.StatsDClient;

import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/v1/user")
@Validated(CustomizedResponseEntityExceptionHandler.class)
public class UserController {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(UserController.class.getName());
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    StatsDClient statsDClient;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> addUser(@RequestBody User user) throws Exception {
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.http.post");
        LOGGER.info("Adding user in user_table....");
        // check if user is present
        ResponseEntity<User> responseEntity;
        if (user.getId() == null && user.getAccount_created() == null && user.getAccount_updated() == null) {
            User user1 = userService.saveUser(user);
            responseEntity = new ResponseEntity<User>(user1, HttpStatus.CREATED);

        } else {
            LOGGER.error("Given user already present in the db");
            throw new UserAlreadyPresentException("Given user already present in the db");
        }
        LOGGER.info("user has been created!!!");
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.post",result);
        return responseEntity;
    }

    @GetMapping("/self")
    public User getUser(Authentication authentication) {
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.http.get");
        LOGGER.info("Fetching user in");
        Object object = userRepository.findUserByEmailaddress(authentication.getName()).get();
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.get",result);
        return (User) object;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/self")
    public ResponseEntity<User> updateUser(@RequestBody User user, Authentication authentication) throws Exception {
        long start = System.currentTimeMillis();
        statsDClient.incrementCounter("endpoint.user.http.put");
        LOGGER.info("Updating the user....");
        Object object = userService.updateUser(user, authentication, statsDClient);
        long end = System.currentTimeMillis();
        long result = end-start;
        statsDClient.recordExecutionTime("timer.user.put",result);
        return (ResponseEntity<User>) object;
    }

}
