create or replace PROCEDURE lx_bank_relation_050_fill
AS
   v_best_id number;
   v_report_date date;
   v_procedure_name varchar2(250) := 'lx_bank_relation_fill';
   v_bank_relation_id number;
  v_update_cnt number;
  v_info_id number;
  v_subject_id number;
begin

  delete from lx_bank_relation_050p;

  insert into lx_bank_relation_050p
    select tm.set_id, sets.entity_id as person_info_id from
    (select set_id from eav_be_complex_set_values where set_id in (
      (select sets.id as set_Id from eav_be_entity_complex_sets sets
        join eav_be_complex_set_values sv on sets.id = sv.set_id
        join eav_be_complex_values cv on sv.entity_value_id = cv.entity_id
      where sets.attribute_id = 9
            and cv.entity_value_id = 1931))
    group by set_id
    having count(1) > 1) tm
    left outer join eav_be_entity_complex_sets sets on tm.set_id = sets.id;

end lx_bank_relation_050_fill;