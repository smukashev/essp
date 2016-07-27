CREATE OR REPLACE PROCEDURE LX_CURRENCY_BY_CREDIT_FILL
AS
  v_procedure_name VARCHAR2(250) := 'LX_CURRENCY_BY_CREDIT_FILL';
  v_pno VARCHAR(250);
  v_pdate date;
  v_creditor_id number;
  v_cid number;
  v_error VARCHAR2(250);
  v_short_name VARCHAR2(100);
  v_currency_id number;
  BEGIN

    delete from lx_currency_by_credit;

    insert into lx_currency_by_credit (
      select id as entity_id, rpad('0',250,'0') as pno, date '1990-01-01' as pdate, -1 as creditor_id, -1 as cid, 'NNNN' as f_currency_sn, -1 as f_currency_id from (
      select ebe.id, c1.entity_value_id, c2.entity_value_id as type_id from eav_be_entities ebe
      left outer join eav_be_complex_values c1 on c1.attribute_id = 65 and c1.entity_id = ebe.id
      left outer join eav_be_complex_values c2 on c2.attribute_id = 64 and c2.entity_id = ebe.id
      where ebe.class_id= 59
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


    begin
      select (select short_name from ref.currency@credits where id = vch.currency_id) into v_short_name
        from v_credit_his@credits vch
       where vch.primary_contract_no = v_pno
         and vch.primary_contract_date = v_pdate
         and vch.creditor_id = v_cid
         and vch.currency_id is not null
         and rownum = 1;

      select entity_id into v_currency_id
        from eav_be_string_values
      where attribute_id = 133
        and value = v_short_name;


      update lx_currency_by_credit
         set f_currency_sn = v_short_name,
             f_currency_id = v_currency_id
        where entity_id = cr.entity_id;

      EXCEPTION
        when no_data_found then
           continue;
    end;

    END LOOP;

  END LX_CURRENCY_BY_CREDIT_FILL;