/*
create table lx_e103_fixer(
  id number (14) primary key,
  cv_id number (14) not null,
  attribute_id number (14) not null,
  creditor_id number (14) not null,
  report_date date)

create sequence seq_lxe103_fixer_id
    minvalue 1
    maxvalue 9999999999999999999999
    start with 1
    increment by 1
    cache 20

create table lx_e103_worker (
    id number(14) primary key,
    start_id number(14) not null,
    end_id number(14) not null,
    status varchar(35),
    start_date date,
    end_date date)

create sequence seq_lx_e103_worker_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20

create table lx_e103_log(
   id number(14) primary key,
   message varchar2(512))

create sequence seq_e103_log_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20


 */

create or replace package PKG_E103_FIX is
  c_default_job_max_count constant number := 20;
  c_default_job_size constant number := 1000000;
  procedure run;
  procedure run_as_job;
  procedure run_interval (p_start_index number,
                          p_end_index   number);

end PKG_E103_FIX;
/



create or replace PACKAGE BODY PKG_E103_FIX IS
  procedure run_as_job is
  begin
    delete from lx_e103_worker;
    delete from lx_e103_fixer;

    dbms_scheduler.create_job(job_name => 'ES_E103_job_runner',
                              job_type => 'PLSQL_BLOCK',
                              job_action => 'BEGIN
                                              PKG_E103_FIX.RUN;
                                            END;',
                              start_date => systimestamp,
                              repeat_interval => null,
                              enabled => true,
                              auto_drop => true);
  end;

  procedure run is
    v_job_count   number;
    v_start_id    number;
    v_end_id     number;
  begin

  while(true)
    loop
      select count(*)
        into v_job_count
        from lx_e103_worker
       where status = 'RUNNING';


      if(v_job_count < c_default_job_max_count) then
        begin
          select nvl(max(end_id), 2)
            into v_start_id
            from lx_e103_worker;

          exception
            when no_data_found then
              v_start_id := 1;
        end;


        select max(id)
          into v_end_id
          from (select id
                  from eav_be_complex_values
                  where id >= v_start_Id
                  order by id)
          where rownum <= c_default_job_size;

        if(v_start_id = v_end_id) then
          exit;
        end if;


        insert into lx_e103_worker(id, start_id, end_id,status, start_date)
              values (seq_lx_e103_worker_id.nextval, v_start_Id, v_end_id, 'RUNNING', systimestamp);

        dbms_scheduler.create_job( job_name => 'ES_JOB_E103_' || v_start_id || '_' || v_end_id,
                              job_type => 'PLSQL_BLOCK',
                              job_action => 'BEGIN
                                              PKG_E103_FIX.run_interval(p_start_index => '|| v_start_Id ||', p_end_index => '|| v_end_id || ');
                                            END;',
                              start_date => systimestamp,
                              repeat_interval => null,
                              enabled =>true,
                              auto_drop => true);
      else
        dbms_lock.sleep(5);
      end if;

    end loop;
  end;

  procedure run_interval( p_start_index number,
                          p_end_index number) is
  begin
    insert into lx_e103_fixer (id, cv_id, attribute_id, creditor_id, report_date)
      select seq_lxe103_fixer_id.nextval id, t.id as cv_id, t.attribute_id, t.creditor_id, t.report_date
        from eav_be_complex_values t
       where not exists (select 1 from eav_be_entity_report_dates where entity_id = t.entity_value_id)
         and t.id >= p_start_index
         and t.id < p_end_index;

    update lx_e103_worker set status = 'COMPLETED', end_date = systimestamp
      where start_id = p_start_index
        and end_id = p_end_index;

    commit;
  end;


end PKG_E103_FIX;
/