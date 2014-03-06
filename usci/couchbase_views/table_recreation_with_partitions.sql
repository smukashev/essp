declare
  v_schema VARCHAR2(32 CHAR) := 'CORE';

  v_table_ddl VARCHAR2(4000 CHAR);
  v_index_ddl VARCHAR2(4000 CHAR);
  v_trigger_ddl VARCHAR2(4000 CHAR);
  v_drop_ddl  VARCHAR2(4000 CHAR);
  v_trigger_enable_ddl VARCHAR2(4000 CHAR);

  type string_table IS TABLE OF VARCHAR2(4000 CHAR);
  v_index_ddl_table string_table := string_table();
  --v_index_name_table string_table := string_table();
begin
  for rec_table in (select t.table_name, tc.column_name, tg.trigger_name
                      from user_tab_columns tc, user_tables t, user_triggers tg
                     where ((t.table_name like 'EAV_BE%VALUES'
                       and (tc.column_name = 'ENTITY_ID'))
                        or (t.table_name like 'EAV_BE%_SET_%'
                       and (tc.column_name = 'SET_ID'))
                        or (t.table_name like 'EAV_BE_ENTITY_%_SETS'
                       and (tc.column_name = 'SET_ID'))
                        or (t.table_name like 'EAV_BE_ENTITIES'
                       and tc.column_name = 'ID')
                        or (t.table_name like 'EAV_BE_SETS'
                       and tc.column_name = 'ID')
                        or (t.table_name like 'EAV_BE_ENTITY_REPORT_DATES'
                       and tc.column_name = 'ENTITY_ID'))
                       and tc.table_name = t.table_name
                       and t.partitioned = 'NO'
                       and tg.table_name = t.table_name) loop
    dbms_output.put_line(rec_table.TABLE_NAME);      
        dbms_output.put_line(rec_table.column_name); 
                       
    select dbms_metadata.get_ddl('TABLE', rec_table.table_name, v_schema) ||
           ' PARTITION BY HASH(' || rec_table.column_name || ') PARTITIONS 100'
      into v_table_ddl
      from dual;
      
    select 'DROP TABLE "' || v_schema || '"."' ||
                  rec_table.table_name || '" CASCADE CONSTRAINTS'
      into v_drop_ddl
      from dual;
  
    select dbms_metadata.get_ddl('TRIGGER', rec_table.trigger_name, v_schema)
      into v_trigger_ddl
      from dual;
      
    v_trigger_enable_ddl:= substr(v_trigger_ddl, instr(v_trigger_ddl, 'ALTER'), length(v_trigger_ddl) - instr(v_trigger_ddl, 'ALTER') + 1);
    v_trigger_ddl := substr(v_trigger_ddl, 1, instr(v_trigger_ddl, 'ALTER') - 1);
    --dbms_output.put_line(v_table_ddl);
    --dbms_output.put_line(v_trigger_ddl);
    --dbms_output.put_line(v_trigger_enable_ddl);
  
    for rec_index in (select ui.index_name, rownum as num_pp
                        from user_indexes ui
                       where ui.table_name = rec_table.table_name
                         and ui.table_owner = v_schema) loop
      
    
      select dbms_metadata.get_ddl('INDEX', rec_index.index_name, 'CORE')
        into v_index_ddl
        from dual;
    
      --dbms_output.put_line(v_index_ddl);
    
      v_index_ddl_table.extend();
      v_index_ddl_table(v_index_ddl_table.last) := v_index_ddl;
    
    end loop;
  
    dbms_output.put_line('--DROP TABLE: ' || rec_table.table_name);
    execute immediate v_drop_ddl;
    dbms_output.put_line('--CREATE TABLE: ' || rec_table.table_name);
    execute immediate v_table_ddl;
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
  
    --dbms_output.put_line(v_drop_ddl);
  
    v_index_ddl_table.delete;
  end loop;
end;
