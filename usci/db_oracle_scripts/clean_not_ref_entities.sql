declare
  maxId NUMBER;
begin
  select max(entts.id) into maxId from eav_be_entities entts
    join eav_m_classes clss on entts.class_id = CLSS.ID
    where CLSS.IS_REFERENCE = 1;

  delete from eav_be_entities where id >= maxId;
  delete from eav_be_entity_report_dates where entity_id >= maxId;
  delete from eav_be_integer_values where entity_id >= maxId;
  delete from eav_be_string_values where entity_id >= maxId;
  delete from eav_be_double_values where entity_id >= maxId;
  delete from eav_be_boolean_values where entity_id >= maxId;
  delete from eav_be_complex_values where entity_id >= maxId;

  delete from eav_be_complex_set_values where set_id in (select id from eav_be_entity_complex_sets where entity_id >= maxId);
  delete from eav_be_integer_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id >= maxId);
  delete from eav_be_string_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id >= maxId);
  delete from eav_be_double_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id >= maxId);
  delete from eav_be_boolean_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id >= maxId);

  delete from eav_be_entity_complex_sets where entity_id >= maxId;
  delete from eav_be_entity_simple_sets where entity_id >= maxId;

  delete from eav_optimizer where entity_id >= maxId;

  delete from eav_entity_statuses where entity_id >= maxId or entity_id <= 0;
  commit;
end;