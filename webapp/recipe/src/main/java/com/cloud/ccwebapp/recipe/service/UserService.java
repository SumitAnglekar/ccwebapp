package com.cloud.ccwebapp.recipe.service;

import com.cloud.ccwebapp.recipe.model.User;
import com.cloud.ccwebapp.recipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public ResponseEntity<User> updateUser(User user, Authentication auth) {
        // check if user is updating his own record
        if (user.getEmailaddress() != null
                && !user.getEmailaddress().equals(auth.getName())) {
            return new ResponseEntity<User>(HttpStatus.UNAUTHORIZED);
        }

        // check if user has updated other fields
        if (user.getEmailaddress() != null
                || user.getAccount_created() != null
                || user.getAccount_updated() != null
                /*|| user.getId() != null*/) {
            return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
        }

        // check if user is present
        Optional dbRecord = userRepository.findUserByEmailaddress(auth.getName());
        if (dbRecord.isPresent()) {
            User dbUser = (User) dbRecord.get();

            // check if valid fields are updated
            if (!dbUser.getFirst_name().equals(user.getFirst_name())
                    || !dbUser.getLast_name().equals(user.getLast_name())
                    || !dbUser.getPassword().equals(user.getPassword())) {
                if (!dbUser.getPassword().equals(user.getPassword())) {
                    // check if password is strong
                }

                // ok to save
                if (user.getFirst_name() != null) {
                    dbUser.setFirst_name(user.getFirst_name());
                }
                if (user.getLast_name() != null) {
                    dbUser.setLast_name(user.getLast_name());
                }
                if (user.getPassword() != null) {
                    dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
                }

                // save
                userRepository.save(dbUser);
                return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
            }
        }

        // return bad request
        return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
    }
}
