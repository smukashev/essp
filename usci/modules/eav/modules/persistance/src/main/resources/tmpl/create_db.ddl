<?xml version="1.0"?>
<!DOCTYPE database SYSTEM "http://db.apache.org/torque/dtd/database">
<database name="model">
	<table name="eav_m_classes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="complex_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="begin_date" primaryKey="false" required="true" type="DATE" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="parent_is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_closable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique name ="emc_UN_n">
			<unique-column name="name"/>
		</unique>
		<index name="emc_IN_n_id">
			<index-column name="name"/>
			<index-column name="is_disabled"/>
		</index>
		<index name="emc_IN_ir">
			<index-column name="is_reference"/>
		</index>
	</table>
	<table name="eav_m_complex_attributes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_optional_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_required" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="class_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<index name="emca_IN_ci">
			<index-column name="class_id"/>
		</index>
		<index name="emca_IN_ci_ct">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_complex_set">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_required" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="class_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_cumulative" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<index name="emcs_IN_ci_ct">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_simple_attributes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_optional_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_required" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="type_code" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<index name="emsa_IN_ci_ct">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
		<index name="emsa_IN_if">
			<index-column name="is_final"/>
		</index>
	</table>
	<table name="eav_m_simple_set">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_required" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="type_code" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_cumulative" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<index name="emss_IN_ci_ct">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_be_boolean_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_002_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
    <!-- previous value -->
    <!-- next value -->
		<index name="ebbsv_IN_si_ci_v_rd">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
		</index>
		<!-- closed value -->
		<index name="ebbsv_IN_si_ci_v_rd_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebbsv_IN_si_ci_v_il">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="value"/>
			<index-column name="is_last"/>
		</index>
		<!-- load values -->
		<index name="ebbsv_IN_si_rd_ic">
			<index-column name="set_id"/>
      <index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_boolean_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_003_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_003_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
    <!-- next value -->
    <!-- previous value -->
		<unique name="ebbv_UN_ei_ai_ci_rd">
			<unique-column name="entity_id" />
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
    <!-- closed value -->
		<index name="ebbv_IN_ei_ai_ci_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
    <!-- last value -->
		<index name="ebbv_IN_ei_ai_ci_il">
			<index-column name="entity_id"/>
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
    <!-- load values -->
		<index name="ebbv_IN_ei_rd">
			<index-column name="entity_id"/>
      <index-column name="report_date"/>
		</index>
		<!-- improved searcher -->
		<index name="ebbv_IN_ai_ci_v_ic_il">
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="value"/>
		</index>
	</table>
	<table name="eav_be_complex_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="entity_value_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_004_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_004_02">
			<reference local="entity_value_id" foreign="id"/>
		</foreign-key>-->
		<!-- previous value -->
		<!-- next value -->
		<index name="ebcsv_IN_si_ci_v_rd">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="entity_value_id"/>
		</index>
		<!-- closed value -->
		<index name="ebcsv_IN_si_ci_v_rd_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="entity_value_id"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebcsv_IN_si_ci_v_il">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="entity_value_id"/>
			<index-column name="is_last"/>
		</index>
		<!-- improved searcher -->
		<index name="ebcsv_IN_si_ci_v_il_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="entity_value_id"/>
		</index>
		<!-- load values -->
		<index name="ebcsv_IN_si_rd_ic">
			<index-column name="set_id"/>
      <index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_complex_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="entity_value_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_005_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_complex_attributes" name="eav_fk_005_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<!-- next value -->
    <!-- previous value -->
		<unique name="ebcv_UN_ei_ai_ci_rd">
			<unique-column name="entity_id" />
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
    <!-- closed value -->
		<index name="ebcv_IN_ei_ai_ci_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
    <!-- last value -->
		<index name="ebcv_IN_ei_ai_ci_il">
			<index-column name="entity_id"/>
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
    <!-- load values -->
		<index name="ebcv_IN_ei_rd">
			<index-column name="entity_id"/>
      <index-column name="report_date"/>
		</index>
		<!-- improved searcher -->
		<index name="ebcv_IN_ai_ci_ic_il">
		  <index-column name="entity_id"/>
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
		</index>
		<index name="EBCV_IN_EVI">
		  <index-column name="entity_value_id"/>
		</index>
	</table>
	<table name="eav_be_date_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_006_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!-- previous value -->
		<!-- next value -->
		<index name="ebdasv_IN_si_ci_v_rd">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
		</index>
		<!-- closed value -->
		<index name="ebdasv_IN_si_ci_v_rd_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebdasv_IN_si_ci_v_il">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="value"/>
			<index-column name="is_last"/>
		</index>
		<!-- load values -->
		<index name="ebdasv_IN_si_rd_ic">
			<index-column name="set_id"/>
      <index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_date_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_007_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_007_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<!-- next value -->
    <!-- previous value -->
		<unique name="ebdav_UN_ei_ai_ci_rd">
			<unique-column name="entity_id" />
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
    <!-- closed value -->
		<index name="ebdav_IN_ei_ai_ci_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
    <!-- last value -->
		<index name="ebdav_IN_ei_ai_ci_il">
			<index-column name="entity_id"/>
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
    <!-- load values -->
		<index name="ebdav_IN_ei_rd">
			<index-column name="entity_id"/>
      <index-column name="report_date"/>
		</index>
		<!-- improved searcher -->
		<index name="ebdav_IN_ai_ci_v_ic_il">
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="value"/>
		</index>
	</table>
	<table name="eav_be_double_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="17,3" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_008_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!-- previous value -->
		<!-- next value -->
		<index name="ebdosv_IN_si_ci_v_rd">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
		</index>
		<!-- closed value -->
		<index name="ebdosv_IN_si_ci_v_rd_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebdosv_IN_si_ci_v_il">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="value"/>
			<index-column name="is_last"/>
		</index>
		<!-- load values -->
		<index name="ebdosv_IN_si_rd_ic">
			<index-column name="set_id"/>
      <index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_double_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="17,3" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_009_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_009_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<!-- next value -->
    <!-- previous value -->
		<unique name="ebdov_UN_ei_ai_ci_rd">
			<unique-column name="entity_id" />
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
    <!-- closed value -->
		<index name="ebdov_IN_ei_ai_ci_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
    <!-- last value -->
		<index name="ebdov_IN_ei_ai_ci_il">
			<index-column name="entity_id"/>
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
    <!-- load values -->
		<index name="ebdov_IN_ei_rd">
			<index-column name="entity_id"/>
      <index-column name="report_date"/>
		</index>
		<!-- improved searcher -->
		<index name="ebdov_IN_ai_ci_v_ic_il">
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="value"/>
		</index>
	</table>
	<table name="eav_be_entities">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="class_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_m_classes" name="eav_fk_010_00">
			<reference local="class_id" foreign="id"/>
		</foreign-key>-->
		<index name="ebe_IN_ci">
			<index-column name="class_id"/>
		</index>
	</table>
	<table name="eav_be_entity_complex_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_011_00">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_complex_set" name="eav_fk_011_01">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_011_02">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!-- next value -->
		<!-- previous value -->
		<unique name="ebecs_UN_ei_ci_ai_rd">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
		<!-- closed value -->
		<index name="ebecs_IN_ei_ci_ai_si_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebecs_IN_ei_ai_si_il">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
		<!-- improved searcher -->
    <index name="ebecs_IN_id_ai_ci">
			<index-column name="attribute_id"/>
			<index-column name="id"/>
		</index>
		<!-- load values -->
		<index name="ebecs_IN_ei_il">
			<index-column name="entity_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
      <index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_entity_report_dates">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="integer_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="date_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="string_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="boolean_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="double_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="complex_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="simple_sets_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="complex_sets_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="false" default="0" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_012_00">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<unique name="eberd_UN_ei_rd">
			<unique-column name="entity_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="eav_be_entity_simple_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_014_00">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_014_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_set" name="eav_fk_014_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<!-- next value -->
		<!-- previous value -->
		<unique name="ebess_UN_ei_ci_ai_rd">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
		<!-- closed value -->
		<index name="ebess_IN_ei_ci_ai_si_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebess_IN_ei_ai_si_il">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
		<!-- load values -->
		<index name="ebess_IN_ei_il">
			<index-column name="entity_id"/>
			<index-column name="report_date"/>
		</index>
	</table>
	<table name="eav_be_integer_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="10,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_015_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!-- previous value -->
		<!-- next value -->
		<index name="ebisv_IN_si_ci_v_rd">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
		</index>
		<!-- closed value -->
		<index name="ebisv_IN_si_ci_v_rd_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebisv_IN_si_ci_v_il">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="value"/>
			<index-column name="is_last"/>
		</index>
		<!-- load values -->
		<index name="ebisv_IN_si_rd_ic">
			<index-column name="set_id"/>
      <index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_integer_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="10,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_016_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_016_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<!-- next value -->
    <!-- previous value -->
		<unique name="ebiv_UN_ei_ai_ci_rd">
			<unique-column name="entity_id" />
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
		</unique>
    <!-- closed value -->
		<index name="ebiv_IN_ei_ai_ci_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
    <!-- last value -->
		<index name="ebiv_IN_ei_ai_ci_il">
			<index-column name="entity_id"/>
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="is_last"/>
		</index>
    <!-- load values -->
		<index name="ebiv_IN_ei_rd">
			<index-column name="entity_id"/>
      <index-column name="report_date"/>
		</index>
		<!-- improved searcher -->
		<index name="ebiv_IN_ai_ci_v_ic_il">
      <index-column name="attribute_id"/>
      <index-column name="creditor_id"/>
			<index-column name="value"/>
		</index>
	</table>
	<table name="eav_be_string_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_020_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!-- previous value -->
		<!-- next value -->
		<index name="ebssv_IN_si_ci_v_rd">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
		</index>
		<!-- closed value -->
		<index name="ebssv_IN_si_ci_v_rd_ic">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="value"/>
			<index-column name="is_closed"/>
		</index>
		<!-- last value -->
		<index name="ebssv_IN_si_ci_v_il">
			<index-column name="set_id"/>
			<index-column name="creditor_id"/>
			<index-column name="value"/>
			<index-column name="is_last"/>
		</index>
		<!-- load values -->
		<index name="ebssv_IN_si_rd_ic">
			<index-column name="set_id"/>
      <index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
	</table>
	<table name="eav_be_string_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_021_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_021_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
    <!-- next value -->
    <!-- previous value -->
    <unique name="ebsv_UN_ei_ai_ci_rd">
			<unique-column name="entity_id" />
			<unique-column name="attribute_id"/>
			<unique-column name="creditor_id"/>
			<unique-column name="report_date"/>
    </unique>
    <!-- closed value -->
    <index name="ebsv_IN_ei_ai_ci_rd_ic">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
    </index>
    <!-- last value -->
    <index name="ebsv_IN_ei_ai_ci_il">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="is_last"/>
    </index>
    <!-- load values -->
    <index name="ebsv_IN_ei_rd">
			<index-column name="entity_id"/>
			<index-column name="report_date"/>
    </index>
    <!-- improved searcher -->
    <index name="ebsv_IN_ai_ci_v_ic_il">
			<index-column name="attribute_id"/>
			<index-column name="creditor_id"/>
			<index-column name="value"/>
    </index>
	</table>
	<table name="eav_a_user">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="user_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="screen_name" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="email" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="first_name" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="last_name" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="middle_name" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
    <column name="modified_date" primaryKey="false" required="false" type="DATE"/>
    <column name="is_active" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="1"/>
    <unique name="eau_UN_ui">
      <unique-column name="user_id"/>
    </unique>
	</table>
	<table name="eav_a_creditor_state">
      <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
      <column name="creditor_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
      <column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
      <!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_022_01">
        <reference local="creditor_id" foreign="id"/>
      </foreign-key>-->
	</table>
	<table name="eav_a_creditor_user">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="user_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <unique name="eacu_UN_ui_ci">
      <unique-column name="user_id"/>
      <unique-column name="creditor_id"/>
    </unique>
	</table>
	<table name="eav_batches">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="user_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="receipt_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="rep_date" primaryKey="false" required="false" type="TIMESTAMP" autoIncrement="false"/>
		<column name="file_name" primaryKey="false" required="false" type="VARCHAR" size="2048" autoIncrement="false"/>
		<column name="hash" primaryKey="false" required="false" type="VARCHAR" size="128" autoIncrement="false"/>
		<column name="sign" primaryKey="false" required="false" type="VARCHAR" size="4000" autoIncrement="false"/>
		<column name="batch_type" primaryKey="false" required="false" type="VARCHAR" size="1" autoIncrement="false"/>
		<column name="total_count" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="actual_count" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="0"/>
		<column name="is_maintenance" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="0"/>
		<column name="is_maintenance_approved" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="0"/>
		<column name="is_maintenance_declined" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="0"/>
		<index name="eb_IN_ci">
			<index-column name="creditor_id"/>
		</index>
	</table>
	<table name="eav_global">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="type" primaryKey="false" required="true" type="VARCHAR" size="256" autoIncrement="false"/>
		<column name="code" primaryKey="false" required="true" type="VARCHAR" size="256" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="description" primaryKey="false" required="true" type="VARCHAR" size="1024" autoIncrement="false"/>
		<unique name="eg_UN_t_c">
			<unique-column name="type"/>
			<unique-column name="code"/>
		</unique>
	</table>
	<table name="eav_batch_statuses">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="status_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="receipt_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="description" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_a_user" name="ebs_FK_eau_id">
			<reference local="user_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_global" name="ebs_FK_eg_id">
			<reference local="status_id" foreign="id"/>
		</foreign-key>-->
		<!--foreign-key foreignTable="eav_batches" name="ebs_FK_eb_id">
			<reference local="batch_id" foreign="id"/>
		</foreign-key-->
		<index name="ebs_IN_bi">
			<index-column name="batch_id"/>
		</index>
	</table>
	<table name="eav_entity_statuses">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="receipt_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="status_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="description" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="error_code" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<column name="dev_description" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_global" name="ees_FK_eg_id">
			<reference local="status_id" foreign="id"/>
		</foreign-key>-->
		<!--foreign-key foreignTable="eav_batches" name="ees_FK_eb_id">
			<reference local="batch_id" foreign="id"/>
		</foreign-key-->
		<!--foreign-key foreignTable="eav_be_entities" name="ees_FK_ebe_id">
			<reference local="entity_id" foreign="id"/>
		</foreign-key-->
		<index name="ees_IN_bi">
			<index-column name="batch_id"/>
		</index>
	</table>
	<table name="batch_entries">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="user_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="value" primaryKey="false" required="true" type="CLOB" size="14,0" autoIncrement="false"/>
    <column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
    <column name="updated_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
    <column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="is_maintenance" primaryKey="false" required="false" type="NUMERIC" size="1" autoIncrement="false"/>
  </table>
	<table name="eav_optimizer">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="creditor_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false" />
	  <column name="meta_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
	  <column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
	  <column name="key_string" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
	  <unique name="eo_UN_ci_mi_ks">
	    <unique-column name="creditor_id"/>
	    <unique-column name="meta_id"/>
      <unique-column name="key_string"/>
    </unique>
    <unique name="eo_UN_ei">
      <unique-column name="entity_id" />
    </unique>
	</table>
	<!-- DROOLS -->
	<table name="logic_packages">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="name" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="name_ru" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="name_kz" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="description" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
	</table>
	<table name="logic_rules">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="rule" primaryKey="false" required="false" type="VARCHAR" size="4000" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="title_ru" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="title_kz" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="open_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="close_date" primaryKey="false" required="false" type="TIMESTAMP" autoIncrement="false"/>
	</table>
	<table name="logic_rules_his">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="rule_id" required="true" type="NUMERIC" size="14,0"/>
		<column name="rule" primaryKey="false" required="false" type="VARCHAR" size="4000" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="title_ru" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="title_kz" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="open_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="close_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
	</table>
	<table name="logic_rule_package">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="rule_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="package_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
	</table>
	<!-- MAINTENANCE -->
	<table name="mnt_operations">
			<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
			<column name="name" type="VARCHAR" size="250" />
	</table>
	<table name="mnt_logs" >
			<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
			<column name="mnt_operation_id" required="true" type = "NUMERIC" size="14,0" />
			<column name="foreign_id" required="true" type = "NUMERIC" size="14,0" />
			<column name="execution_time" required="true" type = "TIMESTAMP"  />
			<column name="status" required="true" type="NUMERIC" size="1" />
			<column name="error_msg" type="VARCHAR" size="250" />
			<column name="contract_no" type = "VARCHAR" size="64" />
			<column name="contract_date" required="true" type = "TIMESTAMP"  />
			<column name="credit_id" type = "NUMERIC" size="14,0" />
	</table>
	<!-- entity_editor portlet tables -->
	<table name="eav_a_user_class">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="user_id" required="true" type="NUMERIC" size="14,0"/>
		<column name="meta_name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_m_classes" name="eav_fk_023_00">
			<reference local="meta_name" foreign="name"/>
		</foreign-key>-->
		<index name="eauc_IN_ui_mn">
			<index-column name="user_id"/>
			<index-column name="meta_name"/>
		</index>
	</table>
	<table name="eav_a_user_ref">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="user_id" required="true" type="NUMERIC" size="14,0"/>
		<column name="meta_name" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="entity_id"  required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<unique name="eaur_UN_ui_mn_ei">
			<unique-column name="user_id"/>
			<unique-column name="meta_name"/>
			<unique-column name="entity_id"/>
		</unique>
		<index name="eaur_IN_ui_mn_ei">
			<index-column name="user_id"/>
			<index-column name="meta_name"/>
			<index-column name="entity_id" />
		</index>
		<!--<foreign-key foreignTable="eav_m_classes" name="eav_fk_024_00">
			<reference local="meta_name" foreign="name"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_classes" name="eav_fk_024_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
	</table>
  <!-- BEGIN: tables for approval portlet-->
	<table name="eav_report">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="creditor_id" required="true" type="NUMERIC" size="14,0"/>
    <column name="total_count" required="true" type="NUMERIC" size="14,0"/>
    <column name="actual_count" required="true" type="NUMERIC" size="14,0"/>
    <column name="beg_date" required="false" type="DATE"/>
    <column name="end_date" required="false" type="DATE"/>
    <column name="report_date" required="true" type="DATE"/>
    <column name="status_id" required="true" type="NUMERIC" size="14,0"/>
    <column name="username" required="false" type="VARCHAR" size="80"/>
    <column name="last_manual_edit_date" required="false" type="DATE"/>
    <unique name="er_UN_ci_rd">
      <unique-column name="creditor_id"/>
      <unique-column name="report_date"/>
    </unique>
	</table>
  <table name="eav_report_message">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="report_id" required="true" type="NUMERIC" size="14,0"/>
    <column name="username" required="true" type="VARCHAR" size="80"/>
    <column name="send_date" required="false" type="DATE"/>
    <column name="text" required="false" type="VARCHAR" size="1000"/>
  </table>
  <table name="eav_report_message_attachment">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="report_message_id" required="true" type="NUMERIC" size="14,0"/>
    <column name="filename" required="false" type="VARCHAR" size="1000"/>
    <column name="content" required="false" type="BLOB"/>
  </table>
  <!-- END: tables for approval portlet-->
  <!--BEGIN: tables for notifications portlet -->
	<table name="mail_template">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="subject" type="VARCHAR" size="1000" />
	  <column name="text" type="VARCHAR" size="3000" />
	  <column name="code" type="VARCHAR" size="30" />
	  <column name="name_ru" type="VARCHAR" size="300" />
	  <column name="name_kz" type="VARCHAR" size="300" />
	  <column name="configuration_type_id" type="NUMERIC" size="14,0" />
	</table>
	<table name="mail_template_parameter">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="mail_template_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="code" required="true" type="VARCHAR" size="20" />
	  <column name="order_number"  type="NUMERIC" size="14,0" />
	</table>
	<table name="mail_message">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="recipient_user_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="status_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="mail_template_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="creation_date"  type="TIMESTAMP" />
	  <column name="sending_date"  type="TIMESTAMP" />
	</table>
	<table name="mail_message_parameter">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="mail_message_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="mail_template_parameter_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="value"  type="VARCHAR" size="1000" />
	</table>
	<table name="mail_user_mail_template">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="portal_user_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="mail_template_id" required="true" type="NUMERIC" size="14,0" />
	  <column name="enabled" required="true" type="NUMERIC" size="1" default="0"/>
	</table>
  <!--END: tables for notifications portlet -->
  <table name="eav_log_deletes">
	  <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="batch_id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="user_id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="base_value_id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
	  <column name="class_name" required="true" type="VARCHAR" size="50" />
	  <column name="creditor_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
	  <column name="container_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
	  <column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
	  <column name="value" primaryKey="false" required="true" type="VARCHAR" size="1024" autoIncrement="false"/>
	  <column name="stacktrace" primaryKey="false" required="true" type="VARCHAR" size="4000" autoIncrement="false"/>
	  <column name="RECEIPT_DATE" primaryKey="false" required="true" type="DATE" />
	</table>
</database>
