package com.cloud.ccwebapp.recipe.helper;

import com.cloud.ccwebapp.recipe.exception.InvalidInputException;
import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserHelper {

    private static final Logger LOGGER = (Logger) LogManager.getLogger(UserHelper.class.getName());
    public final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    @Autowired
    private UserService userService;

    public void isUserValid(User user) {
        if (user.getFirst_name() == null
                || user.getLast_name() == null
                || user.getPassword() == null
                || user.getEmailaddress() == null) {
            LOGGER.error("Incomplete information ...");
            throw new InvalidInputException("Incomplete Information");
        }

        if (!user.getEmailaddress().matches(EMAIL_REGEX)) {
            LOGGER.error("Email address is not valid");
            throw new InvalidInputException("Email address not valid!");
        }

        if (!userService.isPasswordStrong(user.getPassword())) {
            LOGGER.error("Weak Password!");
            throw new InvalidInputException("Weak Password!");
        }

    }

}
