create or replace PROCEDURE LX_PLEDGE_ABSENSE_FIX AS
BEGIN

  update LX_PLEDGE_ABSENSE lx set lx.cr_not_found=0 where lx.credit_id is not null;
  delete from lx_log where rule_name is not null;

  for credit in (select * from LX_PLEDGE_ABSENSE)
   loop
      declare
      v_cr_credit_id number;
      v_cr_pledge_count number;
      v_cr_pledge_contract varchar2(100);
      v_cr_pledge_type number;
      v_cr_pledge_value number;
      v_cr_count number;

      credit_id number;
      pledges_id number;
      pledge_id number;
      pledges_pledge_id number;

      pledge_type_id number;
      pledge_type_code varchar2(100);
      pledge_type_name_ru varchar2(525);
      pledge_type_name_kz varchar2(525);
      pledge_type_is_liquid_all char(1);
      pledge_type_is_liquid_invest char(1);
      begin


            for pledge in(
                  SELECT cr.id credit_id, pl.id id, pl.contract_no contract_no, pl.type_id type_id, pl.value_ value
                  FROM credit@credits cr
                  left outer join v_creditor_map cmap on cmap.ref_creditor_id=2364
                  left outer join pledge@credits pl on pl.credit_id=cr.id
                  WHERE cr.PRIMARY_CONTRACT_NO = '27-07/364/31-12(40833)'
                      AND cr.PRIMARY_CONTRACT_DATE = to_date('18.02.2013','dd.mm.yyyy')
                      AND cr.creditor_id = cmap.creditor_id
                      and pl.id is not null
            )
            loop


              if credit_id != pledge.credit_id then
                --add complex set to credit
                update eav_be_entity_report_dates set complex_sets_count=complex_sets_count+1 where entity_id=pledge.credit_id;

                --insert pledges complex_sets
                insert into eav_be_entity_complex_sets(entity_id, attribute_id, creditor_id, report_date,is_closed,is_last)
                values(credit_id , 14,credit.creditor_id, credit.report_date, 0, 1)
                RETURNING id INTO pledges_id;
                credit_id := pledge.credit_id;

              end if;

              --insert pledge entities
              insert into eav_be_entities(class_id) values(56) returning id into pledge_id;
              insert into eav_be_entity_report_dates(entity_id, creditor_id, report_date, integer_values_count, date_values_count, string_values_count, boolean_values_count, double_values_count, complex_values_count, simple_sets_count, complex_sets_count, is_closed)
              values(pledge_id, credit.creditor_id, credit.report_date, 0, 0, 1, 0, 1, 1, 0, 0, 0);

              --bind pledge to pledges complex_set_values
              insert into eav_be_complex_set_values(set_id, creditor_id, report_date, entity_value_id, is_closed, is_last)
              values(pledges_id, credit.creditor_id, credit.report_date, pledge_id, 0, 1)
              returning id into pledges_pledge_id;

              --insert contract
              insert into eav_be_string_values(entity_id,attribute_id,creditor_id, report_date, value, is_closed, is_last)
              values(pledge_id, 150, credit.creditor_id, credit.report_date, pledge.contract_no, 0, 1);

              --insert value
              insert into eav_be_double_values(entity_id,attribute_id,creditor_id, report_date,value, is_closed, is_last)
              values(pledge_id, 151, credit.creditor_id, credit.report_date, pledge.value, 0, 1);

              --insert pledge type
              insert into eav_be_entities(class_id) values(54) returning id into pledge_type_id;
              insert into eav_be_entity_report_dates(entity_id, creditor_id, report_date, integer_values_count, date_values_count, string_values_count, boolean_values_count, double_values_count, complex_values_count, simple_sets_count, complex_sets_count, is_closed)
              values(pledge_type_id, credit.creditor_id, credit.report_date, 0, 0, 3, 2, 0, 0, 0, 0, 0);


              select pt.code, pt.name_ru, pt.name_kz, pt.is_liquid_all, pt.is_liquid_invest
              into pledge_type_code, pledge_type_name_ru, pledge_type_name_kz, pledge_type_is_liquid_all, pledge_type_is_liquid_invest
              from Ref.pledge_type@credits pt where pt.id=pledge.type_id;

              insert into eav_be_string_values(entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
              values(pledge_type_id, 141, credit.creditor_id, credit.report_date, pledge_type_code, 0, 1);

              insert into eav_be_string_values(entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
              values(pledge_type_id, 144, credit.creditor_id, credit.report_date, pledge_type_name_kz, 0, 1);

              insert into eav_be_string_values(entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
              values(pledge_type_id, 145, credit.creditor_id, credit.report_date, pledge_type_name_ru, 0, 1);

              insert into eav_be_boolean_values(entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
              values(pledge_type_id, 142, credit.creditor_id, credit.report_date, pledge_type_is_liquid_all, 0, 1);

              insert into eav_be_boolean_values(entity_id, attribute_id, creditor_id, report_date, value, is_closed, is_last)
              values(pledge_type_id, 143, credit.creditor_id, credit.report_date,pledge_type_is_liquid_invest, 0, 1);


              insert into EAV_BE_COMPLEX_VALUES(entity_id, attribute_id,creditor_id, report_date, entity_value_id, is_closed, is_last)
              values(pledge_id, 53, credit.creditor_id, credit.report_date, pledge_type_id, 0, 1);
            end loop;

      EXCEPTION
        WHEN no_data_found THEN
          update LX_PLEDGE_ABSENSE set cr_not_found=1 where credit_id=credit.credit_id;
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'pledge_absense','MKR credit not foud for ESSP credit_id : '||credit.credit_id);

        WHEN TOO_MANY_ROWS THEN
          insert into lx_log(log_date,rule_name,log_message) values(sysdate, 'pledge_absense','MKR too many debt_remains, may be report_date problem for ESSP credit_id : '||credit.credit_id);
      end;
    end loop;

END LX_PLEDGE_ABSENSE_FIX;