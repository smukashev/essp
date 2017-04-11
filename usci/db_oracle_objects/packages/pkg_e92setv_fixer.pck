/*
CREATE TABLE LX_E92SETV_FIXER" (
  "ID" NUMBER(14,0) primary key,
  "SET_ID" NUMBER(14,0),
  "CREDITOR_ID" NUMBER(14,0),
  "REPORT_DATE" DATE NOT NULL,
  "ENTITY_VALUE_ID" NUMBER(14,0),
  "IS_CLOSED" NUMBER(1,0) NOT NULL,
  "IS_LAST" NUMBER(1,0) NOT NULL)
*/

CREATE OR REPLACE PACKAGE PKG_E92SETV_FIX
IS
  c_default_job_max_count CONSTANT NUMBER := 20;
  c_default_job_size      CONSTANT NUMBER := 1000000;
  PROCEDURE run;
  PROCEDURE run_as_job;
  PROCEDURE run_interval(
    p_start_index NUMBER,
    p_end_index   NUMBER);
  PROCEDURE write_log(
    p_message IN VARCHAR2);
END PKG_E92SETV_FIX;
/



CREATE OR REPLACE PACKAGE BODY PKG_E92SETV_FIX
IS
  PROCEDURE write_log(
    p_message IN VARCHAR2)
  IS
    BEGIN
      INSERT INTO lx_E92_log VALUES
        (seq_E92_log_id.nextval, p_message
        );
    END;
  PROCEDURE run_as_job
  IS
    BEGIN
      DELETE FROM lx_E92_worker;
      DELETE FROM lx_E92SETV_fixer;
      dbms_scheduler.create_job(job_name => 'ES_E92SETV_job_runner', job_type => 'PLSQL_BLOCK', job_action => 'BEGIN PKG_E92SETV_FIX.RUN; END;', start_date => systimestamp, repeat_interval => NULL, enabled => true, auto_drop => true);
    END;
  PROCEDURE run
  IS
    v_job_count NUMBER;
    v_start_id  NUMBER;
    v_end_id    NUMBER;
    BEGIN
      WHILE(true)
      LOOP
        SELECT COUNT(*) INTO v_job_count FROM lx_E92_worker WHERE status = 'RUNNING';
        IF(v_job_count < c_default_job_max_count) THEN
          BEGIN
            SELECT NVL(MAX(end_id), 2) INTO v_start_id FROM lx_E92_worker;
            EXCEPTION
            WHEN no_data_found THEN
            v_start_id := 1;
          END;
          SELECT MAX(id)
          INTO v_end_id
          FROM
            (SELECT id FROM eav_be_complex_set_values WHERE id >= v_start_Id ORDER BY id
            )
          WHERE rownum <= c_default_job_size;
          IF(v_start_id = v_end_id) THEN
            EXIT;
          END IF;
          INSERT
          INTO lx_E92_worker
          (
            id,
            start_id,
            end_id,
            status,
            start_date
          )
          VALUES
            (
              seq_lx_E92_worker_id.nextval,
              v_start_Id,
              v_end_id,
              'RUNNING',
              systimestamp
            );
          dbms_scheduler.create_job( job_name => 'ES_JOB_E92SETV_' || v_end_id, job_type => 'PLSQL_BLOCK', job_action => 'BEGIN PKG_E92SETV_FIX.run_interval(p_start_index => '|| v_start_Id ||', p_end_index => '|| v_end_id || '); END;', start_date => systimestamp, repeat_interval => NULL, enabled =>true, auto_drop => true);
        ELSE
          dbms_lock.sleep(5);
        END IF;
      END LOOP;

      --wait jobs to finish
      while(true)
      loop
        select count(*)
        into v_job_count
        from lx_e92_worker
        where status = 'RUNNING';

        if(v_job_count > 0) then
          dbms_lock.sleep(3);
        else
          exit;
        end if;
      end loop;

      EXCEPTION
      WHEN OTHERS THEN
      write_log(p_message => SQLERRM);
    END;
  PROCEDURE run_interval
    (
      p_start_index NUMBER,
      p_end_index   NUMBER
    )
  IS
    BEGIN
      INSERT
      INTO lx_E92SETV_fixer
      (
        id,
        set_id,
        creditor_id,
        report_date,
        entity_value_id,
        is_closed,
        is_last
      )
        SELECT seq_lxE92_fixer_id.nextval id,
               t.id AS set_id,
          t.creditor_id,
          t.report_date,
          t.entity_value_id,
          t.is_closed,
          t.is_last
        FROM eav_be_complex_set_values t
        WHERE NOT EXISTS
        (SELECT 1 FROM eav_be_entities WHERE id = t.entity_value_id
        )
              AND t.id >= p_start_index
              AND t.id  < p_end_index;
      UPDATE lx_E92_worker
      SET status     = 'COMPLETED',
        end_date     = systimestamp
      WHERE start_id = p_start_index
            AND end_id     = p_end_index;
      COMMIT;

      EXCEPTION
        WHEN OTHERS THEN
            write_log(p_message => SQLERRM);
    END;
END PKG_E92SETV_FIX;