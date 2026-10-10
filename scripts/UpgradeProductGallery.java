import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Compile alongside UpgradePlanning.java. Defaults to a read-only preview; --apply backs up first. */
public class UpgradeProductGallery {
  public static void main(String[] args) throws Exception {
    Properties p = new Properties();
    try (var reader = Files.newBufferedReader(Path.of(".env"), StandardCharsets.UTF_8)) { p.load(reader); }
    String host = UpgradePlanning.setting(p, "MYSQL_HOST", "localhost");
    String url = UpgradePlanning.setting(p, "DB_URL", "jdbc:mysql://" + host + ":" +
        UpgradePlanning.setting(p, "MYSQL_PUBLISHED_PORT", UpgradePlanning.setting(p, "MYSQL_CONTAINER_PORT", "3306")) + "/" +
        UpgradePlanning.setting(p, "MYSQL_DATABASE", "superfurniyure") + "?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC");
    if (!url.matches("jdbc:mysql://(localhost|127\\.0\\.0\\.1):[0-9]+/.*")) throw new IllegalArgumentException("Local MySQL only");
    try (Connection c = DriverManager.getConnection(url,
        UpgradePlanning.setting(p, "DB_USERNAME", UpgradePlanning.setting(p, "MYSQL_USER", "superfurniyure")),
        UpgradePlanning.setting(p, "DB_PASSWORD", UpgradePlanning.setting(p, "MYSQL_PASSWORD", "")))) {
      boolean exists = UpgradePlanning.column(c, "sf_product", "supporting_images");
      long count = UpgradePlanning.count(c, "sf_product");
      System.out.println("supporting_images=" + exists + ", products=" + count);
      if (!Arrays.asList(args).contains("--apply")) return;
      Path backup = Path.of("backups", "product-gallery-before-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".sql");
      UpgradePlanning.backup(c, backup);
      System.out.println("Backup: " + backup.toAbsolutePath());
      if (!exists) try (var s = c.createStatement()) { s.executeUpdate("ALTER TABLE sf_product ADD COLUMN supporting_images TEXT"); }
      c.setAutoCommit(false);
      try {
        int updated = 0;
        for (String sql : UpgradePlanning.statements(Files.readString(Path.of("ruoyi-admin/src/main/resources/db/product-gallery.sql")))) {
          try (var s = c.createStatement()) { updated += s.executeUpdate(sql); }
        }
        if (UpgradePlanning.count(c, "sf_product") != count) throw new IllegalStateException("Product count changed");
        c.commit();
        System.out.println("Gallery migration complete; seeded products=" + updated);
      } catch (Exception e) { c.rollback(); throw e; }
    }
  }
}
