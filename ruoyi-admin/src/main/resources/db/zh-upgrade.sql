-- One-time upgrade for existing MySQL databases created before Chinese copy fields.
-- Run this once before starting the updated backend.

alter table sf_product add column name_zh varchar(160) null after name_en;
alter table sf_product add column description_zh text null after description_en;
alter table sf_product add column specs_zh text null after specs_en;
alter table sf_content add column zh text null after en;

update sf_product
set
  name_zh = case slug
    when 'linea-wardrobe' then 'Linea 衣柜'
    when 'forma-sideboard' then 'Forma 边柜'
    when 'atelier-kitchen' then 'Atelier 橱柜'
    when 'aqua-vanity' then 'Aqua 浴室柜'
    when 'terra-outdoor' then 'Terra 户外柜'
    else name_en
  end,
  description_zh = case slug
    when 'linea-wardrobe' then '线条克制，收纳清晰，木纹质感与平整门板相互平衡。配置可根据您的项目定制。'
    when 'forma-sideboard' then '融入客厅空间的收纳柜。可为您的产品系列选择布局、饰面与五金规格。'
    when 'atelier-kitchen' then '围绕日常生活设计的厨房柜系统。布局、门板和内部配置会在生产前通过图纸确认。'
    when 'aqua-vanity' then '线条简洁、收纳实用的浴室柜。材料、台盆与安装方式可按项目条件确认。'
    when 'terra-outdoor' then '适用于有顶露台与户外空间的柜类产品。确认订单前会评估使用环境和材料要求。'
    else description_en
  end,
  specs_zh = case slug
    when 'linea-wardrobe' then '尺寸：按确认图纸
饰面：按确认样品
五金：按规格配置
包装与组装：生产前确认'
    when 'forma-sideboard' then '尺寸：按确认图纸
饰面：按确认样品
五金：按规格配置
包装与组装：生产前确认'
    when 'atelier-kitchen' then '尺寸：按确认图纸
饰面：按确认样品
五金：按规格配置
包装与组装：生产前确认'
    when 'aqua-vanity' then '尺寸：按确认图纸
饰面：按确认样品
五金：按规格配置
包装与组装：生产前确认'
    when 'terra-outdoor' then '尺寸：按确认图纸
饰面：按确认样品
五金：按规格配置
包装与组装：生产前确认'
    else specs_en
  end
where name_zh is null or description_zh is null;

update sf_content
set zh = case content_key
  when 'factory_intro' then 'superFurniyure 是来自中国的柜类家具生产合作伙伴，为经销商、设计师和项目采购方提供衣柜、客厅柜、橱柜、浴室柜与户外收纳系统。我们的方式是在早期确认细节，从材料、尺寸到配置和包装。'
  when 'factory_process' then '从项目沟通、样品确认、图纸准备，到生产、配置检查和包装，每个订单都会单独确认规格与交付条件。'
  when 'contact_intro' then '请告诉我们您的项目需求：柜类、数量、尺寸和交付城市。我们会进一步确认细节并准备方案。'
  else en
end
where zh is null;

alter table sf_product modify column name_zh varchar(160) not null;
alter table sf_product modify column description_zh text not null;
alter table sf_content modify column zh text not null;
