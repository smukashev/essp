---------- METADATA

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

---------- ENTITIES

CREATE TABLE IF NOT EXISTS $batches$
(
    id serial NOT NULL,
    receipt_date TIMESTAMP WITH TIME ZONE NOT NULL,
    begin_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    CONSTRAINT $batches$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $entities$
(
    id serial NOT NULL,
    class_id int references $classes$(id) ON DELETE CASCADE,
    CONSTRAINT $entities$_primary_key_index PRIMARY KEY (id )
);

CREATE TABLE IF NOT EXISTS $base_values$
(
    id serial NOT NULL,
    entity_id int references $entities$ (id) ON DELETE CASCADE,
    batch_id bigint references $batches$ (id) ON DELETE CASCADE,
    attribute_id int references $attributes$ (id) ON DELETE CASCADE,
    index bigint,
    CONSTRAINT $base_values$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $date_base_values$
(
    value DATE,
    CONSTRAINT $date_base_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $date_base_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $date_base_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $base_values$
);

CREATE TABLE IF NOT EXISTS $double_base_values$
(
    value double precision,
    CONSTRAINT $double_base_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $double_base_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $double_base_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $base_values$
);

CREATE TABLE IF NOT EXISTS $integer_base_values$
(
    value integer,
    CONSTRAINT $integer_base_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $integer_base_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $integer_base_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $base_values$
);

CREATE TABLE IF NOT EXISTS $boolean_base_values$
(
    value boolean,
    CONSTRAINT $boolean_base_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $boolean_base_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $boolean_base_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $base_values$
);

CREATE TABLE IF NOT EXISTS $string_base_values$
(
    value character varying($string_value_length$),
    CONSTRAINT $string_base_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $string_base_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $string_base_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $base_values$
);

CREATE TABLE IF NOT EXISTS $complex_base_values$
(
    entity_value_id bigint references $entities$(id),
    CONSTRAINT $complex_base_values$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $complex_base_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $complex_base_values$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $complex_attributes$ (id)
        ON DELETE CASCADE
)
INHERITS
(
    $base_values$
);

CREATE TABLE IF NOT EXISTS $base_sets$
(
    id serial NOT NULL,
    entity_id int references $entities$ (id) ON DELETE CASCADE,
    batch_id bigint references $batches$ (id) ON DELETE CASCADE,
    attribute_id int references $complex_attributes$ (id) ON DELETE CASCADE,
    index bigint,
    CONSTRAINT $base_sets$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $base_simple_sets$
(
    CONSTRAINT $base_simple_sets$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $base_simple_sets$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $base_simple_sets$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_simple_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $base_sets$
);

CREATE TABLE IF NOT EXISTS $base_complex_sets$
(
    CONSTRAINT $base_complex_sets$_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES $entities$ (id) ON DELETE CASCADE,
    CONSTRAINT $base_complex_sets$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE,
    CONSTRAINT $base_complex_sets$_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES $complex_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_complex_sets$_primary_key_index PRIMARY KEY (id)
)
INHERITS
(
    $base_sets$
);

CREATE TABLE IF NOT EXISTS $base_sets_values$
(
    id serial NOT NULL,
    set_id bigint references $base_sets$(id) ON DELETE CASCADE,
    batch_id bigint references $batches$(id) ON DELETE CASCADE,
    index bigint, CONSTRAINT $base_sets_values$_primary_key_index PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS $base_sets_dates_values$
(
    value DATE,
    CONSTRAINT $base_sets_dates_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $base_simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_sets_dates_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $base_sets_values$
);

CREATE TABLE IF NOT EXISTS $base_sets_double_values$
(
    value double precision,
    CONSTRAINT $base_sets_double_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $base_simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_sets_double_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $base_sets_values$
);

CREATE TABLE IF NOT EXISTS $base_sets_integer_values$
(
    value integer,
    CONSTRAINT $base_sets_integer_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $base_simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_sets_integer_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $base_sets_values$
);

CREATE TABLE IF NOT EXISTS $base_sets_boolean_values$
(
    value boolean,
    CONSTRAINT $base_sets_boolean_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $base_simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_sets_boolean_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $base_sets_values$
);

CREATE TABLE IF NOT EXISTS $base_sets_string_values$
(
    value character varying($string_value_length$),
    CONSTRAINT $base_sets_string_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $base_simple_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_sets_string_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $base_sets_values$
);

CREATE TABLE IF NOT EXISTS $base_sets_complex_values$
(
    entity_value_id bigint references $entities$ (id),
    CONSTRAINT $base_sets_complex_values$_set_id_fkey FOREIGN KEY (set_id) REFERENCES $base_complex_sets$ (id)
        ON DELETE CASCADE,
    CONSTRAINT $base_sets_complex_values$_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES $batches$ (id) ON DELETE CASCADE
)
INHERITS
(
    $base_sets_values$
);

