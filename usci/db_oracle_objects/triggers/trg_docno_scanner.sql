create or replace trigger trg_docno_scanner
before DELETE
  on eav_be_string_values for each row
DECLARE
  v_control number;
BEGIN
  if(:OLD.attribute_id = 14) then
    raise_application_error(-20002,'Нельзая удалить номер документа');
  end if;
end;