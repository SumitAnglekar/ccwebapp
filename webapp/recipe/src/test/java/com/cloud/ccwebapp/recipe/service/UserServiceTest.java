package com.cloud.ccwebapp.recipe.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class UserServiceTest {

  private UserService userService = new UserService();

  @Test
  public void testPasswordStrength() {

    // Test weak password
    final String weakDecodedPassword = "123456";
    assertThat(userService.isPasswordStrong(weakDecodedPassword))
        .isFalse();

    // Test strong password
    final String strongDecodedPassword = "Cloud@123";
    assertThat(userService.isPasswordStrong(strongDecodedPassword))
        .isTrue();
  }

}
