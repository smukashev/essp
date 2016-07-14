create or replace PROCEDURE "LX_DEBT_PASTDUE_VS_RD_FIX" IS
BEGIN

  for pd in (select * from LX_DEBT_PASTDUE_VS_RD)
   loop
      declare
      crReportDate date;
      crOpenDate date;
      crCreditId number;
      begin

            SELECT dr.rep_date, dr.pastdue_open_date, cr.id
                INTO crReportDate, crOpenDate, crCreditId
            FROM credit@credits cr
              LEFT OUTER JOIN debt_remains@credits dr
                ON cr.id                    =dr.credit_id
            WHERE cr.PRIMARY_CONTRACT_NO=pd.p_cont_no
                AND cr.PRIMARY_CONTRACT_DATE=pd.p_cont_date
                AND dr.type_id              =56
                AND dr.rep_date             =pd.report_date;

            UPDATE LX_DEBT_PASTDUE_VS_RD lx
                SET lx.cr_report_date=crReportDate
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;

            UPDATE LX_DEBT_PASTDUE_VS_RD lx
                SET lx.cr_open_date=crOpenDate
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;

            UPDATE LX_DEBT_PASTDUE_VS_RD lx
                SET lx.cr_credit_id=crCreditId
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;


          IF crReportDate <> pd.cr_report_date THEN
              --update value
              dbms_output.put_line('update eav_be_date_values set report_date='||crReportDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 54');
--              update eav_be_date_values
--              set value=cr_open_date
--              where entity_id = pd.pastdue_id
--              and attribute_id = 54;
          END IF;

          IF crOpenDate <> pd.open_date THEN
              --update open date
              dbms_output.put_line('update eav_be_date_values set value='||crOpenDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 54');
--              update eav_be_date_values
--              set value=cr_open_date
--              where entity_id = pd.pastdue_id
--              and attribute_id = 54;
          END IF;

          IF crReportDate < crOpendate THEN
            dbms_output.put_line('MKR report_date and open_date incorrect, can not fix for ESSP credit_id : '||pd.credit_id);
          END IF;

       EXCEPTION
        WHEN no_data_found THEN
          update LX_DEBT_PASTDUE_VS_RD set cr_not_found=1 where pastdue_id=pd.pastdue_id;
          dbms_output.put_line('MKR credit not foud for ESSP credit_id : '||pd.credit_id);
        WHEN TOO_MANY_ROWS THEN
          dbms_output.put_line('MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||pd.credit_id);

      end;
    end loop;

END LX_DEBT_PASTDUE_VS_RD_FIX;