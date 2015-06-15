create or replace view vw_complex_attribute as
select comp.id, comp.name as attribute_name, class1.name as containing_name, class2.name as class_name
       from
       eav_m_complex_attributes comp,
       eav_m_classes class1,
       eav_m_classes class2
       where comp.containing_id = class1.id
         and comp.class_id = class2.id;
