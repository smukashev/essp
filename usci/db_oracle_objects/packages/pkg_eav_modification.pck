create or replace package PKG_EAV_MODIFICATION is

  -- Author  : ALEXANDR.MOTOV
  -- Created : 11.12.2014 5:35:12 PM
  -- Purpose : 
  
  c_job_check_timeout constant number := 1; -- IN SECOND
  c_job_wait_timeout constant number := 36000; -- IN SECOND
  c_default_job_max_count constant number := 20;
  
  PROCEDURE insert_cd_as_job
  (
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count
  );
  
  PROCEDURE insert_cd
  (
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count
  );
  
  PROCEDURE insert_cd_by_id
  (
    p_base_entity_id IN NUMBER
  );

end PKG_EAV_MODIFICATION;
/
create or replace package body PKG_EAV_MODIFICATION is

  PROCEDURE insert_cd_as_job
  (
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count
  )
  IS
    v_job_name   VARCHAR2(200 CHAR);
    v_job_action VARCHAR2(1000 CHAR);
  BEGIN
    v_job_name := 'INSERT_CD';
    v_job_action := 'BEGIN ' ||
                      'PKG_EAV_MODIFICATION.INSERT_CD(P_JOB_MAX_COUNT => ' || p_job_max_count || '); ' ||
                    'END;';
    dbms_output.put_line(v_job_action);
    dbms_scheduler.create_job(job_name        => v_job_name,
                              job_type        => 'PLSQL_BLOCK',
                              job_action      => v_job_action,
                              start_date      => SYSTIMESTAMP,
                              repeat_interval => NULL,
                              enabled         => TRUE,
                              auto_drop       => TRUE);
  END;

  PROCEDURE insert_cd
  (
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count
  )
  IS
    v_job_name             VARCHAR2(200 CHAR);
    v_wait_begin_date      DATE;
    v_wait_end_date        DATE;
    v_now                  DATE;
    v_job_count            NUMBER;
    v_base_entity_id       NUMBER;
  BEGIN
    LOOP
      v_wait_begin_date := sysdate;
      v_wait_end_date := v_wait_begin_date + (c_job_wait_timeout * (1/86400));

      LOOP
        SELECT count(*)
          INTO v_job_count
          FROM all_scheduler_jobs j
         WHERE j.job_name like 'INSERT_CD_BY_ID_%';

        IF (v_job_count < p_job_max_count) THEN
          SELECT e.id
            INTO v_base_entity_id
            FROM eav_be_entities e
           WHERE NOT EXISTS (SELECT ecd.*
                               FROM eav_be_entity_change_dates ecd
                              WHERE ecd.entity_id = e.id)
             AND e.id NOT IN (SELECT to_number(regexp_substr(j.job_name, '([[:digit:]]{1,})'))
                                FROM all_scheduler_jobs j
                               WHERE j.job_name like 'INSERT_CD_BY_ID_%')
             AND rownum = 1;

          v_job_name := 'INSERT_CD_BY_ID_' || v_base_entity_id;
          dbms_scheduler.create_job(job_name        => v_job_name,
                                    job_type        => 'PLSQL_BLOCK',
                                    job_action      => 'BEGIN
                                                           PKG_EAV_MODIFICATION.INSERT_CD_BY_ID_(' || 'P_BASE_ENTITY_ID => ' || v_base_entity_id || ');
                                                        END;',
                                    start_date      => SYSTIMESTAMP,
                                    repeat_interval => NULL,
                                    enabled         => TRUE,
                                    auto_drop       => TRUE);
          EXIT;
        ELSE
          IF (v_wait_end_date < sysdate) THEN
            RETURN;
          END IF;

          v_now := sysdate;
          LOOP
            EXIT WHEN v_now + (c_job_check_timeout * (1/86400)) >= sysdate;
          END LOOP;
        END IF;
      END LOOP;

    END LOOP;  
  END;
  
  PROCEDURE insert_cd_by_id
  (
    p_base_entity_id IN NUMBER
  )
  IS
  BEGIN
    INSERT INTO eav_be_entity_change_dates ecd (ecd.entity_id, ecd.change_date)
      SELECT p_base_entity_id,
             rd.report_date
        FROM (SELECT v.report_date
                FROM eav_be_integer_values v
               WHERE v.entity_id = p_base_entity_id
               GROUP BY v.report_date
              UNION
              SELECT v.report_date
                FROM eav_be_boolean_values v
               WHERE v.entity_id = p_base_entity_id
               GROUP BY v.report_date
              UNION
              SELECT v.report_date
                FROM eav_be_string_values v
               WHERE v.entity_id = p_base_entity_id
               GROUP BY v.report_date
              UNION
              SELECT v.report_date
                FROM eav_be_double_values v
               WHERE v.entity_id = p_base_entity_id
               GROUP BY v.report_date
              UNION
              SELECT v.report_date
                FROM eav_be_date_values v
               WHERE v.entity_id = p_base_entity_id
               GROUP BY v.report_date
              UNION
              SELECT v.report_date
                FROM eav_be_complex_values v
               WHERE v.entity_id = p_base_entity_id
               GROUP BY v.report_date
              UNION
              SELECT ess.report_date
                FROM eav_be_entity_simple_sets ess
               WHERE ess.entity_id = p_base_entity_id
               GROUP BY ess.report_date
              UNION
              SELECT sv.report_date
                FROM eav_be_entity_simple_sets ess,
                     eav_be_sets s,
                     eav_be_double_set_values sv
               WHERE ess.entity_id = p_base_entity_id
                 AND ess.set_id = s.id
                 AND s.id = sv.set_id
               GROUP BY sv.report_date
              UNION
              SELECT sv.report_date
                FROM eav_be_entity_simple_sets ess,
                     eav_be_sets s,
                     eav_be_integer_set_values sv
               WHERE ess.entity_id = p_base_entity_id
                 AND ess.set_id = s.id
                 AND s.id = sv.set_id
               GROUP BY sv.report_date
              UNION
              SELECT sv.report_date
                FROM eav_be_entity_simple_sets ess,
                     eav_be_sets s,
                     eav_be_string_set_values sv
               WHERE ess.entity_id = p_base_entity_id
                 AND ess.set_id = s.id
                 AND s.id = sv.set_id
               GROUP BY sv.report_date
              UNION
              SELECT sv.report_date
                FROM eav_be_entity_simple_sets ess,
                     eav_be_sets s,
                     eav_be_date_set_values sv
               WHERE ess.entity_id = p_base_entity_id
                 AND ess.set_id = s.id
                 AND s.id = sv.set_id
               GROUP BY sv.report_date
              UNION
              SELECT sv.report_date
                FROM eav_be_entity_simple_sets ess,
                     eav_be_sets s,
                     eav_be_boolean_set_values sv
               WHERE ess.entity_id = p_base_entity_id
                 AND ess.set_id = s.id
                 AND s.id = sv.set_id
               GROUP BY sv.report_date
              UNION
              SELECT ecs.report_date
                FROM eav_be_entity_complex_sets ecs
               WHERE ecs.entity_id = p_base_entity_id
               GROUP BY ecs.report_date
              UNION
              SELECT sv.report_date
                FROM eav_be_entity_complex_sets ecs,
                     eav_be_sets s,
                     eav_be_complex_set_values sv
               WHERE ecs.entity_id = p_base_entity_id
                 AND ecs.set_id = s.id
                 AND s.id = sv.set_id
               GROUP BY sv.report_date) rd
       GROUP BY rd.report_date;
    COMMIT;
  END;

end PKG_EAV_MODIFICATION;
/
