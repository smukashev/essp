create or replace PROCEDURE "LX_PASTDUE_CD_RD_FIX" IS
  v_primary_contract_no varchar(255);
  v_primary_contract_date date;
BEGIN

  update lx_pastdue_cd_rd lx set lx.cr_close_date = null, lx.cr_report_date = null, lx.cr_not_found=0 where lx.pastdue_id is not null;

  for pd in (select * from LX_PASTDUE_CD_RD)
   loop
      declare
      crCreditId number;
      crCloseDate date;
      crReportDate date;
      begin

            SELECT cr.id,dr.pastdue_close_date, dr.rep_date
                INTO crCreditId, crCloseDate, crReportDate
            FROM credit@credits cr
              LEFT OUTER JOIN debt_remains@credits dr
                ON cr.id                    =dr.credit_id
            WHERE cr.PRIMARY_CONTRACT_NO=pd.p_cont_no
                AND cr.PRIMARY_CONTRACT_DATE=pd.p_cont_date
                AND dr.type_id              =56
                AND dr.rep_date             =pd.report_date;

            UPDATE lx_pastdue_cd_rd lx
                SET lx.cr_close_date = crCloseDate
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;

            UPDATE lx_pastdue_cd_rd lx
                SET lx.cr_report_date=crReportDate
            WHERE
                lx.P_CONT_NO   =pd.p_cont_no
                AND lx.P_CONT_DATE   =pd.p_cont_date;


          IF crCloseDate <> pd.close_date THEN
              --update open date
              dbms_output.put_line('update eav_be_date_values set value='||crCloseDate||' where entity_id = '||pd.pastdue_id||' and attribute_id = 53');
--              update eav_be_date_values
--              set value=crCloseDate
--              where entity_id = pd.pastdue_id
--              and attribute_id = 53;
          END IF;

          IF crReportDate <> pd.report_date THEN
              --update open date
              dbms_output.put_line('incorrect report_date');
          END IF;

          IF  (crCloseDate > crReportDate) THEN
              dbms_output.put_line('MKR dates are incorrect');
          END IF;

       EXCEPTION
        WHEN no_data_found THEN
          update lx_pastdue_cd_rd set cr_not_found=1 where pastdue_id=pd.pastdue_id;
          dbms_output.put_line('MKR credit not foud for ESSP credit_id : '||pd.credit_id);
        WHEN TOO_MANY_ROWS THEN
          dbms_output.put_line('MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||pd.credit_id);

       crCreditId := null;
       crCloseDate := null;
       crReportDate := null;
      end;
    end loop;

END LX_PASTDUE_CD_RD_FIX;