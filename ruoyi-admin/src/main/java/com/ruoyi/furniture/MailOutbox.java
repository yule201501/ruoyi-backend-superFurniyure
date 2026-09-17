package com.ruoyi.furniture;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.*;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "furniture.mail-enabled", havingValue = "true")
public class MailOutbox {
  private static final Logger log = LoggerFactory.getLogger(MailOutbox.class);
  private final JdbcTemplate db;
  private final JavaMailSender mail;
  private final String from, to, url;

  public MailOutbox(
      JdbcTemplate db,
      JavaMailSender mail,
      @Value("${furniture.mail-from}") String from,
      @Value("${furniture.mail-to}") String to,
      @Value("${furniture.admin-url}") String url) {
    this.db = db;
    this.mail = mail;
    this.from = from;
    this.to = to;
    this.url = url;
    if (from.isBlank() || to.isBlank())
      throw new IllegalArgumentException("MAIL_FROM and MAIL_TO are required when mail is enabled");
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 15000)
  public void deliver() {
    db.update(
        "update sf_mail_outbox set status='PENDING' where status='SENDING' and updated_at<?",
        java.sql.Timestamp.valueOf(
            java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(5)));
    for (Map<String, Object> r :
        db.queryForList(
            "select o.id,o.attempts,i.receipt from sf_mail_outbox o join sf_inquiry i on"
                + " i.id=o.inquiry_id where o.status='PENDING' and"
                + " o.next_attempt_at<=CURRENT_TIMESTAMP order by o.id limit 10")) {
      long id = ((Number) r.get("id")).longValue();
      int attempt = ((Number) r.get("attempts")).intValue() + 1;
      if (db.update(
              "update sf_mail_outbox set status='SENDING',updated_at=CURRENT_TIMESTAMP where id=?"
                  + " and status='PENDING'",
              id)
          != 1) continue;
      try {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to.split(","));
        m.setSubject("superFurniyure inquiry " + r.get("receipt"));
        m.setText(
            "A new inquiry has been saved. Sign in to review and respond:\n"
                + url
                + "\nReference: "
                + r.get("receipt"));
        mail.send(m);
        db.update(
            "update sf_mail_outbox set status='SENT',attempts=?,updated_at=CURRENT_TIMESTAMP where"
                + " id=?",
            attempt,
            id);
      } catch (Exception e) {
        db.update(
            "update sf_mail_outbox set"
                + " status=?,attempts=?,next_attempt_at=?,updated_at=CURRENT_TIMESTAMP where id=?",
            attempt >= 8 ? "FAILED" : "PENDING",
            attempt,
            java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
                    .plusMinutes(Math.min(240, 1L << attempt))),
            id);
        log.warn("Inquiry notification {} failed; attempt {}. Review SMTP settings.", id, attempt);
      }
    }
  }
}
