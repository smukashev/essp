create or replace PROCEDURE "LX_DEBT_PASTDUE_OD_VS_CD_FILL" IS
  v_primary_contract_no varchar(255);
  v_primary_contract_date date;
  BEGIN

    DELETE FROM LX_DEBT_PASTDUE_OD_VS_CD;

    --GET DEPT AND PASTDUE
    for pastdues in (
    select pd.entity_id deptId, pd.entity_value_id pastdueId, pd.report_date reportDate, od.value openDate, cd.value closeDate
    from eav_be_complex_values pd
      left outer join eav_be_date_values cd on cd.entity_id = pd.entity_value_id and cd.attribute_id = 53
      left outer join eav_be_date_values od on od.entity_id = pd.entity_value_id and od.attribute_id = 54
    where
      od.value is not null
      and cd.value is not null
      and (cd.value < od.value)
    )
    loop
      insert into LX_DEBT_PASTDUE_OD_VS_CD(pastdue_id, dept_id, report_date, open_date, close_date)
      values(pastdues.pastdueId, pastdues.deptId, pastdues.reportDate, pastdues.openDate, pastdues.closeDate);
    end loop;


    --GET REMAINS
    for remains in (
    SELECT ebe.id remain, clx.entity_value_id dept
    from eav_be_entities ebe
      left outer join eav_be_complex_values clx on ebe.id = clx.entity_id and clx.attribute_id = 40
    where ebe.class_id = 43 and clx.entity_value_id in (
      select dept_id  from LX_DEBT_PASTDUE_OD_VS_CD
    )
    )
    loop
      update LX_DEBT_PASTDUE_OD_VS_CD lx set lx.remains_id=remains.remain where lx.dept_id=remains.dept;
    end loop;

    --GET CHANGES
    for changes in (
    SELECT ebe.id chng, clx.entity_value_id remain
    from eav_be_entities ebe
      left outer join eav_be_complex_values clx on ebe.id = clx.entity_id and clx.attribute_id = 55
    where ebe.class_id = 57 and clx.entity_value_id in  (
      select remains_id from LX_DEBT_PASTDUE_OD_VS_CD
    )
    )
    loop
      update LX_DEBT_PASTDUE_OD_VS_CD lx set lx.change_id=changes.chng where lx.remains_id=changes.remain;
    end loop;


    --GET CREDITS
    for credits in (
    SELECT ebe.id credit, clx.entity_value_id chng
    from eav_be_entities ebe
      left outer join eav_be_complex_values clx on ebe.id = clx.entity_id and clx.attribute_id = 68
    where ebe.class_id = 59 and clx.entity_value_id in (
      select change_id from LX_DEBT_PASTDUE_OD_VS_CD
    )
    )
    loop
      update LX_DEBT_PASTDUE_OD_VS_CD lx set lx.credit_id=credits.credit where lx.change_id=credits.chng;
    end loop;

    --GET CONTRACT NO and DATE
    for cr in (select * from LX_DEBT_PASTDUE_OD_VS_CD)
    loop
      select primary_contract_no, primary_contract_date
      into v_primary_contract_no, v_primary_contract_date
      from r_core_credit@showcase sh
      where cr.credit_id = sh.credit_id;

      update LX_DEBT_PASTDUE_OD_VS_CD lx
      set lx.p_cont_no = v_primary_contract_no,
        lx.p_cont_date = v_primary_contract_date
      where lx.credit_id = cr.credit_id;
    end loop;

  END LX_DEBT_PASTDUE_OD_VS_CD_FILL;