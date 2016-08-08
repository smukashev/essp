create or replace PROCEDURE LX_ORGANIZATION_HEAD_FIX AS
BEGIN


  delete from lx_log where rule_name='organization_head';

  for lx in (
    select * from lx_organization_head
  )
  loop

      for todel in (

        select * from
          (
              select set_values.set_id nameSetId, set_values.entity_value_id itemId, lx.report_date report_date, rownum rn

              from eav_be_entities be

                left outer join eav_be_entity_complex_sets name_set
                on name_set.entity_id=be.id and name_set.attribute_id=3

                left outer join eav_be_complex_set_values set_values
                on set_values.set_id=name_set.id

                left outer join eav_be_string_values midname
                on midname.entity_id=set_values.entity_value_id and midname.attribute_id=33 and midname.report_date=lx.report_date

                left outer join eav_be_string_values lastname
                on lastname.entity_id=set_values.entity_value_id and lastname.attribute_id=32 and lastname.report_date=lx.report_date

                left outer join eav_be_string_values firstname
                on firstname.entity_id=set_values.entity_value_id and firstname.attribute_id=30 and firstname.report_date=lx.report_date

                left outer join eav_be_string_values lang
                on lang.entity_id=set_values.entity_value_id and lang.attribute_id=31 and lang.report_date=lx.report_date


              where

                be.id = lx.head_id and
                set_values.report_date = lx.report_date and
                ((midname.value = lx.midname) or (midname.value is null and lx.midname is null)) and
                ((lastname.value = lx.lastname) or (lastname.value is null and lx.lastname is null)) and
                ((firstname.value = lx.firstname) or (firstname.value is null and lx.firstname is null)) and
                ((lang.value = lx.lang) or (lx.lang is null and lang.id is null))

              order by set_values.report_date
          )
          where rn > 1
    )
    loop

    --DELETE FROM EAV

    delete from eav_be_string_values where entity_id=todel.itemId and attribute_id in (33,32,30,31) and report_date=todel.report_date;
    --insert into lx_log(log_date, rule_name, log_message)
    --values(sysdate, 'organization_head', 'delete from eav_be_string_values where entity_id='||todel.itemId||' and attribute_id in (33,32,30,31) and report_date='||todel.report_date||';');

    delete from eav_be_complex_set_values where set_id=todel.nameSetId and entity_value_id=todel.itemId;
    --insert into lx_log(log_date, rule_name, log_message)
    --values(sysdate, 'organization_head', 'delete from eav_be_complex_set_values where set_id='||todel.nameSetId||' and entity_value_id='||todel.itemId||';');


    --DELETE FROM SHOWCASE

    delete from r_core_org_head_names@showcase where name_id=todel.itemId and open_date=todel.report_date;
    --insert into lx_log(log_date, rule_name, log_message)
    --values(sysdate, 'organization_head', 'delete from r_core_org_head_names@showcase where name_id='||todel.itemId||'  and open_date='||todel.report_date||';');


    end loop;

  end loop;



END LX_ORGANIZATION_HEAD_FIX;