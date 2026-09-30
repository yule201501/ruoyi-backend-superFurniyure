-- Optional visual refresh of built-in demonstration imagery only. Run after 20260929-xingjiu-planning.sql.
update sf_product set image_url='/images/xj-kitchen-v2.jpg' where image_url='/images/xj-kitchen.jpg';
update sf_product set variants=replace(variants, '/images/xj-kitchen.jpg', '/images/xj-kitchen-v2.jpg') where variants like '%/images/xj-kitchen.jpg%';
update sf_content set en=replace(en, '/images/xj-kitchen.jpg', '/images/xj-kitchen-v2.jpg'), zh=replace(zh, '/images/xj-kitchen.jpg', '/images/xj-kitchen-v2.jpg') where content_key in ('hero_slides','catalog_banner');
update sf_product set image_url='/images/xj-living-v2.jpg' where image_url='/images/xj-living.jpg';
update sf_product set variants=replace(variants, '/images/xj-living.jpg', '/images/xj-living-v2.jpg') where variants like '%/images/xj-living.jpg%';
update sf_content set en=replace(en, '/images/xj-living.jpg', '/images/xj-living-v2.jpg'), zh=replace(zh, '/images/xj-living.jpg', '/images/xj-living-v2.jpg') where content_key in ('hero_slides','catalog_banner');
update sf_product set image_url='/images/xj-wardrobe-v2.jpg' where image_url='/images/xj-wardrobe.jpg';
update sf_product set variants=replace(variants, '/images/xj-wardrobe.jpg', '/images/xj-wardrobe-v2.jpg') where variants like '%/images/xj-wardrobe.jpg%';
update sf_content set en=replace(en, '/images/xj-wardrobe.jpg', '/images/xj-wardrobe-v2.jpg'), zh=replace(zh, '/images/xj-wardrobe.jpg', '/images/xj-wardrobe-v2.jpg') where content_key in ('hero_slides','catalog_banner');
update sf_product set image_url='/images/xj-bathroom-v2.jpg' where image_url='/images/xj-bathroom.jpg';
update sf_product set variants=replace(variants, '/images/xj-bathroom.jpg', '/images/xj-bathroom-v2.jpg') where variants like '%/images/xj-bathroom.jpg%';
update sf_content set en=replace(en, '/images/xj-bathroom.jpg', '/images/xj-bathroom-v2.jpg'), zh=replace(zh, '/images/xj-bathroom.jpg', '/images/xj-bathroom-v2.jpg') where content_key in ('hero_slides','catalog_banner');
update sf_product set image_url='/images/xj-outdoor-v2.jpg' where image_url='/images/xj-outdoor.jpg';
update sf_product set variants=replace(variants, '/images/xj-outdoor.jpg', '/images/xj-outdoor-v2.jpg') where variants like '%/images/xj-outdoor.jpg%';
update sf_content set en=replace(en, '/images/xj-outdoor.jpg', '/images/xj-outdoor-v2.jpg'), zh=replace(zh, '/images/xj-outdoor.jpg', '/images/xj-outdoor-v2.jpg') where content_key in ('hero_slides','catalog_banner');
update sf_content set en='[{"imageUrl": "/images/xj-factory-v2.jpg", "altEn": "Metal cabinet workshop — AI concept illustration", "altZh": "金属柜生产车间 · AI 示意图"}, {"imageUrl": "/images/xj-material-v2.jpg", "altEn": "Brushed steel and satin metal — AI design concept", "altZh": "拉丝钢与哑光金属 · AI 设计示意"}]', zh='[{"imageUrl": "/images/xj-factory-v2.jpg", "altEn": "Metal cabinet workshop — AI concept illustration", "altZh": "金属柜生产车间 · AI 示意图"}, {"imageUrl": "/images/xj-material-v2.jpg", "altEn": "Brushed steel and satin metal — AI design concept", "altZh": "拉丝钢与哑光金属 · AI 设计示意"}]' where content_key='factory_gallery' and en='[{"imageUrl": "/images/xj-factory-v2.jpg", "altEn": "Metal cabinet workshop — AI concept illustration", "altZh": "金属柜生产车间 · AI 示意图"}]' and zh='[{"imageUrl": "/images/xj-factory-v2.jpg", "altEn": "Metal cabinet workshop — AI concept illustration", "altZh": "金属柜生产车间 · AI 示意图"}]';
