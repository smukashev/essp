declare
  v_job_action VARCHAR2(1000 CHAR);
begin
  v_job_action := 'BEGIN ' ||
                  'PKG_NOTIFICATION.SPEED_TEST; ' ||
                  'END;';

  dbms_scheduler.create_job(job_name  => 'ES_SPEED_TEST',
                            job_type  => 'PLSQL_BLOCK',
                            job_action => v_job_action,
                            start_date => systimestamp,
                            repeat_interval => 'freq=hourly',
                            enabled => true,
                            auto_drop => true);
end;