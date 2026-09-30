-- Apply once to an existing MySQL database AFTER 20260917-add-zh-fields.sql.
-- Existing products, inquiries and edited copy are preserved.

alter table sf_product add column variants text;
alter table sf_inquiry add column city varchar(100) not null default '';
insert into sf_content(content_key,ru,en,zh) select 'brand_story','','Years ago, a friend about to get married showed us a photo of his parents'' wardrobe — after only five years it had cracked, its doors had warped, the back panel had darkened from moisture, and insects had left their marks. He asked us seriously whether any material could make a cabinet truly strong, durable and eco-friendly, without daily worry about damp, mould and insects.

He did not want to repeat his parents'' problem in his new home. We talked late into the night, and the answer turned out to be simple: if wood is the problem, remove the wood.

That is why we build cabinets entirely from stainless steel and cold-rolled steel — no wood at all — for wardrobes, kitchens, bathrooms and outdoor use.

That broken wardrobe is our origin, and still the direction we keep walking in: to bring strong, durable, eco-friendly metal cabinets into more homes.','多年前，一个即将结婚的朋友找到我们，带来一块使用了五年就已经腐烂变形的木板柜板。他很慎重地问，是否有更好的材料，可以让家具柜子更加结实、耐用、环保，也不需要担心虫子的啃咬破坏。

那是他父母使用的衣柜，经过五年，已经出现开裂、变形、长虫等种种问题。他不希望自己的新家庭也遇到这样的情况，希望衣柜、橱柜更加健康、耐用、易打理。

我们一起聊了一夜，最终选择不锈钢和冷轧钢作为原材料，打造不使用任何木材的金属衣柜、橱柜。

这是我们的起点，也将是我们的使命：让耐用环保的家用柜走进千家万户。' where not exists (select 1 from sf_content where content_key='brand_story');
insert into sf_content(content_key,ru,en,zh) select 'factory_intro','','XingjiuCabinets creates metal cabinetry for the places we call home. From wardrobes and living-room storage to kitchens, bathrooms and outdoor spaces, we work with stainless steel and cold-rolled steel to make everyday storage feel considered and welcoming.','XingjiuCabinets 专注家用金属柜，从卧室衣柜、家居收纳，到厨房、浴室与户外空间，以不锈钢和冷轧钢为材料，为日常生活打造温馨、实用的收纳。' where not exists (select 1 from sf_content where content_key='factory_intro');
insert into sf_content(content_key,ru,en,zh) select 'factory_process','','Materials, dimensions, finishes and hardware are agreed before production. Share your drawings and quantities with us to discuss sampling, metal forming, surface finishing, assembly, inspection and packing. Capacity and delivery schedules are confirmed for each order.','生产前逐项确认材料、尺寸、表面处理与五金配置。欢迎提供图纸和数量，沟通打样、金属成型、表面处理、组装、检验和包装。具体产能与交期按每个订单确认。' where not exists (select 1 from sf_content where content_key='factory_process');
insert into sf_content(content_key,ru,en,zh) select 'contact_intro','','A home, a collection, or a new project. Tell us the cabinet type, quantity, country and delivery city, and we will help you work through the details.','无论是一个家、一组产品系列，还是新的项目，欢迎告诉我们柜类、数量、国家和交付城市，我们会与你一起确认细节。' where not exists (select 1 from sf_content where content_key='contact_intro');
insert into sf_content(content_key,ru,en,zh) select 'hero_slides','','[{"imageUrl": "/images/xj-kitchen.jpg", "altEn": "A warm metal kitchen — design concept", "altZh": "暖色金属厨房 · 设计示意"}, {"imageUrl": "/images/xj-wardrobe.jpg", "altEn": "A calm bedroom wardrobe — design concept", "altZh": "卧室金属衣柜 · 设计示意"}, {"imageUrl": "/images/xj-living.jpg", "altEn": "Thoughtful living room storage — design concept", "altZh": "客厅金属收纳 · 设计示意"}, {"imageUrl": "/images/xj-bathroom.jpg", "altEn": "A welcoming bathroom — design concept", "altZh": "温馨浴室柜 · 设计示意"}]','[{"imageUrl": "/images/xj-kitchen.jpg", "altEn": "A warm metal kitchen — design concept", "altZh": "暖色金属厨房 · 设计示意"}, {"imageUrl": "/images/xj-wardrobe.jpg", "altEn": "A calm bedroom wardrobe — design concept", "altZh": "卧室金属衣柜 · 设计示意"}, {"imageUrl": "/images/xj-living.jpg", "altEn": "Thoughtful living room storage — design concept", "altZh": "客厅金属收纳 · 设计示意"}, {"imageUrl": "/images/xj-bathroom.jpg", "altEn": "A welcoming bathroom — design concept", "altZh": "温馨浴室柜 · 设计示意"}]' where not exists (select 1 from sf_content where content_key='hero_slides');
insert into sf_content(content_key,ru,en,zh) select 'catalog_banner','','[{"imageUrl": "/images/xj-kitchen.jpg", "altEn": "A warm metal kitchen — design concept", "altZh": "暖色金属厨房 · 设计示意"}]','[{"imageUrl": "/images/xj-kitchen.jpg", "altEn": "A warm metal kitchen — design concept", "altZh": "暖色金属厨房 · 设计示意"}]' where not exists (select 1 from sf_content where content_key='catalog_banner');
insert into sf_content(content_key,ru,en,zh) select 'factory_gallery','','[{"imageUrl": "/images/xj-factory-v2.jpg", "altEn": "Metal cabinet workshop — AI concept illustration", "altZh": "金属柜生产车间 · AI 示意图"}]','[{"imageUrl": "/images/xj-factory-v2.jpg", "altEn": "Metal cabinet workshop — AI concept illustration", "altZh": "金属柜生产车间 · AI 示意图"}]' where not exists (select 1 from sf_content where content_key='factory_gallery');
insert into sf_content(content_key,ru,en,zh) select 'social_links','','[]','[]' where not exists (select 1 from sf_content where content_key='social_links');
insert into sf_content(content_key,ru,en,zh) select 'factory_news','','[]','[]' where not exists (select 1 from sf_content where content_key='factory_news');
update sf_content set en=replace(en,'superFurniyure','XingjiuCabinets'),zh=replace(zh,'superFurniyure','XingjiuCabinets');
update sys_dept set dept_name=replace(dept_name,'superFurniyure','XingjiuCabinets') where dept_name like '%superFurniyure%';
update sys_user set nick_name=replace(nick_name,'superFurniyure','XingjiuCabinets') where nick_name like '%superFurniyure%';
update sys_menu set menu_name='网站内容与工厂资讯' where component='furniture/content/index';
