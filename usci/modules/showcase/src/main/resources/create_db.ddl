<?xml version="1.0"?>
<!DOCTYPE database SYSTEM "http://db.apache.org/torque/dtd/database">
<database name="model">
	<table name="eav_sc_showcases">
    		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
    		<column name="table_name" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
    		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
    		<column name="class_name" primaryKey="false" required="true" type="varchar" size="100" autoIncrement="false"/>
    		<column name="down_path" primaryKey="false" required="false" type="varchar" size="100" />
    		<unique name="ind_uk_sc_001_00">
    			<unique-column name="name"/>
    		</unique>
    	</table>
    	<table name="eav_sc_showcase_fields">
    		<column name="id" primaryKey="true" required="true" type="NUMERIC" size="14,0" autoIncrement="true"/>
    		<column name="name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
    		<column name="column_name" primaryKey="false" required="true" type="VARCHAR" size="128" autoIncrement="false"/>
    		<column name="title" primaryKey="false" required="false" type="VARCHAR" size="127" autoIncrement="false"/>
    		<column name="showcase_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    		<column name="attribute_id" primaryKey="false" required="true" type="NUMERIC" size="14,0" autoIncrement="false"/>
    		<column name="attribute_name" primaryKey="false" required="true" type="VARCHAR" size="64" autoIncrement="false"/>
    		<column name="attribute_path" primaryKey="false" required="false" type="VARCHAR" size="1024" autoIncrement="false"/>
    		<unique name="ind_uk_sc_002_00">
    			<unique-column name="name"/>
			    <unique-column name="showcase_id"/>
    		</unique>
    		<!--<foreign-key foreignTable="eav_sc_showcases" name="FK_eav_sc_showcases">
    			<reference local="showcase_id" foreign="id"/>
    		</foreign-key>-->
    	</table>
</database>