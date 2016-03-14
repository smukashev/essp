<?xml version="1.0"?>
<!DOCTYPE database SYSTEM "http://db.apache.org/torque/dtd/database">
<database name="model">
	<table name="eav_sc_showcases">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="parent_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="name" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="table_name" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="class_name" primaryKey="false" required="true" type="varchar" size="128" autoIncrement="false"/>
    <column name="down_path" primaryKey="false" required="false" type="varchar" size="128" />
    <column name="is_final" required="true" type="NUMERIC" size="5,0" autoIncrement="false"/>
    <column name="is_child" required="true" type="NUMERIC" size="5,0" autoIncrement="false"/>
    <column name="is_revival" required="true" type="NUMERIC" size="5,0" autoIncrement="false"/>
    <unique name="ess_UN_n">
      <unique-column name="name"/>
    </unique>
  </table>
  <table name="eav_sc_showcase_fields">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="showcase_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="meta_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="attribute_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="attribute_path" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="column_name" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="type" required="true" type="NUMERIC" size="5,0" autoIncrement="false"/>
    <!--<foreign-key foreignTable="eav_sc_showcases" name="FK_eav_sc_showcases">
      <reference local="showcase_id" foreign="id"/>
    </foreign-key>-->
  </table>
  <table name="eav_sc_bad_entities">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="sc_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="report_date" primaryKey="false" required="true" type="DATE" size="128" autoIncrement="false"/>
    <column name="stack_trace" primaryKey="false" required="false" type="VARCHAR" size="2048" autoIncrement="false"/>
    <column name="message" primaryKey="false" required="true" type="VARCHAR" size="2048" autoIncrement="false"/>
  </table>
</database>