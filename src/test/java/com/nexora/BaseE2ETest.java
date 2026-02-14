package com.nexora;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseE2ETest {

  private static final MongoDBContainer mongo = new MongoDBContainer(
      "mongo:8");

  static {
    mongo.start();
  }

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    registry.add("spring.data.mongodb.database", () -> "nexora-test");
  }
}
