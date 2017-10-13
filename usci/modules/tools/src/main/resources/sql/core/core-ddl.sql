create sequence SEQ_EAV_M_CLASSES_ID
/

create sequence SEQ_EAV_BE_BOOLEAN_VALUES_ID
/

create sequence SEQ_EAV_BE_BOOL_SET_VALUES_ID
/

create sequence SEQ_EAV_BE_COMPLEX_VALUES_ID
/

create sequence SEQ_EAV_BE_COMP_SET_VALUES_ID
/

create sequence SEQ_EAV_BE_DATE_SET_VALUES_ID
/

create sequence SEQ_EAV_BE_DATE_VALUES_ID
/

create sequence SEQ_EAV_BE_DOUBLE_VALUES_ID
/

create sequence SEQ_EAV_BE_DOUB_SET_VALUES_ID
/

create sequence SEQ_EAV_BE_ENTITIES_ID
/

create sequence SEQ_EAV_BE_ENTI_OMPLEX_SETS_ID
/

create sequence SEQ_EAV_M_COMPLEX_SET_ID
/

create sequence SEQ_EAV_M_COMPL_ATTRIBUTES_ID
/

create sequence SEQ_EAV_M_SIMPLE_ATTRIBUTES_ID
/

create sequence SEQ_EAV_M_SIMPLE_SET_ID
/

create sequence SEQ_BATCH_ENTRIES_ID
/

create sequence SEQ_EAV_A_CREDITOR_STATE_ID
/

create sequence SEQ_EAV_A_CREDITOR_USER_ID
/

create sequence SEQ_EAV_A_USER_CLASS_ID
/

create sequence SEQ_EAV_A_USER_ID
/

create sequence SEQ_EAV_BATCHES_ID
/

create sequence SEQ_EAV_BATCH_STATUSES_ID
/

create sequence SEQ_EAV_BE_ENTI_EPORT_DATES_ID
/

create sequence SEQ_EAV_BE_ENTI_SIMPLE_SETS_ID
/

create sequence SEQ_EAV_BE_INTEGER_VALUES_ID
/

create sequence SEQ_EAV_BE_INTE_SET_VALUES_ID
/

create sequence SEQ_EAV_BE_STRING_VALUES_ID
/

create sequence SEQ_EAV_BE_STRI_SET_VALUES_ID
/

create sequence SEQ_EAV_ENTITY_STATUSES_ID
/

create sequence SEQ_EAV_GLOBAL_ID
/

create sequence SEQ_EAV_OPTIMIZER_ID
/

create sequence SEQ_LOGIC_PACKAGES_ID
/

create sequence SEQ_LOGIC_RULES_HIS_ID
/

create sequence SEQ_LOGIC_RULES_ID
/

create sequence SEQ_LOGIC_RULE_PACKAGE_ID
/

create sequence SEQ_MNT_LOGS_ID
/

create sequence SEQ_MNT_OPERATIONS_ID
/

create sequence SEQ_EAV_A_USER_REF_ENTITY_ID
/

create sequence SEQ_EAV_A_USER_REF_ID
/

create sequence SEQ_EAV_LOGS_ID
/

create sequence SEQ_EAV_LOG_DELETES_ID
/

create sequence SEQ_EAV_REPORT_ID
/

create sequence SEQ_EAV_REPORT_MESSAGE_ID
/

create sequence SEQ_EAV_REPORT__ATTACHMENT_ID
/

create sequence SEQ_MAIL_MESSAGE_ID
/

create sequence SEQ_MAIL_MESSAGE_PARAMETER_ID
/

create sequence SEQ_MAIL_TEMPLATE_ID
/

create sequence SEQ_MAIL_TEMPLATE_PARAMETER_ID
/

create sequence SEQ_MAIL_USER_MAIL_TEMPLATE_ID
/

create table EAV_M_CLASSES
(
	ID NUMBER(14) not null
		primary key,
	COMPLEX_KEY_TYPE VARCHAR2(16),
	BEGIN_DATE DATE not null,
	IS_DISABLED NUMBER(1) not null,
	NAME VARCHAR2(64) not null,
	TITLE VARCHAR2(512),
	PARENT_IS_KEY NUMBER(1) not null,
	IS_CLOSABLE NUMBER(1) not null,
	IS_REFERENCE NUMBER(1) not null
)
/

create unique index EMC_UN_N
	on EAV_M_CLASSES (NAME)
/

create index EMC_IN_N_ID
	on EAV_M_CLASSES (NAME, IS_DISABLED)
/

create index EMC_IN_IR
	on EAV_M_CLASSES (IS_REFERENCE)
/

create trigger TRG_EAV_M_CLASSES_ID
	before insert
	on EAV_M_CLASSES
	for each row
BEGIN SELECT seq_eav_m_classes_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_BOOLEAN_SET_VALUES
(
	ID NUMBER(14) not null
		primary key,
	SET_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE NUMBER(1) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create index EBBSV_IN_SI_CI_V_RD_IC
	on EAV_BE_BOOLEAN_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE, IS_CLOSED)
/

create index EBBSV_IN_SI_CI_V_RD
	on EAV_BE_BOOLEAN_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE)
/

create index EBBSV_IN_SI_RD_IC
	on EAV_BE_BOOLEAN_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBBSV_IN_SI_CI_V_IL
	on EAV_BE_BOOLEAN_SET_VALUES (SET_ID, CREDITOR_ID, VALUE, IS_LAST)
/

create trigger TRG_EAV_BE_BOOL_SET_VALUES_ID
	before insert
	on EAV_BE_BOOLEAN_SET_VALUES
	for each row
BEGIN SELECT seq_eav_be_bool_set_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_BOOLEAN_VALUES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE NUMBER(1) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBBV_UN_EI_AI_CI_RD
	on EAV_BE_BOOLEAN_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBBV_IN_EI_AI_CI_RD_IC
	on EAV_BE_BOOLEAN_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBBV_IN_EI_AI_CI_IL
	on EAV_BE_BOOLEAN_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBBV_IN_EI_RD
	on EAV_BE_BOOLEAN_VALUES (ENTITY_ID, REPORT_DATE)
/

create index EBBV_IN_AI_CI_V_IC_IL
	on EAV_BE_BOOLEAN_VALUES (ATTRIBUTE_ID, CREDITOR_ID, VALUE)
/

create trigger TRG_EAV_BE_BOOLEAN_VALUES_ID
	before insert
	on EAV_BE_BOOLEAN_VALUES
	for each row
BEGIN SELECT seq_eav_be_boolean_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_COMPLEX_SET_VALUES
(
	ID NUMBER(14) not null
		primary key,
	SET_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	ENTITY_VALUE_ID NUMBER(14),
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create index EBCSV_IN_SI_CI_V_RD_IC
	on EAV_BE_COMPLEX_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, ENTITY_VALUE_ID, IS_CLOSED)
/

create index EBCSV_IN_SI_CI_V_RD
	on EAV_BE_COMPLEX_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, ENTITY_VALUE_ID)
/

create index EBCSV_IN_SI_RD_IC
	on EAV_BE_COMPLEX_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBCSV_IN_SI_CI_V_IL
	on EAV_BE_COMPLEX_SET_VALUES (SET_ID, CREDITOR_ID, ENTITY_VALUE_ID, IS_LAST)
/

create index EBCSV_IN_SI_CI_V_IL_IC
	on EAV_BE_COMPLEX_SET_VALUES (SET_ID, CREDITOR_ID, ENTITY_VALUE_ID)
/

create trigger TRG_EAV_BE_COMP_SET_VALUES_ID
	before insert
	on EAV_BE_COMPLEX_SET_VALUES
	for each row
BEGIN SELECT seq_eav_be_comp_set_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_COMPLEX_VALUES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	ENTITY_VALUE_ID NUMBER(14),
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBCV_UN_EI_AI_CI_RD
	on EAV_BE_COMPLEX_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBCV_IN_EI_AI_CI_RD_IC
	on EAV_BE_COMPLEX_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBCV_IN_EI_AI_CI_IL
	on EAV_BE_COMPLEX_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBCV_IN_AI_CI_IC_IL
	on EAV_BE_COMPLEX_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID)
/

create index EBCV_IN_EI_RD
	on EAV_BE_COMPLEX_VALUES (ENTITY_ID, REPORT_DATE)
/

create index EBCV_IN_EVI
	on EAV_BE_COMPLEX_VALUES (ENTITY_VALUE_ID)
/

create trigger TRG_EAV_BE_COMPLEX_VALUES_ID
	before insert
	on EAV_BE_COMPLEX_VALUES
	for each row
BEGIN SELECT seq_eav_be_complex_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_DATE_SET_VALUES
(
	ID NUMBER(14) not null
		primary key,
	SET_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE DATE not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create index EBDASV_IN_SI_CI_V_RD_IC
	on EAV_BE_DATE_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE, IS_CLOSED)
/

create index EBDASV_IN_SI_CI_V_RD
	on EAV_BE_DATE_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE)
/

create index EBDASV_IN_SI_RD_IC
	on EAV_BE_DATE_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBDASV_IN_SI_CI_V_IL
	on EAV_BE_DATE_SET_VALUES (SET_ID, CREDITOR_ID, VALUE, IS_LAST)
/

create trigger TRG_EAV_BE_DATE_SET_VALUES_ID
	before insert
	on EAV_BE_DATE_SET_VALUES
	for each row
BEGIN SELECT seq_eav_be_date_set_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_DATE_VALUES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE DATE not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBDAV_UN_EI_AI_CI_RD
	on EAV_BE_DATE_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBDAV_IN_EI_AI_CI_RD_IC
	on EAV_BE_DATE_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBDAV_IN_EI_AI_CI_IL
	on EAV_BE_DATE_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBDAV_IN_EI_RD
	on EAV_BE_DATE_VALUES (ENTITY_ID, REPORT_DATE)
/

create index EBDAV_IN_AI_CI_V_IC_IL
	on EAV_BE_DATE_VALUES (ATTRIBUTE_ID, CREDITOR_ID, VALUE)
/

create trigger TRG_EAV_BE_DATE_VALUES_ID
	before insert
	on EAV_BE_DATE_VALUES
	for each row
BEGIN SELECT seq_eav_be_date_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_DOUBLE_SET_VALUES
(
	ID NUMBER(14) not null
		primary key,
	SET_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE NUMBER(17,3) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create index EBDOSV_IN_SI_CI_V_RD_IC
	on EAV_BE_DOUBLE_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE, IS_CLOSED)
/

create index EBDOSV_IN_SI_CI_V_RD
	on EAV_BE_DOUBLE_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE)
/

create index EBDOSV_IN_SI_RD_IC
	on EAV_BE_DOUBLE_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBDOSV_IN_SI_CI_V_IL
	on EAV_BE_DOUBLE_SET_VALUES (SET_ID, CREDITOR_ID, VALUE, IS_LAST)
/

create trigger TRG_EAV_BE_DOUB_SET_VALUES_ID
	before insert
	on EAV_BE_DOUBLE_SET_VALUES
	for each row
BEGIN SELECT seq_eav_be_doub_set_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_DOUBLE_VALUES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE NUMBER(17,3) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBDOV_UN_EI_AI_CI_RD
	on EAV_BE_DOUBLE_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBDOV_IN_EI_AI_CI_RD_IC
	on EAV_BE_DOUBLE_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBDOV_IN_EI_AI_CI_IL
	on EAV_BE_DOUBLE_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBDOV_IN_EI_RD
	on EAV_BE_DOUBLE_VALUES (ENTITY_ID, REPORT_DATE)
/

create index EBDOV_IN_AI_CI_V_IC_IL
	on EAV_BE_DOUBLE_VALUES (ATTRIBUTE_ID, CREDITOR_ID, VALUE)
/

create trigger TRG_EAV_BE_DOUBLE_VALUES_ID
	before insert
	on EAV_BE_DOUBLE_VALUES
	for each row
BEGIN SELECT seq_eav_be_double_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_ENTITIES
(
	ID NUMBER(14) not null
		primary key,
	CLASS_ID NUMBER(14)
)
/

create index EBE_IN_CI
	on EAV_BE_ENTITIES (CLASS_ID)
/

create trigger TRG_EAV_BE_ENTITIES_ID
	before insert
	on EAV_BE_ENTITIES
	for each row
BEGIN SELECT seq_eav_be_entities_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_ENTITY_COMPLEX_SETS
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14) not null,
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14) not null,
	REPORT_DATE DATE not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBECS_UN_EI_CI_AI_RD
	on EAV_BE_ENTITY_COMPLEX_SETS (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBECS_IN_EI_CI_AI_SI_RD_IC
	on EAV_BE_ENTITY_COMPLEX_SETS (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBECS_IN_EI_AI_SI_IL
	on EAV_BE_ENTITY_COMPLEX_SETS (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBECS_IN_EI_IL
	on EAV_BE_ENTITY_COMPLEX_SETS (ENTITY_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBECS_IN_ID_AI_CI
	on EAV_BE_ENTITY_COMPLEX_SETS (ATTRIBUTE_ID, ID)
/

create trigger TRG_EAV_BE_ENTI_OMPLEX_SETS_ID
	before insert
	on EAV_BE_ENTITY_COMPLEX_SETS
	for each row
BEGIN SELECT seq_eav_be_enti_omplex_sets_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_M_COMPLEX_ATTRIBUTES
(
	ID NUMBER(14) not null
		primary key,
	CONTAINING_ID NUMBER(14),
	CONTAINER_TYPE NUMBER(10) not null,
	NAME VARCHAR2(64) not null,
	TITLE VARCHAR2(512),
	IS_KEY NUMBER(1) not null,
	IS_OPTIONAL_KEY NUMBER(1) not null,
	IS_NULLABLE_KEY NUMBER(1) not null,
	IS_REQUIRED NUMBER(1) not null,
	IS_NULLABLE NUMBER(1) not null,
	IS_IMMUTABLE NUMBER(1) not null,
	IS_FINAL NUMBER(1) not null,
	IS_DISABLED NUMBER(1) not null,
	CLASS_ID NUMBER(14)
)
/

create index EMCA_IN_CI_CT
	on EAV_M_COMPLEX_ATTRIBUTES (CONTAINING_ID, CONTAINER_TYPE)
/

create index EMCA_IN_CI
	on EAV_M_COMPLEX_ATTRIBUTES (CLASS_ID)
/

create trigger TRG_EAV_M_COMPL_ATTRIBUTES_ID
	before insert
	on EAV_M_COMPLEX_ATTRIBUTES
	for each row
BEGIN SELECT seq_eav_m_compl_attributes_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_M_COMPLEX_SET
(
	ID NUMBER(14) not null
		primary key,
	CONTAINING_ID NUMBER(14),
	CONTAINER_TYPE NUMBER(10) not null,
	NAME VARCHAR2(64) not null,
	TITLE VARCHAR2(512),
	IS_KEY NUMBER(1) not null,
	IS_NULLABLE NUMBER(1) not null,
	IS_REQUIRED NUMBER(1) not null,
	ARRAY_KEY_TYPE VARCHAR2(16),
	CLASS_ID NUMBER(14),
	IS_IMMUTABLE NUMBER(1) not null,
	IS_CUMULATIVE NUMBER(1) not null,
	IS_FINAL NUMBER(1) not null,
	IS_REFERENCE NUMBER(1) not null,
	IS_DISABLED NUMBER(1) not null
)
/

create index EMCS_IN_CI_CT
	on EAV_M_COMPLEX_SET (CONTAINING_ID, CONTAINER_TYPE)
/

create trigger TRG_EAV_M_COMPLEX_SET_ID
	before insert
	on EAV_M_COMPLEX_SET
	for each row
BEGIN SELECT seq_eav_m_complex_set_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_M_SIMPLE_ATTRIBUTES
(
	ID NUMBER(14) not null
		primary key,
	CONTAINING_ID NUMBER(14),
	CONTAINER_TYPE NUMBER(10) not null,
	NAME VARCHAR2(64) not null,
	TITLE VARCHAR2(512),
	IS_KEY NUMBER(1) not null,
	IS_OPTIONAL_KEY NUMBER(1) not null,
	IS_NULLABLE_KEY NUMBER(1) not null,
	IS_REQUIRED NUMBER(1) not null,
	IS_NULLABLE NUMBER(1) not null,
	IS_IMMUTABLE NUMBER(1) not null,
	IS_FINAL NUMBER(1) not null,
	IS_DISABLED NUMBER(1) not null,
	TYPE_CODE VARCHAR2(16)
)
/

create index EMSA_IN_CI_CT
	on EAV_M_SIMPLE_ATTRIBUTES (CONTAINING_ID, CONTAINER_TYPE)
/

create index EMSA_IN_IF
	on EAV_M_SIMPLE_ATTRIBUTES (IS_FINAL)
/

create trigger TRG_EAV_M_SIMPLE_ATTRIBUTES_ID
	before insert
	on EAV_M_SIMPLE_ATTRIBUTES
	for each row
BEGIN SELECT seq_eav_m_simple_attributes_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_M_SIMPLE_SET
(
	ID NUMBER(14) not null
		primary key,
	CONTAINING_ID NUMBER(14),
	CONTAINER_TYPE NUMBER(10) not null,
	NAME VARCHAR2(64) not null,
	TITLE VARCHAR2(512),
	IS_KEY NUMBER(1) not null,
	IS_NULLABLE NUMBER(1) not null,
	IS_REQUIRED NUMBER(1) not null,
	ARRAY_KEY_TYPE VARCHAR2(16),
	TYPE_CODE VARCHAR2(16),
	IS_IMMUTABLE NUMBER(1) not null,
	IS_CUMULATIVE NUMBER(1) not null,
	IS_FINAL NUMBER(1) not null,
	IS_REFERENCE NUMBER(1) not null,
	IS_DISABLED NUMBER(1) not null
)
/

create index EMSS_IN_CI_CT
	on EAV_M_SIMPLE_SET (CONTAINING_ID, CONTAINER_TYPE)
/

create trigger TRG_EAV_M_SIMPLE_SET_ID
	before insert
	on EAV_M_SIMPLE_SET
	for each row
BEGIN SELECT seq_eav_m_simple_set_id.nextval INTO :new.id FROM dual; END;
/

create table BATCH_ENTRIES
(
	ID NUMBER(14) not null
		primary key,
	USER_ID NUMBER(14) not null,
	VALUE CLOB not null,
	REPORT_DATE DATE not null,
	UPDATED_DATE DATE not null,
	ENTITY_ID NUMBER(14),
	IS_MAINTENANCE NUMBER(1)
)
/

create trigger TRG_BATCH_ENTRIES_ID
	before insert
	on BATCH_ENTRIES
	for each row
BEGIN SELECT seq_batch_entries_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_A_CREDITOR_STATE
(
	ID NUMBER(14) not null
		primary key,
	CREDITOR_ID NUMBER(14) not null,
	REPORT_DATE DATE not null
)
/

create trigger TRG_EAV_A_CREDITOR_STATE_ID
	before insert
	on EAV_A_CREDITOR_STATE
	for each row
BEGIN SELECT seq_eav_a_creditor_state_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_A_CREDITOR_USER
(
	ID NUMBER(14) not null
		primary key,
	USER_ID NUMBER(14) not null,
	CREDITOR_ID NUMBER(14) not null
)
/

create unique index EACU_UN_UI_CI
	on EAV_A_CREDITOR_USER (USER_ID, CREDITOR_ID)
/

create trigger TRG_EAV_A_CREDITOR_USER_ID
	before insert
	on EAV_A_CREDITOR_USER
	for each row
BEGIN SELECT seq_eav_a_creditor_user_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_A_USER
(
	ID NUMBER(14) not null
		primary key,
	USER_ID NUMBER(14) not null,
	SCREEN_NAME VARCHAR2(128),
	EMAIL VARCHAR2(128),
	FIRST_NAME VARCHAR2(128),
	LAST_NAME VARCHAR2(128),
	MIDDLE_NAME VARCHAR2(128),
	MODIFIED_DATE DATE,
	IS_ACTIVE NUMBER(1) default 1  not null
)
/

create unique index EAU_UN_UI
	on EAV_A_USER (USER_ID)
/

create trigger TRG_EAV_A_USER_ID
	before insert
	on EAV_A_USER
	for each row
BEGIN SELECT seq_eav_a_user_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_A_USER_CLASS
(
	ID NUMBER(14) not null
		primary key,
	USER_ID NUMBER(14) not null,
	META_NAME VARCHAR2(64) not null
)
/

create index EAUC_IN_UI_MN
	on EAV_A_USER_CLASS (USER_ID, META_NAME)
/

create trigger TRG_EAV_A_USER_CLASS_ID
	before insert
	on EAV_A_USER_CLASS
	for each row
BEGIN SELECT seq_eav_a_user_class_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BATCHES
(
	ID NUMBER(14) not null
		primary key,
	USER_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	RECEIPT_DATE DATE not null,
	REP_DATE DATE,
	FILE_NAME VARCHAR2(2048),
	HASH VARCHAR2(128),
	SIGN VARCHAR2(4000),
	SIGN_INFO VARCHAR2(4000),
	SIGN_TIME DATE,
	BATCH_TYPE VARCHAR2(1),
	TOTAL_COUNT NUMBER(14),
	ACTUAL_COUNT NUMBER(14),
	REPORT_ID NUMBER(14),
	IS_DISABLED NUMBER(1) default 0  not null,
	IS_MAINTENANCE NUMBER(1) default 0  not null,
	IS_MAINTENANCE_APPROVED NUMBER(1) default 0  not null,
	IS_MAINTENANCE_DECLINED NUMBER(1) default 0  not null
)
/

create index EB_IN_CI
	on EAV_BATCHES (CREDITOR_ID)
/

create trigger TRG_EAV_BATCHES_ID
	before insert
	on EAV_BATCHES
	for each row
BEGIN SELECT seq_eav_batches_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BATCH_STATUSES
(
	ID NUMBER(14) not null
		primary key,
	BATCH_ID NUMBER(14),
	STATUS_ID NUMBER(14),
	RECEIPT_DATE DATE not null,
	DESCRIPTION VARCHAR2(512)
)
/

create index EBS_IN_BI
	on EAV_BATCH_STATUSES (BATCH_ID)
/

create trigger TRG_EAV_BATCH_STATUSES_ID
	before insert
	on EAV_BATCH_STATUSES
	for each row
BEGIN SELECT seq_eav_batch_statuses_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_ENTITY_REPORT_DATES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14) not null,
	CREDITOR_ID NUMBER(14) not null,
	REPORT_DATE DATE not null,
	INTEGER_VALUES_COUNT NUMBER(14) not null,
	DATE_VALUES_COUNT NUMBER(14) not null,
	STRING_VALUES_COUNT NUMBER(14) not null,
	BOOLEAN_VALUES_COUNT NUMBER(14) not null,
	DOUBLE_VALUES_COUNT NUMBER(14) not null,
	COMPLEX_VALUES_COUNT NUMBER(14) not null,
	SIMPLE_SETS_COUNT NUMBER(14) not null,
	COMPLEX_SETS_COUNT NUMBER(14) not null,
	IS_CLOSED NUMBER(1) default 0
)
/

create unique index EBERD_UN_EI_RD
	on EAV_BE_ENTITY_REPORT_DATES (ENTITY_ID, REPORT_DATE)
/

create trigger TRG_EAV_BE_ENTI_EPORT_DATES_ID
	before insert
	on EAV_BE_ENTITY_REPORT_DATES
	for each row
BEGIN SELECT seq_eav_be_enti_eport_dates_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_ENTITY_SIMPLE_SETS
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBESS_UN_EI_CI_AI_RD
	on EAV_BE_ENTITY_SIMPLE_SETS (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBESS_IN_EI_CI_AI_SI_RD_IC
	on EAV_BE_ENTITY_SIMPLE_SETS (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBESS_IN_EI_AI_SI_IL
	on EAV_BE_ENTITY_SIMPLE_SETS (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBESS_IN_EI_IL
	on EAV_BE_ENTITY_SIMPLE_SETS (ENTITY_ID, REPORT_DATE)
/

create trigger TRG_EAV_BE_ENTI_SIMPLE_SETS_ID
	before insert
	on EAV_BE_ENTITY_SIMPLE_SETS
	for each row
BEGIN SELECT seq_eav_be_enti_simple_sets_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_INTEGER_SET_VALUES
(
	ID NUMBER(14) not null
		primary key,
	SET_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE NUMBER(10) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create index EBISV_IN_SI_CI_V_RD_IC
	on EAV_BE_INTEGER_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE, IS_CLOSED)
/

create index EBISV_IN_SI_CI_V_RD
	on EAV_BE_INTEGER_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE)
/

create index EBISV_IN_SI_RD_IC
	on EAV_BE_INTEGER_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBISV_IN_SI_CI_V_IL
	on EAV_BE_INTEGER_SET_VALUES (SET_ID, CREDITOR_ID, VALUE, IS_LAST)
/

create trigger TRG_EAV_BE_INTE_SET_VALUES_ID
	before insert
	on EAV_BE_INTEGER_SET_VALUES
	for each row
BEGIN SELECT seq_eav_be_inte_set_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_INTEGER_VALUES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE NUMBER(10) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBIV_UN_EI_AI_CI_RD
	on EAV_BE_INTEGER_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBIV_IN_EI_AI_CI_RD_IC
	on EAV_BE_INTEGER_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBIV_IN_EI_AI_CI_IL
	on EAV_BE_INTEGER_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBIV_IN_EI_RD
	on EAV_BE_INTEGER_VALUES (ENTITY_ID, REPORT_DATE)
/

create index EBIV_IN_AI_CI_V_IC_IL
	on EAV_BE_INTEGER_VALUES (ATTRIBUTE_ID, CREDITOR_ID, VALUE)
/

create trigger TRG_EAV_BE_INTEGER_VALUES_ID
	before insert
	on EAV_BE_INTEGER_VALUES
	for each row
BEGIN SELECT seq_eav_be_integer_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_STRING_SET_VALUES
(
	ID NUMBER(14) not null
		primary key,
	SET_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE VARCHAR2(1024) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create index EBSSV_IN_SI_CI_V_RD_IC
	on EAV_BE_STRING_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE, IS_CLOSED)
/

create index EBSSV_IN_SI_CI_V_RD
	on EAV_BE_STRING_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, VALUE)
/

create index EBSSV_IN_SI_RD_IC
	on EAV_BE_STRING_SET_VALUES (SET_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBSSV_IN_SI_CI_V_IL
	on EAV_BE_STRING_SET_VALUES (SET_ID, CREDITOR_ID, VALUE, IS_LAST)
/

create trigger TRG_EAV_BE_STRI_SET_VALUES_ID
	before insert
	on EAV_BE_STRING_SET_VALUES
	for each row
BEGIN SELECT seq_eav_be_stri_set_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_BE_STRING_VALUES
(
	ID NUMBER(14) not null
		primary key,
	ENTITY_ID NUMBER(14),
	ATTRIBUTE_ID NUMBER(14),
	CREDITOR_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE VARCHAR2(1024) not null,
	IS_CLOSED NUMBER(1) not null,
	IS_LAST NUMBER(1) not null
)
/

create unique index EBSV_UN_EI_AI_CI_RD
	on EAV_BE_STRING_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE)
/

create index EBSV_IN_EI_AI_CI_RD_IC
	on EAV_BE_STRING_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, REPORT_DATE, IS_CLOSED)
/

create index EBSV_IN_EI_AI_CI_IL
	on EAV_BE_STRING_VALUES (ENTITY_ID, ATTRIBUTE_ID, CREDITOR_ID, IS_LAST)
/

create index EBSV_IN_EI_RD
	on EAV_BE_STRING_VALUES (ENTITY_ID, REPORT_DATE)
/

create index EBSV_IN_AI_CI_V_IC_IL
	on EAV_BE_STRING_VALUES (ATTRIBUTE_ID, CREDITOR_ID, VALUE)
/

create trigger TRG_EAV_BE_STRING_VALUES_ID
	before insert
	on EAV_BE_STRING_VALUES
	for each row
BEGIN SELECT seq_eav_be_string_values_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_ENTITY_STATUSES
(
	ID NUMBER(14) not null
		primary key,
	BATCH_ID NUMBER(14),
	ENTITY_ID NUMBER(14),
	RECEIPT_DATE DATE not null,
	INDEX_ NUMBER(14),
	STATUS_ID NUMBER(14),
	OPERATION VARCHAR2(512),
	DESCRIPTION VARCHAR2(512),
	ERROR_CODE VARCHAR2(512),
	DEV_DESCRIPTION VARCHAR2(512)
)
/

create index EES_IN_BI
	on EAV_ENTITY_STATUSES (BATCH_ID)
/

create trigger TRG_EAV_ENTITY_STATUSES_ID
	before insert
	on EAV_ENTITY_STATUSES
	for each row
BEGIN SELECT seq_eav_entity_statuses_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_GLOBAL
(
	ID NUMBER(14) not null
		primary key,
	TYPE VARCHAR2(256) not null,
	CODE VARCHAR2(256) not null,
	VALUE VARCHAR2(512) not null,
	DESCRIPTION VARCHAR2(1024) not null
)
/

create unique index EG_UN_T_C
	on EAV_GLOBAL (TYPE, CODE)
/

create trigger TRG_EAV_GLOBAL_ID
	before insert
	on EAV_GLOBAL
	for each row
BEGIN SELECT seq_eav_global_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_OPTIMIZER
(
	ID NUMBER(14) not null
		primary key,
	CREDITOR_ID NUMBER(14) not null,
	META_ID NUMBER(14) not null,
	ENTITY_ID NUMBER(14),
	KEY_STRING VARCHAR2(128) not null
)
/

create unique index EO_UN_CI_MI_KS
	on EAV_OPTIMIZER (CREDITOR_ID, META_ID, KEY_STRING)
/

create unique index EO_UN_EI
	on EAV_OPTIMIZER (ENTITY_ID)
/

create trigger TRG_EAV_OPTIMIZER_ID
	before insert
	on EAV_OPTIMIZER
	for each row
BEGIN SELECT seq_eav_optimizer_id.nextval INTO :new.id FROM dual; END;
/

create table LOGIC_PACKAGES
(
	ID NUMBER(14) not null
		primary key,
	NAME VARCHAR2(1024),
	NAME_RU VARCHAR2(1024),
	NAME_KZ VARCHAR2(1024),
	DESCRIPTION VARCHAR2(1024)
)
/

create trigger TRG_LOGIC_PACKAGES_ID
	before insert
	on LOGIC_PACKAGES
	for each row
BEGIN SELECT seq_logic_packages_id.nextval INTO :new.id FROM dual; END;
/

create table LOGIC_RULES
(
	ID NUMBER(14) not null
		primary key,
	RULE VARCHAR2(4000),
	TITLE VARCHAR2(1024),
	TITLE_RU VARCHAR2(1024),
	TITLE_KZ VARCHAR2(1024),
	OPEN_DATE DATE not null,
	CLOSE_DATE DATE
)
/

create trigger TRG_LOGIC_RULES_ID
	before insert
	on LOGIC_RULES
	for each row
BEGIN SELECT seq_logic_rules_id.nextval INTO :new.id FROM dual; END;
/

create table LOGIC_RULES_HIS
(
	ID NUMBER(14) not null
		primary key,
	RULE_ID NUMBER(14) not null,
	RULE VARCHAR2(4000),
	TITLE VARCHAR2(1024),
	TITLE_RU VARCHAR2(1024),
	TITLE_KZ VARCHAR2(1024),
	OPEN_DATE DATE not null,
	CLOSE_DATE DATE not null
)
/

create trigger TRG_LOGIC_RULES_HIS_ID
	before insert
	on LOGIC_RULES_HIS
	for each row
BEGIN SELECT seq_logic_rules_his_id.nextval INTO :new.id FROM dual; END;
/

create table LOGIC_RULE_PACKAGE
(
	ID NUMBER(14) not null
		primary key,
	RULE_ID NUMBER(14) not null,
	PACKAGE_ID NUMBER(14) not null
)
/

create trigger TRG_LOGIC_RULE_PACKAGE_ID
	before insert
	on LOGIC_RULE_PACKAGE
	for each row
BEGIN SELECT seq_logic_rule_package_id.nextval INTO :new.id FROM dual; END;
/

create table MNT_LOGS
(
	ID NUMBER(14) not null
		primary key,
	MNT_OPERATION_ID NUMBER(14) not null,
	FOREIGN_ID NUMBER(14) not null,
	EXECUTION_TIME DATE not null,
	STATUS NUMBER(1) not null,
	ERROR_MSG VARCHAR2(250),
	CONTRACT_NO VARCHAR2(64),
	CONTRACT_DATE DATE not null,
	CREDIT_ID NUMBER(14)
)
/

create trigger TRG_MNT_LOGS_ID
	before insert
	on MNT_LOGS
	for each row
BEGIN SELECT seq_mnt_logs_id.nextval INTO :new.id FROM dual; END;
/

create table MNT_OPERATIONS
(
	ID NUMBER(14) not null
		primary key,
	NAME VARCHAR2(250)
)
/

create trigger TRG_MNT_OPERATIONS_ID
	before insert
	on MNT_OPERATIONS
	for each row
BEGIN SELECT seq_mnt_operations_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_A_USER_REF
(
	ID NUMBER(14) not null
		primary key,
	USER_ID NUMBER(14) not null,
	META_NAME VARCHAR2(64) not null,
	ENTITY_ID NUMBER(14) not null
)
/

create unique index EAUR_UN_UI_MN_EI
	on EAV_A_USER_REF (USER_ID, META_NAME, ENTITY_ID)
/

create trigger TRG_EAV_A_USER_REF_ENTITY_ID
	before insert
	on EAV_A_USER_REF
	for each row
BEGIN SELECT seq_eav_a_user_ref_entity_id.nextval INTO :new.entity_id FROM dual; END;
/

create trigger TRG_EAV_A_USER_REF_ID
	before insert
	on EAV_A_USER_REF
	for each row
BEGIN SELECT seq_eav_a_user_ref_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_LOGS
(
	ID NUMBER(14) not null
		primary key,
	PORTLETNAME VARCHAR2(4000),
	PORTALUSERNAME VARCHAR2(4000),
	PORTLETCOMMENT VARCHAR2(4000),
	RECEIPT_DATE DATE not null
)
/

create trigger TRG_EAV_LOGS_ID
	before insert
	on EAV_LOGS
	for each row
BEGIN SELECT seq_eav_logs_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_LOG_DELETES
(
	ID NUMBER(14) not null
		primary key,
	BATCH_ID NUMBER(14) not null,
	USER_ID NUMBER(14) not null,
	BASE_VALUE_ID NUMBER(14) not null,
	CLASS_NAME VARCHAR2(50) not null,
	CREDITOR_ID NUMBER(14),
	CONTAINER_ID NUMBER(14),
	REPORT_DATE DATE not null,
	VALUE VARCHAR2(1024) not null,
	STACKTRACE VARCHAR2(4000) not null,
	RECEIPT_DATE DATE not null
)
/

create trigger TRG_EAV_LOG_DELETES_ID
	before insert
	on EAV_LOG_DELETES
	for each row
BEGIN SELECT seq_eav_log_deletes_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_REPORT
(
	ID NUMBER(14) not null
		primary key,
	CREDITOR_ID NUMBER(14) not null,
	TOTAL_COUNT NUMBER(14) not null,
	ACTUAL_COUNT NUMBER(14) not null,
	BEG_DATE DATE,
	END_DATE DATE,
	REPORT_DATE DATE not null,
	STATUS_ID NUMBER(14) not null,
	USERNAME VARCHAR2(80),
	LAST_MANUAL_EDIT_DATE DATE
)
/

create unique index ER_UN_CI_RD
	on EAV_REPORT (CREDITOR_ID, REPORT_DATE)
/

create trigger TRG_EAV_REPORT_ID
	before insert
	on EAV_REPORT
	for each row
BEGIN SELECT seq_eav_report_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_REPORT_MESSAGE
(
	ID NUMBER(14) not null
		primary key,
	REPORT_ID NUMBER(14) not null,
	USERNAME VARCHAR2(80) not null,
	SEND_DATE DATE,
	TEXT VARCHAR2(1000)
)
/

create trigger TRG_EAV_REPORT_MESSAGE_ID
	before insert
	on EAV_REPORT_MESSAGE
	for each row
BEGIN SELECT seq_eav_report_message_id.nextval INTO :new.id FROM dual; END;
/

create table EAV_REPORT_MESSAGE_ATTACHMENT
(
	ID NUMBER(14) not null
		primary key,
	REPORT_MESSAGE_ID NUMBER(14) not null,
	FILENAME VARCHAR2(1000),
	CONTENT BLOB
)
/

create trigger TRG_EAV_REPORT__ATTACHMENT_ID
	before insert
	on EAV_REPORT_MESSAGE_ATTACHMENT
	for each row
BEGIN SELECT seq_eav_report__attachment_id.nextval INTO :new.id FROM dual; END;
/

create table MAIL_MESSAGE
(
	ID NUMBER(14) not null
		primary key,
	RECIPIENT_USER_ID NUMBER(14) not null,
	STATUS_ID NUMBER(14) not null,
	MAIL_TEMPLATE_ID NUMBER(14) not null,
	CREATION_DATE DATE,
	SENDING_DATE DATE
)
/

create trigger TRG_MAIL_MESSAGE_ID
	before insert
	on MAIL_MESSAGE
	for each row
BEGIN SELECT seq_mail_message_id.nextval INTO :new.id FROM dual; END;
/

create table MAIL_MESSAGE_PARAMETER
(
	ID NUMBER(14) not null
		primary key,
	MAIL_MESSAGE_ID NUMBER(14) not null,
	MAIL_TEMPLATE_PARAMETER_ID NUMBER(14) not null,
	VALUE VARCHAR2(2048)
)
/

create trigger TRG_MAIL_MESSAGE_PARAMETER_ID
	before insert
	on MAIL_MESSAGE_PARAMETER
	for each row
BEGIN SELECT seq_mail_message_parameter_id.nextval INTO :new.id FROM dual; END;
/

create table MAIL_TEMPLATE
(
	ID NUMBER(14) not null
		primary key,
	SUBJECT VARCHAR2(1000),
	TEXT VARCHAR2(3000),
	CODE VARCHAR2(30),
	NAME_RU VARCHAR2(300),
	NAME_KZ VARCHAR2(300),
	CONFIGURATION_TYPE_ID NUMBER(14)
)
/

create trigger TRG_MAIL_TEMPLATE_ID
	before insert
	on MAIL_TEMPLATE
	for each row
BEGIN SELECT seq_mail_template_id.nextval INTO :new.id FROM dual; END;
/

create table MAIL_TEMPLATE_PARAMETER
(
	ID NUMBER(14) not null
		primary key,
	MAIL_TEMPLATE_ID NUMBER(14) not null,
	CODE VARCHAR2(20) not null,
	ORDER_NUMBER NUMBER(14)
)
/

create trigger TRG_MAIL_TEMPLATE_PARAMETER_ID
	before insert
	on MAIL_TEMPLATE_PARAMETER
	for each row
BEGIN SELECT seq_mail_template_parameter_id.nextval INTO :new.id FROM dual; END;
/

create table MAIL_USER_MAIL_TEMPLATE
(
	ID NUMBER(14) not null
		primary key,
	PORTAL_USER_ID NUMBER(14) not null,
	MAIL_TEMPLATE_ID NUMBER(14) not null,
	ENABLED NUMBER(1) default 0  not null
)
/

create trigger TRG_MAIL_USER_MAIL_TEMPLATE_ID
	before insert
	on MAIL_USER_MAIL_TEMPLATE
	for each row
BEGIN SELECT seq_mail_user_mail_template_id.nextval INTO :new.id FROM dual; END;
/

CREATE VIEW V_COMPLEX_ATTRIBUTE AS SELECT
    a.id,
    a.containing_id,
    m1.name AS containing_name,
    a.container_type,
    a.name,
    a.title,
    a.is_key,
    a.is_optional_key,
    a.is_required,
    a.is_nullable,
    a.is_immutable,
    a.is_final,
    a.is_disabled,
    a.class_id,
    m2.name as class_name,
    a.is_nullable_key
  FROM eav_m_complex_attributes a,
    eav_m_classes m1,
    eav_m_classes m2
  WHERE a.containing_id = m1.id
    and a.class_id = m2.id
/

CREATE VIEW V_SIMPLE_ATTRIBUTE AS SELECT
    a.id,
    a.containing_id,
    m1.name AS containing_name,
    a.container_type,
    a.name,
    a.title,
    a.is_key,
    a.is_optional_key,
    a.is_required,
    a.is_nullable,
    a.is_immutable,
    a.is_final,
    a.is_disabled,
    a.type_code,
    a.is_nullable_key
  FROM eav_m_simple_attributes a,
    eav_m_classes m1
  WHERE a.containing_id = m1.id
/

