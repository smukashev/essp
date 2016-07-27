create or replace PROCEDURE lx_bank_relation_050_fix
AS
  v_best_id number;
  v_report_date date;
  v_procedure_name varchar2(250) := 'lx_bank_relation_fix';
  v_bank_relation_id number;
  v_update_cnt number;
  v_info_id number;
  v_subject_id number;
  begin

    for cr in (select set_id from lx_bank_relation_050)
    loop
      select entity_value_id into v_bank_relation_id from (
        select sv.entity_value_id, rank() over (partition by sets.id order by cv.report_date) num_pp
        from eav_be_entity_complex_sets sets
          join eav_be_complex_set_values sv on sets.id = sv.set_id
          join eav_be_complex_values cv on sv.entity_value_id = cv.entity_id
        where sets.id = cr.set_id
              and cv.attribute_id = 25)
      where num_pp = 1
            and rownum = 1;

      delete
      from eav_be_complex_set_values
      where set_id = cr.set_id
            and entity_value_id != v_bank_relation_id;


      select count(1) into v_update_cnt
      from eav_be_complex_values
      where attribute_id = 26
            and entity_id = v_bank_relation_id;

      if v_update_cnt <> 1 then
        lx_write_log(sysdate, v_procedure_name, 'one row update required bank_relation_id = ' || v_bank_relation_id
                                                || ' v_update_cnt = ' || v_update_cnt);
        continue;
      end if;


      update eav_be_complex_values
      set entity_value_id = creditor_id
      where attribute_id = 26
            and entity_id = v_bank_relation_id;

      --begin showcase oper_sc information
      begin

        select entity_id into v_info_id
        from eav_be_entity_complex_sets where id = cr.set_id;

        select entity_id into v_subject_id
        from eav_be_complex_values where entity_value_id = v_info_id;

        insert into oper_sc (
          select entity_id, report_date
          from eav_be_complex_values where entity_value_id = v_subject_id);

        EXCEPTION
        when no_data_found then
           lx_write_log(sysdate, v_procedure_name, SQLERRM || ' set_id = ' || cr.set_id);

      end;


    END LOOP;
  end lx_bank_relation_050_fix;