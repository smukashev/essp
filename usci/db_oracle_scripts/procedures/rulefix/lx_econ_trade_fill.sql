CREATE OR REPLACE PROCEDURE LX_ECON_TRADE_FILL
AS
  v_econ_trade_code varchar2(250);
  v_econ_trade_id number;
  v_report_date date;
BEGIN


  execute immediate 'truncate table lx_econ_trade#';
  --execute immediate 'truncate table lx_econ_trade';

  insert into lx_econ_trade#
  (select credits.entity_id, subjects.*, scred.primary_contract_no pno, scred.primary_contract_date pdate from (select subjects.entity_id as subject_id , org.* from
  (select * from (select ebe.id as organization_info_id, ct.entity_value_id as country_id, ct.creditor_id,(select creditor_id from v_creditor_map where ref_creditor_id = ct.creditor_id) cid, et.entity_value_id as econ_trade_id from
     eav_be_entities ebe
       left outer join eav_be_complex_values ct on ebe.id = ct.entity_id and ct.attribute_id = 27
       left outer join eav_be_complex_values et on ebe.id = et.entity_id and et.attribute_id = 28
      where ebe.class_id = 39)
   where country_id = 2109 and econ_trade_id is null) org
  left outer join eav_be_complex_values subjects on (subjects.attribute_id = 36 and subjects.entity_value_id = org.organization_info_id)) subjects
  left outer join eav_be_complex_values credits on (credits.attribute_id = 59 and credits.entity_value_id = subjects.subject_id)
  left outer join r_core_credit@showcase scred on (credits.entity_id = scred.credit_id));

  for cr in (select * from lx_econ_trade#)
   loop
      begin
      select report_date into v_report_date from eav_be_complex_values where attribute_id = 57 and entity_id = cr.entity_id;

      select (select code from ref.econ_trade@credits where id = voh.econ_trade_id) into v_econ_trade_code from v_credit_his@credits vch
         left outer join v_debtor_his@credits vdh on (vdh.credit_id = vch.id and vdh.open_date <= v_report_date and (vdh.close_date > v_report_date or vdh.close_date is null) )
         left outer join v_organization_his@credits voh on (voh.id = vdh.org_id and voh.open_date <= v_report_date and (voh.close_date > v_report_date or voh.close_date is null))
         where vch.open_date <= v_report_date and (vch.close_date > v_report_date or vch.close_date is null)
           and vch.primary_contract_no = cr.pno and vch.primary_contract_date = cr.pdate
           and vch.creditor_id = cr.cid;

       select entity_id into v_econ_trade_id from eav_be_string_values cv where cv.attribute_id = 95 and value = v_econ_trade_code;
       update lx_econ_trade# set econ_trade_id = v_econ_trade_id where entity_id = cr.entity_id;

      exception
         when others then
           lx_write_log(sysdate, 'rule_econ_trade', sqlerrm);
           continue;
      end;
   end loop;


END LX_ECON_TRADE_FILL;
