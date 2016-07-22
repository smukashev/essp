create or replace PROCEDURE LX_PLEDGE_NO_ABSENSE_FIX AS
BEGIN

  update LX_PLEDGE_NO_ABSENSE lx set lx.cr_not_found=0, lx.cr_pl_contract=null, lx.cr_pl_type_code=null where lx.credit_id is not null;
  delete from lx_log where rule_name is not null;

  for credit in (select * from LX_PLEDGE_NO_ABSENSE)
   loop
      begin


            for pledge in(
                  SELECT cr.id credit_id, pl.id id, pl.contract_no contract_no,
                  plt.code pl_type_code, plt.name_kz pl_type_name_kz, plt.name_ru pl_type_name_ru, plt.is_liquid_all pl_type_is_liquid_all, plt.is_liquid_invest pl_type_is_liquid_invest
                  FROM credit@credits cr
                    left outer join v_creditor_map cmap on cmap.ref_creditor_id=credit.creditor_id
                    join pledge@credits pl on pl.credit_id=cr.id
                    join ref.pledge_type@credits plt on plt.id = pl.type_id
                  WHERE cr.PRIMARY_CONTRACT_NO = credit.p_cont_no
                      AND cr.PRIMARY_CONTRACT_DATE = credit.p_cont_date
                      AND cr.creditor_id = cmap.creditor_id
            )
            loop

                update lx_pledge_no_absense set cr_pl_contract = pledge.contract_no where credit_id=credit.credit_id;
                --insert contract_no
                if pledge.contract_no != credit.pl_contract then
                    null;
                    --insert into eav_be_string_values(entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
                    --values(credit.pledge_id, 150, credit.creditor_id, credit.report_date, pledge.contract_no, 0, 1);
                end if;

                update lx_pledge_no_absense set cr_pl_type_code = pledge.pl_type_code where credit_id=credit.credit_id;
                --update pledge type
                if pledge.pl_type_code != pledge.pl_type_code then
                  null;
                  --update code
                  --update eav_be_string_values set value = pledge.pl_type_code where entity_id = credit.pledge_type_id and attribute_id=141;
                  --update name_kz
                  --update eav_be_string_values set value = pledge.pl_type_name_kz where entity_id = credit.pledge_type_id and attribute_id=144;
                  --update name_ru
                  --update eav_be_string_values set value = pledge.pl_type_name_ru where entity_id = credit.pledge_type_id and attribute_id=145;
                  --update is_liquid_all
                  --update eav_be_boolean_values set value = pledge.pl_type_is_liquid_all where entity_id = credit.pledge_type_id and attribute_id=142;
                  --update is_liquid_invest
                  --update eav_be_boolean_values set value = pledge.pl_type_is_liquid_invests where entity_id = credit.pledge_type_id and attribute_id=143;
                end if;


            end loop;

      EXCEPTION
        WHEN no_data_found THEN
          update LX_PLEDGE_NO_ABSENSE set cr_not_found=1 where credit_id=credit.credit_id;
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'pledge_no_absense','MKR credit not foud for ESSP credit_id : '||credit.credit_id);

        WHEN TOO_MANY_ROWS THEN
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'pledge_no_absense','MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||credit.credit_id);
      end;
    end loop;

END LX_PLEDGE_NO_ABSENSE_FIX;