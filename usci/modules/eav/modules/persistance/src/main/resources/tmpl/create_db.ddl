<?xml version="1.0"?>
<!DOCTYPE database SYSTEM "http://db.apache.org/torque/dtd/database">
<database name="model">
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
    <unique>
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
    <unique>
      <unique-column name="user_id"/>
      <unique-column name="creditor_id"/>
    </unique>
	</table>
    <table name="batch_entries">
    <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    <column name="user_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    <column name="value" primaryKey="false" required="true" type="CLOB" size="14,0" autoIncrement="false"/>
    <column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
    <column name="updated_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
    <unique>
    <unique-column name="id"/>
    </unique>
    </table>
	<!--<table name="eav_m_array_key_filter">
      <column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
      <column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
      <column name="attribute_name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
      <unique>
        <unique-column name="id"/>
      </unique>
    </table>-->
	<table name="eav_m_array_key_filter_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="filter_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
	</table>
	<table name="eav_m_attributes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique>
			<unique-column name="containing_id"/>
			<unique-column name="name"/>
		</unique>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_001_01">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
		<index name="ind_m_001_02">
			<index-column name="name"/>
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
		<unique>
			<unique-column name="id"/>
		</unique>
	</table>
	<table name="eav_m_classes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="complex_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="begin_date" primaryKey="false" required="true" type="DATE" autoIncrement="false"/>
		<column name="is_disabled" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="parent_is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique>
			<unique-column name="name"/>
			<unique-column name="begin_date"/>
		</unique>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_002_01">
			<index-column name="begin_date"/>
			<index-column name="name"/>
			<index-column name="is_disabled"/>
		</index>
		<index name="ind_m_002_02">
			<index-column name="begin_date"/>
		</index>
		<index name="ind_m_002_03">
			<index-column name="is_reference"/>
		</index>
	</table>
	<table name="eav_m_complex_attributes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_required" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="class_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_003_01">
			<index-column name="class_id"/>
		</index>
		<index name="ind_m_003_02">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_complex_set">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="class_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_004_01">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_set_key_filter">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attr_name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_005_01">
			<index-column name="set_id"/>
		</index>
	</table>
	<table name="eav_meta_object">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<unique>
			<unique-column name="id"/>
		</unique>
	</table>
	<table name="eav_m_set">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_006_01">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_set_of_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_007_01">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_simple_attributes">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_required" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="type_code" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_008_01">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<table name="eav_m_simple_set">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="containing_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="container_type" required="true" type="NUMERIC" size="10,0"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
		<column name="is_key" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_nullable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="array_key_type" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="type_code" primaryKey="false" required="false" type="VARCHAR" size="16" autoIncrement="false"/>
		<column name="is_immutable" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_final" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_reference" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_m_009_01">
			<index-column name="containing_id"/>
			<index-column name="container_type"/>
		</index>
	</table>
	<!-- EAV_BATCHES -->
	<table name="eav_batches">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="user_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="receipt_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="begin_date" primaryKey="false" required="false" type="TIMESTAMP" autoIncrement="false"/>
		<column name="end_date" primaryKey="false" required="false" type="TIMESTAMP" autoIncrement="false"/>
		<column name="rep_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
	</table>
	<!-- EAV_BE_BOOLEAN_SET_VALUES -->
	<table name="eav_be_boolean_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_002_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_002_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_002_00">
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_002_01">
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<!-- EAV_BE_BOOLEAN_VALUES -->
	<table name="eav_be_boolean_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_003_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_003_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_003_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_003_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_003_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_003_02">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_003_03">
			<index-column name="attribute_id"/>
			<index-column name="value"/>
		</index>
		<unique name="eav_uk_003_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<!-- EAV_BE_COMPLEX_SET_VALUES -->
	<table name="eav_be_complex_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="entity_value_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_004_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_004_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_004_02">
			<reference local="entity_value_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_004_00">
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_004_01">
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_004_02">
			<index-column name="set_id"/>
		</index>
		<index name="eav_ind_004_03">
			<index-column name="entity_value_id"/>
		</index>
		<index name="eav_ind_004_04">
			<index-column name="entity_value_id"/>
			<index-column name="set_id"/>
		</index>
	</table>
	<!-- EAV_BE_COMPLEX_VALUES -->
	<table name="eav_be_complex_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="entity_value_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_005_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_005_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_complex_attributes" name="eav_fk_005_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_005_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_005_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="is_last"/>
		</index>
		<unique name="eav_uk_005_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
		<index name="eav_ind_005_02">
			<index-column name="attribute_id"/>
			<index-column name="entity_value_id"/>
		</index>
		<index name="eav_ind_005_03">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_005_04">
			<index-column name="attribute_id"/>
			<index-column name="entity_value_id"/>
			<index-column name="entity_id"/>
		</index>
	</table>
	<!-- EAV_BE_DATE_SET_VALUES -->
	<table name="eav_be_date_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_006_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_006_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_006_00">
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_006_01">
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<!-- EAV_BE_DATE_VALUES -->
	<table name="eav_be_date_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_007_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_007_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_007_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_007_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_007_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_007_02">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_007_03">
			<index-column name="attribute_id"/>
			<index-column name="value"/>
		</index>
		<unique name="eav_uk_007_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<!-- EAV_BE_DOUBLE_SET_VALUES -->
	<table name="eav_be_double_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="17,3" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_008_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_008_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_008_00">
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_008_01">
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<!-- EAV_BE_DOUBLE_VALUES -->
	<table name="eav_be_double_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="17,3" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_009_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_009_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_009_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_009_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_009_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_009_02">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_009_03">
			<index-column name="attribute_id"/>
			<index-column name="value"/>
		</index>
		<unique name="eav_uk_009_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="eav_be_entities">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="class_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="deleted" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="0"/>
		<!--<foreign-key foreignTable="eav_m_classes" name="eav_fk_010_00">
			<reference local="class_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_010_00">
			<index-column name="class_id"/>
		</index>
		<index name="eav_ind_010_01">
			<index-column name="id"/>
			<index-column name="class_id"/>
		</index>
		<unique name="eav_uk_010_00">
		  <unique-column name="id"/>
		  <unique-column name="deleted"/>
		</unique>
	</table>
	<table name="eav_be_entity_complex_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
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
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_011_03">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_011_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_011_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_011_02">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_011_03">
			<index-column name="attribute_id"/>
			<index-column name="set_id"/>
		</index>
		<index name="eav_ind_011_04">
			<index-column name="attribute_id"/>
			<index-column name="set_id"/>
			<index-column name="entity_id"/>
		</index>
		<unique name="eav_uk_011_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="eav_be_entity_report_dates">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="integer_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="date_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="string_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="boolean_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="double_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="complex_values_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="simple_sets_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="complex_sets_count" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_012_00">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<unique name="eav_ind_012_00">
			<unique-column name="entity_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="eav_be_entity_simple_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
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
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_014_03">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_014_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_014_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
		<unique name="eav_uk_014_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="eav_be_integer_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="10,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_015_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_015_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_015_00">
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_015_01">
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<table name="eav_be_integer_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="NUMERIC" size="10,0" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_016_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_016_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_016_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_016_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_016_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_016_02">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_016_03">
			<index-column name="attribute_id"/>
			<index-column name="value"/>
		</index>
		<unique name="eav_uk_016_00">
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="eav_be_set_of_complex_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="parent_set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="child_set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_017_00">
			<reference local="child_set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_017_01">
			<reference local="parent_set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_017_02">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_017_00">
			<index-column name="parent_set_id"/>
			<index-column name="child_set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_017_01">
			<index-column name="parent_set_id"/>
			<index-column name="child_set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<table name="eav_be_set_of_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="parent_set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="child_set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_018_00">
			<reference local="child_set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_018_01">
			<reference local="parent_set_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_018_00">
			<index-column name="parent_set_id"/>
			<index-column name="child_set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_018_01">
			<index-column name="parent_set_id"/>
			<index-column name="child_set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<table name="eav_be_set_of_simple_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="parent_set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="child_set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_be_sets" name="fk_1044">
			<reference local="child_set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="fk_1045">
			<reference local="parent_set_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_batches" name="fk_1046">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_019_00">
			<index-column name="parent_set_id"/>
			<index-column name="child_set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_019_01">
			<index-column name="parent_set_id"/>
			<index-column name="child_set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<table name="eav_be_sets">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="level_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
	</table>
	<table name="eav_be_string_set_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="set_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_020_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_sets" name="eav_fk_020_01">
			<reference local="set_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_020_00">
			<index-column name="set_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_020_01">
			<index-column name="set_id"/>
			<index-column name="is_last"/>
		</index>
	</table>
	<table name="eav_be_string_values">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="entity_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="batch_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="attribute_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="index_" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="value" primaryKey="false" required="true" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="is_closed" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<column name="is_last" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false"/>
		<!--<foreign-key foreignTable="eav_batches" name="eav_fk_021_00">
			<reference local="batch_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_be_entities" name="eav_fk_021_01">
			<reference local="entity_id" foreign="id"/>
		</foreign-key>-->
		<!--<foreign-key foreignTable="eav_m_simple_attributes" name="eav_fk_021_02">
			<reference local="attribute_id" foreign="id"/>
		</foreign-key>-->
		<index name="eav_ind_021_00">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="report_date"/>
			<index-column name="is_closed"/>
		</index>
		<index name="eav_ind_021_01">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="is_last"/>
		</index>
		<index name="eav_ind_021_02">
			<index-column name="entity_id"/>
		</index>
		<index name="eav_ind_021_03">
			<index-column name="attribute_id"/>
			<index-column name="value"/>
		</index>
		<index name="eav_ind_021_04">
			<index-column name="entity_id"/>
			<index-column name="attribute_id"/>
			<index-column name="value"/>
		</index>
		<unique>
			<unique-column name="entity_id"/>
			<unique-column name="attribute_id"/>
			<unique-column name="report_date"/>
		</unique>
	</table>
	<table name="AUDIT_EVENT_KIND">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="100" autoIncrement="false"/>
		<column name="is_always_auditable" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="is_active" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false" default="1"/>
		<column name="code" primaryKey="false" required="true" type="VARCHAR" size="512" autoIncrement="false"/>
	</table>
	<table name="AUDIT_EVENT">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="kind_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="user_id" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="event_begin_d" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="event_begin_dt" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<column name="event_end_dt" primaryKey="false" required="false" type="TIMESTAMP" autoIncrement="false"/>
		<column name="is_success" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="err_code" primaryKey="false" required="false" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="err_msg" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="add_info" primaryKey="false" required="false" type="VARCHAR" size="4000" autoIncrement="false"/>
		<column name="table_name" primaryKey="false" required="false" type="VARCHAR" size="512" autoIncrement="false"/>
		<!--<foreign-key foreignTable="AUDIT_EVENT_KIND" name="FK_AUDIT_EVENT_AEK">
			<reference local="kind_id" foreign="id"/>
		</foreign-key>-->
	</table>
	<table name="sc_id_bag">
			<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
			<column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
			<column name="showcase_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
			<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
			<index name="eav_ind_022_01">
					<index-column name="entity_id"/>
					<index-column name="showcase_id"/>
          <index-column name="report_date"/>
			</index>
			<unique name="ind_uk_sc_003_00">
				<unique-column name="entity_id"/>
				<unique-column name="showcase_id"/>
        <unique-column name="report_date"/>
			</unique>
	</table>

	<!-- Table for showcase entity revisions -->
	<table name="sc_entities">
			<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
			<column name="entity_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
			<index name="eav_ind_023_01">
							<index-column name="entity_id"/>
					</index>
			<unique name="ind_uk_sc_023_00">
				<unique-column name="entity_id"/>
			</unique>
	</table>
	<!-- Table for showcase entity revisions -->

	<!-- DROOLS -->
	<table name="logic_packages">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="name" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="description" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="report_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_packages_01">
			<index-column name="id"/>
		</index>
	</table>
	<table name="logic_rules">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="rule" primaryKey="false" required="false" type="VARCHAR" size="2024" autoIncrement="false"/>
		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
		<column name="is_active" primaryKey="false" required="true" type="NUMERIC" size="1" autoIncrement="false" default="1"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_rules_01">
			<index-column name="id"/>
		</index>
	</table>
	<table name="logic_package_versions">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="package_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="open_date" primaryKey="false" required="true" type="TIMESTAMP" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_package_versions_01">
			<index-column name="id"/>
		</index>
		<index name="ind_package_versions_02">
			<index-column name="package_id"/>
		</index>
	</table>
	<table name="logic_rule_package_versions">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="rule_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<column name="package_versions_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
		<unique>
			<unique-column name="id"/>
		</unique>
		<index name="ind_rule_package_versions_01">
			<index-column name="id"/>
		</index>
		<index name="ind_rule_package_versions_02">
			<index-column name="rule_id"/>
		</index>
		<index name="ind_rule_package_versions_03">
			<index-column name="package_versions_id"/>
		</index>
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
		<index name="ind_023_00">
			<index-column name="user_id"/>
			<index-column name="meta_name"/>
		</index>
	</table>

	<table name="eav_a_user_ref">
		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<column name="user_id" required="true" type="NUMERIC" size="14,0"/>
		<column name="meta_name" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
		<column name="entity_id"  required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
		<unique>
			<unique-column name="user_id"/>
			<unique-column name="meta_name"/>
			<unique-column name="entity_id"/>
		</unique>
		<index name="ind_024_00">
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
    <unique name="uk_report__c_r">
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
  <table name="eav_a_sysconfig">
  	<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true" />
  	<column name="key_"  type="VARCHAR" size="100"  />
  	<column name="value_" type="VARCHAR" size="100" />
  </table>
</database>
