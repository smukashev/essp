/*
CREATE TABLE LX_E92_WORKER (
    "ID"         NUMBER(14,0) primary key,
    "START_ID"   NUMBER(14,0) NOT NULL,
    "END_ID"     NUMBER(14,0) NOT NULL,
    "STATUS"     VARCHAR2(35),
    "START_DATE" DATE,
    "END_DATE"   DATE)

CREATE SEQUENCE seq_E92_log_id
    MINVALUE 1
    MAXVALUE 9999999999999999 INCREMENT BY 1
    START WITH 1
    CACHE 20

CREATE SEQUENCE seq_lx_E92_worker_id
    MINVALUE 1
    MAXVALUE 9999999999999999
    INCREMENT BY 1
    START WITH 1
    CACHE 20

CREATE SEQUENCE seq_lxE92_fixer_id
    MINVALUE 1
    MAXVALUE 9999999999999999999999
    INCREMENT BY 1
    START WITH 1
    CACHE 20

CREATE TABLE LX_E92_FIXER (
    "ID" NUMBER(14,0) primary key,
    "CV_ID" NUMBER(14,0) NOT NULL,
    "ENTITY_ID" NUMBER(14,0) NOT NULL,
    "ATTRIBUTE_ID" NUMBER(14,0) NOT NULL,
    "CREDITOR_ID" NUMBER(14,0) NOT NULL,
    "REPORT_DATE" DATE)

CREATE TABLE LX_E92_LOG (
    "ID" NUMBER(14,0) primary key,
    "MESSAGE" VARCHAR2(512))
*/

CREATE OR REPLACE PACKAGE PKG_E92_FIX
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
END PKG_E92_FIX;
/


CREATE OR REPLACE PACKAGE BODY PKG_E92_FIX
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
      DELETE FROM lx_E92_fixer;
      dbms_scheduler.create_job(job_name => 'ES_E92_job_runner', job_type => 'PLSQL_BLOCK', job_action => 'BEGIN PKG_E92_FIX.RUN; END;', start_date => systimestamp, repeat_interval => NULL, enabled => true, auto_drop => true);
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
            (SELECT id FROM eav_be_complex_values WHERE id >= v_start_Id ORDER BY id
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
          dbms_scheduler.create_job( job_name => 'ES_JOB_E92_' || v_end_id, job_type => 'PLSQL_BLOCK', job_action => 'BEGIN PKG_E92_FIX.run_interval(p_start_index => '|| v_start_Id ||', p_end_index => '|| v_end_id || '); END;', start_date => systimestamp, repeat_interval => NULL, enabled =>true, auto_drop => true);
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
      INTO lx_E92_fixer
      (
        id,
        cv_id,
        entity_id,
        attribute_id,
        creditor_id,
        report_date
      )
        SELECT seq_lxE92_fixer_id.nextval id,
               t.id AS cv_id,
               t.entity_value_id entity_Id,
          t.attribute_id,
          t.creditor_id,
          t.report_date
        FROM eav_be_complex_values t
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
    END;
END PKG_E92_FIX;