CREATE OR REPLACE PROCEDURE SYNC_CREDITOR AS
  cr_name varchar2(255);
  usci_id integer;
  entity_found integer(1);
BEGIN
  for cu in (select * from eav_a_creditor_user)
  loop

    entity_found := 1;

    select cr.name into cr_name from ref.creditor@core_credits cr
    where cr.id = cu.creditor_id;

    if(cr_name is not null) then
      begin
        select st.entity_id into usci_id from eav_be_string_values st
          where st.attribute_id = 41 and st.value = cr_name;
      exception
        when NO_DATA_FOUND THEN
          dbms_output.put_line(cu.creditor_id || '    ' || cr_name);
          entity_found := 0;
      end;

        if(usci_id is not null and entity_found > 0) then
          update eav_a_creditor_user cu2 set cu2.creditor_id = usci_id
          where cu2.id = cu.id;
        end if;
    else
      dbms_output.put_line('Not found: ' || cu.creditor_id);
    end if;

  end loop;
END SYNC_CREDITOR;


---------------------------------------------------------------------------------------------------------
-- to check
select en.id, cr.id, st.value, cr.name from eav_be_entities en
join eav_be_string_values st on st.ENTITY_ID = en.id and st.attribute_id = 41
join ref.creditor@core_credits cr on cr.name = st.value
where en.class_id = 8
order by st.value;

-- get name by id
select cr.name from ref.creditor@core_credits cr where cr.id = 37;

-- get entity id by name
select st.entity_id from eav_be_string_values st where st.attribute_id = 41 and st.value = 'АО «Национальная компания Продовольственная контрактная корпорация»';
