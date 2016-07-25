CREATE OR REPLACE PROCEDURE LX_CURRENCY_BY_CREDIT_FILL
AS
  v_procedure_name VARCHAR2(250) := 'LX_CURRENCY_BY_CREDIT_FILL';
  v_pno VARCHAR(250);
  v_pdate date;
  v_creditor_id number;
  v_cid number;
  v_error VARCHAR2(250);
  BEGIN

    delete from lx_currency_by_credit;

    insert into lx_currency_by_credit (
      select id as entity_id, rpad('0',250,'0') as pno, date '1990-01-01' as pdate, -1 as creditor_id, -1 as cid, 'NNNN' as f_currency_sn, -1 as f_currency_id from (
      select ebe.id, c1.entity_value_id, c2.entity_value_id as type_id from eav_be_entities ebe
      left outer join eav_be_complex_values c1 on c1.attribute_id = 65 and c1.entity_id = ebe.id
      left outer join eav_be_complex_values c2 on c2.attribute_id = 64 and c2.entity_id = ebe.id
      where ebe.class_id= 59
      and ebe.id = 214716346
      ) where entity_value_id is null and type_id not in (2244,2249) /*creditnaia linia, uslovnie obiazatelsva po zaimam */);

    for cr in (select * from lx_currency_by_credit)
      loop
         lx_find_primary_contract(cr.entity_id, v_pno, v_pdate, v_creditor_id, v_cid, v_error);

         if(v_error is not null) THEN
           lx_write_log(sysdate, v_procedure_name, v_error);
           CONTINUE;
         END IF;

         update lx_currency_by_credit
             set pno = v_pno,
                 pdate = v_pdate,
                 creditor_id = v_creditor_id,
                 cid = v_cid
          where entity_id = cr.entity_id;

    END LOOP;

  END LX_CURRENCY_BY_CREDIT_FILL;