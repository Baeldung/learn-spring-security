package com.baeldung.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.baeldung.lss.spring.LssApp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { LssApp.class }, properties = {
    "spring.datasource.url=jdbc:hsqldb:mem:lssc-acl-data-structure",
    "spring.datasource.driver-class-name=org.hsqldb.jdbcDriver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.sql.init.platform=hsqldb",
    "spring.jpa.hibernate.ddl-auto=none"
})
public class LssIntegrationTest {

    @Test
    public void whenLoadApplication_thenSuccess() {

    }
}
