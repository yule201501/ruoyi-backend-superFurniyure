package com.ruoyi.furniture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validation;
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

class ProductGalleryTest {
  private final ObjectMapper json = new ObjectMapper();
  private CatalogService catalog;
  private JdbcTemplate db;

  @BeforeEach void database() {
    var ds = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE", "sa", "");
    var init = new ResourceDatabasePopulator();
    for (String file : List.of("dev-schema", "dev-data", "zh-upgrade", "planning-upgrade", "planning-demo", "visual-refresh", "product-gallery"))
      init.addScript(new ClassPathResource("db/" + file + ".sql"));
    init.execute(ds);
    db = new JdbcTemplate(ds);
    catalog = new CatalogService(db);
  }

  private Models.Product product(List<String> images) throws Exception {
    ObjectNode node = json.valueToTree(catalog.list(true).get(0));
    node.set("supportingImages", json.valueToTree(images));
    return json.treeToValue(node, Models.Product.class);
  }

  @Test void migratedScenesRoundTripOrderAndRemoval() throws Exception {
    assertThat(catalog.list(true)).allSatisfy(p -> {
      assertThat(p.supportingImages()).hasSize(2).doesNotContain(p.imageUrl());
    });
    var changed = product(List.of("/profile/catalog/uploaded.png", "/images/xj-wardrobe-3.jpg"));
    catalog.save(changed, changed.id());
    assertThat(catalog.publicProduct(changed.slug()).supportingImages()).containsExactlyElementsOf(changed.supportingImages());
    var cleared = product(List.of());
    catalog.save(cleared, cleared.id());
    // Rerunning the seed must not resurrect images explicitly removed in the admin.
    new ResourceDatabasePopulator(new ClassPathResource("db/product-gallery.sql")).execute(db.getDataSource());
    assertThat(catalog.get(cleared.id()).supportingImages()).isEmpty();
  }

  @Test void newProductPersistsGalleryAndLegacyInputIsAccepted() throws Exception {
    ObjectNode node = json.valueToTree(product(List.of("/images/xj-kitchen-2.jpg")));
    node.putNull("id"); node.put("slug", "gallery-new-product");
    catalog.save(json.treeToValue(node, Models.Product.class), null);
    assertThat(catalog.publicProduct("gallery-new-product").supportingImages()).containsExactly("/images/xj-kitchen-2.jpg");
    node.put("slug", "legacy-new-product"); node.remove("supportingImages");
    catalog.save(json.treeToValue(node, Models.Product.class), null);
    assertThat(catalog.publicProduct("legacy-new-product").supportingImages()).isEmpty();
  }

  @Test void rejectsTooManyBlankAndRemoteImages() throws Exception {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      for (var invalid : List.of(List.of("/images/a.jpg", "/images/b.jpg", "/images/c.jpg"), List.of(""), List.of("https://example.com/a.jpg")))
        assertThat(validator.validate(product(invalid))).anyMatch(v -> v.getPropertyPath().toString().startsWith("supportingImages"));
      assertThat(validator.validate(product(List.of("/images/a.jpg", "/profile/catalog/b.png"))))
          .noneMatch(v -> v.getPropertyPath().toString().startsWith("supportingImages"));
    }
    var traversal = product(List.of("/profile/../secret.png"));
    assertThatThrownBy(() -> catalog.save(traversal, traversal.id())).isInstanceOf(ResponseStatusException.class);
  }
}
