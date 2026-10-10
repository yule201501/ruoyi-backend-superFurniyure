import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Repairs missing planning fields/content in a local MySQL database without replacing catalog data.
 * Run from backend: java -cp <mysql-connector-j.jar> scripts/UpgradePlanning.java [--apply]
 * Default is read-only. Credentials are read from .env, never command-line arguments.
 */
class UpgradePlanning {
  static String setting(Properties p, String key, String fallback) {
    String value = System.getenv(key);
    return value != null ? value : p.getProperty(key, fallback);
  }
  static boolean column(Connection c, String table, String column) throws SQLException {
    try (var s=c.prepareStatement("select count(*) from information_schema.columns where table_schema=database() and table_name=? and column_name=?")) {
      s.setString(1,table); s.setString(2,column);
      try(var r=s.executeQuery()) { r.next(); return r.getInt(1)>0; }
    }
  }
  static long count(Connection c, String table) throws SQLException {
    try(var s=c.createStatement();var r=s.executeQuery("select count(*) from "+table)) { r.next();return r.getLong(1); }
  }
  static String value(Object v) {
    if(v==null)return "NULL";
    byte[] bytes=v instanceof byte[] b ? b : v.toString().getBytes(StandardCharsets.UTF_8);
    String hex=HexFormat.of().formatHex(bytes);
    return v instanceof byte[] ? "X'"+hex+"'" : "CONVERT(X'"+hex+"' USING utf8mb4)";
  }
  static void backup(Connection c, Path dest) throws Exception {
    Files.createDirectories(dest.getParent());
    try(var out=Files.newBufferedWriter(dest,StandardCharsets.UTF_8,StandardOpenOption.CREATE_NEW)) {
      out.write("-- Pre-upgrade local backup. Contains private business data; do not commit.\nSET NAMES utf8mb4;\n");
      for(String table:List.of("sf_product","sf_inquiry","sf_content")) {
        try(var s=c.createStatement();var r=s.executeQuery("show create table "+table)) { r.next();out.write(r.getString(2)+";\n"); }
        try(var s=c.createStatement();var r=s.executeQuery("select * from "+table)) {
          var meta=r.getMetaData(); var fields=new ArrayList<String>();
          for(int i=1;i<=meta.getColumnCount();i++)fields.add("`"+meta.getColumnName(i).replace("`","``")+"`");
          while(r.next()) {
            var values=new ArrayList<String>();
            for(int i=1;i<=meta.getColumnCount();i++)values.add(value(r.getObject(i)));
            out.write("INSERT INTO "+table+" ("+String.join(",",fields)+") VALUES ("+String.join(",",values)+");\n");
          }
        }
      }
    }
  }
  static List<String> statements(String sql) {
    sql=sql.replaceAll("(?m)^--[^\\r\\n]*", "");
    var result=new ArrayList<String>();var current=new StringBuilder();boolean quoted=false;
    for(int i=0;i<sql.length();i++) {
      char ch=sql.charAt(i);
      if(ch=='\'' && (i==0 || sql.charAt(i-1)!='\\')) {
        if(quoted && i+1<sql.length() && sql.charAt(i+1)=='\'') {current.append("''");i++;continue;}
        quoted=!quoted;
      }
      if(ch==';'&&!quoted) {result.add(current.toString().trim());current.setLength(0);}
      else current.append(ch);
    }
    if(quoted)throw new IllegalArgumentException("Unclosed SQL string");
    if(!current.toString().isBlank())result.add(current.toString().trim());
    return result;
  }
  public static void main(String[] args) throws Exception {
    Properties p=new Properties();
    try(var reader=Files.newBufferedReader(Path.of(".env"),StandardCharsets.UTF_8)){p.load(reader);}
    String host=setting(p,"MYSQL_HOST","localhost");
    String url=setting(p,"DB_URL","jdbc:mysql://"+host+":"+setting(p,"MYSQL_PUBLISHED_PORT",setting(p,"MYSQL_CONTAINER_PORT","3306"))+"/"+setting(p,"MYSQL_DATABASE","superfurniyure")+"?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC");
    if(!url.matches("jdbc:mysql://(localhost|127\\.0\\.0\\.1):[0-9]+/.*"))throw new IllegalArgumentException("This utility only upgrades local MySQL.");
    try(Connection c=DriverManager.getConnection(url,setting(p,"DB_USERNAME",setting(p,"MYSQL_USER","superfurniyure")),setting(p,"DB_PASSWORD",setting(p,"MYSQL_PASSWORD","")))) {
      for(String[] field:new String[][]{{"sf_product","name_zh"},{"sf_content","zh"}})
        if(!column(c,field[0],field[1]))throw new IllegalStateException("Apply 20260917-add-zh-fields.sql first");
      boolean variants=column(c,"sf_product","variants"),city=column(c,"sf_inquiry","city");
      long products=count(c,"sf_product"),inquiries=count(c,"sf_inquiry");
      System.out.println("variants="+variants+", city="+city+", products="+products+", inquiries="+inquiries);
      if(!Arrays.asList(args).contains("--apply"))return;
      Path backup=Path.of("backups","planning-before-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))+".sql");
      c.setAutoCommit(false);
      backup(c,backup); c.commit(); c.setAutoCommit(true);
      System.out.println("Backup: "+backup.toAbsolutePath());
      try(var s=c.createStatement()) {
        if(!variants)s.executeUpdate("alter table sf_product add column variants text");
        if(!city)s.executeUpdate("alter table sf_inquiry add column city varchar(100) not null default ''");
      }
      c.setAutoCommit(false);
      try {
        int inserted=0;
        for(String sql:statements(Files.readString(Path.of("sql/20260929-xingjiu-planning.sql")))) {
          if(sql.startsWith("insert into sf_content("))try(var s=c.createStatement()){inserted+=s.executeUpdate(sql);}
        }
        if(count(c,"sf_product")!=products||count(c,"sf_inquiry")!=inquiries)throw new IllegalStateException("Unexpected record count change");
        c.commit(); System.out.println("Upgrade complete. Missing content entries added: "+inserted+". Existing products and inquiries preserved.");
      }catch(Exception e){c.rollback();throw e;}
    }
  }
}
