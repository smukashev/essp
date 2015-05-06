create or replace FUNCTION                   GET_REPORT_STATUS_NAME_RU (status_id in NUMBER) RETURN VARCHAR2 AS
BEGIN
  IF status_id = 90 then
    return 'В процессе';
  ELSIF status_id = 91 then
    return 'Ошибка межформенного контроля';
  ELSIF status_id = 92 then
    return 'Завершен/Утвержден';
  ELSIF status_id = 74 then
    return 'Отчитались не полностью';
  ELSIF status_id = 75 then
    return 'Отчитались полностью';
  ELSIF status_id = 76 then
    return 'Отконтроллирован с ошибками';
  ELSIF status_id = 77 then
    return 'Отконтроллирован без ошибок';
  ELSIF status_id = 128 then
    return 'Утвержден организацией';
  END IF;
END GET_REPORT_STATUS_NAME_RU;