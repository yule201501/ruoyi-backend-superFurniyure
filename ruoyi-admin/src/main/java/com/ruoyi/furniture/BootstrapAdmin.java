package com.ruoyi.furniture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdmin implements ApplicationRunner {
  private final JdbcTemplate db;
  private final PasswordEncoder encoder;
  private final String initial;

  public BootstrapAdmin(
      JdbcTemplate db,
      PasswordEncoder encoder,
      @Value("${furniture.bootstrap-password}") String initial) {
    this.db = db;
    this.encoder = encoder;
    this.initial = initial;
  }

  @Override
  public void run(ApplicationArguments args) {
    String saved = db.queryForObject("select password from sys_user where user_id=1", String.class);
    if ("!SET_ON_FIRST_START!".equals(saved)) {
      if (initial.length() < 14 || initial.length() > 20)
        throw new IllegalStateException(
            "Set ADMIN_INITIAL_PASSWORD to a unique password of 14 to 20 characters");
      db.update(
          "update sys_user set password=?,pwd_update_date=CURRENT_TIMESTAMP where user_id=1 and"
              + " password='!SET_ON_FIRST_START!'",
          encoder.encode(initial));
    }
  }
}
