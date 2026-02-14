package com.nexora.shared.mcp;

import java.util.List;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ToolRegistration {

  private final List<ToolProvider> providers;

  @PostConstruct
  public void postConstruct() {
    log.info("Registered {} tool providers: {}", providers.size(),
        providers.stream().map(p -> p.getClass().getSimpleName()).toList());
  }

  @Bean
  public ToolCallbackProvider allTools() {
    return MethodToolCallbackProvider.builder()
        .toolObjects(providers.toArray())
        .build();
  }
}
