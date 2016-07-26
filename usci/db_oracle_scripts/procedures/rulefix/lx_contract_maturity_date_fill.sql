CREATE OR REPLACE PROCEDURE LX_CONTRACT_MATURITY_DATE_FILL
AS
  v_procedure_name VARCHAR2(250) := 'LX_CONTRACT_MATURITY_DATE_FILL';
  v_pno VARCHAR(250);
  v_pdate date;
  v_creditor_id number;
  v_cid number;
  v_error VARCHAR2(250);
  v_cmd date;
  BEGIN

    delete from lx_contract_maturity_date;

    insert into lx_contract_maturity_date (
      select id as entity_id, rpad('0',250,'0') as pno, null as pdate, -1 as creditor_id, -1 as cid,
        null as contract_maturity_date,
        -1 as credit_id from (
      select ebe.id, c1.value as contract_maturity_date, c2.entity_value_id as type_id from eav_be_entities ebe
      left outer join eav_be_date_values c1 on c1.attribute_id = 156 and c1.entity_id = ebe.id
      left outer join eav_be_complex_values c2 on c2.attribute_id = 64 and c2.entity_id = ebe.id
      where ebe.class_id= 59
      ) where contract_maturity_date is null and type_id not in (2259,2261,2251,2246,2247,2244,2249,2268));

    for cr in (select * from lx_contract_maturity_date)
      loop
         lx_find_primary_contract(cr.entity_id, v_pno, v_pdate, v_creditor_id, v_cid, v_error);

         if(v_error is not null) THEN
           lx_write_log(sysdate, v_procedure_name, v_error);
           CONTINUE;
         END IF;

         update lx_contract_maturity_date
             set pno = v_pno,
                 pdate = v_pdate,
                 creditor_id = v_creditor_id,
                 cid = v_cid
          where entity_id = cr.entity_id;


        begin
         select vch.contract_maturity_date into v_cmd
            from v_credit_his@credits vch
           where vch.contract_maturity_date is not NULL
             and vch.primary_contract_no = v_pno
             and vch.primary_contract_date = v_pdate
             and vch.creditor_id = v_cid
             and rownum = 1;

          update lx_contract_maturity_date
             set contract_maturity_date = v_cmd
           where entity_id = cr.entity_id;

        EXCEPTION
          when no_data_found then
            continue;
        end;
    END LOOP;

  END LX_CONTRACT_MATURITY_DATE_FILL;