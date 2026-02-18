package com.easymoney;

import com.easymoney.support.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MySqlTestContainerConfig.class)
class EasymoneyApplicationTests {

    @Test
    void contextLoads() {
    }

}
