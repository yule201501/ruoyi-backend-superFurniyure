package com.ruoyi.furniture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class LocalEnvironmentConfigTest {
  @TempDir Path directory;

  private StandardEnvironment load(String profile, Map<String, Object> overrides) throws Exception {
    Path dotenv = directory.resolve(".env");
    Files.writeString(dotenv, "MYSQL_HOST=127.0.0.1\nMYSQL_PUBLISHED_PORT=13306\n"
        + "MYSQL_CONTAINER_PORT=3306\nMYSQL_DATABASE=config_test\nMYSQL_USER=config_user\n"
        + "MYSQL_PASSWORD=test-database-password\nREDIS_PORT=16379\n"
        + "TOKEN_SECRET=test-signing-secret-with-enough-length\n");
    StandardEnvironment env = new StandardEnvironment();
    env.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
    env.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
    Map<String, Object> settings = new HashMap<>();
    settings.put("spring.config.location", "classpath:/application.yml");
    settings.put("spring.profiles.active", profile);
    settings.put("ENV_FILE", dotenv.toAbsolutePath().toString().replace('\\', '/'));
    settings.putAll(overrides);
    env.getPropertySources().addFirst(new MapPropertySource("test", settings));
    ConfigDataEnvironmentPostProcessor.applyTo(env);
    return env;
  }

  @Test void localLoadsDruidAndExternalDotenv() throws Exception {
    var env = load("local", Map.of());
    assertThat(env.getActiveProfiles()).contains("local", "druid");
    assertThat(env.getRequiredProperty("spring.datasource.druid.initialSize")).isEqualTo("2");
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.url"))
        .startsWith("jdbc:mysql://127.0.0.1:13306/config_test?");
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.username")).isEqualTo("config_user");
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.password")).isEqualTo("test-database-password");
    assertThat(env.getRequiredProperty("spring.data.redis.port")).isEqualTo("16379");
    assertThat(env.getRequiredProperty("token.secret")).isEqualTo("test-signing-secret-with-enough-length");
  }

  @Test void existingDbVariablesTakePrecedence() throws Exception {
    var env = load("druid", Map.of("DB_URL", "jdbc:mysql://db:3306/production",
        "DB_USERNAME", "production_user", "DB_PASSWORD", "production-test-password"));
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.url")).isEqualTo("jdbc:mysql://db:3306/production");
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.username")).isEqualTo("production_user");
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.password")).isEqualTo("production-test-password");
  }

  @Test void devRemainsAnIsolatedInMemoryDemo() throws Exception {
    var env = load("dev", Map.of());
    assertThat(env.getActiveProfiles()).doesNotContain("druid");
    assertThat(env.getRequiredProperty("spring.datasource.druid.master.url")).startsWith("jdbc:h2:mem:");
  }
}
