 declare
 l_errors  NUMBER;
 v_table_ddl VARCHAR2(30000);
 v_trigger_ddl varchar2(4000);
 v_trigger_enable_ddl VARCHAR2(4000);
 v_schema VARCHAR2(32) := ?;
 v_counter number;

 v_index_ddl VARCHAR2(4000 CHAR);
 type string_table IS TABLE OF VARCHAR2(4000 CHAR);
 v_index_ddl_table string_table := string_table();
 begin
 for rec_table in (select t.table_name, tc.column_name, tg.trigger_name
                   from user_tab_columns tc, user_tables t, user_triggers tg
                   where ((t.table_name like 'R_CORE%'
                           and ((tc.column_name = 'OPEN_DATE') or (tc.COLUMN_NAME='REP_DATE'))))
                         and tc.table_name = t.table_name
                         and t.partitioned = 'NO'
                         and tg.table_name = t.table_name) loop

     select dbms_metadata.get_ddl('TABLE', rec_table.table_name, v_schema)
      into v_table_ddl
      from dual;

select replace(v_table_ddl, rec_table.table_name, rec_table.table_name||'_NEW') into v_table_ddl from dual;
select replace(v_table_ddl, ';', '') into v_table_ddl from dual;
v_table_ddl := v_table_ddl||' PARTITION BY RANGE(' || rec_table.column_name || ') (
Partition p1 values less than (to_date(''01.04.2013'', ''dd.mm.yyyy'')),
Partition p2 values less than (to_date(''01.05.2013'', ''dd.mm.yyyy'')),
Partition p3 values less than (to_date(''01.06.2013'', ''dd.mm.yyyy'')),
Partition p4 values less than (to_date(''01.07.2013'', ''dd.mm.yyyy'')),
Partition p5 values less than (to_date(''01.08.2013'', ''dd.mm.yyyy'')),
Partition p6 values less than (to_date(''01.09.2013'', ''dd.mm.yyyy'')),
Partition p7 values less than (to_date(''01.10.2013'', ''dd.mm.yyyy'')),
Partition p8 values less than (to_date(''01.11.2013'', ''dd.mm.yyyy'')),
Partition p9 values less than (to_date(''01.12.2013'', ''dd.mm.yyyy'')),
Partition p10 values less than (to_date(''01.01.2014'', ''dd.mm.yyyy'')),
Partition p11 values less than (to_date(''01.02.2014'', ''dd.mm.yyyy'')),
Partition p12 values less than (to_date(''01.03.2014'', ''dd.mm.yyyy'')),
Partition p13 values less than (to_date(''01.04.2014'', ''dd.mm.yyyy'')),
Partition p14 values less than (to_date(''01.05.2014'', ''dd.mm.yyyy'')),
Partition p15 values less than (to_date(''01.06.2014'', ''dd.mm.yyyy'')),
Partition p16 values less than (to_date(''01.07.2014'', ''dd.mm.yyyy'')),
Partition p17 values less than (to_date(''01.08.2014'', ''dd.mm.yyyy'')),
Partition p18 values less than (to_date(''01.09.2014'', ''dd.mm.yyyy'')),
Partition p19 values less than (to_date(''01.10.2014'', ''dd.mm.yyyy'')),
Partition p20 values less than (to_date(''01.11.2014'', ''dd.mm.yyyy'')),
Partition p21 values less than (to_date(''01.12.2014'', ''dd.mm.yyyy'')),
Partition p22 values less than (to_date(''01.01.2015'', ''dd.mm.yyyy'')),
Partition p23 values less than (to_date(''01.02.2015'', ''dd.mm.yyyy'')),
Partition p24 values less than (to_date(''01.03.2015'', ''dd.mm.yyyy'')),
Partition p25 values less than (to_date(''01.04.2015'', ''dd.mm.yyyy'')),
Partition p26 values less than (to_date(''01.05.2015'', ''dd.mm.yyyy'')),
Partition p27 values less than (to_date(''01.06.2015'', ''dd.mm.yyyy'')),
Partition p28 values less than (to_date(''01.07.2015'', ''dd.mm.yyyy'')),
Partition p29 values less than (to_date(''01.08.2015'', ''dd.mm.yyyy'')),
Partition p30 values less than (to_date(''01.09.2015'', ''dd.mm.yyyy'')),
Partition p31 values less than (to_date(''01.10.2015'', ''dd.mm.yyyy'')),
Partition p32 values less than (to_date(''01.11.2015'', ''dd.mm.yyyy'')),
Partition p33 values less than (to_date(''01.12.2015'', ''dd.mm.yyyy'')),
Partition p34 values less than (to_date(''01.01.2016'', ''dd.mm.yyyy'')),
Partition p35 values less than (to_date(''01.02.2016'', ''dd.mm.yyyy'')),
Partition p36 values less than (to_date(''01.03.2016'', ''dd.mm.yyyy'')),
Partition p37 values less than (to_date(''01.04.2016'', ''dd.mm.yyyy'')),
Partition p38 values less than (to_date(''01.05.2016'', ''dd.mm.yyyy'')),
Partition p39 values less than (to_date(''01.06.2016'', ''dd.mm.yyyy'')),
Partition p40 values less than (to_date(''01.07.2016'', ''dd.mm.yyyy'')),
Partition p41 values less than (to_date(''01.08.2016'', ''dd.mm.yyyy'')),
Partition p42 values less than (to_date(''01.09.2016'', ''dd.mm.yyyy'')),
Partition p43 values less than (to_date(''01.10.2016'', ''dd.mm.yyyy'')),
Partition p44 values less than (to_date(''01.11.2016'', ''dd.mm.yyyy'')),
Partition p45 values less than (to_date(''01.12.2016'', ''dd.mm.yyyy'')),
Partition p46 values less than (to_date(''01.01.2017'', ''dd.mm.yyyy'')),
Partition p47 values less than (to_date(''01.02.2017'', ''dd.mm.yyyy'')),
Partition p48 values less than (to_date(''01.03.2017'', ''dd.mm.yyyy'')),
Partition p49 values less than (to_date(''01.04.2017'', ''dd.mm.yyyy'')),
Partition p50 values less than (to_date(''01.05.2017'', ''dd.mm.yyyy'')),
Partition p51 values less than (to_date(''01.06.2017'', ''dd.mm.yyyy'')),
Partition p52 values less than (to_date(''01.07.2017'', ''dd.mm.yyyy'')),
Partition p53 values less than (to_date(''01.08.2017'', ''dd.mm.yyyy'')),
Partition p54 values less than (to_date(''01.09.2017'', ''dd.mm.yyyy'')),
Partition p55 values less than (to_date(''01.10.2017'', ''dd.mm.yyyy'')),
Partition p56 values less than (to_date(''01.11.2017'', ''dd.mm.yyyy'')),
Partition p57 values less than (to_date(''01.12.2017'', ''dd.mm.yyyy'')))';
DBMS_OUTPUT.put_line(v_table_ddl);

begin
DBMS_REDEFINITION.can_redef_table(USER, rec_table.table_name);

exception when others then
  dbms_redefinition.abort_redef_table(uname      => USER,
                                      orig_table => rec_table.table_name,
                                      int_table  => rec_table.table_name||'_NEW');
dbms_output.put_line('Got error block 1');

end;

DBMS_REDEFINITION.can_redef_table(USER, rec_table.table_name);

select dbms_metadata.get_ddl('TRIGGER', rec_table.trigger_name, v_schema)
      into v_trigger_ddl
      from dual;

execute immediate 'drop trigger '||rec_table.trigger_name;
v_trigger_enable_ddl:= substr(v_trigger_ddl, instr(v_trigger_ddl, 'ALTER'), length(v_trigger_ddl) - instr(v_trigger_ddl, 'ALTER') + 1);
v_trigger_ddl := substr(v_trigger_ddl, 1, instr(v_trigger_ddl, 'ALTER') - 1);
for rec_index in (select ui.index_name, rownum as num_pp
                        from user_indexes ui
                       where ui.table_name = rec_table.table_name
                         and ui.table_owner = v_schema) loop


      select dbms_metadata.get_ddl('INDEX', rec_index.index_name, v_schema)
        into v_index_ddl
        from dual;

      --dbms_output.put_line(v_index_ddl);

      v_index_ddl_table.extend();
      v_index_ddl_table(v_index_ddl_table.last) := v_index_ddl||' local';
     if(rec_index.index_name not like 'SYS_%') then
       execute immediate 'drop index '||rec_index.index_name;
     end if;
    end loop;
execute immediate v_table_ddl;

begin
DBMS_REDEFINITION.start_redef_table(
    uname      => USER,
    orig_table => rec_table.table_name,
    int_table  => rec_table.table_name||'_NEW');


  dbms_redefinition.sync_interim_table(
    uname      => USER,
    orig_table => rec_table.table_name,
    int_table  => rec_table.table_name||'_NEW');

DBMS_REDEFINITION.copy_table_dependents(
    uname            => USER,
    orig_table       => rec_table.table_name,
    int_table        => rec_table.table_name||'_NEW',
    copy_indexes     => 0,
    copy_triggers    => FALSE,
    copy_constraints => FALSE,
    copy_privileges  => TRUE,
    ignore_errors    => FALSE,
    num_errors       => l_errors,
    copy_statistics  => FALSE,
    copy_mvlog       => FALSE);

DBMS_OUTPUT.put_line('Errors=' || l_errors);

--DBMS_STATS.gather_table_stats(USER, rec_table.table_name||'_NEW', cascade => TRUE);

 dbms_redefinition.finish_redef_table(
    uname      => USER,
    orig_table => rec_table.table_name,
    int_table  => rec_table.table_name||'_NEW');
exception when others then
  dbms_redefinition.abort_redef_table(uname      => USER,
                                      orig_table => rec_table.table_name,
                                      int_table  => rec_table.table_name||'_NEW');
dbms_output.put_line('Got error block 2' || sqlerrm);
end;
execute immediate 'DROP TABLE '||rec_table.table_name||'_NEW';
dbms_output.put_line(v_trigger_ddl);
begin
execute immediate v_trigger_ddl;
execute immediate v_trigger_enable_ddl;
FOR i IN v_index_ddl_table.first .. v_index_ddl_table.last LOOP
      begin
        execute immediate v_index_ddl_table(i);
        --dbms_output.put_line('--INDEX CREATED: ' || rec_index.index_name);
      exception
        when others then
          dbms_output.put_line('--INDEX FAILED: ' || v_index_ddl_table(i));
      end;
    end loop;
exception when others then
DBMS_OUTPUT.PUT_LINE(sqlerrm);
end;
--DBMS_STATS.gather_table_stats(USER, rec_table.table_name, cascade => TRUE);

end loop;
-- todo:: B.I nado pofixed zdes. tablici zahardcodeni. Nujno sdelat universalny
begin
  execute immediate 'alter table R_CORE_CREDIT enable row movement';
  execute immediate 'alter table R_CORE_CREDIT_FLOW enable row movement';
  execute immediate 'alter table R_CORE_CREDIT_HIS enable row movement';
  execute immediate 'alter table R_CORE_DEBTOR enable row movement';
  execute immediate 'alter table R_CORE_DEBTOR_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG enable row movement';
  execute immediate 'alter table R_CORE_ORG_ADDRESS enable row movement';
  execute immediate 'alter table R_CORE_ORG_ADDRESS_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG_CONTACTS enable row movement';
  execute immediate 'alter table R_CORE_ORG_CONTACTS_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG_DI enable row movement';
  execute immediate 'alter table R_CORE_ORG_DI_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG_HEAD_DOCS enable row movement';
  execute immediate 'alter table R_CORE_ORG_HEAD_DOCS_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG_HEAD_NAMES enable row movement';
  execute immediate 'alter table R_CORE_ORG_HEAD_NAMES_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG_HIS enable row movement';
  execute immediate 'alter table R_CORE_ORG_NAME enable row movement';
  execute immediate 'alter table R_CORE_ORG_NAME_HIS enable row movement';
  execute immediate 'alter table R_CORE_PERSON enable row movement';
  execute immediate 'alter table R_CORE_PERSON_ADDRESS enable row movement';
  execute immediate 'alter table R_CORE_PERSON_ADDRESS_HIS enable row movement';
  execute immediate 'alter table R_CORE_PERSON_CONTACTS enable row movement';
  execute immediate 'alter table R_CORE_PERSON_CONTACTS_HIS enable row movement';
  execute immediate 'alter table R_CORE_PERSON_DI enable row movement';
  execute immediate 'alter table R_CORE_PERSON_DI_HIS enable row movement';
  execute immediate 'alter table R_CORE_PERSON_HIS enable row movement';
  execute immediate 'alter table R_CORE_PERSON_NAME enable row movement';
  execute immediate 'alter table R_CORE_PERSON_NAME_HIS enable row movement';
  execute immediate 'alter table R_CORE_PLEDGE enable row movement';
  execute immediate 'alter table R_CORE_PLEDGE_HIS enable row movement';
  execute immediate 'alter table R_CORE_PORTFOLIO_FLOW_KFN enable row movement';
  execute immediate 'alter table R_CORE_PORTFOLIO_FLOW_MSFO enable row movement';
  execute immediate 'alter table R_CORE_REMAINS enable row movement';
  execute immediate 'alter table R_CORE_SUBJECT_DOC enable row movement';
  execute immediate 'alter table R_CORE_SUBJECT_DOC_HIS enable row movement';
  execute immediate 'alter table R_CORE_TURNOVER enable row movement';
end;

end;