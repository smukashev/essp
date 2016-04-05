 declare
 l_errors  NUMBER;
 v_trigger_ddl varchar2(4000 CHAR);
 v_trigger_enable_ddl VARCHAR2(4000 CHAR);
 begin

begin
DBMS_REDEFINITION.can_redef_table(USER, 'EAV_BE_ENTITIES');

exception when others then
  dbms_redefinition.abort_redef_table(uname      => USER,
    orig_table => 'EAV_BE_ENTITIES',
    int_table  => 'EAV_BE_ENTITIES_NEW');

end;

DBMS_REDEFINITION.can_redef_table(USER, 'EAV_BE_ENTITIES');
select dbms_metadata.get_ddl('TRIGGER', 'TRG_EAV_BE_ENTITIES_ID', 'CORE') --change schema
into v_trigger_ddl
from dual;
execute immediate 'drop trigger TRG_EAV_BE_ENTITIES_ID';
v_trigger_enable_ddl:= substr(v_trigger_ddl, instr(v_trigger_ddl, 'ALTER'), length(v_trigger_ddl) - instr(v_trigger_ddl, 'ALTER') + 1);
v_trigger_ddl := substr(v_trigger_ddl, 1, instr(v_trigger_ddl, 'ALTER') - 1);


execute immediate 'CREATE TABLE EAV_BE_ENTITIES_NEW
   (	"ID" NUMBER(14,0) NOT NULL ENABLE,
	"CLASS_ID" NUMBER(14,0),
	 PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE
   ) SEGMENT CREATION IMMEDIATE
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"
  partition by list(CLASS_ID)(
  partition class_6 values(6),
  partition class_11 values(11),
  partition class_12 values(12),
  partition class_13 values(13),
  partition class_14 values(14),
  partition class_15 values(15),
  partition class_16 values(16),
  partition class_17 values(17),
  partition class_18 values(18),
  partition class_19 values(19),
  partition class_20 values(20),
  partition class_21 values(21),
  partition class_22 values(22),
  partition class_23 values(23),
  partition class_24 values(24),
  partition class_25 values(25) ,
  partition class_26 values(26),
  partition class_27 values(27),
  partition class_28 values(28),
  partition class_29 values(29),
  partition class_37 values(37),
  partition class_39 values(39),
  partition class_40 values(40),
  partition class_41 values(41),
  partition class_42 values(42),
  partition class_43 values(43),
  partition class_44 values(44),
  partition class_45 values(45),
  partition class_46 values(46),
  partition class_47 values(47),
  partition class_48 values(48),
  partition class_58 values(57),
  partition class_59 values(59),
  partition class_others values(DEFAULT)
  )';

begin

 DBMS_REDEFINITION.start_redef_table(
    uname      => USER,
    orig_table => 'EAV_BE_ENTITIES',
    int_table  => 'EAV_BE_ENTITIES_NEW');


  dbms_redefinition.sync_interim_table(
    uname      => USER,
    orig_table => 'EAV_BE_ENTITIES',
    int_table  => 'EAV_BE_ENTITIES_NEW');

DBMS_REDEFINITION.copy_table_dependents(
    uname            => USER,
    orig_table       => 'EAV_BE_ENTITIES',
    int_table        => 'EAV_BE_ENTITIES_NEW',
    copy_indexes     => 0,
    copy_triggers    => false,
    copy_constraints => false,
    copy_privileges  => TRUE,
    ignore_errors    => FALSE,
    num_errors       => l_errors,
    copy_statistics  => FALSE,
    copy_mvlog       => FALSE);

  DBMS_OUTPUT.put_line('Errors=' || l_errors);

 dbms_redefinition.finish_redef_table(
    uname      => USER,
    orig_table => 'EAV_BE_ENTITIES',
    int_table  => 'EAV_BE_ENTITIES_NEW');
exception when others then
 dbms_redefinition.abort_redef_table(uname      => USER,
                                          orig_table => 'EAV_BE_ENTITIES',
                                          int_table  => 'EAV_BE_ENTITIES_NEW');
end;
execute immediate 'DROP TABLE EAV_BE_ENTITIES_NEW';
execute immediate v_trigger_ddl;
execute immediate v_trigger_enable_ddl;

 end;