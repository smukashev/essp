  create table EAV_LOG_DELETES
(
  id            NUMBER(14) not null,
  batch_id      NUMBER(14),
  user_id       NUMBER(14),
  base_value_id NUMBER(14),
  class_name    VARCHAR2(50),
  creditor_id   NUMBER(14),
  container_id  NUMBER(14),
  report_date   DATE,
  value         VARCHAR2(1024),
  stacktrace    VARCHAR2(4000),
  receipt_date  DATE
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 80K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Create/Recreate primary, unique and foreign key constraints
alter table EAV_LOG_DELETES
  add primary key (ID)
  using index
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 80K
    next 1M
    minextents 1
    maxextents unlimited
  );

  CREATE OR REPLACE TRIGGER "CORE"."TRG_EAV_LOG_DELETES_ID" BEFORE INSERT ON eav_log_deletes FOR EACH ROW  WHEN (new.id IS NULL) BEGIN SELECT seq_eav_log_deletes_id.nextval INTO :new.id FROM dual; END;
/
ALTER TRIGGER "CORE"."TRG_EAV_LOG_DELETES_ID" ENABLE;



CREATE SEQUENCE  "CORE"."seq_eav_log_deletes_id"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE  NOPARTITION ;

