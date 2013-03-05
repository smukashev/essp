package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.storage;



import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STRawGroupDir;

import java.util.List;
import java.util.Map;


/**
 *
 * @author a.tkachenko
 */
@Repository
public class PostgreSQLStorageImpl extends JDBCSupport implements IStorage {
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLStorageImpl.class);

    private final static String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private final static String DROP_TABLE_CASCADE = "DROP TABLE IF EXISTS %s CASCADE";
    private final static String COUNT_TABLE = "SELECT count(*) FROM %s";

    @Autowired
    private STRawGroupDir stRawGroupDir;

    //TODO: Remove?
    //private final static String BATCH_FILES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL, file_data BYTEA NOT NULL, file_size double precision NOT NULL, file_name character varying(%d), batch_id int references %s(id) ON DELETE CASCADE, CONSTRAINT %s_primary_key_index PRIMARY KEY (id))";

    private void setTableNames(ST st)
    {
        st.add("meta_objects", getConfig().getMetaObjectTableName());
        st.add("classes", getConfig().getClassesTableName());
        st.add("attributes", getConfig().getAttributesTableName());
        st.add("simple_attributes", getConfig().getSimpleAttributesTableName());
        st.add("complex_attributes", getConfig().getComplexAttributesTableName());
        st.add("sets", getConfig().getSetTableName());
        st.add("simple_sets", getConfig().getSimpleSetTableName());
        st.add("complex_sets", getConfig().getComplexSetTableName());
        st.add("set_of_sets", getConfig().getSetOfSetsTableName());
        st.add("sets_key_filter", getConfig().getArrayKeyFilterTableName());
        st.add("sets_key_filter_values", getConfig().getArrayKeyFilterValuesTableName());

        st.add("batches", getConfig().getBatchesTableName());
        st.add("be_entities", getConfig().getEntitiesTableName());
        st.add("be_values", getConfig().getBaseValuesTableName());
        st.add("be_date_values", getConfig().getBaseDateValuesTableName());
        st.add("be_double_values", getConfig().getBaseDoubleValuesTableName());
        st.add("be_integer_values", getConfig().getBaseIntegerValuesTableName());
        st.add("be_boolean_values", getConfig().getBaseBooleanValuesTableName());
        st.add("be_string_values", getConfig().getBaseStringValuesTableName());
        st.add("be_complex_values", getConfig().getBaseComplexValuesTableName());

        st.add("be_sets", getConfig().getBaseSetsTableName());
        st.add("be_entity_sets", getConfig().getBaseEntitySetsTableName());
        st.add("be_entity_simple_sets", getConfig().getBaseEntitySimpleSetsTableName());
        st.add("be_entity_complex_sets", getConfig().getBaseEntityComplexSetsTableName());
        st.add("be_set_of_sets", getConfig().getBaseSetOfSetsTableName());
        st.add("be_set_of_simple_sets", getConfig().getBaseSetOfSimpleSetsTableName());
        st.add("be_set_of_complex_sets", getConfig().getBaseSetOfComplexSetsTableName());

        st.add("be_set_values", getConfig().getBaseSetValuesTableName());
        st.add("be_set_dates_values", getConfig().getBaseDateSetValuesTableName());
        st.add("be_set_double_values", getConfig().getBaseDoubleSetValuesTableName());
        st.add("be_set_integer_values", getConfig().getBaseIntegerSetValuesTableName());
        st.add("be_set_boolean_values", getConfig().getBaseBooleanSetValuesTableName());
        st.add("be_set_string_values", getConfig().getBaseStringSetValuesTableName());
        st.add("be_set_complex_values", getConfig().getBaseComplexSetValuesTableName());

        st.add("complex_key_length", getConfig().getComplexKeyTypeCodeLength());
        st.add("class_name_length", getConfig().getClassNameLength());
        st.add("attribute_name_length", getConfig().getAttributeNameLength());
        st.add("type_code_length", getConfig().getTypeCodeLength());
        st.add("array_key_length", getConfig().getArrayKeyTypeCodeLength());
        st.add("string_value_length", getConfig().getStringValueLength());
        st.add("array_key_filter_length", getConfig().getArrayKeyFilterValueLength());
    }

    @Override
	public void initialize() {
        ST st = stRawGroupDir.getInstanceOf("pg_create");

        setTableNames(st);

        String query = st.render();

        logger.debug(query);

        jdbcTemplate.execute(query);
	}

	@Override
	public void clear() {

        ST st = stRawGroupDir.getInstanceOf("pg_drop");

        setTableNames(st);

        String query = st.render();

        logger.debug(query);

        jdbcTemplate.execute(query);
    }

	@Override
	public void empty() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public boolean isClean() {
        String query = String.format(COUNT_TABLE, getConfig().getArrayKeyFilterValuesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getArrayKeyFilterValuesTableName() + " is not clean.");
            return false;
        }

        //array key filter
        query = String.format(COUNT_TABLE, getConfig().getArrayKeyFilterTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getArrayKeyFilterTableName() + " is not clean.");
            return false;
        }

        //attributes
        query = String.format(COUNT_TABLE, getConfig().getAttributesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getAttributesTableName() + " is not clean.");
            return false;
        }

        //entities
        query = String.format(COUNT_TABLE, getConfig().getEntitiesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getEntitiesTableName() + " is not clean.");
            return false;
        }

        //classes
        query = String.format(COUNT_TABLE, getConfig().getClassesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getClassesTableName() + " is not clean.");
            return false;
        }

        //batches
        query = String.format(COUNT_TABLE, getConfig().getBatchesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getBatchesTableName() + " is not clean.");
            return false;
        }

        //values
        query = String.format(COUNT_TABLE, getConfig().getBaseValuesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getBaseValuesTableName() + " is not clean.");
            return false;
        }

        return true;
    }

    public STRawGroupDir getStRawGroupDir()
    {
        return stRawGroupDir;
    }
}
