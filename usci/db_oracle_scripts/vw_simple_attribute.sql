create or replace view vw_simple_attribute as
select sa.id, classes.name as class_name, sa.name as attribute_name
    from eav_m_simple_attributes sa,
         eav_m_classes classes
   where sa.containing_id = classes.id;
