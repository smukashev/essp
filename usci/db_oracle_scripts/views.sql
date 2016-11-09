begin
  execute immediate
  'create OR REPLACE VIEW v_simple_attribute AS
  SELECT
    a.id,
    a.containing_id,
    m1.name AS containing_name,
    a.container_type,
    a.name,
    a.title,
    a.is_key,
    a.is_optional_key,
    a.is_required,
    a.is_nullable,
    a.is_immutable,
    a.is_final,
    a.is_disabled,
    a.type_code,
    a.is_nullable_key
  FROM eav_m_simple_attributes a,
    eav_m_classes m1
  WHERE a.containing_id = m1.id';

  EXECUTE IMMEDIATE
  'create or replace view v_complex_attribute as
  SELECT
    a.id,
    a.containing_id,
    m1.name AS containing_name,
    a.container_type,
    a.name,
    a.title,
    a.is_key,
    a.is_optional_key,
    a.is_required,
    a.is_nullable,
    a.is_immutable,
    a.is_final,
    a.is_disabled,
    a.class_id,
    m2.name as class_name,
    a.is_nullable_key
  FROM eav_m_complex_attributes a,
    eav_m_classes m1,
    eav_m_classes m2
  WHERE a.containing_id = m1.id
    and a.class_id = m2.id';

end;