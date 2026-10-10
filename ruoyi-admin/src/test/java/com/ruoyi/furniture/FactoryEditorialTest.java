package com.ruoyi.furniture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FactoryEditorialTest {
  private final ObjectMapper json = new ObjectMapper();
  private JdbcTemplate db;
  private CatalogService catalog;

  @BeforeEach void setup() {
    var ds = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE", "sa", "");
    var init = new ResourceDatabasePopulator();
    for (String file : List.of("dev-schema", "dev-data", "zh-upgrade", "planning-upgrade", "factory-editorial"))
      init.addScript(new ClassPathResource("db/" + file + ".sql"));
    init.execute(ds);
    db = new JdbcTemplate(ds);
    catalog = new CatalogService(db);
  }
  private Models.Content content(String key) {
    return new Models.Content(key, "", db.queryForObject("select en from sf_content where content_key=?", String.class, key), db.queryForObject("select zh from sf_content where content_key=?", String.class, key));
  }
  private Models.Content changed(String key, ArrayNode nodes) {
    return new Models.Content(key, "", nodes.toString(), nodes.toString());
  }
  @Test void seedIsValidAndPreservesEditedArticles() throws Exception {
    var capabilities = content("factory_capabilities");
    WebsiteContent.validate(capabilities);
    assertThat(json.readTree(capabilities.en())).hasSize(4);
    var news = content("factory_news");
    WebsiteContent.validate(news);
    ArrayNode items = (ArrayNode) json.readTree(news.en());
    assertThat(items).hasSize(3);
    assertThat(items.get(0).path("bodyZh").asText()).contains("\n\n");
    ((ObjectNode) items.get(0)).put("titleZh", "后台自定义标题");
    catalog.saveContent(changed("factory_news", items));
    new ResourceDatabasePopulator(new ClassPathResource("db/factory-editorial.sql")).execute(db.getDataSource());
    assertThat(content("factory_news").en()).contains("后台自定义标题");
  }
  @Test void coverRemovalAndDraftFilteringRemainCompatible() throws Exception {
    ArrayNode nodes = (ArrayNode) json.readTree(content("factory_news").en());
    ((ObjectNode) nodes.get(0)).put("published", false);
    ((ObjectNode) nodes.get(1)).remove(List.of("imageUrl", "altEn", "altZh"));
    ((ObjectNode) nodes.get(2)).put("imageUrl", "");
    catalog.saveContent(changed("factory_news", nodes));
    var published = json.readTree(WebsiteContent.publicCopy(content("factory_news")).en());
    assertThat(published).hasSize(2);
    assertThat(published.get(0).has("imageUrl")).isFalse();
    assertThat(published.get(1).path("imageUrl").asText()).isEmpty();
  }
  @Test void validatesUploadedPathsAndBilingualCaptions() throws Exception {
    for (String key : List.of("factory_news", "factory_capabilities")) {
      ArrayNode nodes = (ArrayNode) json.readTree(content(key).en());
      ObjectNode first = (ObjectNode) nodes.get(0);
      first.put("imageUrl", "/profile/catalog/upload.png");
      assertThatCode(() -> WebsiteContent.validate(changed(key, nodes))).doesNotThrowAnyException();
      first.put("imageUrl", "/profile/../secret.png");
      assertThatThrownBy(() -> WebsiteContent.validate(changed(key, nodes))).isInstanceOf(ResponseStatusException.class);
      first.put("imageUrl", "/images/xj-material-v2.jpg");
      first.put("altZh", "");
      assertThatThrownBy(() -> WebsiteContent.validate(changed(key, nodes))).isInstanceOf(ResponseStatusException.class);
    }
  }
}
