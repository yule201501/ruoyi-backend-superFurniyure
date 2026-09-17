package com.ruoyi.furniture;

import com.ruoyi.furniture.Models.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private final JdbcTemplate db;

  public CatalogService(JdbcTemplate db) {
    this.db = db;
  }

  private static final RowMapper<Product> MAP =
      (r, n) ->
          new Product(
              r.getLong("id"),
              r.getString("slug"),
              r.getString("category"),
              r.getString("name_ru"),
              r.getString("name_en"),
              r.getString("name_zh"),
              r.getString("description_ru"),
              r.getString("description_en"),
              r.getString("description_zh"),
              r.getString("specs_ru"),
              r.getString("specs_en"),
              r.getString("specs_zh"),
              r.getString("image_url"),
              r.getString("model"),
              r.getBoolean("published"),
              r.getInt("sort_order"));

  public List<Product> list(boolean publicOnly) {
    return db.query(
        "select * from sf_product"
            + (publicOnly ? " where published=true" : "")
            + " order by sort_order,id",
        MAP);
  }

  public Product publicProduct(String slug) {
    return db.query("select * from sf_product where slug=? and published=true", MAP, slug).stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  public Product get(long id) {
    return db.query("select * from sf_product where id=?", MAP, id).stream()
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @Transactional
  public void save(Product p, Long id) {
    if (p.imageUrl().contains(".."))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path");
    Object[] values = {
      p.slug(),
      p.category(),
      p.nameRu(),
      p.nameEn(),
      p.nameZh(),
      p.descriptionRu(),
      p.descriptionEn(),
      p.descriptionZh(),
      p.specsRu(),
      p.specsEn(),
      p.specsZh(),
      p.imageUrl(),
      p.model(),
      p.published(),
      p.sortOrder()
    };
    if (id == null)
      db.update(
          "insert into"
              + " sf_product(slug,category,name_ru,name_en,name_zh,description_ru,description_en,description_zh,specs_ru,specs_en,specs_zh,image_url,model,published,sort_order)"
              + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
          values);
    else {
      List<Object> args = new ArrayList<>(Arrays.asList(values));
      args.add(id);
      if (db.update(
              "update sf_product set"
                  + " slug=?,category=?,name_ru=?,name_en=?,name_zh=?,description_ru=?,description_en=?,description_zh=?,specs_ru=?,specs_en=?,specs_zh=?,image_url=?,model=?,published=?,sort_order=?,updated_at=CURRENT_TIMESTAMP"
                  + " where id=?",
              args.toArray())
          != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  public void delete(long id) {
    if (db.update("delete from sf_product where id=?", id) != 1)
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }

  public List<Content> content() {
    return db.query(
        "select * from sf_content order by content_key",
        (r, n) ->
            new Content(
                r.getString("content_key"),
                r.getString("ru"),
                r.getString("en"),
                r.getString("zh")));
  }

  public void saveContent(Content c) {
    if (db.update(
            "update sf_content set ru=?,en=?,zh=? where content_key=?",
            c.ru(),
            c.en(),
            c.zh(),
            c.key())
        != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }
}
