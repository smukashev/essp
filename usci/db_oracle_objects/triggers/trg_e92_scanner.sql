create or replace trigger trg_e92_scanner
before DELETE
  on eav_be_entities for each row
DECLARE
  v_control number;
BEGIN

  select 1 into v_control from DUAL
      where exists (select 1 from eav_be_complex_values where entity_value_id = :old.id);

  if(v_control = 1) THEN
    raise_application_error(-20001,'e92 scanner fail');
  end if;

  exception
    when no_data_found then
      null;
end;