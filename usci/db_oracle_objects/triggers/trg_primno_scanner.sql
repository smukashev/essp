create or replace trigger trg_primno_scanner
before DELETE
  on eav_be_string_values for each row
DECLARE
  v_control number;
BEGIN
  if(:OLD.attribute_id = 153) then
    raise_application_error(-20002,'Нельзая удалить номер документа договора');
  end if;
end;