create or replace PROCEDURE LX_CONT_MATUR_DATE_MISM_FILL
AS
BEGIN

  delete from lx_actual_issue_date_mism;

  for problems in (
    SELECT  cr.id credit_id, pc.entity_value_id p_cont_id, pcn.value p_cont_no, pcd.value p_cont_date, cmd.value cont_maturity_date, pc.creditor_id creditor_id

      FROM eav_be_entities cr

      left outer join eav_be_date_values cmd
        on cmd.entity_id = cr.id and cmd.attribute_id =  156

      left outer join eav_be_complex_values pc
        ON pc.entity_id     = cr.id AND pc.attribute_id = 57

      LEFT OUTER JOIN eav_be_date_values pcd
        ON pcd.entity_id    =pc.entity_value_id AND pcd.attribute_id= 152

      LEFT OUTER JOIN eav_be_string_values pcn
        ON pcn.entity_id    =pc.entity_value_id AND pcn.attribute_id= 153

      WHERE cr.class_id   = 59 and  cmd.value < pcd.value
  )
  loop

  insert into lx_cont_maturity_date_mism(credit_id, p_cont_id, p_cont_no, p_cont_date, cont_maturity_date, creditor_id)
  values(problems.credit_id, problems.p_cont_id, problems.p_cont_no, problems.p_cont_date, problems.cont_maturity_date, problems.creditor_id);

  end loop;

END LX_CONT_MATUR_DATE_MISM_FILL;