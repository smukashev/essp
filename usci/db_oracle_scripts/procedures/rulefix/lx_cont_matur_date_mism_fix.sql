create or replace PROCEDURE LX_CONT_MATUR_DATE_MISM_FIX AS
BEGIN

  update LX_CONT_MATURITY_DATE_MISM lx set lx.cr_p_cont_date= null, lx.cr_cont_maturity_date = null, lx.cr_not_found=0 where lx.credit_id is not null;

  for credit in (select * from LX_CONT_MATURITY_DATE_MISM)
   loop
      declare
      v_cr_p_cont_date date;
      v_cr_maturity_date date;
      v_cr_count number;
      begin

            SELECT cr.primary_contract_date, cr.contract_maturity_date
                INTO v_cr_p_cont_date, v_cr_maturity_date
            FROM credit@credits cr
            left outer join v_creditor_map cmap on cmap.ref_creditor_id=credit.creditor_id
            WHERE cr.PRIMARY_CONTRACT_NO = credit.p_cont_no
                AND cr.PRIMARY_CONTRACT_DATE = credit.p_cont_date
                AND cr.creditor_id = cmap.creditor_id;


            UPDATE LX_CONT_MATURITY_DATE_MISM lx
                SET lx.cr_p_cont_date = v_cr_p_cont_date
            WHERE
                lx.P_CONT_NO   =credit.p_cont_no
                AND lx.P_CONT_DATE   =credit.p_cont_date;

            UPDATE LX_CONT_MATURITY_DATE_MISM lx
                SET lx.cr_cont_maturity_date=v_cr_maturity_date
            WHERE
                lx.P_CONT_NO   =credit.p_cont_no
                AND lx.P_CONT_DATE   =credit.p_cont_date;


          IF v_cr_maturity_date <> credit.cont_maturity_date THEN
              null;
              --update actual issue date
              --update eav_be_date_values
              --set value=v_cr_a_issue_date
              --where entity_id = cr.credit_id and attribute_id = 154;
          END IF;

          IF  (v_cr_p_cont_date > v_cr_maturity_date) THEN
              null;
              --dbms_output.put_line('MKR dates are incorrect');
          END IF;

       EXCEPTION
        WHEN no_data_found THEN
          update LX_CONT_MATURITY_DATE_MISM set cr_not_found=1 where credit_id=credit.credit_id;
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'contract_maturity_date_mismatch','MKR credit not foud for ESSP credit_id : '||credit.credit_id);

        WHEN TOO_MANY_ROWS THEN
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'contract_maturity_date_mismatch','MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||credit.credit_id);
      end;
    end loop;

END LX_CONT_MATUR_DATE_MISM_FIX;