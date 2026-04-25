package dev.gukin.einvestlab;

import dev.gukin.einvestlab.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MySqlTestContainerConfig.class)
class EInvestLabApplicationTests {

    @Test
    void contextLoads() {
    }

}
