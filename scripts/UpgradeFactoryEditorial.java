import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Local-only content migration. Defaults to a preview; --apply backs up before writing. */
public class UpgradeFactoryEditorial {
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
      try (var s = c.createStatement(); var r = s.executeQuery("SELECT content_key,CHAR_LENGTH(en) FROM sf_content WHERE content_key LIKE 'factory_%'")) {
        while (r.next()) System.out.println(r.getString(1) + ": " + r.getInt(2) + " characters");
      }
      if (!Arrays.asList(args).contains("--apply")) return;
      Path backup = Path.of("backups", "factory-editorial-before-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".sql");
      UpgradePlanning.backup(c, backup);
      System.out.println("Backup: " + backup.toAbsolutePath());
      c.setAutoCommit(false);
      try {
        int updated = 0;
        for (String sql : UpgradePlanning.statements(Files.readString(Path.of("sql/20261010-factory-editorial.sql")))) {
          try (var s = c.createStatement()) { updated += s.executeUpdate(sql); }
        }
        c.commit();
        System.out.println("Factory editorial updated; affected rows=" + updated);
      } catch (Exception e) { c.rollback(); throw e; }
    }
  }
}
