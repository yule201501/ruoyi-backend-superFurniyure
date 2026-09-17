create table sys_dept (
  dept_id           bigint      not null auto_increment,
  parent_id         bigint      default 0,
  ancestors         varchar(50)     default '',
  dept_name         varchar(30)     default '',
  order_num         int          default 0,
  leader            varchar(20)     default null,
  phone             varchar(11)     default null,
  email             varchar(50)     default null,
  status            char(1)         default '0',
  del_flag          char(1)         default '0',
  create_by         varchar(64)     default '',
  create_time 	    datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  primary key (dept_id)
);

create table sys_user (
  user_id           bigint      not null auto_increment,
  dept_id           bigint      default null,
  user_name         varchar(30)     not null,
  nick_name         varchar(30)     not null,
  user_type         varchar(2)      default '00',
  email             varchar(50)     default '',
  phonenumber       varchar(11)     default '',
  sex               char(1)         default '0',
  avatar            varchar(100)    default '',
  password          varchar(100)    default '',
  status            char(1)         default '0',
  del_flag          char(1)         default '0',
  login_ip          varchar(128)    default '',
  login_date        datetime,
  pwd_update_date   datetime,
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default null,
  primary key (user_id)
);

create table sys_post
(
  post_id       bigint      not null auto_increment,
  post_code     varchar(64)     not null,
  post_name     varchar(50)     not null,
  post_sort     int          not null,
  status        char(1)         not null,
  create_by     varchar(64)     default '',
  create_time   datetime,
  update_by     varchar(64)     default '',
  update_time   datetime,
  remark        varchar(500)    default null,
  primary key (post_id)
);

create table sys_role (
  role_id              bigint      not null auto_increment,
  role_name            varchar(30)     not null,
  role_key             varchar(100)    not null,
  role_sort            int          not null,
  data_scope           char(1)         default '1',
  menu_check_strictly  tinyint      default 1,
  dept_check_strictly  tinyint      default 1,
  status               char(1)         not null,
  del_flag             char(1)         default '0',
  create_by            varchar(64)     default '',
  create_time          datetime,
  update_by            varchar(64)     default '',
  update_time          datetime,
  remark               varchar(500)    default null,
  primary key (role_id)
);

create table sys_menu (
  menu_id           bigint      not null auto_increment,
  menu_name         varchar(50)     not null,
  parent_id         bigint      default 0,
  order_num         int          default 0,
  path              varchar(200)    default '',
  component         varchar(255)    default null,
  query             varchar(255)    default null,
  route_name        varchar(50)     default '',
  is_frame          int          default 1,
  is_cache          int          default 0,
  menu_type         char(1)         default '',
  visible           char(1)         default 0,
  status            char(1)         default 0,
  perms             varchar(100)    default null,
  icon              varchar(100)    default '#',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default '',
  primary key (menu_id)
);

create table sys_user_role (
  user_id   bigint not null,
  role_id   bigint not null,
  primary key(user_id, role_id)
);

create table sys_role_menu (
  role_id   bigint not null,
  menu_id   bigint not null,
  primary key(role_id, menu_id)
);

create table sys_role_dept (
  role_id   bigint not null,
  dept_id   bigint not null,
  primary key(role_id, dept_id)
);

create table sys_user_post
(
  user_id   bigint not null,
  post_id   bigint not null,
  primary key (user_id, post_id)
);

create table sys_oper_log (
  oper_id           bigint      not null auto_increment,
  title             varchar(50)     default '',
  business_type     int          default 0,
  method            varchar(200)    default '',
  request_method    varchar(10)     default '',
  operator_type     int          default 0,
  oper_name         varchar(50)     default '',
  dept_name         varchar(50)     default '',
  oper_url          varchar(255)    default '',
  oper_ip           varchar(128)    default '',
  oper_location     varchar(255)    default '',
  oper_param        varchar(2000)   default '',
  json_result       varchar(2000)   default '',
  status            int          default 0,
  error_msg         varchar(2000)   default '',
  oper_time         datetime,
  cost_time         bigint      default 0,
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
);

create table sys_dict_type
(
  dict_id          bigint      not null auto_increment,
  dict_name        varchar(100)    default '',
  dict_type        varchar(100)    default '',
  status           char(1)         default '0',
  create_by        varchar(64)     default '',
  create_time      datetime,
  update_by        varchar(64)     default '',
  update_time      datetime,
  remark           varchar(500)    default null,
  primary key (dict_id),
  unique (dict_type)
);

create table sys_dict_data
(
  dict_code        bigint      not null auto_increment,
  dict_sort        int          default 0,
  dict_label       varchar(100)    default '',
  dict_value       varchar(100)    default '',
  dict_type        varchar(100)    default '',
  css_class        varchar(100)    default null,
  list_class       varchar(100)    default null,
  is_default       char(1)         default 'N',
  status           char(1)         default '0',
  create_by        varchar(64)     default '',
  create_time      datetime,
  update_by        varchar(64)     default '',
  update_time      datetime,
  remark           varchar(500)    default null,
  primary key (dict_code)
);

create table sys_config (
  config_id         int          not null auto_increment,
  config_name       varchar(100)    default '',
  config_key        varchar(100)    default '',
  config_value      varchar(500)    default '',
  config_type       char(1)         default 'N',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default null,
  primary key (config_id)
);

create table sys_logininfor (
  info_id        bigint     not null auto_increment,
  user_name      varchar(50)    default '',
  ipaddr         varchar(128)   default '',
  login_location varchar(255)   default '',
  browser        varchar(50)    default '',
  os             varchar(50)    default '',
  status         char(1)        default '0',
  msg            varchar(255)   default '',
  login_time     datetime,
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
);

create table sys_job (
  job_id              bigint    not null auto_increment,
  job_name            varchar(64)   default '',
  job_group           varchar(64)   default 'DEFAULT',
  invoke_target       varchar(500)  not null,
  cron_expression     varchar(255)  default '',
  misfire_policy      varchar(20)   default '3',
  concurrent          char(1)       default '1',
  status              char(1)       default '0',
  create_by           varchar(64)   default '',
  create_time         datetime,
  update_by           varchar(64)   default '',
  update_time         datetime,
  remark              varchar(500)  default '',
  primary key (job_id, job_name, job_group)
);

create table sys_job_log (
  job_log_id          bigint     not null auto_increment,
  job_name            varchar(64)    not null,
  job_group           varchar(64)    not null,
  invoke_target       varchar(500)   not null,
  job_message         varchar(500),
  status              char(1)        default '0',
  exception_info      varchar(2000)  default '',
  start_time          datetime,
  end_time            datetime,
  create_time         datetime,
  primary key (job_log_id)
);

create table sys_notice (
  notice_id         int          not null auto_increment,
  notice_title      varchar(50)     not null,
  notice_type       char(1)         not null,
  notice_content    longblob        default null,
  status            char(1)         default '0',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(255)    default null,
  primary key (notice_id)
);

create table sys_notice_read (
  read_id          bigint       not null auto_increment,
  notice_id        int           not null,
  user_id          bigint       not null,
  read_time        datetime         not null,
  primary key (read_id),
  unique key uk_user_notice (user_id, notice_id)
);

create table gen_table (
  table_id          bigint      not null auto_increment,
  table_name        varchar(200)    default '',
  table_comment     varchar(500)    default '',
  sub_table_name    varchar(64)     default null,
  sub_table_fk_name varchar(64)     default null,
  class_name        varchar(100)    default '',
  tpl_category      varchar(200)    default 'crud',
  tpl_web_type      varchar(30)     default '',
  package_name      varchar(100),
  module_name       varchar(30),
  business_name     varchar(30),
  function_name     varchar(50),
  function_author   varchar(50),
  form_col_num      int          default 1,
  gen_type          char(1)         default '0',
  gen_path          varchar(200)    default '/',
  options           varchar(1000),
  create_by         varchar(64)     default '',
  create_time 	    datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default null,
  primary key (table_id)
);

create table gen_table_column (
  column_id         bigint      not null auto_increment,
  table_id          bigint,
  column_name       varchar(200),
  column_comment    varchar(500),
  column_type       varchar(100),
  java_type         varchar(500),
  java_field        varchar(200),
  is_pk             char(1),
  is_increment      char(1),
  is_required       char(1),
  is_insert         char(1),
  is_edit           char(1),
  is_list           char(1),
  is_query          char(1),
  query_type        varchar(200)    default 'EQ',
  html_type         varchar(200),
  dict_type         varchar(200)    default '',
  sort              int,
  create_by         varchar(64)     default '',
  create_time 	    datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  primary key (column_id)
);

create table sf_product (
 id bigint auto_increment primary key,
 slug varchar(100) not null unique, category varchar(30) not null,
 name_ru varchar(160) not null, name_en varchar(160) not null,
 description_ru text not null, description_en text not null,
 specs_ru text, specs_en text, image_url varchar(500) not null,
 model varchar(50) not null, published boolean not null default false,
 sort_order int not null default 0,
 created_at timestamp default CURRENT_TIMESTAMP,
 updated_at timestamp default CURRENT_TIMESTAMP
);
create index idx_product_public on sf_product(published,sort_order);
create table sf_content (content_key varchar(100) primary key, ru text not null, en text not null);
create table sf_inquiry (
 id bigint auto_increment primary key,
 request_id varchar(36) not null unique, payload_hash varchar(64) not null,
 receipt varchar(30) not null unique, name varchar(80) not null, email varchar(180) not null,
 company varchar(120), country varchar(100) not null, contact varchar(80), product_slug varchar(100),
 message text not null, locale varchar(5) not null,
 privacy_consent boolean not null, publish_consent boolean not null default false,
 ip_hash varchar(64) not null,
 moderation_status varchar(20) not null default 'PENDING',
 followup_status varchar(20) not null default 'NEW',
 public_name varchar(80), public_text text, reply text, internal_note text,
 version int not null default 0, updated_by varchar(64),
 created_at timestamp not null default CURRENT_TIMESTAMP,
 updated_at timestamp not null default CURRENT_TIMESTAMP
);
create index idx_inquiry_public on sf_inquiry(moderation_status,publish_consent,locale,id);
create table sf_mail_outbox (
 id bigint auto_increment primary key, inquiry_id bigint not null unique,
 status varchar(20) not null default 'PENDING', attempts int not null default 0,
 next_attempt_at timestamp not null default CURRENT_TIMESTAMP,
 updated_at timestamp not null default CURRENT_TIMESTAMP,
 constraint fk_outbox_inquiry foreign key(inquiry_id) references sf_inquiry(id) on delete cascade
);
