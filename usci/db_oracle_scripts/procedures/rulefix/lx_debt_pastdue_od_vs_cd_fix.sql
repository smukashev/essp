create or replace PROCEDURE "LX_DEBT_PASTDUE_OD_VS_CD_FIX" IS
  BEGIN

    for pd in (select * from LX_DEBT_PASTDUE_OD_VS_CD)
    loop
      declare
        crCreditId number;
        crOpenDate date;
        crCloseDate date;
      begin

        SELECT cr.id,dr.pastdue_open_date, dr.pastdue_close_date
        INTO crCreditId, crOpenDate, crCloseDate
        FROM credit@credits cr
          LEFT OUTER JOIN debt_remains@credits dr
            ON cr.id                    =dr.credit_id
        WHERE cr.PRIMARY_CONTRACT_NO=pd.p_cont_no
              AND cr.PRIMARY_CONTRACT_DATE=pd.p_cont_date
              AND dr.type_id              =56
              AND dr.rep_date             =pd.report_date;

        UPDATE LX_DEBT_PASTDUE_OD_VS_CD lx
        SET lx.cr_open_date=crOpenDate
        WHERE
          lx.P_CONT_NO   =pd.p_cont_no
          AND lx.P_CONT_DATE   =pd.p_cont_date;

        UPDATE LX_DEBT_PASTDUE_OD_VS_CD lx
        SET lx.cr_close_date = crCloseDate
        WHERE
          lx.P_CONT_NO   =pd.p_cont_no
          AND lx.P_CONT_DATE   =pd.p_cont_date;

        IF crOpenDate <> pd.open_date THEN
          --update open date
          dbms_output.put_line('update eav_be_date_values set value='||crCloseDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 54');
          --              update eav_be_date_values
          --              set value=cr_open_date
          --              where entity_id = pd.pastdue_id
          --              and attribute_id = 54;
        END IF;

        IF crCloseDate <> pd.close_date THEN
          --update close date
          dbms_output.put_line('update eav_be_date_values set value='||crCloseDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 53');
          --              update eav_be_date_values
          --              set value=cr_close_date
          --              where entity_id = pd.pastdue_id
          --              and attribute_id = 53;
        END IF;

        IF crOpenDate is not null and crCloseDate is not null and crOpenDate < crCloseDate THEN
          dbms_output.put_line('MKR open_date and close_date incorrect, can not fix for ESSP credit_id : '||pd.credit_id);
        END IF;

        EXCEPTION
        WHEN no_data_found THEN
        update LX_DEBT_PASTDUE_OD_VS_CD set cr_not_found=1 where pastdue_id=pd.pastdue_id;
        dbms_output.put_line('MKR credit not foud for ESSP credit_id : '||pd.credit_id);
        WHEN TOO_MANY_ROWS THEN
        dbms_output.put_line('MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||pd.credit_id);

        crCreditId := null;
      end;
    end loop;

  END LX_DEBT_PASTDUE_OD_VS_CD_FIX;