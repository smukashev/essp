CREATE OR REPLACE PROCEDURE LX_ECON_TRADE_FIX AS
   v_report_date date;
   v_econ_trade_id number;
   v_unique_insert number;
BEGIN

  for cr in (select * from lx_econ_trade# where econ_trade_id is not null)
    loop
      select report_date into v_report_date from eav_be_complex_values where entity_id = cr.organization_info_id and attribute_id = 57;

      select count(1) into v_unique_insert
         from eav_be_complex_values where entity_id = cr.organization_info_id and attribute_id = 28;

      if v_unique_insert = 0 then
        insert into eav_be_complex_values (entity_id, attribute_id, creditor_id, report_date, entity_value_id,is_closed, is_last)
             values (cr.organization_info_id, 28, cr.creditor_id, v_report_date, v_econ_trade_id, 0, 1);
      end if;

    end loop;

  EXCEPTION
     when others then
       lx_write_log(sysdate, 'lx_econ_trade_fix', sqlerrm);

END;
