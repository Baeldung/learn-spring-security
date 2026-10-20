package com.baeldung.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import jakarta.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.baeldung.lss.model.Possession;
import com.baeldung.lss.model.User;
import com.baeldung.lss.spring.LssApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { LssApp.class }, properties = {
    "spring.datasource.url=jdbc:h2:mem:lssc-acl-data-structure;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.platform=h2",
    "spring.jpa.hibernate.ddl-auto=none"
})
public class LssIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void whenLoadApplication_thenLessonAclDataIsAvailableInMemory() {
        assertTrue(jdbcTemplate.<String>execute((java.sql.Connection connection) ->
            connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:"));

        assertEquals(Arrays.asList(
            "Eugen Possession|eugen@email.com|eugen@email.com|16|true",
            "Common Possession|eugen@email.com|eugen@email.com|16|true",
            "Common Possession|eugen@email.com|eric@email.com|1|true",
            "Eric Possession|eric@email.com|eric@email.com|16|true"
        ), jdbcTemplate.query(
            "select p.name, u.email, sid.sid, ace.mask, ace.granting "
                + "from acl_entry ace "
                + "join acl_object_identity object_identity on object_identity.id = ace.acl_object_identity "
                + "join acl_class object_class on object_class.id = object_identity.object_id_class "
                + "join possession p on p.id = object_identity.object_id_identity "
                + "join users u on u.id = p.owner_id "
                + "join acl_sid owner on owner.id = object_identity.owner_sid and owner.sid = u.email "
                + "join acl_sid sid on sid.id = ace.sid "
                + "where object_class.class = 'com.baeldung.lss.model.Possession' "
                + "order by p.id, ace.ace_order",
            (row, index) -> row.getString(1) + "|" + row.getString(2) + "|"
                + row.getString(3) + "|" + row.getInt(4) + "|" + row.getBoolean(5)));
    }

    @Test
    @Transactional
    public void whenCreateDomainRecords_thenGeneratedIdsFollowSeededRecords() {
        long highestUserId = jdbcTemplate.queryForObject("select max(id) from users", Long.class);
        long highestPossessionId = jdbcTemplate.queryForObject("select max(id) from possession", Long.class);

        User user = new User();
        user.setEmail("fixture@email.com");
        user.setPassword("fixture-password");
        entityManager.persist(user);

        Possession possession = new Possession();
        possession.setName("Fixture Possession");
        possession.setOwner(user);
        entityManager.persist(possession);
        entityManager.flush();
        entityManager.clear();

        assertTrue(user.getId() > highestUserId);
        assertTrue(possession.getId() > highestPossessionId);
        assertEquals("fixture@email.com", entityManager.find(Possession.class, possession.getId()).getOwner().getEmail());
        assertEquals("eugen@email.com", entityManager.find(User.class, 1L).getEmail());
        assertEquals("Eric Possession", entityManager.find(Possession.class, 3L).getName());
    }
}
