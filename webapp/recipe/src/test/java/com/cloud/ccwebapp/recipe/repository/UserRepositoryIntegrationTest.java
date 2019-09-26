package com.cloud.ccwebapp.recipe.repository;

import com.cloud.ccwebapp.recipe.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenFindByEmail_thenReturnUser() {
        // given
        User user = new User();
        user.setEmailaddress("test@test.com");
        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findUserByEmailaddress(user.getEmailaddress()).get();

        // then
        assertThat(found.getEmailaddress())
                .isEqualTo(user.getEmailaddress());
    }
}
