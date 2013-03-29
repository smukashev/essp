<?xml version="1.0"?>
<!DOCTYPE database SYSTEM "http://db.apache.org/torque/dtd/database">
  <database name="model">
    <table name="eav_array_key_filter">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="attribute_name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_array_key_filter_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="filter_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_attributes">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="true" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="BIGINT" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <unique>
        <unique-column name="containing_id"/>
        <unique-column name="name"/>
      </unique>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
      <index>
        <index-column name="name"/>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_batches">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="receipt_date" primaryKey="false" required="true" type="TIMESTAMP" size="35,6" autoIncrement="false"/>
      <column name="begin_date" primaryKey="false" required="false" type="TIMESTAMP" size="35,6" autoIncrement="false"/>
      <column name="end_date" primaryKey="false" required="false" type="TIMESTAMP" size="35,6" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_boolean_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="BIT" size="1" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_boolean_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="BIT" size="1" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_simple_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_complex_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="entity_value_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_value_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_value_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_complex_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="entity_value_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_complex_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities" name="cv_e_fk1">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities" name="cv_e_fk2">
        <reference local="entity_value_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <index>
        <index-column name="entity_value_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_date_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="DATE" size="13" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_date_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="DATE" size="13" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_simple_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_double_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="DOUBLE" size="17,17" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_double_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="DOUBLE" size="17,17" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_simple_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_entity_complex_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_complex_set">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
    </table>
    <table name="eav_be_entity_set_of_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_set_of_sets">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
    </table>
    <table name="eav_be_entity_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_set">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
    </table>
    <table name="eav_be_entity_simple_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_simple_set">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
    </table>
    <table name="eav_be_integer_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
    </table>
    <table name="eav_be_integer_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="INTEGER" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_simple_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_set_of_complex_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="parent_set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="child_set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets" name="socs_s_fk1">
        <reference local="child_set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets" name="socs_s_fk2">
        <reference local="parent_set_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="child_set_id"/>
      </index>
      <index>
        <index-column name="parent_set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_set_of_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="parent_set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="child_set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets" name="sos_s_fk1">
        <reference local="child_set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets" name="sos_s_fk2">
        <reference local="parent_set_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="child_set_id"/>
      </index>
      <index>
        <index-column name="parent_set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_set_of_simple_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="parent_set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="child_set_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_be_sets" name="soss_s_fk1">
        <reference local="child_set_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets" name="soss_s_fk2">
        <reference local="parent_set_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="child_set_id"/>
      </index>
      <index>
        <index-column name="parent_set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="batch_id"/>
      </index>
    </table>
    <table name="eav_be_string_set_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="set_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_be_sets">
        <reference local="set_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="set_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_string_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <column name="value" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_simple_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_be_values">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="entity_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="batch_id" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="attribute_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="index" primaryKey="false" required="false" type="BIGINT" size="19" autoIncrement="false"/>
      <column name="rep_date" primaryKey="false" required="true" type="DATE" size="13" autoIncrement="false"/>
      <foreign-key foreignTable="eav_attributes">
        <reference local="attribute_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_batches">
        <reference local="batch_id" foreign="id"/>
      </foreign-key>
      <foreign-key foreignTable="eav_entities">
        <reference local="entity_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="attribute_id"/>
      </index>
      <index>
        <index-column name="batch_id"/>
      </index>
      <index>
        <index-column name="entity_id"/>
      </index>
    </table>
    <table name="eav_classes">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="complex_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <column name="begin_date" primaryKey="false" required="true" type="TIMESTAMP" size="35,6" autoIncrement="false"/>
      <column name="is_disabled" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <unique>
        <unique-column name="name"/>
        <unique-column name="begin_date"/>
      </unique>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="begin_date"/>
        <index-column name="name"/>
        <index-column name="is_disabled"/>
      </index>
    </table>
    <table name="eav_complex_attributes">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="INTEGER" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="class_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="class_id"/>
      </index>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
    </table>
    <table name="eav_complex_set">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="INTEGER" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <column name="class_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
    </table>
    <table name="eav_entities">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="class_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <foreign-key foreignTable="eav_classes">
        <reference local="class_id" foreign="id"/>
      </foreign-key>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="class_id"/>
      </index>
    </table>
    <table name="eav_meta_object">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>
    <table name="eav_set">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="INTEGER" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
    </table>
    <table name="eav_set_of_sets">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="INTEGER" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
    </table>
    <table name="eav_simple_attributes">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="INTEGER" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="type_code" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
    </table>
    <table name="eav_simple_set">
      <column name="id" primaryKey="true" required="true" type="BIGINT" size="10" autoIncrement="true"/>
      <column name="containing_id" primaryKey="false" required="false" type="BIGINT" size="10" autoIncrement="false"/>
      <column name="container_type" required="true" type="INTEGER" size="1"/>
      <column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <column name="is_key" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="is_nullable" primaryKey="false" required="true" type="BIT" size="1" autoIncrement="false"/>
      <column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <column name="type_code" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
      <index>
        <index-column name="containing_id"/>
        <index-column name="container_type"/>
      </index>
    </table>
  </database>
