declare
  l_errors  NUMBER;
  v_table_ddl VARCHAR2(30000);
  v_trigger_ddl varchar2(4000);
  v_trigger_enable_ddl VARCHAR2(4000);
  v_schema VARCHAR2(32) := 'CORE';
  v_counter number;

  v_index_ddl VARCHAR2(4000 CHAR);
  type string_table IS TABLE OF VARCHAR2(4000 CHAR);
  v_index_ddl_table string_table := string_table();
begin
  for rec_table in (select  t.table_name, tc.column_name, tg.TRIGGER_NAME
                    from user_tab_columns tc, user_tables t, user_triggers tg
                    where ((t.table_name like 'EAV_BE%VALUES'
                            and (tc.column_name = 'ATTRIBUTE_ID'))
                           or (t.table_name like 'EAV_BE%_SET_VALUES'
                               and (tc.column_name = 'REPORT_DATE'))
                           or (t.table_name like 'EAV_BE_ENTITY_%_SETS'
                               and (tc.column_name = 'ATTRIBUTE_ID'))
                           or (t.table_name like 'EAV_BE_ENTITIES'
                               and tc.column_name = 'CLASS_ID')
                           or (t.table_name like 'EAV_BE_SETS'
                               and tc.column_name = 'REPORT_DATE')
                           or (t.table_name like 'EAV_BE_ENTITY_REPORT_DATES'
                               and tc.column_name = 'REPORT_DATE')
                           or (t.table_name = 'EAV_OPTIMIZER'
                               and tc.COLUMN_NAME='META_ID' and tg.trigger_name <>'TRG_EAV_OPTIMIZER_CREDITOR_ID'))
                          and tc.table_name = t.table_name
                          and t.partitioned = 'NO'
                          and tg.table_name = t.table_name) loop

    select dbms_metadata.get_ddl('TABLE', rec_table.table_name, v_schema)
    into v_table_ddl
    from dual;

    select replace(v_table_ddl, rec_table.table_name, rec_table.table_name||'_NEW') into v_table_ddl from dual;
    select replace(v_table_ddl, ';', '') into v_table_ddl from dual;
    if(rec_table.column_name='REPORT_DATE') then
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
    elsif(rec_table.table_name = 'EAV_BE_ENTITIES') then
      v_table_ddl := v_table_ddl|| ' PARTITION BY LIST (' || rec_table.column_name || ') (
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
    partition class_others values(DEFAULT))';
    elsif(rec_table.table_name = 'EAV_OPTIMIZER') then
      v_table_ddl := v_table_ddl|| ' PARTITION BY LIST (' || rec_table.column_name || ') (
    partition meta_3 values(3),
    partition meta_42 values(42),
    partition meta_58 values(58),
    partition meta_59 values(59),
    partition meta_others values(DEFAULT))';
    elsif(rec_table.column_name='ATTRIBUTE_ID') then
          v_table_ddl := v_table_ddl|| ' PARTITION BY LIST (' || rec_table.column_name || ') (';
          if(rec_table.table_name = 'EAV_BE_COMPLEX_VALUES') then
            for rec_complex in (select id from eav_m_complex_attributes) loop
              v_table_ddl :=v_table_ddl|| 'partition attribute'||rec_complex.id||' values('||rec_complex.id||'),';
            end loop;
          elsif (rec_table.table_name='EAV_BE_ENTITY_COMPLEX_SETS') then
            for rec_complex_sets in (select id from eav_m_complex_set) loop
              v_table_ddl := v_table_ddl|| 'partition attribute'||rec_complex_sets.id||' values('||rec_complex_sets.id||'),';
            end loop;
          elsif (rec_table.table_name='EAV_BE_ENTITY_SIMPLE_SETS') then
            for rec_simple_sets in (select id from eav_m_simple_set) loop
              v_table_ddl := v_table_ddl|| 'partition attribute'||rec_simple_sets.id||' values('||rec_simple_sets.id||'),';
            end loop;
          else
            for rec_simple in (select id from eav_m_simple_attributes a where rec_table.table_name like '%'||a.type_code||'%') loop
              v_table_ddl :=v_table_ddl|| 'partition attribute'||rec_simple.id||' values('||rec_simple.id||'),';
            end loop;
          end if;
          v_table_ddl := v_table_ddl|| 'partition attribute_others values(DEFAULT))';
    end if;
    begin
      DBMS_REDEFINITION.can_redef_table(USER, rec_table.table_name);

      exception when others then
      dbms_redefinition.abort_redef_table(uname      => USER,
                                          orig_table => rec_table.table_name,
                                          int_table  => rec_table.table_name||'_NEW');
      dbms_output.put_line('Got error at block 1: ' || sqlerrm);

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
      dbms_redefinition.finish_redef_table(
          uname      => USER,
          orig_table => rec_table.table_name,
          int_table  => rec_table.table_name||'_NEW');
      exception when others then
      dbms_redefinition.abort_redef_table(uname      => USER,
                                          orig_table => rec_table.table_name,
                                          int_table  => rec_table.table_name||'_NEW');
      dbms_output.put_line('Got error at block 2: ' || sqlerrm);
    end;
    execute immediate 'DROP TABLE '||rec_table.table_name||'_NEW';
    dbms_output.put_line(v_trigger_ddl);
    begin
      execute immediate v_trigger_ddl;
      execute immediate v_trigger_enable_ddl;
      FOR i IN v_index_ddl_table.first .. v_index_ddl_table.last LOOP
        begin
          execute immediate v_index_ddl_table(i);
          exception
          when others then
          dbms_output.put_line('--INDEX FAILED: ' || v_index_ddl_table(i));
        end;
      end loop;
      exception when others then
      DBMS_OUTPUT.PUT_LINE(sqlerrm);
    end;
  end loop;
  begin
    execute immediate 'alter table eav_entity_statuses nologging';
    execute immediate 'alter table eav_batch_statuses nologging';
    execute immediate 'alter table EAV_BE_BOOLEAN_SET_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_BOOLEAN_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_COMPLEX_SET_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_COMPLEX_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_DATE_SET_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_DATE_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_DOUBLE_SET_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_DOUBLE_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_ENTITIES enable row movement';
    execute immediate 'alter table EAV_BE_ENTITY_COMPLEX_SETS enable row movement';
    execute immediate 'alter table EAV_BE_ENTITY_REPORT_DATES enable row movement';
    execute immediate 'alter table EAV_BE_ENTITY_SIMPLE_SETS enable row movement';
    execute immediate 'alter table EAV_BE_INTEGER_SET_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_INTEGER_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_STRING_SET_VALUES enable row movement';
    execute immediate 'alter table EAV_BE_STRING_VALUES enable row movement';
    execute immediate 'alter table EAV_OPTIMIZER enable row movement';
  end;
end;