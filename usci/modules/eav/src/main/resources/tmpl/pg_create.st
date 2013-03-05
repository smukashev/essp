---------- META_CLASS

CREATE TABLE IF NOT EXISTS $meta_objects$
(
    id serial NOT NULL,
    CONSTRAINT $meta_objects$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $classes$
(
    complex_key_type character varying($complex_key_length$),
    begin_date TIMESTAMP WITH TIME ZONE NOT NULL,
    is_disabled BOOLEAN NOT NULL,
    name character varying($class_name_length$) NOT NULL,
    CONSTRAINT $classes$_primary_key_index PRIMARY KEY (id),
    UNIQUE (name, begin_date)
)
INHERITS
(
    $meta_objects$
);

CREATE TABLE IF NOT EXISTS $attributes$
(
    containing_id int,
    name character varying($attribute_name_length$) NOT NULL,
    is_key boolean NOT NULL,
    is_nullable boolean NOT NULL,
    CONSTRAINT $attributes$_primary_key_index PRIMARY KEY (id ),
    UNIQUE (containing_id, name)
)
INHERITS
(
    $meta_objects$
);

CREATE TABLE IF NOT EXISTS $simple_attributes$
(
type_code character varying($type_code_length$),
CONSTRAINT $simple_attributes$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $attributes$
);

CREATE TABLE IF NOT EXISTS $complex_attributes$
(
    class_id int,
    CONSTRAINT $complex_attributes$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $attributes$
);

CREATE TABLE IF NOT EXISTS $sets$
(
    array_key_type character varying($array_key_length$),
    CONSTRAINT $sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $attributes$
);

CREATE TABLE IF NOT EXISTS $simple_sets$
(
    CONSTRAINT $simple_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $sets$,
    $simple_attributes$
);

CREATE TABLE IF NOT EXISTS $complex_sets$
(
    CONSTRAINT $complex_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $sets$,
    $complex_attributes$
);

CREATE TABLE IF NOT EXISTS $set_of_sets$
(
    CONSTRAINT $set_of_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $sets$
);

CREATE TABLE IF NOT EXISTS $sets_key_filter$
(
    id serial NOT NULL,
    attribute_id int,
    attribute_name character varying($attribute_name_length$) NOT NULL,
    CONSTRAINT $sets_key_filter$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $sets_key_filter_values$
(
    id serial NOT NULL,
    filter_id int,
    value character varying($array_key_filter_length$) NOT NULL,
    CONSTRAINT $sets_key_filter_values$_primary_key_index PRIMARY KEY (id)
);

---------- BASE_ENTITY

CREATE TABLE IF NOT EXISTS $batches$
(
    id serial NOT NULL,
    receipt_date TIMESTAMP WITH TIME ZONE NOT NULL,
    begin_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    CONSTRAINT $batches$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $be_entities$
(
    id serial NOT NULL,
    class_id int references $classes$(id) ON DELETE CASCADE,
    CONSTRAINT $be_entities$_primary_key_index PRIMARY KEY (id )
);

CREATE TABLE IF NOT EXISTS $be_values$
(
    id serial NOT NULL,
    entity_id int references $be_entities$ (id) ON DELETE CASCADE,
    batch_id bigint references $batches$ (id) ON DELETE CASCADE,
    attribute_id int references $attributes$ (id) ON DELETE CASCADE,
    index bigint,
    CONSTRAINT $be_values$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $be_date_values$
(
    value DATE,
    CONSTRAINT $be_date_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_date_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_date_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $be_values$
);

CREATE TABLE IF NOT EXISTS $be_double_values$
(
    value double precision,
    CONSTRAINT $be_double_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_double_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_double_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $be_values$
);

CREATE TABLE IF NOT EXISTS $be_integer_values$
(
    value integer,
    CONSTRAINT $be_integer_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_integer_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_integer_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $be_values$
);

CREATE TABLE IF NOT EXISTS $be_boolean_values$
(
    value boolean,
    CONSTRAINT $be_boolean_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_boolean_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_boolean_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $be_values$
);

CREATE TABLE IF NOT EXISTS $be_string_values$
(
    value character varying($string_value_length$),
    CONSTRAINT $be_string_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_string_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_string_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $be_values$
);

CREATE TABLE IF NOT EXISTS $be_complex_values$
(
    entity_value_id bigint references $be_entities$(id),
    CONSTRAINT $be_complex_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_complex_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_complex_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $complex_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $be_values$
);

CREATE TABLE IF NOT EXISTS $be_sets$
(
    id serial NOT NULL,
    batch_id bigint references $batches$ (id) ON DELETE CASCADE,
    index bigint,
    CONSTRAINT $be_sets$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $be_entity_sets$
(
    id serial NOT NULL,
    entity_id int references $be_entities$ (id) ON DELETE CASCADE,
    attribute_id int references $sets$ (id) ON DELETE CASCADE,
    set_id int references $be_sets$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_entity_sets$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $be_entity_simple_sets$
(
    CONSTRAINT $be_entity_simple_sets$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_simple_sets$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_simple_sets$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
            ON DELETE CASCADE,
    CONSTRAINT $be_entity_simple_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $be_entity_sets$
);

CREATE TABLE IF NOT EXISTS $be_entity_complex_sets$
(
    CONSTRAINT $be_entity_complex_sets$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_complex_sets$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $complex_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_complex_sets$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_complex_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $be_entity_sets$
);

CREATE TABLE IF NOT EXISTS $be_entity_set_of_sets$
(
    CONSTRAINT $be_entity_set_of_sets$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $be_entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_set_of_sets$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $set_of_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_set_of_sets$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_entity_set_of_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $be_entity_sets$
);

CREATE TABLE IF NOT EXISTS $be_set_of_sets$
(
    id serial NOT NULL,
    parent_set_id int references $be_sets$ (id) ON DELETE CASCADE,
    child_set_id int references $be_sets$ (id) ON DELETE CASCADE,
    CONSTRAINT $be_set_of_sets$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $be_set_of_simple_sets$
(
    CONSTRAINT $be_set_of_simple_sets$_parent_set_id_fkey FOREIGN KEY (parent_set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_of_simple_sets$_child_set_id_fkey FOREIGN KEY (child_set_id) REFERENCES $be_sets$ (id)
            ON DELETE CASCADE,
    CONSTRAINT $be_set_of_simple_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $be_set_of_sets$
);

CREATE TABLE IF NOT EXISTS $be_set_of_complex_sets$
(
    CONSTRAINT $be_set_of_complex_sets$_parent_set_id_fkey FOREIGN KEY (parent_set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_of_complex_sets$_child_set_id_fkey FOREIGN KEY (child_set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_of_complex_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $be_set_of_sets$
);

CREATE TABLE IF NOT EXISTS $be_set_values$
(
    id serial NOT NULL,
    set_id bigint references $be_sets$(id) ON DELETE CASCADE,
    batch_id bigint references $batches$(id) ON DELETE CASCADE,
    index bigint, CONSTRAINT $be_set_values$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $be_set_dates_values$
(
    value DATE,
    CONSTRAINT $be_set_dates_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_dates_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $be_set_values$
);

CREATE TABLE IF NOT EXISTS $be_set_double_values$
(
    value double precision,
    CONSTRAINT $be_set_double_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_double_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $be_set_values$
);

CREATE TABLE IF NOT EXISTS $be_set_integer_values$
(
    value integer,
    CONSTRAINT $be_set_integer_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_integer_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $be_set_values$
);

CREATE TABLE IF NOT EXISTS $be_set_boolean_values$
(
    value boolean,
    CONSTRAINT $be_set_boolean_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_boolean_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $be_set_values$
);

CREATE TABLE IF NOT EXISTS $be_set_string_values$
(
    value character varying($string_value_length$),
    CONSTRAINT $be_set_string_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_string_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $be_set_values$
);

CREATE TABLE IF NOT EXISTS $be_set_complex_values$
(
    entity_value_id bigint references $be_entities$ (id),
    CONSTRAINT $be_set_complex_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $be_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $be_set_complex_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $be_set_values$
);

-------- INDEXES

CREATE OR REPLACE FUNCTION createIndexesOnFK () RETURNS VOID AS '
DECLARE
    v_rec RECORD;
BEGIN
    FOR v_rec IN

        select
            pg_index.indexrelid::regclass,
            ''create index '' || relname || ''_'' || array_to_string(column_name_list, ''_'') || ''_idx on '' || conrelid ||
                '' ('' || array_to_string(column_name_list, '','') || '')'' as query
        from
        (
            select distinct
                conrelid,
                array_agg(attname) column_name_list,
                array_agg(attnum) as column_list
            from pg_attribute
                join (select conrelid::regclass,
                        conname,
                        unnest(conkey) as column_index
                        from (select distinct
                                conrelid, conname, conkey
                              from pg_constraint
                                join pg_class on pg_class.oid = pg_constraint.conrelid
                                join pg_namespace on pg_namespace.oid = pg_class.relnamespace
                              where nspname !~ ''^pg_'' and nspname <> ''information_schema''
                            ) fkey
                    ) fkey
                on fkey.conrelid = pg_attribute.attrelid
                    and fkey.column_index = pg_attribute.attnum
            group by conrelid, conname
        ) candidate_index
            join pg_class on pg_class.oid = candidate_index.conrelid
            left join pg_index on pg_index.indrelid = conrelid
            and indkey::text = array_to_string(column_list, '' '')
        where indexrelid is null

    LOOP
        EXECUTE v_rec.query;
    END LOOP;
    RETURN;
END;
' LANGUAGE plpgsql;

SELECT createIndexesOnFK();

DO '
BEGIN

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = ''$attributes$_containing_id_idx''
    AND    n.nspname = ''public''
    ) THEN

    CREATE INDEX $attributes$_containing_id_idx ON $attributes$ (containing_id);
END IF;

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = ''$simple_attributes$_containing_id_idx''
    AND    n.nspname = ''public''
    ) THEN

    CREATE INDEX $simple_attributes$_containing_id_idx ON $simple_attributes$ (containing_id);
END IF;

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = ''$complex_attributes$_containing_id_idx''
    AND    n.nspname = ''public''
    ) THEN

    CREATE INDEX $complex_attributes$_containing_id_idx ON $complex_attributes$ (containing_id);
END IF;

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = ''$simple_sets$_containing_id_idx''
    AND    n.nspname = ''public''
    ) THEN

    CREATE INDEX $simple_sets$_containing_id_idx ON $simple_sets$ (containing_id);
END IF;

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = ''$complex_sets$_containing_id_idx''
    AND    n.nspname = ''public''
    ) THEN

    CREATE INDEX $complex_sets$_containing_id_idx ON $complex_sets$ (containing_id);
END IF;

IF NOT EXISTS (
    SELECT 1
    FROM   pg_class c
    JOIN   pg_namespace n ON n.oid = c.relnamespace
    WHERE  c.relname = ''$complex_attributes$_class_id_idx''
    AND    n.nspname = ''public''
    ) THEN

    CREATE INDEX $complex_attributes$_class_id_idx ON $complex_attributes$ (class_id);
END IF;

END';


