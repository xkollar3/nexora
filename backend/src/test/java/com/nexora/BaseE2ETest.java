package com.nexora;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseE2ETest {

  private static final MongoDBContainer mongo = new MongoDBContainer(
      "mongo:8");

  static {
    mongo.start();
  }

  @Autowired
  private MongoTemplate mongoTemplate;

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    registry.add("spring.data.mongodb.database", () -> "nexora-test");
  }

  @BeforeEach
  void cleanDatabase() {
    for (String name : mongoTemplate.getCollectionNames()) {
      mongoTemplate.dropCollection(name);
    }
  }
}
