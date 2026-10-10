-- Replace only the built-in factory concept photo; uploaded images remain unchanged.
update sf_content
set en=replace(en, '/images/xj-factory.jpg', '/images/xj-factory-v2.jpg'),
    zh=replace(zh, '/images/xj-factory.jpg', '/images/xj-factory-v2.jpg')
where content_key='factory_gallery';
