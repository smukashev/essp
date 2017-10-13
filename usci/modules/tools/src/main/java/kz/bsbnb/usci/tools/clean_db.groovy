package kz.bsbnb.usci.tools

import groovy.sql.Sql

/**
 * Created by emles on 07.09.17
 */


final cote_clean = """DECLARE

  maxId NUMBER;

BEGIN

  SELECT max(entts.id)
  INTO maxId
  FROM eav_be_entities entts
    JOIN eav_m_classes clss ON entts.class_id = CLSS.ID
  WHERE CLSS.IS_REFERENCE = 1;

  DELETE FROM eav_be_entities
  WHERE id > maxId;
  DELETE FROM eav_be_entity_report_dates
  WHERE entity_id > maxId;
  DELETE FROM eav_be_integer_values
  WHERE entity_id > maxId;
  DELETE FROM eav_be_string_values
  WHERE entity_id > maxId;
  DELETE FROM eav_be_double_values
  WHERE entity_id > maxId;
  DELETE FROM eav_be_boolean_values
  WHERE entity_id > maxId;
  DELETE FROM eav_be_complex_values
  WHERE entity_id > maxId;

  DELETE FROM eav_be_complex_set_values
  WHERE set_id IN (SELECT id
                   FROM eav_be_entity_complex_sets
                   WHERE entity_id > maxId);
  DELETE FROM eav_be_integer_set_values
  WHERE set_id IN (SELECT id
                   FROM eav_be_entity_simple_sets
                   WHERE entity_id > maxId);
  DELETE FROM eav_be_string_set_values
  WHERE set_id IN (SELECT id
                   FROM eav_be_entity_simple_sets
                   WHERE entity_id > maxId);
  DELETE FROM eav_be_double_set_values
  WHERE set_id IN (SELECT id
                   FROM eav_be_entity_simple_sets
                   WHERE entity_id > maxId);
  DELETE FROM eav_be_boolean_set_values
  WHERE set_id IN (SELECT id
                   FROM eav_be_entity_simple_sets
                   WHERE entity_id > maxId);

  DELETE FROM eav_be_entity_complex_sets
  WHERE entity_id > maxId;
  DELETE FROM eav_be_entity_simple_sets
  WHERE entity_id > maxId;

  DELETE FROM eav_optimizer
  WHERE entity_id > maxId;

  DELETE FROM eav_entity_statuses
  WHERE entity_id > maxId OR entity_id <= 0;

  DELETE FROM EAV_BATCHES
  WHERE FILE_NAME NOT LIKE 'ref%';

  COMMIT;

END;
"""

final showcase_clean = """BEGIN

  DELETE FROM R_CORE_CREDIT;
  DELETE FROM R_CORE_CREDIT_FLOW;
  DELETE FROM R_CORE_CREDIT_HIS;
  DELETE FROM R_CORE_DEBTOR;
  DELETE FROM R_CORE_DEBTOR_HIS;
  DELETE FROM R_CORE_ORG;
  DELETE FROM R_CORE_ORG_ADDRESS;
  DELETE FROM R_CORE_ORG_ADDRESS_HIS;
  DELETE FROM R_CORE_ORG_CONTACTS;
  DELETE FROM R_CORE_ORG_CONTACTS_HIS;
  DELETE FROM R_CORE_ORG_DI;
  DELETE FROM R_CORE_ORG_DI_HIS;
  DELETE FROM R_CORE_ORG_HEAD_DOCS;
  DELETE FROM R_CORE_ORG_HEAD_DOCS_HIS;
  DELETE FROM R_CORE_ORG_HEAD_NAMES;
  DELETE FROM R_CORE_ORG_HEAD_NAMES_HIS;
  DELETE FROM R_CORE_ORG_HIS;
  DELETE FROM R_CORE_ORG_NAME;
  DELETE FROM R_CORE_ORG_NAME_HIS;
  DELETE FROM R_CORE_PERSON;
  DELETE FROM R_CORE_PERSON_ADDRESS;
  DELETE FROM R_CORE_PERSON_ADDRESS_HIS;
  DELETE FROM R_CORE_PERSON_CONTACTS;
  DELETE FROM R_CORE_PERSON_CONTACTS_HIS;
  DELETE FROM R_CORE_PERSON_DI;
  DELETE FROM R_CORE_PERSON_DI_HIS;
  DELETE FROM R_CORE_PERSON_HIS;
  DELETE FROM R_CORE_PERSON_NAME;
  DELETE FROM R_CORE_PERSON_NAME_HIS;
  DELETE FROM R_CORE_PLEDGE;
  DELETE FROM R_CORE_PORTFOLIO_FLOW_KFN;
  DELETE FROM R_CORE_PORTFOLIO_FLOW_MSFO;
  DELETE FROM R_CORE_REMAINS;
  DELETE FROM R_CORE_SUBJECT_DOC;
  DELETE FROM R_CORE_SUBJECT_DOC_HIS;
  DELETE FROM R_CORE_TURNOVER;

  COMMIT;

END;
"""


final def propertiesPath = 'properties/oracle.properties'
final Properties props = new Properties()

block:
{
    props.load(Thread.currentThread().contextClassLoader.getResourceAsStream(propertiesPath))
}

def getSqlCore = {

    def (url, user, password, driver) = [props.getProperty('jdbc.core.url'), props.getProperty('jdbc.core.user'), props.getProperty('jdbc.core.password'), props.getProperty('jdbc.core.driver')]

    return Sql.newInstance(url, user, password, driver)

}

def getSqlShowcase = {

    def (url, user, password, driver) = [props.getProperty('jdbc.showcase.url'), props.getProperty('jdbc.showcase.user'), props.getProperty('jdbc.showcase.password'), props.getProperty('jdbc.showcase.driver')]

    return Sql.newInstance(url, user, password, driver)

}

Sql sql


println "Starting to clean core..."
sql = getSqlCore()
sql.execute cote_clean
println "Finished."

println "Starting to clean chowcase..."
sql = getSqlShowcase()
sql.execute showcase_clean
println "Finished."



