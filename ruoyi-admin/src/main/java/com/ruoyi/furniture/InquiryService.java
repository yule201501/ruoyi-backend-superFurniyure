package com.ruoyi.furniture;

import com.ruoyi.furniture.Models.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InquiryService {
  private final JdbcTemplate db;
  private final StringRedisTemplate redis;
  private final String salt;
  private final DefaultRedisScript<Long> limiter =
      new DefaultRedisScript<>(
          "local v=redis.call('incr',KEYS[1]);if v==1 then redis.call('expire',KEYS[1],ARGV[1])"
              + " end;return v",
          Long.class);

  public InquiryService(
      JdbcTemplate db, StringRedisTemplate redis, @Value("${token.secret}") String salt) {
    this.db = db;
    this.redis = redis;
    this.salt = salt;
  }

  public String hash(String s) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest((salt + ":" + s).getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void limit(String key, int max) {
    Long n = redis.execute(limiter, List.of("sf:rate:" + hash(key)), "600");
    if (n == null || n > max)
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS, "Please wait before sending another message");
  }

  @Transactional
  public String submit(InquiryInput in, String address) {
    if (in.website() != null && !in.website().isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid submission");
    String fingerprint = hash(in.toString());
    List<Map<String, Object>> previous =
        db.queryForList(
            "select receipt,payload_hash from sf_inquiry where request_id=?",
            in.requestId().toString());
    if (!previous.isEmpty()) {
      if (!fingerprint.equals(previous.get(0).get("payload_hash")))
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Submission identifier already used");
      return (String) previous.get(0).get("receipt");
    }
    limit("ip:" + address, 10);
    limit("email:" + in.email().trim().toLowerCase(Locale.ROOT), 5);
    String receipt = "XJ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    db.update(
        "insert into"
            + " sf_inquiry(request_id,payload_hash,receipt,name,email,company,country,city,contact,product_slug,message,locale,privacy_consent,publish_consent,ip_hash)"
            + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        in.requestId().toString(),
        fingerprint,
        receipt,
        in.name().trim(),
        in.email().trim(),
        in.company(),
        in.country().trim(),
        in.city().trim(),
        in.contact(),
        in.productSlug(),
        in.message().trim(),
        in.locale(),
        true,
        in.publishConsent(),
        hash(address));
    db.update(
        "insert into sf_mail_outbox(inquiry_id,status,attempts,next_attempt_at) select"
            + " id,'PENDING',0,CURRENT_TIMESTAMP from sf_inquiry where request_id=?",
        in.requestId().toString());
    return receipt;
  }

  public Map<String, Object> list(String status, String keyword, int page, int size) {
    page = Math.max(1, page);
    size = Math.min(50, Math.max(1, size));
    String where = " where 1=1";
    List<Object> args = new ArrayList<>();
    if (status != null && !status.isBlank()) {
      where += " and moderation_status=?";
      args.add(status);
    }
    if (keyword != null && !keyword.isBlank()) {
      where += " and (lower(name) like ? or lower(email) like ? or lower(receipt) like ?)";
      String q = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
      args.add(q);
      args.add(q);
      args.add(q);
    }
    Long total =
        db.queryForObject("select count(*) from sf_inquiry" + where, Long.class, args.toArray());
    args.add(size);
    args.add((page - 1) * size);
    return Map.of(
        "rows",
        db.queryForList(
            "select"
                + " id,receipt,name,email,company,country,city,product_slug,moderation_status,followup_status,publish_consent,created_at"
                + " from sf_inquiry"
                + where
                + " order by id desc limit ? offset ?",
            args.toArray()),
        "total",
        total);
  }

  public Map<String, Object> detail(long id) {
    return db
        .queryForList(
            "select"
                + " id,receipt,name,email,company,country,city,contact,product_slug,message,locale,publish_consent,moderation_status,followup_status,public_name,public_text,reply,internal_note,version,created_at,updated_at"
                + " from sf_inquiry where id=?",
            id)
        .stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @Transactional
  public void moderate(long id, Moderation m, String actor) {
    Map<String, Object> item = detail(id);
    if (m.moderationStatus().equals("APPROVED")) {
      if (!Boolean.TRUE.equals(item.get("publish_consent"))
          && !Integer.valueOf(1).equals(item.get("publish_consent")))
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Visitor did not consent to publication");
      if (m.publicName() == null
          || m.publicName().isBlank()
          || m.publicText() == null
          || m.publicText().isBlank())
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Public name and redacted public text are required");
    }
    int n =
        db.update(
            "update sf_inquiry set"
                + " moderation_status=?,followup_status=?,public_name=?,public_text=?,reply=?,internal_note=?,updated_by=?,version=version+1,updated_at=CURRENT_TIMESTAMP"
                + " where id=? and version=?",
            m.moderationStatus(),
            m.followupStatus(),
            m.publicName(),
            m.publicText(),
            m.reply(),
            m.internalNote(),
            actor,
            id,
            m.version());
    if (n != 1)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "This message was modified. Reload before saving.");
  }

  public void delete(long id) {
    if (db.update("delete from sf_inquiry where id=?", id) != 1)
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }

  public Map<String, Object> publicList(String locale, int page) {
    int safe = Math.max(1, page), size = 10;
    String where = " where moderation_status='APPROVED' and publish_consent=true and locale=?";
    List<PublicMessage> rows =
        db.query(
            "select id,public_name,public_text,reply,locale,created_at from sf_inquiry"
                + where
                + " order by id desc limit ? offset ?",
            (r, n) ->
                new PublicMessage(
                    r.getLong("id"),
                    r.getString("public_name"),
                    r.getString("public_text"),
                    r.getString("reply"),
                    r.getString("locale"),
                    r.getTimestamp("created_at").toLocalDateTime()),
            locale,
            size,
            (safe - 1) * size);
    return Map.of(
        "rows",
        rows,
        "total",
        db.queryForObject("select count(*) from sf_inquiry" + where, Long.class, locale));
  }
}
