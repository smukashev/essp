/*
create temporary table lx_pledge_his#(
    id number(14),
    pledge_id number(14),
    value number(17,3),
    open_date date,
    close_date date,
    h_id number(14))

create table lx_pledge_his (
    id number(14),
    pledge_id number(14),
    value number(17,3),
    open_date date,
    close_date date,
    h_id number(14))

create index ind_lx_pledge_his on lx_pledge_his (pledge_id)

create table lx_history_value_worker (
    id number(14) primary key,
    start_id number(14) not null,
    end_id number(14) not null,
    status varchar(35),
    start_date date,
    end_date date
)

create sequence seq_lx_history_value_worker_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20

create table lx_pledge_keys (
    id number(14),
    creditor_id number(14) not null,
    pledge_id number(14),
    contract_no varchar2(1024),
    type_id number(14),
    value number(17,3),
    open_date date,
    close_date date)

create index ind_lx_pledge_keys on lx_pledge_keys (pledge_id)

create sequence seq_lx_pledge_keys_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20

create table lx_pledge_keys_worker (
    id number(14) primary key,
    start_id number(14) not null,
    end_id number(14) not null,
    status varchar(35),
    start_date date,
    end_date date
)

create sequence seq_lx_pledge_keys_worker_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20


create table lx_pledge_setv (
    id number(14),
    set_id number(14),
    creditor_id number(14),
    pledge_id number(14),
    contract_no varchar2(1024),
    type_id number(14),
    value number(17,3),
    open_date date,
    close_date date)

create index ind_lx_pledge_setv on lx_pledge_setv (set_id)

create sequence seq_lx_pledge_setv_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20

create table lx_pledge_setv_worker (
    id number(14) primary key,
    start_id number(14) not null,
    end_id number(14) not null,
    status varchar(35),
    start_date date,
    end_date date
)

create sequence seq_lx_pledge_setv_worker_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20



create table lx_pledge_showcase (
  id number (14) primary key,
  credit_id number (14),
  creditor_id number (14),
  pledge_id number (14) not null,
  contract_no varchar2(1024),
  type_id number(14) not null,
  value number(17,3),
  open_date date,
  close_date date)

create sequence seq_lx_pledge_showcase_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20

create table lx_pledge_credit_worker (
    id number(14) primary key,
    start_id number(14) not null,
    end_id number(14) not null,
    status varchar(35),
    start_date date,
    end_date date
)

create sequence seq_lx_pledge_credit_worker_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20

create table lx_pledge_builder_log (
   id number(14) primary key,
   message varchar2(512))

create sequence seq_pledge_builder_log_id
  minvalue 1
  maxvalue 9999999999999999
  start with 1
  increment by 1
  cache 20
 */

create or replace package PKG_PLEDGE_BUILDER is
  c_default_job_max_count constant number := 5;
  c_default_job_size constant number := 1000000;
  procedure run;
  procedure run_as_job;
  procedure clear_all;
  /*procedure run_interval (p_start_index number,
                          p_end_index   number);*/
  procedure write_log(p_message in varchar2);

  procedure RUN_HISTORY_VALUE;
  procedure run_pledge_keys;
  procedure run_pledge_setv;
  procedure run_pledge_credit;

  procedure build_pledge_value(p_start_id number, p_end_id number);
  procedure build_pledge_keys(p_start_id number, p_end_id number);
  procedure build_pledge_setv(p_start_id number, p_end_id number);
  procedure build_pledge_credit(p_start_id number, p_end_id number);

end PKG_PLEDGE_BUILDER;
/


create or replace PACKAGE BODY PKG_PLEDGE_BUILDER IS

  procedure write_log(p_message in varchar2) is
    begin
      insert into lx_pledge_builder_log
      values (seq_pledge_builder_log_id.nextval, p_message);
    end;

  procedure run_as_job is
    begin
      --delete from lx_pledge_his_worker;
      --delete from lx_pbuild_fixer;
      --truncate table lx_history_value_worker;
      --truncate table lx_pledge_his;
      --truncate table lx_pledge_builder_log;

      dbms_scheduler.create_job(job_name => 'ES_pbuild_job_runner',
                                job_type => 'PLSQL_BLOCK',
                                job_action => 'BEGIN
                                              PKG_PLEDGE_BUILDER.RUN;
                                            END;',
                                start_date => systimestamp,
                                repeat_interval => null,
                                enabled => true,
                                auto_drop => true);
    end;


  procedure run is
    begin
      run_history_value;
      run_pledge_keys;
      run_pledge_setv;
      run_pledge_credit;
    END;

  procedure clear_all is
    begin
      execute immediate 'truncate table lx_pledge_his';
      execute immediate 'truncate table lx_history_value_worker';
      execute immediate 'truncate table lx_pledge_keys';
      execute immediate 'truncate table lx_pledge_keys_worker';
      execute immediate 'truncate table lx_pledge_setv';
      execute immediate 'truncate table lx_pledge_setv_worker';
      execute immediate 'truncate table lx_pledge_showcase';
      execute immediate 'truncate table lx_pledge_credit_worker';
      execute immediate 'truncate table lx_pledge_builder_log';
    END;


  procedure run_history_value is
    v_job_count   number;
    v_start_id    number;
    v_end_id     number;
    begin
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_history_value_worker
        where status = 'RUNNING';

        if(v_job_count < c_default_job_max_count) then
          begin
            select nvl(max(end_id), 1)
            into v_start_id
            from lx_history_value_worker;

            exception
            when no_data_found then
            v_start_id := 1;
          end;

          select max(id)
          into v_end_id
          from (select id
                from eav_be_entities
                where id >= v_start_Id
                  and class_id = 56
                order by id)
          where rownum <= c_default_job_size;

          if(v_end_id is null) then
            exit;
          end if;

          if(v_start_id = v_end_id) then
            v_end_id := v_end_id + 1;
          end if;

          insert into lx_history_value_worker(id, start_id, end_id,status, start_date)
          values (seq_lx_history_value_worker_id.nextval, v_start_Id, v_end_id, 'RUNNING', systimestamp);

          dbms_scheduler.create_job( job_name => 'ES_HV_' || v_end_id,
                                     job_type => 'PLSQL_BLOCK',
                                     job_action => 'BEGIN
                                              PKG_PLEDGE_BUILDER.build_pledge_value(p_start_id => '|| v_start_Id ||', p_end_id => '|| v_end_id || ');
                                            END;',
                                     start_date => systimestamp,
                                     repeat_interval => null,
                                     enabled =>true,
                                     auto_drop => true);
        else
          dbms_lock.sleep(3);
        end if;
      end loop;


      --wait jobs to finish
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_history_value_worker
        where status = 'RUNNING';

        if(v_job_count > 0) then
          dbms_lock.sleep(3);
        else
          exit;
        end if;
      end loop;

      exception
      when others then
      write_log(p_message => SQLERRM);
    end;

  procedure build_pledge_value(p_start_id number,
                                p_end_id number) is
    begin

      insert into lx_pledge_his#
        select rownum id,
               d.entity_id pledge_id,
          --(select value from eav_be_string_values where attribute_id = 150 and entity_id = d.entity_id) contract_no,
          --(select entity_value_id from eav_be_complex_values where attribute_id = 53 and entity_id = d.entity_id) type_id,
          d.value,
               d.report_date open_date,
               null close_date,
               rank() over (partition by entity_id order by report_date )  h_id
        from eav_be_double_values d
        where d.attribute_id = 151
              and d.entity_id >= p_start_id
              and d.entity_id < p_end_Id;

      insert into lx_pledge_his
        select t1.id, t1.pledge_id, /*t1.contract_no, t1.type_id,*/ t1.value, t1.open_date,
          case
          when t2.open_date is null and t1.open_date < setv.report_date then setv.report_date
          else t2.open_date
          end close_date,
          t1.h_id
        from lx_pledge_his# t1
          left outer join lx_pledge_his# t2 on (t1.pledge_id = t2.pledge_id and t1.h_id + 1 = t2.h_id)
          left outer join eav_be_complex_set_values setv on (entity_value_id = t1.pledge_id and is_closed = 1);

      update lx_history_value_worker
      set status = 'COMPLETED', end_date = systimestamp
      where start_id = p_start_id
            and end_id = p_end_id;

      exception
      when others then
            write_log(p_message => SQLERRM);

    end;


  procedure run_pledge_keys is
    v_job_count   number;
    v_start_id    number;
    v_end_id     number;
    begin
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_pledge_keys_worker
        where status = 'RUNNING';

        if(v_job_count < c_default_job_max_count) then
          begin
            select nvl(max(end_id), 1)
            into v_start_id
            from lx_pledge_keys_worker;

            exception
            when no_data_found then
            v_start_id := 1;
          end;

          select max(id)
          into v_end_id
          from (select /*+ PARALLEL(15) */ id
                from eav_be_complex_values
                where id >= v_start_Id
                      and attribute_id = 53
                order by id)
          where rownum <= c_default_job_size;

          if(v_end_id is null) then
            exit;
          end if;

          if(v_start_id = v_end_id) then
            v_end_id := v_end_id + 1;
          end if;

          insert into lx_pledge_keys_worker(id, start_id, end_id,status, start_date)
          values (seq_lx_pledge_keys_worker_id.nextval, v_start_Id, v_end_id, 'RUNNING', systimestamp);

          dbms_scheduler.create_job( job_name => 'ES_PL_KEYS_' || v_end_id,
                                     job_type => 'PLSQL_BLOCK',
                                     job_action => 'BEGIN
                                              PKG_PLEDGE_BUILDER.build_pledge_keys(p_start_id => '|| v_start_Id ||', p_end_id => '|| v_end_id || ');
                                            END;',
                                     start_date => systimestamp,
                                     repeat_interval => null,
                                     enabled =>true,
                                     auto_drop => true);
        else
          dbms_lock.sleep(3);
        end if;
      end loop;

      --wait jobs to finish
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_pledge_keys_worker
        where status = 'RUNNING';

        if(v_job_count > 0) then
          dbms_lock.sleep(3);
        else
          exit;
        end if;
      end loop;


      exception
      when others then
      write_log(p_message => SQLERRM);
    end;

  procedure build_pledge_keys(p_start_id number,
                              p_end_id number) is
    begin
      insert into lx_pledge_keys
        select seq_lx_pledge_keys_id.nextval id,
          cv.creditor_id,
               cv.entity_id pledge_id,
               sv.value contract_no,
               cv.entity_value_id type_id,
          h.value,
          h.open_date,
          h.close_date
        from eav_be_complex_values cv
          left outer join eav_be_string_values sv on (sv.entity_id = cv.entity_id and sv.attribute_id = 150)
          left outer join lx_pledge_his h on (cv.entity_id = h.pledge_id)
        where cv.attribute_id = 53
              and cv.id >= p_start_id
              and cv.id < p_end_id;

      update lx_pledge_keys_worker
      set status = 'COMPLETED', end_date = systimestamp
      where start_id = p_start_id
            and end_id = p_end_id;
    end;

  procedure run_pledge_setv is
    v_job_count   number;
    v_start_id    number;
    v_end_id     number;
    begin
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_pledge_setv_worker
        where status = 'RUNNING';

        if(v_job_count < c_default_job_max_count) then
          begin
            select nvl(max(end_id), 1)
            into v_start_id
            from lx_pledge_setv_worker;

            exception
            when no_data_found then
            v_start_id := 1;
          end;

          select max(id)
          into v_end_id
          from (select /*+ PARALLEL(15) */ id
                from eav_be_complex_set_values
                where id >= v_start_Id
                      and is_closed = 0
                order by id)
          where rownum <= c_default_job_size;

          if(v_end_id is null) then
            exit;
          end if;

          if(v_start_id = v_end_id) then
            v_end_id := v_end_id + 1;
          end if;

          insert into lx_pledge_setv_worker(id, start_id, end_id,status, start_date)
          values (seq_lx_pledge_setv_worker_id.nextval, v_start_Id, v_end_id, 'RUNNING', systimestamp);

          dbms_scheduler.create_job( job_name => 'ES_PL_SETV_' || v_end_id,
                                     job_type => 'PLSQL_BLOCK',
                                     job_action => 'BEGIN
                                              PKG_PLEDGE_BUILDER.build_pledge_setv(p_start_id => '|| v_start_Id ||', p_end_id => '|| v_end_id || ');
                                            END;',
                                     start_date => systimestamp,
                                     repeat_interval => null,
                                     enabled =>true,
                                     auto_drop => true);
        else
          dbms_lock.sleep(3);
        end if;
      end loop;


      --wait jobs to finish
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_pledge_setv_worker
        where status = 'RUNNING';

        if(v_job_count > 0) then
          dbms_lock.sleep(3);
        else
          exit;
        end if;
      end loop;


      exception
      when others then
      write_log(p_message => SQLERRM);
    end;

  procedure build_pledge_setv(p_start_id number,
                              p_end_id number) is
    begin
      insert into lx_pledge_setv
        select seq_lx_pledge_setv_id.nextval id,
          setv.set_Id,
          his.creditor_id,
          his.pledge_id,
          his.contract_no, his.type_id, his.value,
          his.open_date, his.close_date
        from eav_be_complex_set_values setv
          join lx_pledge_keys his on (setv.entity_value_id = his.pledge_id)
        where setv.id >= p_start_id
              and setv.id < p_end_id
              and is_closed = 0;

      update lx_pledge_setv_worker
      set status = 'COMPLETED', end_date = systimestamp
      where start_id = p_start_id
            and end_id = p_end_id;
    end;


  procedure run_pledge_credit is
    v_job_count   number;
    v_start_id    number;
    v_end_id     number;
    begin
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_pledge_credit_worker
        where status = 'RUNNING';

        if(v_job_count < c_default_job_max_count) then
          begin
            select nvl(max(end_id), 1)
            into v_start_id
            from lx_pledge_credit_worker;

            exception
            when no_data_found then
            v_start_id := 1;
          end;

          select max(id)
          into v_end_id
          from (select /*+ PARALLEL(15) */ id
                from eav_be_entity_complex_sets
                where id >= v_start_Id
                      and attribute_id = 14
                order by id)
          where rownum <= c_default_job_size;

          if(v_end_id is null) then
            exit;
          end if;

          if(v_start_id = v_end_id) then
            v_end_id := v_end_id + 1;
          end if;

          insert into lx_pledge_credit_worker(id, start_id, end_id,status, start_date)
          values (seq_lx_pledge_credit_worker_id.nextval, v_start_Id, v_end_id, 'RUNNING', systimestamp);

          dbms_scheduler.create_job( job_name => 'ES_PL_SH_' || v_end_id,
                                     job_type => 'PLSQL_BLOCK',
                                     job_action => 'BEGIN
                                              PKG_PLEDGE_BUILDER.build_pledge_credit(p_start_id => '|| v_start_Id ||', p_end_id => '|| v_end_id || ');
                                            END;',
                                     start_date => systimestamp,
                                     repeat_interval => null,
                                     enabled =>true,
                                     auto_drop => true);
        else
          dbms_lock.sleep(3);
        end if;
      end loop;


      --wait jobs to finish
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_pledge_credit_worker
        where status = 'RUNNING';

        if(v_job_count > 0) then
          dbms_lock.sleep(3);
        else
          exit;
        end if;
      end loop;


      exception
      when others then
      write_log(p_message => SQLERRM);
    end;

  procedure build_pledge_credit(p_start_id number,
                                p_end_id number) is
    begin
      insert into lx_pledge_showcase
        select seq_lx_pledge_showcase_id.nextval id,
               s.entity_id as credit_id,
          pset.creditor_id,
          pset.pledge_id, pset.contract_no, pset.type_id,
          pset.value,
          pset.open_date,
          pset.close_date
        from eav_be_entity_complex_sets s
          join lx_pledge_setv pset on (s.id = pset.set_id)
        where s.attribute_id = 14
              and s.id >= p_start_id
              and s.id < p_end_id;

      update lx_pledge_credit_worker
      set status = 'COMPLETED', end_date = systimestamp
      where start_id = p_start_id
            and end_id = p_end_id;
    end;


end PKG_PLEDGE_BUILDER;
/