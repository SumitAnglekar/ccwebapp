package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.exception.UserAlreadyPresentException;
import com.cloud.ccwebapp.recipe.helper.UserHelper;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.timgroup.statsd.StatsDClient;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(UserService.class.getName());
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    StatsDClient statsDClient;

    public ResponseEntity<User> updateUser(User user, Authentication auth) throws Exception {

        // check if user is valid
        userHelper.isUserValid(user);

        // check if user is present
        Optional dbRecord = userRepository.findUserByEmailaddress(auth.getName());
        if (dbRecord.isPresent()) {
            User dbUser = (User) dbRecord.get();

            if(!user.getEmailaddress().equals(auth.getName())){
                LOGGER.error("User is not allowed to update other user's information!!!");
                throw new InvalidInputException("Email Address does not match the auth!!");
            }
            // check if valid fields are updated
            if (!dbUser.getFirst_name().equals(user.getFirst_name())
                    || !dbUser.getLast_name().equals(user.getLast_name())
                    || ((user.getPassword() != null && !user.getPassword().isEmpty()) &&
                    !passwordEncoder.matches(user.getPassword(), dbUser.getPassword()))
                    || !dbUser.getPassword().equals(user.getPassword())) {

                // ok to save
                dbUser.setFirst_name(user.getFirst_name());
                dbUser.setLast_name(user.getLast_name());
                dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
                // save
                LOGGER.info("Updated user information successfully");
                long start = System.currentTimeMillis();
                userRepository.save(dbUser);
                statsDClient.time("dbquery.update.user", (System.currentTimeMillis() - start));

                return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
            }
        }
        LOGGER.error("Bad request while updating the user information!!!");
        // return bad request
        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }

    public User saveUser(User user) throws Exception {
        // check if user is valid
        userHelper.isUserValid(user);
        Optional<User> dbRecord = userRepository.findUserByEmailaddress(user.getEmailaddress());
        if (dbRecord.isPresent()) {
            LOGGER.error("User with username " + user.getEmailaddress() + " is already present");
            throw new UserAlreadyPresentException("User already present!!");
        } else {
            if (isPasswordStrong(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                LOGGER.info("Saving user with username " + user.getEmailaddress());
                long start = System.currentTimeMillis();
                User savedUser = userRepository.save(user);
                statsDClient.time("dbquery.save.user", (System.currentTimeMillis() - start));
                return savedUser;
            } else {
                LOGGER.error("Password provided is not a strong password");
                throw new Exception("Password not valid!!");
            }
        }
    }


    /**
     * Password must satisfy following constraints:
     * - must contain a digit
     * - must contain a lower case letter
     * - must contain an upper case letter
     * - must contain a special character (!@#$%^&+=)
     * - must be at least 8 characters in length
     * - must not contain any whitespace characters
     *
     * @param password
     * @return boolean
     */
    public boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$");
    }


}
