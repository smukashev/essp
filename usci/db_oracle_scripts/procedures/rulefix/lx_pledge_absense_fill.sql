create or replace PROCEDURE LX_PLEDGE_ABSENSE_FILL
AS
BEGIN

  delete from lx_pledge_absense;

  for problems in (
    select cr.id credit_id, pcn.value p_cont_no, pcd.value p_cont_date, pc.creditor_id creditor_id, pc.report_date report_date

    from EAV_BE_ENTITIES  cr

      left join eav_be_entity_complex_sets pl
      on pl.entity_id = cr.id and pl.attribute_id =  14

      left outer join eav_be_complex_values pc
        ON pc.entity_id     = cr.id AND pc.attribute_id = 57

      LEFT OUTER JOIN eav_be_date_values pcd
        ON pcd.entity_id    =pc.entity_value_id AND pcd.attribute_id= 152

      LEFT OUTER JOIN eav_be_string_values pcn
        ON pcn.entity_id    =pc.entity_value_id AND pcn.attribute_id= 153

     where cr.class_id=59 and pl.id is null
  )
  loop

  insert into lx_pledge_absense(credit_id, p_cont_no, p_cont_date, creditor_id, report_date)
  values(problems.credit_id, problems.p_cont_no, problems.p_cont_date, problems.creditor_id, problems.report_date);

  end loop;

END LX_PLEDGE_ABSENSE_FILL;