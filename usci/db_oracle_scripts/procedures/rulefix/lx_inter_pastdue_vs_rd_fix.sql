create or replace PROCEDURE "LX_INTER_PASTDUE_VS_RD_FIX" IS
BEGIN

  for pd in (select * from LX_INTER_PASTDUE_VS_RD)
   loop
      declare
      crReportDate date;
      crOpenDate date;
      crCreditId number;
      v_cr_count number;
      begin

            SELECT dr.rep_date, dr.pastdue_open_date, cr.id
                INTO crReportDate, crOpenDate, crCreditId
            FROM credit@credits cr
              LEFT OUTER JOIN debt_remains@credits dr
                ON cr.id                    =dr.credit_id
            WHERE cr.PRIMARY_CONTRACT_NO=pd.p_cont_no
                AND cr.PRIMARY_CONTRACT_DATE=pd.p_cont_date
                AND dr.type_id              =59
                AND dr.rep_date             =pd.report_date;

            UPDATE LX_INTER_PASTDUE_VS_RD lx
                SET lx.cr_report_date=crReportDate
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;

            UPDATE LX_INTER_PASTDUE_VS_RD lx
                SET lx.cr_open_date=crOpenDate
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;

            UPDATE LX_INTER_PASTDUE_VS_RD lx
                SET lx.cr_credit_id=crCreditId
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;


          IF crReportDate <> pd.cr_report_date THEN
              null;
              --update value
              --dbms_output.put_line('update eav_be_date_values set report_date='||crReportDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 63');
              --update eav_be_date_values
              --set report_date=crReportDate
              --where entity_id = pd.pastdue_id and attribute_id = 63;
          END IF;

          IF crOpenDate <> pd.open_date THEN
              null;
              --update open date
              --dbms_output.put_line('update eav_be_date_values set value='||crOpenDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 63');
              --update eav_be_date_values
              --set value=crOpenDate
              --where entity_id = pd.pastdue_id and attribute_id = 63;
          END IF;

          IF crReportDate < crOpendate THEN
            null;
            --dbms_output.put_line('MKR report_date and open_date incorrect, can not fix for ESSP credit_id : '||pd.credit_id);
          END IF;

       EXCEPTION
        WHEN no_data_found THEN
          update LX_INTER_PASTDUE_VS_RD set cr_not_found=1 where pastdue_id=pd.pastdue_id;

          SELECT count(cr.id)
              into v_cr_count
            FROM credit@credits cr
            WHERE cr.PRIMARY_CONTRACT_NO=pd.p_cont_no
                AND cr.PRIMARY_CONTRACT_DATE=pd.p_cont_date;

            if v_cr_count > 0 then
              insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'inter_pastdue_vs_rd','delete pastdue from mkr  in credit_id : '||pd.credit_id);
              --delete from eav_be_date_values where attribute_id=63 and entity_id = pd.pastdue_id;
              --delete from eav_be_date_values where attribute_id=62 and entity_id = pd.pastdue_id;
              --delete from eav_be_double_values where attribute_id=64 and entity_id = pd.pastdue_id;
              --delete from eav_be_double_values where attribute_id=65 and entity_id = pd.pastdue_id;
              --delete from eav_be_complex_values where attribute_id=16 and entity_value_id = pd.pastdue_id;
            else
              insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'inter_pastdue_vs_rd','MKR credit not foud for ESSP credit_id : '||pd.credit_id);
            end if;
        WHEN TOO_MANY_ROWS THEN
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'inter_pastdue_vs_rd','MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||pd.credit_id);
      end;
    end loop;

END LX_INTER_PASTDUE_VS_RD_FIX;