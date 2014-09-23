create or replace procedure ref_sc_script is
  i integer;
  v_complex integer;
  v_set_complex integer;
  v_set_simple integer;
  v_name varchar(100);
  type Dictionary is table of varchar(100) index by varchar(100);
  bag Dictionary;
begin
  i:=0;
  /*
      if shortener needed uncomment
  bag('REF_BALANCE_ACCOUNT') := 'REF_B_ACC';
  bag('REF_BANK_RELATION') := 'REF_B_REL';
  bag('REF_CLASSIFICATION') := 'REF_CLAS';
  bag('REF_CONTACT_TYPE') := 'R_REF_CONTRACT_TYPE';
  bag('REF_COUNTRY') := 'REF_COUNTRY';
  bag('REF_CREDIT_OBJECT') := 'REF_CRED_OB';
  bag('REF_CREDITOR') := 'REF_CREDITOR';
  bag('REF_CREDITOR_BRANCH') := 'REF_CREDITOR_BRANCH';
  bag('REF_CREDIT_PURPOSE') := 'REF_C_PUR';
  bag('REF_CREDIT_TYPE') := 'REF_CCREDIT_TYPE';
  bag('REF_CURRENCY') := 'REF_CURRENCY';
  bag('REF_DEBTOR_TYPE') := 'REF_DEBTOR_T';
  bag('REF_DOC_TYPE') := 'REF_DOC_TYPE';
  bag('REF_ECON_TRADE') := 'REF_ECON';
  bag('REF_ENTERPRISE_TYPE') := 'REF_ENT_TYPE';
  bag('REF_FINANCE_SOURCE') := 'REF_FINANCE_S';
  bag('REF_LEGAL_FORM') := 'REF_LEG_FORM';
  bag('REF_OFFSHORE') := 'REF_OFFSHORE';
  bag('REF_PLEDGE_TYPE') := 'REF_PLEDGE_T';
  bag('REF_PORTFOLIO') := 'REF_PORTFOLIO';
  bag('REF_REGION') := 'REF_REGION';
  bag('REF_SUBJECT_TYPE') := 'REF_SUBJECT_T';*/


  for c in (select * from eav_m_classes where name like 'ref%' order by name)
  loop
       select count(1)
         into v_complex
         from eav_m_complex_attributes t1
         where t1.containing_id = c.id;

       select count(1)
         into v_set_complex
         from eav_m_complex_set t2
         where t2.containing_id = c.id;

       select count(1)
         into v_set_simple
         from eav_m_simple_set t3
         where t3.containing_id = c.id;

       v_name := upper(c.name);
       if(v_complex + v_set_complex + v_set_simple > 0) then
         v_name := v_name || '*';
       end if;

       dbms_output.put_line('#=================================================================================');
       dbms_output.put_line('#  ' || to_char(i+1) || '.' || v_name);
       dbms_output.put_line('#=================================================================================');
       dbms_output.put_line('');

       dbms_output.put_line('showcase set meta ' || c.name);
       dbms_output.put_line('');

       dbms_output.put_line('showcase set name ' || upper(c.name) );
       dbms_output.put_line('showcase set tableName ' || upper(c.name));
       dbms_output.put_line('');

       for iter in (select * from eav_m_simple_attributes t where t.containing_id = c.id order by t.name)
       loop
         dbms_output.put_line('showcase list add ' || iter.name);
       end loop;


       dbms_output.put_line('');
       dbms_output.put_line('showcase save');
       dbms_output.put_line('#---------------------------------------------------------------------------------');
       dbms_output.put_line('');
       dbms_output.put_line('');
       i:=i+1;
  end loop;
end ref_sc_script;
