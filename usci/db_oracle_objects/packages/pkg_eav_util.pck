create or replace package PKG_EAV_UTIL is

  -- Author  : ALEXANDR.MOTOV
  -- Created : 11/18/2013 2:52:06 PM
  -- Purpose : A package of utilities to work with tables USCI project

  separator CONSTANT VARCHAR2(1 CHAR) := '.';

  dt_integer CONSTANT NUMBER := 0;
  dt_date CONSTANT NUMBER := 1;
  dt_string CONSTANT NUMBER := 2;
  dt_boolean CONSTANT NUMBER := 3;
  dt_double CONSTANT NUMBER := 4;

  FUNCTION get_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_data_type      IN NUMBER,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN ANYDATA;

  FUNCTION get_integer_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN NUMBER;

  FUNCTION get_string_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN VARCHAR2;

  FUNCTION get_date_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN DATE;

  FUNCTION get_double_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN NUMBER;

  FUNCTION get_complex_value_id
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN NUMBER;
  
  PROCEDURE fill_credit_as_job
  (
    p_report_date  IN DATE
  );
  
  PROCEDURE fill_credit
  (
    p_report_date  IN DATE
  );

end PKG_EAV_UTIL;
/
create or replace package body PKG_EAV_UTIL is

  FUNCTION get_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_data_type      IN NUMBER,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN ANYDATA
  IS
    v_result ANYDATA := null;
  BEGIN

    IF (INSTR(p_attribute_name, '.') <> 0) THEN
      v_result := null;
    ELSE
      CASE p_data_type
        WHEN dt_integer THEN
          v_result := sys.anydata.convertnumber(get_integer_value(p_entity_id, p_attribute_name, p_report_date));
        WHEN dt_string THEN
          v_result := sys.anydata.convertvarchar2(get_integer_value(p_entity_id, p_attribute_name, p_report_date));

      END CASE;
    END IF;

    RETURN v_result;
  END;

  FUNCTION get_integer_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN NUMBER
  IS
    v_entity_id      NUMBER;
    v_attribute_name VARCHAR2(64);
    v_result         NUMBER;
    v_position       NUMBER;
  BEGIN
    IF (INSTR(p_attribute_name, '.') <> 0) THEN
      v_position := regexp_instr(p_attribute_name, '' || eav_tools.separator || '[^' || eav_tools.separator || ']*$');
      v_attribute_name := substr(p_attribute_name, v_position + 1);

      v_entity_id := get_complex_value_id(p_entity_id, substr(p_attribute_name, 1, v_position - 1), p_report_date);
    ELSE
      v_entity_id := p_entity_id;
      v_attribute_name := p_attribute_name;
    END IF;

    IF (p_report_date IS NULL) THEN
      BEGIN
        SELECT v.value
          INTO v_result
          FROM eav_be_integer_values v,
               eav_m_simple_attributes a
         WHERE v.attribute_id = a.id
           AND a.name = v_attribute_name
           AND v.entity_id = v_entity_id
           AND v.is_last = 1
           AND v.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    ELSE
      BEGIN
        SELECT vn.value
          INTO v_result
          FROM (SELECT rank() over(partition by v.attribute_id order by v.report_date desc) as num_pp,
                       v.attribute_id,
                       v.value,
                       v.is_closed
                  FROM eav_be_integer_values v
                 WHERE v.entity_id = v_entity_id
                   AND v.report_date <= p_report_date) vn,
               eav_m_simple_attributes a
         WHERE vn.attribute_id = a.id
           AND vn.num_pp = 1
           AND a.name = v_attribute_name
           AND vn.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    END IF;

    RETURN v_result;
  END;

  FUNCTION get_string_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN VARCHAR2
  IS
    v_entity_id      NUMBER;
    v_attribute_name VARCHAR2(64);
    v_result         VARCHAR2(1024);
    v_position       NUMBER;
  BEGIN
    IF (INSTR(p_attribute_name, '.') <> 0) THEN
      v_position := regexp_instr(p_attribute_name, '' || eav_tools.separator || '[^' || eav_tools.separator || ']*$');
      v_attribute_name := substr(p_attribute_name, v_position + 1);

      v_entity_id := get_complex_value_id(p_entity_id, substr(p_attribute_name, 1, v_position - 1), p_report_date);
    ELSE
      v_entity_id := p_entity_id;
      v_attribute_name := p_attribute_name;
    END IF;

    IF (p_report_date IS NULL) THEN
      BEGIN
        SELECT v.value
          INTO v_result
          FROM eav_be_string_values v,
               eav_m_simple_attributes a
         WHERE v.attribute_id = a.id
           AND a.name = v_attribute_name
           AND v.entity_id = v_entity_id
           AND v.is_last = 1
           AND v.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    ELSE
      BEGIN
        SELECT vn.value
          INTO v_result
          FROM (SELECT rank() over(partition by v.attribute_id order by v.report_date desc) as num_pp,
                       v.attribute_id,
                       v.value,
                       v.is_closed
                  FROM eav_be_string_values v
                 WHERE v.entity_id = v_entity_id
                   AND v.report_date <= p_report_date) vn,
               eav_m_simple_attributes a
         WHERE vn.attribute_id = a.id
           AND vn.num_pp = 1
           AND a.name = v_attribute_name
           AND vn.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    END IF;

    RETURN v_result;
  END;

  FUNCTION get_date_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN DATE
  IS
    v_entity_id      NUMBER;
    v_attribute_name VARCHAR2(64);
    v_result         DATE;
    v_position       NUMBER;
  BEGIN
    IF (INSTR(p_attribute_name, '.') <> 0) THEN
      v_position := regexp_instr(p_attribute_name, '' || eav_tools.separator || '[^' || eav_tools.separator || ']*$');
      v_attribute_name := substr(p_attribute_name, v_position + 1);

      v_entity_id := get_complex_value_id(p_entity_id, substr(p_attribute_name, 1, v_position - 1), p_report_date);
    ELSE
      v_entity_id := p_entity_id;
      v_attribute_name := p_attribute_name;
    END IF;

    IF (p_report_date IS NULL) THEN
      BEGIN
        SELECT v.value
          INTO v_result
          FROM eav_be_date_values v,
               eav_m_simple_attributes a
         WHERE v.attribute_id = a.id
           AND a.name = v_attribute_name
           AND v.entity_id = v_entity_id
           AND v.is_last = 1
           AND v.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    ELSE
      BEGIN
        SELECT vn.value
          INTO v_result
          FROM (SELECT rank() over(partition by v.attribute_id order by v.report_date desc) as num_pp,
                       v.attribute_id,
                       v.value,
                       v.is_closed
                  FROM eav_be_date_values v
                 WHERE v.entity_id = v_entity_id
                   AND v.report_date <= p_report_date) vn,
               eav_m_simple_attributes a
         WHERE vn.attribute_id = a.id
           AND vn.num_pp = 1
           AND a.name = v_attribute_name
           AND vn.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    END IF;

    RETURN v_result;
  END;

  FUNCTION get_double_value
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN NUMBER
  IS
    v_entity_id      NUMBER;
    v_attribute_name VARCHAR2(64);
    v_result         NUMBER;
    v_position       NUMBER;
  BEGIN
    IF (INSTR(p_attribute_name, '.') <> 0) THEN
      v_position := regexp_instr(p_attribute_name, '' || eav_tools.separator || '[^' || eav_tools.separator || ']*$');
      v_attribute_name := substr(p_attribute_name, v_position + 1);

      v_entity_id := get_complex_value_id(p_entity_id, substr(p_attribute_name, 1, v_position - 1), p_report_date);
    ELSE
      v_entity_id := p_entity_id;
      v_attribute_name := p_attribute_name;
    END IF;

    IF (p_report_date IS NULL) THEN
      BEGIN
        SELECT v.value
          INTO v_result
          FROM eav_be_double_values v,
               eav_m_simple_attributes a
         WHERE v.attribute_id = a.id
           AND a.name = v_attribute_name
           AND v.entity_id = v_entity_id
           AND v.is_last = 1
           AND v.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    ELSE
      BEGIN
        SELECT vn.value
          INTO v_result
          FROM (SELECT rank() over(partition by v.attribute_id order by v.report_date desc) as num_pp,
                       v.attribute_id,
                       v.value,
                       v.is_closed
                  FROM eav_be_double_values v
                 WHERE v.entity_id = v_entity_id
                   AND v.report_date <= p_report_date) vn,
               eav_m_simple_attributes a
         WHERE vn.attribute_id = a.id
           AND vn.num_pp = 1
           AND a.name = v_attribute_name
           AND vn.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_result := null;
      END;
    END IF;

    RETURN v_result;
  END;

  FUNCTION get_complex_value_id
  (
    p_entity_id      IN NUMBER,
    p_attribute_name IN VARCHAR2,
    p_report_date    IN DATE DEFAULT NULL
  ) RETURN NUMBER
  IS
    v_entity_id      NUMBER;
    v_attribute_name VARCHAR2(64);
    v_result         NUMBER;
    v_position       NUMBER;
  BEGIN
    IF (INSTR(p_attribute_name, '.') <> 0) THEN
      v_position := instr(p_attribute_name, eav_tools.separator);
      v_attribute_name := substr(p_attribute_name, 1, v_position - 1);
    ELSE
      v_attribute_name := p_attribute_name;
    END IF;

    IF (p_report_date IS NULL) THEN
      BEGIN
        SELECT v.entity_value_id
          INTO v_entity_id
          FROM eav_be_complex_values v,
               eav_m_complex_attributes a
         WHERE v.attribute_id = a.id
           AND a.name = v_attribute_name
           AND v.entity_id = p_entity_id
           AND v.is_last = 1
           AND v.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_entity_id := null;
      END;
    ELSE
      BEGIN
        SELECT vn.entity_value_id
          INTO v_entity_id
          FROM (SELECT rank() over(partition by v.attribute_id order by v.report_date desc) as num_pp,
                       v.attribute_id,
                       v.entity_value_id,
                       v.is_closed
                  FROM eav_be_complex_values v
                 WHERE v.entity_id = p_entity_id
                   AND v.report_date <= p_report_date) vn,
               eav_m_complex_attributes a
         WHERE vn.attribute_id = a.id
           AND vn.num_pp = 1
           AND a.name = v_attribute_name
           AND vn.is_closed = 0;
      EXCEPTION
        WHEN no_data_found THEN
          v_entity_id := null;
      END;
    END IF;

    IF (v_position IS NOT NULL) THEN
       v_result := get_complex_value_id(v_entity_id, substr(p_attribute_name, v_position + 1), p_report_date);
    ELSE
       v_result := v_entity_id;
    END IF;


    RETURN v_result;
  END;

  PROCEDURE fill_credit_as_job
  (
    p_report_date  IN DATE
  )
  IS
    v_job_name   VARCHAR2(200 CHAR);
    v_job_action VARCHAR2(1000 CHAR);
  BEGIN
    v_job_name := 'FILL_CREDIT_BY_RD_' || to_char(p_report_date, 'yyyyMMdd');
    v_job_action := 'BEGIN ' ||
                      'PKG_EAV_UTIL.FILL_CREDIT(P_REPORT_DATE => TO_DATE(''' || to_char(p_report_date, 'dd.MM.yyyy') || ''', ''dd.MM.yyyy'')); ' ||
                    'END;';
    dbms_scheduler.create_job(job_name        => v_job_name,
                              job_type        => 'PLSQL_BLOCK',
                              job_action      => v_job_action,
                              start_date      => SYSTIMESTAMP,
                              repeat_interval => NULL,
                              enabled         => TRUE,
                              auto_drop       => TRUE);
  END;

  PROCEDURE fill_credit
  (
    p_report_date  IN DATE
  )
  IS
  BEGIN
    execute immediate 'truncate table eav_be_temp_credit';
    for rec_credit in (select e.id
                         from eav_be_entities e,
                              eav_m_classes c
                        where c.id = e.class_id
                          and c.name = 'credit'
                          and exists (select rd.id
                                        from eav_be_entity_report_dates rd
                                       where rd.entity_id = e.id
                                         and rd.report_date = p_report_date))
    loop
      insert into eav_be_temp_credit 
        (select rec_credit.id as credit_id,
                get_string_value(rec_credit.id, 'primary_contract.no', p_report_date) as primary_contract_no,
                get_date_value(rec_credit.id, 'primary_contract.date', p_report_date) as primary_contract_date,
                get_complex_value_id(rec_credit.id, 'creditor', p_report_date) as creditor_id
           from dual);
      commit;
    end loop;
  END;
  
  PROCEDURE fill_creditor
  (
    p_report_date  IN DATE
  )
  IS
    v_creditor_rnn VARCHAR2(100 char);
    v_creditor_bin VARCHAR2(100 char);
    v_creditor_bik VARCHAR2(100 char);
    
    v_doucment_type_rnn_id NUMBER := 10;
    v_doucment_type_bin_id NUMBER := 20;
    v_doucment_type_bik_id NUMBER := 11;
  BEGIN
    /*begin
      select e.id
        into v_doucment_type_rnn_id
        from eav_be_entities e,
             eav_m_classes c
       where c.id = e.class_id
         and c.name = 'ref_doc_type'
         and get_string_value(e.id, 'code', p_report_date) = '11';
    exception 
      when others then
        v_doucment_type_rnn_id := null;
    end;

    begin
      select e.id
        into v_doucment_type_bin_id
        from eav_be_entities e,
             eav_m_classes c
       where c.id = e.class_id
         and c.name = 'ref_doc_type'
         and get_string_value(e.id, 'code', p_report_date) = '07';
    exception 
      when others then
        v_doucment_type_bin_id := null;
    end;
       
    begin
      select e.id
        into v_doucment_type_bik_id
        from eav_be_entities e,
             eav_m_classes c
       where c.id = e.class_id
         and c.name = 'ref_doc_type'
         and get_string_value(e.id, 'code', p_report_date) = '15';
    exception
      when others then
        v_doucment_type_bik_id := null;
    end;*/

    execute immediate 'truncate table eav_be_temp_creditor';
    for rec_creditor in (select e.id
                           from eav_be_entities e,
                                eav_m_classes c
                          where c.id = e.class_id
                            and c.name = 'ref_creditor')
    loop
      begin
        select get_string_value(csvn.entity_value_id, 'no', p_report_date)
          into v_creditor_rnn
          from (select rank() over(order by csv.report_date desc) as num_pp,
                       csv.entity_value_id,
                       csv.is_closed
                  from (select tn.attribute_id,
                               tn.set_id
                          from (select rank() over(partition by t.attribute_id order by t.report_date desc) as num_pp,
                                       t.attribute_id,
                                       t.set_id,
                                       t.is_closed
                                  from eav_be_entity_complex_sets t
                                 where t.entity_id = rec_creditor.id
                                   and t.report_date <= p_report_date) tn
                        where tn.num_pp = 1
                          and tn.is_closed = 0) ecs, 
                       eav_be_complex_set_values csv,
                       eav_m_complex_set cs
                 where ecs.attribute_id = cs.id
                   and cs.name = 'docs'
                   and ecs.set_id = csv.set_id
                   and csv.report_date <= p_report_date) csvn
         where csvn.num_pp = 1
           and csvn.is_closed = 0
           and get_complex_value_id(csvn.entity_value_id, 'doc_type') = v_doucment_type_rnn_id;
      exception
        when others then
          v_creditor_rnn := null;
      end;
      
      begin
        select get_string_value(csvn.entity_value_id, 'no', p_report_date)
          into v_creditor_bin
          from (select rank() over(order by csv.report_date desc) as num_pp,
                       csv.entity_value_id,
                       csv.is_closed
                  from (select tn.attribute_id,
                               tn.set_id
                          from (select rank() over(partition by t.attribute_id order by t.report_date desc) as num_pp,
                                       t.attribute_id,
                                       t.set_id,
                                       t.is_closed
                                  from eav_be_entity_complex_sets t
                                 where t.entity_id = rec_creditor.id
                                   and t.report_date <= p_report_date) tn
                        where tn.num_pp = 1
                          and tn.is_closed = 0) ecs, 
                       eav_be_complex_set_values csv,
                       eav_m_complex_set cs
                 where ecs.attribute_id = cs.id
                   and cs.name = 'docs'
                   and ecs.set_id = csv.set_id
                   and csv.report_date <= p_report_date) csvn
         where csvn.num_pp = 1
           and csvn.is_closed = 0
           and get_complex_value_id(csvn.entity_value_id, 'doc_type') = v_doucment_type_bin_id;
      exception
        when others then
          v_creditor_bin := null;
      end;
      
      begin
        select get_string_value(csvn.entity_value_id, 'no', p_report_date)
          into v_creditor_bik
          from (select rank() over(order by csv.report_date desc) as num_pp,
                       csv.entity_value_id,
                       csv.is_closed
                  from (select tn.attribute_id,
                               tn.set_id
                          from (select rank() over(partition by t.attribute_id order by t.report_date desc) as num_pp,
                                       t.attribute_id,
                                       t.set_id,
                                       t.is_closed
                                  from eav_be_entity_complex_sets t
                                 where t.entity_id = rec_creditor.id
                                   and t.report_date <= p_report_date) tn
                        where tn.num_pp = 1
                          and tn.is_closed = 0) ecs, 
                       eav_be_complex_set_values csv,
                       eav_m_complex_set cs
                 where ecs.attribute_id = cs.id
                   and cs.name = 'docs'
                   and ecs.set_id = csv.set_id
                   and csv.report_date <= p_report_date) csvn
         where csvn.num_pp = 1
           and csvn.is_closed = 0
           and get_complex_value_id(csvn.entity_value_id, 'doc_type') = v_doucment_type_bik_id;
      exception
        when others then
          v_creditor_bik := null;
      end;
    
      insert into eav_be_temp_creditor 
        (select rec_creditor.id as creditor_id,
                get_string_value(rec_creditor.id, 'name', p_report_date) as name,
                v_creditor_bin as bin,
                v_creditor_rnn as rnn,
                v_creditor_bik as bik
           from dual);
      commit;
    end loop;
  END;

end PKG_EAV_UTIL;
/
