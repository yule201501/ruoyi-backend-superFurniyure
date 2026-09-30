# XingjiuCabinets 后端项目

基于官方 RuoYi-Vue `springboot3` 分支，保留原生认证、角色权限、系统管理和日志。新增业务代码位于 `ruoyi-admin/src/main/java/com/ruoyi/furniture`。

## 本地启动

需要 Java 17、Maven 3.9+、Redis 7.x（默认 localhost:6379）。

```bash
mvn -pl ruoyi-admin -am clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=dev
```

本地接口端口 8080，演示账号 `admin` / `LocalDemo!2026`。`dev` 使用内存 H2，重启会清空数据，仅用于开发演示。

## 生产数据与配置

### 使用本地 .env 连接 MySQL

配置文件放在 `backend/.env`（参考同目录 `.env.example`），IDEA 的 Working directory 设为 `backend` 的绝对路径，Active profiles 填 `local` 或 `druid`。`local` 会自动加载 `druid` 的 MySQL 与连接池配置；`dev` 才是重启清空数据的 H2 演示模式。

```bash
java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=local
```

从其他目录启动时，可通过环境变量 `ENV_FILE` 或启动参数 `--ENV_FILE=D:/path/to/backend/.env` 指定配置文件。`.env` 按 Java properties 解析，值不要加引号，Windows 路径用 `/`，不要使用 `export` 前缀。文件不打包进 JAR，也不提交 Git。

数据库兼容 `MYSQL_HOST`、`MYSQL_PUBLISHED_PORT`（优先于 `MYSQL_CONTAINER_PORT`）、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`；原有 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 仍优先。Redis 使用 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`。`TOKEN_SECRET` 也必须填写。其他项目的 MinIO、MQTT 等变量在本项目中不会生效。

正式部署使用 MySQL 8.4、Redis 与 `druid` 配置。全新数据库应按顺序执行 `sql/init/` 中的 01 至 06 文件；已有数据库按 `../docs/PLANNING-9.29.md` 和 `../docs/PRODUCT-GALLERY.md` 执行增量升级。不要导入若依原版演示 SQL 来代替，它不包含家具业务数据结构。

环境变量、首次管理员密码、HTTPS、备份及邮件设置见上一层 `README.md` 与 `docs/DEPLOYMENT.md`。根目录的 Docker Compose 会自动导入正确的初始化文件。已有数据库不应重新执行建表脚本。

接口说明见 `../docs/API.md`。本项目启动入口不依赖定时任务管理和代码生成器；原模块仍保留作为若依源码参考。上游说明保存在 `README.upstream.md`，其演示部署步骤不适用于本项目。
