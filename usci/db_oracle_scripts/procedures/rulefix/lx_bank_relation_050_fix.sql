create or replace PROCEDURE lx_bank_relation_050_fix(
  p_mod_value in number
)
AS
  v_best_id number;
  v_report_date date;
  v_procedure_name varchar2(250) := 'lx_bank_relation_fix';
  v_bank_relation_id number;
  v_update_cnt number;
  v_info_id number;
  v_subject_id number;

  v_set_id number;
  v_person_info_id number;
  cr lx_bank_relation_050p%rowtype;

  begin

    --loop
    --for cr in (select set_id, person_info_id from lx_bank_relation_050p)
    loop
      select * into cr
        from lx_bank_relation_050p
       where processed = 0
         and mod(id,7) = p_mod_value
         and rownum = 1;

      update lx_bank_relation_050p
         set processed = -1
        where id = cr.id;

      commit;

      select entity_value_id into v_bank_relation_id from (
        select sv.entity_value_id, rank() over (partition by sets.id order by cv.report_date) num_pp
        from eav_be_entity_complex_sets sets
          join eav_be_complex_set_values sv on sets.id = sv.set_id
          join eav_be_complex_values cv on sv.entity_value_id = cv.entity_id
        where sets.id = cr.set_id
              and cv.attribute_id = 25)
      where num_pp = 1
            and rownum = 1;

      /*delete
      from eav_be_complex_set_values
      where set_id = cr.set_id
            and entity_value_id != v_bank_relation_id;*/

      insert into lx_br_050w1
        select id
          from eav_be_complex_set_values
         where set_id = cr.set_id
          and entity_value_id != v_bank_relation_id;


      /*DELETE
      from r_core_person_di@showcase
      where person_info_id = cr.person_info_id
            and person_bank_relation_id != v_bank_relation_id;*/

      insert into lx_br_050w2
        select id
        from r_core_person_di@showcase
        where person_info_id = cr.person_info_id
              and person_bank_relation_id != v_bank_relation_id;


      /*select count(1) into v_update_cnt
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


      update r_core_person_di@showcase
         set bank_relation_creditor_id = creditor_id
      where person_info_id = cr.person_info_id;*/

      /*lx_write_log(sysdate, v_procedure_name, 'finished person_info_id = ' || cr.person_info_id || ',
                          bank_relation_id = ' || v_bank_relation_id || ',
                          set_id = ' || cr.set_id);*/

      /*delete from lx_bank_relation_050p
          where set_id = cr.set_id
            and person_info_id = cr.person_info_id;*/

      update lx_bank_relation_050p
          set processed = 1
         where id = cr.id;

      commit;

      --exit;

    END LOOP;
  end lx_bank_relation_050_fix;