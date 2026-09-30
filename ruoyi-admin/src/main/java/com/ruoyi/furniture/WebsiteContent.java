package com.ruoyi.furniture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Validates structured website content at the same permission boundary as editorial copy. */
public final class WebsiteContent {
  private static final Set<String> TEXT = Set.of("brand_story", "factory_intro", "factory_process", "contact_intro");
  private static final Set<String> LIST = Set.of("hero_slides", "factory_gallery", "factory_capabilities", "social_links", "factory_news", "catalog_banner");
  private WebsiteContent() {}
  private static void require(boolean valid) {
    if (!valid) throw new IllegalArgumentException("Invalid website content");
  }
  private static String field(JsonNode n, String key, int max) {
    JsonNode v = n.get(key);
    require(v != null && v.isTextual() && !v.asText().isBlank() && v.asText().length() <= max);
    return v.asText();
  }
  private static void image(JsonNode n) {
    String path = field(n, "imageUrl", 500);
    require(!path.contains("..") && path.matches("/images/[a-zA-Z0-9_.-]+|/profile/[a-zA-Z0-9_./-]+"));
  }
  public static void validate(Models.Content content) {
    if (TEXT.contains(content.key())) return;
    try {
      require(LIST.contains(content.key()) && content.en().equals(content.zh()));
      JsonNode rows = new ObjectMapper().readTree(content.en());
      require(rows.isArray() && rows.size() <= 20);
      if (content.key().equals("hero_slides")) require(rows.size() == 4);
      if (content.key().equals("catalog_banner")) require(rows.size() == 1);
      for (JsonNode n : rows) {
        require(n.isObject());
        switch (content.key()) {
          case "social_links" -> {
            field(n, "platform", 40); field(n, "account", 120);
            URI url = URI.create(field(n, "url", 500));
            require("https".equalsIgnoreCase(url.getScheme()) && url.getHost() != null && url.getUserInfo() == null);
          }
          case "factory_news" -> {
            LocalDate.parse(field(n, "date", 10));
            field(n, "titleEn", 160); field(n, "titleZh", 160);
            field(n, "bodyEn", 3000); field(n, "bodyZh", 3000);
            require(n.has("published") && n.get("published").isBoolean());
            // Older text-only articles remain editable. An added cover needs both captions.
            if (n.has("imageUrl") && !n.path("imageUrl").asText().isBlank()) {
              image(n); field(n, "altEn", 200); field(n, "altZh", 200);
            }
          }
          case "factory_capabilities" -> {
            image(n); field(n, "altEn", 200); field(n, "altZh", 200);
            field(n, "titleEn", 160); field(n, "titleZh", 160);
            field(n, "bodyEn", 3000); field(n, "bodyZh", 3000);
          }
          default -> { image(n); field(n, "altEn", 200); field(n, "altZh", 200); }
        }
      }
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check website content fields, image paths and HTTPS social URLs");
    }
  }

  public static Models.Content publicCopy(Models.Content c) {
    if (!c.key().equals("factory_news")) return c;
    try {
      ObjectMapper mapper = new ObjectMapper();
      var published = mapper.createArrayNode();
      for (JsonNode n : mapper.readTree(c.en())) if (n.path("published").asBoolean(false)) published.add(n);
      String json = mapper.writeValueAsString(published);
      return new Models.Content(c.key(), "", json, json);
    } catch (Exception e) { throw new IllegalStateException("Invalid stored factory news", e); }
  }
}
