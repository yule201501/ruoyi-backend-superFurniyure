# superFurniyure 后端项目

基于官方 RuoYi-Vue `springboot3` 分支，保留原生认证、角色权限、系统管理和日志。新增业务代码位于 `ruoyi-admin/src/main/java/com/ruoyi/furniture`。

## 本地启动

需要 Java 17、Maven 3.9+、Redis 7.x（默认 localhost:6379）。

```bash
mvn -pl ruoyi-admin -am clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=dev
```

本地接口端口 8080，演示账号 `admin` / `LocalDemo!2026`。`dev` 使用内存 H2，重启会清空数据，仅用于开发演示。

## 生产数据与配置

正式部署使用 MySQL 8.4、Redis 与 `druid` 配置。数据库应使用本项目 `sql/init/01-schema.sql`、`sql/init/02-data.sql` 初始化；不要导入若依原版演示 SQL 来代替，它不包含家具业务数据结构。

环境变量、首次管理员密码、HTTPS、备份及邮件设置见上一层 `README.md` 与 `docs/DEPLOYMENT.md`。根目录的 Docker Compose 会自动导入正确的初始化文件。已有数据库不应重新执行建表脚本。

接口说明见 `../docs/API.md`。本项目启动入口不依赖定时任务管理和代码生成器；原模块仍保留作为若依源码参考。上游说明保存在 `README.upstream.md`，其演示部署步骤不适用于本项目。
