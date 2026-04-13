package com.classpulse;

import com.classpulse.seed.MockDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.main.lazy-initialization=true"
})
class ClassPulseApplicationTests {

    @MockBean
    private MockDataSeeder mockDataSeeder;

    @Test
    void contextLoads() {
    }

}
