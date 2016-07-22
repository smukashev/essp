create or replace PROCEDURE LX_PLEDGE_SUM_ABSENSE_FILL
AS
BEGIN

  delete from lx_pledge_sum_absense;

  for problems in (

      select be.id pledge_id, plt.entity_value_id pledge_type_id, pl_val.value pl_contract, plt_code.value pl_type_code
      from eav_be_entities be
        left outer join eav_be_double_values pl_val
        on pl_val.entity_id = be.id and pl_val.attribute_id = 151
        left outer join eav_be_complex_values plt
        on plt.entity_id = be.id and plt.attribute_id = 53
        left outer join eav_be_string_values plt_code
        on plt_code.entity_id=plt.entity_value_id and plt_code.attribute_id = 141
      where
        be.class_id = 56 and
        pl_val.id is null and
        plt_code.value != '47'

  )
  loop

  insert into lx_pledge_no_absense(pledge_id, pledge_type_id, pl_contract, pl_type_code)
  values(problems.pledge_id, problems.pledge_type_id, problems.pl_contract , problems.pl_type_code);

  end loop;

  --fill pledges_id
  for pledge in (
    select clx.set_id pledges_id, pl.pledge_id pledge_id
      from eav_be_complex_set_values clx
    join lx_pledge_sum_absense pl
      on clx.entity_value_id = pl.pledge_id
  )
  loop
    update lx_pledge_sum_absense set pledges_id=pledge.pledges_id where pledge_id = pledge.pledge_id;
  end loop;


  --fill credit_id
  for credit in (
    select cls.entity_id credit_id, pl.pledges_id pledges_id
      from eav_be_entity_complex_sets cls
    join lx_pledge_sum_absense pl
      on cls.id=pl.pledges_id
  )
  loop
    update lx_pledge_sum_absense set credit_id = credit.credit_id where pledges_id = credit.pledges_id;
  end loop;


  --fill primary contract
  for p_cont in (
    select pl.credit_id credit_id, pcn.value p_cont_no, pcd.value p_cont_date, pc.creditor_id creditor_id, pc.report_date report_date
    from lx_pledge_sum_absense pl
      join eav_be_complex_values pc
        ON pc.entity_id     = pl.credit_id AND pc.attribute_id = 57
      JOIN eav_be_date_values pcd
        ON pcd.entity_id    =pc.entity_value_id AND pcd.attribute_id= 152
      JOIN eav_be_string_values pcn
        ON pcn.entity_id    =pc.entity_value_id AND pcn.attribute_id= 153
  )
  loop

  update lx_pledge_sum_absense set p_cont_no = p_cont.p_cont_no, p_cont_date = p_cont.p_cont_date,
         creditor_id = p_cont.creditor_id, report_date = p_cont.report_date
  where credit_id = p_cont.credit_id;

  end loop;

END LX_PLEDGE_SUM_ABSENSE_FILL;