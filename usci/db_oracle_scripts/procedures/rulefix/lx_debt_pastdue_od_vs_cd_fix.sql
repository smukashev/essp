create or replace PROCEDURE "LX_DEBT_PASTDUE_OD_VS_CD_FIX" IS
  BEGIN

    for pd in (select * from LX_DEBT_PASTDUE_OD_VS_CD)
    loop
      declare
        crCreditId number;
        crOpenDate date;
        crCloseDate date;
        v_cr_count number;
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
          null;
          --update open date
          --update eav_be_date_values
          --set value=crOpenDate
          --where entity_id = pd.pastdue_id and attribute_id = 54;
        END IF;

        IF crCloseDate <> pd.close_date THEN
          null;
          --update close date
          --update eav_be_date_values
          --set value=crCloseDate
          --where entity_id = pd.pastdue_id and attribute_id = 53;
        END IF;

        IF crOpenDate is not null and crCloseDate is not null and crOpenDate < crCloseDate THEN
          null;
          --dbms_output.put_line('MKR open_date and close_date incorrect, can not fix for ESSP credit_id : '||pd.credit_id);
        END IF;

        EXCEPTION
        WHEN no_data_found THEN
        update LX_DEBT_PASTDUE_OD_VS_CD set cr_not_found=1 where pastdue_id=pd.pastdue_id;

        SELECT count(cr.id)
        into v_cr_count
        FROM credit@credits cr
        WHERE cr.PRIMARY_CONTRACT_NO=pd.p_cont_no
              AND cr.PRIMARY_CONTRACT_DATE=pd.p_cont_date;

        if v_cr_count > 0 then
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'debt_pastdue_cd_rd','delete pastdue from mkr  in credit_id : '||pd.credit_id);
          --delete from eav_be_date_values where attribute_id=53 and entity_id = pd.pastdue_id;
          --delete from eav_be_date_values where attribute_id=54 and entity_id = pd.pastdue_id;
          --delete from eav_be_double_values where attribute_id=55 and entity_id = pd.pastdue_id;
          --delete from eav_be_double_values where attribute_id=56 and entity_id = pd.pastdue_id;
          --delete from eav_be_complex_values where attribute_id=13 and entity_value_id = pd.pastdue_id;
        else
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'debt_pastdue_cd_rd','MKR credit not foud for ESSP credit_id : '||pd.credit_id);
        end if;

        WHEN TOO_MANY_ROWS THEN
        insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'debt_pastdue_cd_rd','MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||pd.credit_id);

        crCreditId := null;
      end;
    end loop;

  END LX_DEBT_PASTDUE_OD_VS_CD_FIX;