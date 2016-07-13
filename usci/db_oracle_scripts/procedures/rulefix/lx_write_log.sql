/*
create table lx_log as
  select sysdate as log_date, rpad('rule name',250,'0') rule_name, rpad('this is sampletext',1050,'0') as log_message from dual;
*/
create or replace procedure lx_write_log(
  p_log_date IN DATE,
  p_rule_name in varchar2,
  p_log_text IN VARCHAR2
) as
begin
  insert into lx_log values (p_log_date, p_rule_name, p_log_text);
  commit;
end;