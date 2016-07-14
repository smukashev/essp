CREATE OR REPLACE PROCEDURE LX_RINTEREST_CURRENT_BA5_FILL IS
  v_approved number;
BEGIN


   execute IMMEDIATE 'truncate table lx_rinterest_current_ba5';

   for cr in (select * from eav_be_entities where class_id = 8)
     loop
        for rd in (select date '2016-01-01' as value from dual union ALL
              select date '2016-02-01' as value from dual union ALL
              select date '2016-03-01' as value from dual union all
              select date '2016-04-01' as value from dual)
              loop

                select count(1) into v_approved from eav_report where creditor_id = cr.id and report_date = rd.value and status_id in (5,6,7);

                if v_approved > 0 THEN
                   continue;
                end if;

                insert into lx_rinterest_current_ba5
                  (select * from (select ebe.id as entity_id, u.entity_value_id as change_id, v.entity_value_id as remains_id, x.entity_value_id as interest_id, y.entity_value_id as current_id, ba.entity_id as ba_id, country.entity_value_id country_id, cr.id as creditor_id, rd.value as report_date, rpad('0',250,'0') as pno, date '1990-01-01' as pdate
                   from eav_be_entities ebe
                   join eav_be_complex_values u on ebe.id = u.entity_id and u.attribute_id = 68 and u.report_date = rd.value and u.creditor_id = cr.id
                   join eav_be_complex_values v on u.entity_value_id = v.entity_id and v.attribute_id = 55 and v.report_date = rd.value and v.creditor_id  = cr.id
                   join eav_be_complex_values x on v.entity_value_id = x.entity_id and x.attribute_id = 42 and x.report_date = rd.value and x.creditor_id = cr.id
                   join eav_be_complex_values y on x.entity_value_id = y.entity_id and y.attribute_id = 15 and y.report_date = rd.value and y.creditor_id = cr.id
                   join eav_be_complex_values z on y.entity_value_id = z.entity_id and z.attribute_id = 10 and z.report_date = rd.value and z.creditor_id = cr.id
                   join eav_be_string_values ba on ba.attribute_id = 23 and ba.value = '1' and z.entity_value_id = ba.entity_id
                   join eav_be_complex_values subjects on subjects.attribute_id = 59 and ebe.id = subjects.entity_id and  subjects.creditor_id = cr.id
                   join eav_be_complex_values orgs on orgs.attribute_id = 36 and subjects.entity_value_id = orgs.entity_id and orgs.creditor_id = cr.id
                   join eav_be_complex_values country on country.attribute_id = 27 and orgs.entity_value_id = country.entity_id and country.creditor_id = cr.id
                   where ebe.class_id = 59)
                   where ba_id is not null and country_id != 2109);

              end loop;
      end loop;

END LX_RINTEREST_CURRENT_BA5_FILL;
