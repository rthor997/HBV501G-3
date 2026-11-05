package org.example.klifurapp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectsToH2Database() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(2));
            assertTrue(
                    connection.getMetaData()
                            .getURL()
                            .startsWith("jdbc:h2:mem:")
            );
        }
    }
}