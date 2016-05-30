declare
  maxId NUMBER;
begin
  select max(entts.id) into maxId from eav_be_entities entts
    join eav_m_classes clss on entts.class_id = CLSS.ID
    where CLSS.IS_REFERENCE = 1;

  delete from eav_be_entities where id > maxId;
  delete from eav_be_entity_report_dates where entity_id > maxId;
  delete from eav_be_integer_values where entity_id > maxId;
  delete from eav_be_string_values where entity_id > maxId;
  delete from eav_be_double_values where entity_id > maxId;
  delete from eav_be_boolean_values where entity_id > maxId;
  delete from eav_be_complex_values where entity_id > maxId;

  delete from eav_be_complex_set_values where set_id in (select id from eav_be_entity_complex_sets where entity_id > maxId);
  delete from eav_be_integer_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id > maxId);
  delete from eav_be_string_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id > maxId);
  delete from eav_be_double_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id > maxId);
  delete from eav_be_boolean_set_values where set_id in (select id from eav_be_entity_simple_sets where entity_id > maxId);

  delete from eav_be_entity_complex_sets where entity_id > maxId;
  delete from eav_be_entity_simple_sets where entity_id > maxId;

  delete from eav_optimizer where entity_id > maxId;

  delete from eav_entity_statuses where entity_id > maxId or entity_id <= 0;

  delete from R_CORE_CREDIT@showcase;
  delete from R_CORE_CREDIT_FLOW@showcase;
  delete from R_CORE_CREDIT_HIS@showcase;
  delete from R_CORE_DEBTOR@showcase;
  delete from R_CORE_DEBTOR_HIS@showcase;
  delete from R_CORE_ORG@showcase;
  delete from R_CORE_ORG_ADDRESS@showcase;
  delete from R_CORE_ORG_ADDRESS_HIS@showcase;
  delete from R_CORE_ORG_CONTACTS@showcase;
  delete from R_CORE_ORG_CONTACTS_HIS@showcase;
  delete from R_CORE_ORG_DI@showcase;
  delete from R_CORE_ORG_DI_HIS@showcase;
  delete from R_CORE_ORG_HEAD_DOCS@showcase;
  delete from R_CORE_ORG_HEAD_DOCS_HIS@showcase;
  delete from R_CORE_ORG_HEAD_NAMES@showcase;
  delete from R_CORE_ORG_HEAD_NAMES_HIS@showcase;
  delete from R_CORE_ORG_HIS@showcase;
  delete from R_CORE_ORG_NAME@showcase;
  delete from R_CORE_ORG_NAME_HIS@showcase;
  delete from R_CORE_PERSON@showcase;
  delete from R_CORE_PERSON_ADDRESS@showcase;
  delete from R_CORE_PERSON_ADDRESS_HIS@showcase;
  delete from R_CORE_PERSON_CONTACTS@showcase;
  delete from R_CORE_PERSON_CONTACTS_HIS@showcase;
  delete from R_CORE_PERSON_DI@showcase;
  delete from R_CORE_PERSON_DI_HIS@showcase;
  delete from R_CORE_PERSON_HIS@showcase;
  delete from R_CORE_PERSON_NAME@showcase;
  delete from R_CORE_PERSON_NAME_HIS@showcase;
  delete from R_CORE_PLEDGE@showcase;
  delete from R_CORE_PORTFOLIO_FLOW_KFN@showcase;
  delete from R_CORE_PORTFOLIO_FLOW_MSFO@showcase;
  delete from R_CORE_REMAINS@showcase;
  delete from R_CORE_SUBJECT_DOC@showcase;
  delete from R_CORE_SUBJECT_DOC_HIS@showcase;
  delete from R_CORE_TURNOVER@showcase;
  commit;
end;