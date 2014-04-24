create or replace package PKG_EAV_XML_UTIL is

  c_number_format constant varchar2(50 char) := '99999999999999999999D99';
  c_date_format constant varchar2(50 char) := 'dd.MM.yyyy';
  c_nls_numeric_characters constant varchar2(50 char) := 'NLS_NUMERIC_CHARACTERS = ''.,''';
  c_job_check_timeout constant number := 1; -- IN SECOND
  c_job_wait_timeout constant number := 36000; -- IN SECOND
  c_default_batch_size constant number := 10000;
  c_default_job_max_count constant number := 20;
  
  c_drt_debt_current constant number := 55;
  c_drt_debt_pastdue constant number := 56;
  c_drt_debt_write_off constant number := 57;
  c_drt_interest_current constant number := 58;
  c_drt_interest_pastdue constant number := 59;
  c_drt_interest_write_off constant number := 60;
  c_drt_discount constant number := 61;
  c_drt_correction constant number := 62;
  c_drt_discounted_value constant number := 63;
  c_drt_limit constant number := 102;
  c_drt_provision_kfn constant number := 103;
  c_drt_provision_msfo constant number := 104;
  c_drt_provision_msfo_ob constant number := 129;
  
  c_tt_issue_debt constant number := 18;
  c_tt_issue_interest constant number := 19;
  
  PROCEDURE remove_by_rd
  (
    p_report_date  IN DATE
  );

  PROCEDURE generate_by_rd_as_job
  (
    p_report_date  IN DATE,
    p_batch_size IN NUMBER DEFAULT c_default_batch_size,
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count,
    p_version IN NUMBER DEFAULT 2
  );

  PROCEDURE generate_by_rd
  (
    p_report_date  IN DATE,
    p_batch_size IN NUMBER DEFAULT c_default_batch_size,
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count,
    p_version IN NUMBER DEFAULT 2
  );

  PROCEDURE generate_by_cid_rd_as_job
  (
    p_creditor_id  IN NUMBER,
    p_report_date  IN DATE,
    p_batch_size IN NUMBER DEFAULT c_default_batch_size,
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count,
    p_version IN NUMBER DEFAULT 2
  );

  PROCEDURE generate_by_cid_rd
  (
    p_creditor_id  IN NUMBER,
    p_report_date  IN DATE,
    p_batch_size IN NUMBER DEFAULT c_default_batch_size,
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count,
    p_version IN NUMBER DEFAULT 2
  );

  PROCEDURE generate_xml_v1
  (
    p_xml_file_id IN NUMBER
  );
  
  PROCEDURE generate_xml_v2
  (
    p_xml_file_id IN NUMBER
  );

  FUNCTION get_ref_balance_account_xml
  (
    p_balance_account_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'balance_account'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_credit_object_xml
  (
    p_credit_object_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'credit_object'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_credit_type_xml
  (
    p_credit_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'credit_type'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_credit_purpose_xml
  (
    p_credit_purpose_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'credit_purpose'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_currency_xml
  (
    p_currency_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'currency'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_portfolio_xml
  (
    p_portfolio_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'portfolio'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_finance_source_xml
  (
    p_finance_source_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'finance_source'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_doc_type_xml
  (
    p_doc_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'doc_type'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_classification_xml
  (
    p_classification_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'classification'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_country_xml
  (
    p_country_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'country'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_legal_form_xml
  (
    p_legal_form_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'legal_form'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_offshore_xml
  (
    p_offshore_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'offshore'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_econ_trade_xml
  (
    p_econ_trade_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'econ_trade'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_enterprise_type_xml
  (
    p_enterprise_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'enterprise_type'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_contact_type_xml
  (
    p_contact_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'contact_type'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_region_xml
  (
    p_region_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'region'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_bank_relation_xml
  (
    p_bank_relation_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'bank_relation'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_pledge_type_xml
  (
    p_pledge_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'pledge_type'
  ) RETURN XMLTYPE;
  
  FUNCTION get_ref_creditor_xml
  (
    p_creditor_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'creditor'
  ) RETURN XMLTYPE;
  
  FUNCTION get_debt_remains_xml
  (
    p_credit_id IN NUMBER,
    p_debt_remains_type_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN XMLTYPE;
  
  FUNCTION get_turnover_xml
  (
    p_credit_id IN NUMBER,
    p_turnover_type_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN XMLTYPE;
  
  FUNCTION get_credit_flow_xml
  (
    p_credit_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN XMLTYPE;
  
  FUNCTION get_organization_xml
  (
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'organization'
  ) RETURN XMLTYPE;
  
  FUNCTION get_person_xml
  (
    p_person_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'person',
    p_type IN NUMBER DEFAULT 0 -- 0 - PERSON, 1 - HEAD
  ) RETURN XMLTYPE;
  
  FUNCTION get_documents_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'docs'
  ) RETURN XMLTYPE;
  
  FUNCTION get_contacts_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'contacts'
  ) RETURN XMLTYPE;
  
  FUNCTION get_person_names_xml
  (
    p_person_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'names'
  ) RETURN XMLTYPE;
  
  FUNCTION get_organization_names_xml
  (
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'names'
  ) RETURN XMLTYPE;
  
  FUNCTION get_addresses_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'addresses'
  ) RETURN XMLTYPE;
  
  FUNCTION get_bank_relations_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'bank_relations'
  ) RETURN XMLTYPE;
  
  FUNCTION get_pledges_xml
  (
    p_credit_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'pledges'
  ) RETURN XMLTYPE;

end PKG_EAV_XML_UTIL;
/
CREATE OR REPLACE PACKAGE BODY PKG_EAV_XML_UTIL IS

  PROCEDURE remove_by_rd
  (
    p_report_date  IN DATE
  )
  IS
  BEGIN
    DELETE FROM core.xml_credit_id xci WHERE xci.xml_file_id IN (SELECT xf.id FROM core.xml_file xf WHERE xf.report_date = p_report_date);
    DELETE FROM core.xml_file xf WHERE xf.report_date = p_report_date;
    COMMIT;
  END;

  core_
    FOR rec_creditor IN (SELECT r.creditor_id as id
                           FROM core.report r
                          where r.report_date = p_report_date
                            AND r.status_id = 92)
    LOOP
      SELECT to_number(xc.value)
        INTO v_status
        FROM XML_CONFIG xc
       WHERE xc.code = 'XML_GENERATION_STATUS';
        
      IF (v_status = 0) THEN
        RETURN;
      END IF;  
    
      generate_by_cid_rd(p_creditor_id => rec_creditor.id,
                         p_report_date => p_report_date,
                         p_batch_size => p_batch_size,
                         p_job_max_count => p_job_max_count,
                         p_version => p_version);
    END LOOP;
  END;

  PROCEDURE generate_by_cid_rd_as_job
  (
    p_creditor_id  IN NUMBER,
    p_report_date  IN DATE,
    p_batch_size IN NUMBER DEFAULT c_default_batch_size,
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count,
    p_version IN NUMBER DEFAULT 2
  )
  IS
    v_job_name   VARCHAR2(200 CHAR);
    v_job_action VARCHAR2(1000 CHAR);
  BEGIN
    v_job_name := 'XML_GEN_BY_CID_' || p_creditor_id || '_RD_' || to_char(p_report_date, 'yyyyMMdd');
    v_job_action := 'BEGIN ' ||
                      'PKG_EAV_XML_UTIL.GENERATE_BY_CID_RD(P_CREDITOR_ID => ' || p_creditor_id || ', ' ||
                                                          'P_REPORT_DATE => TO_DATE(''' || to_char(p_report_date, 'dd.MM.yyyy') || ''', ''dd.MM.yyyy''), ' ||
                                                          'P_BATCH_SIZE => ' || p_batch_size || ', ' ||
                                                          'P_JOB_MAX_COUNT => ' || p_job_max_count || ', ' ||
                                                          'P_VERSION => ' || p_version || '); ' ||
                    'END;';
    dbms_scheduler.create_job(job_name        => v_job_name,
                              job_type        => 'PLSQL_BLOCK',
                              job_action      => v_job_action,
                              start_date      => SYSTIMESTAMP,
                              repeat_interval => NULL,
                              enabled         => TRUE,
                              auto_drop       => TRUE);
  END;

  PROCEDURE generate_by_cid_rd
  (
    p_creditor_id  IN NUMBER,
    p_report_date  IN DATE,
    p_batch_size IN NUMBER DEFAULT c_default_batch_size,
    p_job_max_count IN NUMBER DEFAULT c_default_job_max_count,
    p_version IN NUMBER DEFAULT 2
  )
  IS
    v_previous_report_date DATE;
    v_credit_ids           core.t_number_table := core.t_number_table();
    v_job_name             VARCHAR2(200 CHAR);

    v_xml_file_id          NUMBER;
    v_wait_begin_date      DATE;
    v_wait_end_date        DATE;
    v_now                  DATE;
    v_job_count            NUMBER;
    v_status               NUMBER(1);
  BEGIN
    SELECT add_months(p_report_date, -st.report_period_duration_months)
      INTO v_previous_report_date
      FROM ref.v_creditor_his vch,
           ref.subject_type st
     WHERE vch.id = p_creditor_id
       AND vch.subject_type_id = st.id
       AND vch.open_date <= p_report_date
       AND (vch.close_date > p_report_date OR vch.close_date IS NULL);

    UPDATE core.xml_file xf
       SET xf.status = 'RESTARTING',
           xf.file_content = null,
           xf.begin_date = null,
           xf.end_date = null
     WHERE xf.creditor_id = p_creditor_id
       AND xf.report_date = p_report_date
       AND (xf.status <> 'COMPLETED' OR xf.status IS NULL);
    COMMIT;

    FOR rec_xml_file IN (SELECT xf.*
                           FROM core.xml_file xf
                          WHERE xf.creditor_id = p_creditor_id
                            AND xf.report_date = p_report_date
                            AND xf.status = 'RESTARTING')
    LOOP

      v_wait_begin_date := sysdate;
      v_wait_end_date := v_wait_begin_date + (c_job_wait_timeout * (1/86400));

      LOOP
        SELECT count(*)
          INTO v_job_count
          FROM core.xml_file xf
         WHERE /*xf.creditor_id = p_creditor_id
           AND*/ xf.report_date = p_report_date
           AND (xf.status IN ('RUNNING', 'STARTING'));

        IF (v_job_count < p_job_max_count) THEN
          UPDATE core.xml_file xf
             SET xf.status = 'STARTING',
                 xf.begin_date = sysdate
           WHERE xf.id = rec_xml_file.id;
          COMMIT;

          v_job_name := 'XML_GEN_' || dbms_random.string('X', 10);
          dbms_scheduler.create_job(job_name        => v_job_name,
                                    job_type        => 'PLSQL_BLOCK',
                                    job_action      => 'BEGIN
                                                           PKG_EAV_XML_UTIL.GENERATE_XML_V' || p_version || '(' || 'P_XML_FILE_ID => ' || rec_xml_file.id || ');
                                                        END;',
                                    start_date      => SYSTIMESTAMP,
                                    repeat_interval => NULL,
                                    enabled         => TRUE,
                                    auto_drop       => TRUE);
          EXIT;
        ELSE
          IF (v_wait_end_date < sysdate) THEN
            UPDATE core.xml_file xf
               SET xf.status = 'TIMEOUT_EXPIRED',
                   xf.begin_date = sysdate
             WHERE xf.id = rec_xml_file.id;
            COMMIT;

            RETURN;
          END IF;

          v_now := sysdate;
          LOOP
            EXIT WHEN v_now + (c_job_check_timeout * (1/86400)) = sysdate;
          END LOOP;
        END IF;
      END LOOP;

    END LOOP;

    LOOP
      IF (v_credit_ids.count <> 0) THEN
        v_credit_ids.delete;
      END IF;
      
      FOR rec_credit IN (SELECT c.id
                           FROM (SELECT vch.id,
                                        rank() over(order by vch.primary_contract_no, vch.primary_contract_date) AS num_pp
                                   FROM v_credit_his vch
                                  WHERE vch.creditor_id = p_creditor_id
                                    AND vch.primary_contract_date < p_report_date
                                    AND (vch.maturity_date >= v_previous_report_date OR vch.maturity_date IS NULL)
                                    AND vch.open_date <= p_report_date
                                    AND (vch.close_date > p_report_date OR vch.close_date IS NULL)
                                    AND vch.id NOT IN (SELECT xci.credit_id
                                                         FROM core.xml_credit_id xci,
                                                              core.xml_file xf
                                                        WHERE xci.xml_file_id = xf.id
                                                          AND xf.creditor_id = p_creditor_id
                                                          AND xf.report_date = p_report_date)) c
                          WHERE c.num_pp <= p_batch_size
                          ORDER BY c.num_pp)
      LOOP
        v_credit_ids.extend();
        v_credit_ids(v_credit_ids.last) := rec_credit.id;
      END LOOP;

      IF (v_credit_ids.count = 0) THEN
        EXIT;
      ELSE

        v_wait_begin_date := sysdate;
        v_wait_end_date := v_wait_begin_date + (c_job_wait_timeout * (1/86400));

        LOOP
          SELECT to_number(xc.value)
            INTO v_status
            FROM XML_CONFIG xc
           WHERE xc.code = 'XML_GENERATION_STATUS';
        
          IF (v_status = 0) THEN
            RETURN;
          END IF;
        
          SELECT count(*)
            INTO v_job_count
            FROM core.xml_file xf
           WHERE /*xf.creditor_id = p_creditor_id
             AND*/ xf.report_date = p_report_date
             AND (xf.status IN ('RUNNING', 'STARTING'));

          IF (v_job_count < p_job_max_count) THEN
            EXIT;
          ELSE
            IF (v_wait_end_date < sysdate) THEN
              SELECT seq_xml_file.nextval
                INTO v_xml_file_id
                FROM dual;

              INSERT INTO xml_file (id, creditor_id, report_date, begin_date, end_date, file_name, status, sent)
                VALUES (v_xml_file_id, p_creditor_id, p_report_date, sysdate, sysdate, 'XML_DATA_BY_CID_' || p_creditor_id || '_RD_' || to_char(p_report_date, 'yyyyMMdd') || '_' || ltrim(to_char(v_xml_file_id, '00000')), 'TIMEOUT_EXPIRED', 0);

              INSERT INTO xml_credit_id
                (SELECT seq_xml_credit_id.nextval,
                        v_xml_file_id,
                        i.column_value
                   FROM TABLE(CAST(v_credit_ids AS t_number_table)) i);

              COMMIT;

              RETURN;
            END IF;

            v_now := sysdate;
            LOOP
              EXIT WHEN v_now + (c_job_check_timeout * (1/86400)) = sysdate;
            END LOOP;
            
            
          END IF;
        END LOOP;

        SELECT seq_xml_file.nextval
          INTO v_xml_file_id
          FROM dual;

        INSERT INTO xml_file (id, creditor_id, report_date, begin_date, file_name, status, sent)
          VALUES (v_xml_file_id, p_creditor_id, p_report_date, sysdate, 'XML_DATA_BY_CID_' || p_creditor_id || '_RD_' || to_char(p_report_date, 'yyyyMMdd') || '_' || ltrim(to_char(v_xml_file_id, '00000')), 'STARTING', 0);

        INSERT INTO xml_credit_id
          (SELECT seq_xml_credit_id.nextval,
                  v_xml_file_id,
                  i.column_value
             FROM TABLE(CAST(v_credit_ids AS t_number_table)) i);

        COMMIT;

        v_job_name := 'XML_GEN_' || dbms_random.string('X', 10);
        dbms_scheduler.create_job(job_name        => v_job_name,
                                  job_type        => 'PLSQL_BLOCK',
                                  job_action      => 'BEGIN
                                                         PKG_EAV_XML_UTIL.GENERATE_XML_V' || p_version || '(' || 'P_XML_FILE_ID => ' || v_xml_file_id || ');
                                                      END;',
                                  start_date      => SYSTIMESTAMP,
                                  repeat_interval => NULL,
                                  enabled         => TRUE,
                                  auto_drop       => TRUE);
      END IF;
    END LOOP;

  END;

  PROCEDURE generate_xml_v1
  (
    p_xml_file_id IN NUMBER
  )
  IS
    v_creditor_id  NUMBER;
    v_report_date  DATE;
    v_data_xml     XMLTYPE;
    v_manifest_xml XMLTYPE;
    v_zip          BLOB;
    v_zip_comment  VARCHAR2(4000 CHAR);
  BEGIN

    SELECT xf.creditor_id, xf.report_date
      INTO v_creditor_id, v_report_date
      FROM core.xml_file xf
     WHERE xf.id = p_xml_file_id;

    UPDATE core.xml_file xf
       SET xf.status = 'RUNNING'
     WHERE xf.id = p_xml_file_id;
    COMMIT;

    SELECT xmlroot(xmlelement("entities",
             xmlagg(
               xmlelement("entity",
                 xmlattributes('credit' AS "class"),
                 -- ACTUAL_ISSUE_DATE
                   decode(vch.actual_issue_date, null, null, xmlelement("actual_issue_date", to_char(vch.actual_issue_date, c_date_format))),
                   -- AMOUNT
                   decode(vch.amount, null, null, xmlelement("amount", vch.amount)),
                   -- CHANGE
                 (SELECT xmlelement("change",
                           -- REMAINS
                           (SELECT xmlelement("remains",
                                     -- CORRECTION
                                     (SELECT xmlelement("correction",
                                               -- BALANCE_ACCOUNT
                                               decode(dr.account_id, null, null, xmlelement("balance_account",
                                                 -- NO_
                                                 xmlelement("no_", ref_ba.no_)
                                               )),
                                               -- VALUE
                                               xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                               -- VALUE_CURRENCY
                                               decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))))
                                             )
                                        FROM core.debt_remains dr,
                                             (SELECT t.parent_id AS id,
                                                     t.no_
                                                FROM ref.balance_account t
                                               WHERE t.open_date <= v_report_date
                                                 AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                       WHERE dr.credit_id = vch.id
                                         AND dr.type_id = 62
                                         AND dr.rep_date = v_report_date
                                         AND dr.value IS NOT NULL
                                         AND dr.value <> 0
                                         AND dr.account_id = ref_ba.id (+)),
                                     -- DEBT
                                     (SELECT xmlelement("debt",
                                               -- CURRENT
                                               (SELECT xmlelement("current",
                                                         -- BALANCE_ACCOUNT
                                                         decode(dr.account_id, null, null, xmlelement("balance_account",
                                                           -- NO_
                                                           xmlelement("no_", ref_ba.no_)
                                                         )),
                                                         -- VALUE
                                                         xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                                         -- VALUE_CURRENCY
                                                         decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))))
                                                       )
                                                  FROM core.debt_remains dr,
                                                       (SELECT t.parent_id AS id,
                                                               t.no_
                                                          FROM ref.balance_account t
                                                         WHERE t.open_date <= v_report_date
                                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                 WHERE dr.credit_id = vch.id
                                                   AND dr.type_id = 55
                                                   AND dr.rep_date = v_report_date
                                                   AND dr.value IS NOT NULL
                                                   AND dr.value <> 0
                                                   AND dr.account_id = ref_ba.id (+)),
                                               -- PASTDUE
                                               (SELECT xmlelement("pastdue",
                                                         -- BALANCE_ACCOUNT
                                                         decode(dr.account_id, null, null, xmlelement("balance_account",
                                                           -- NO_
                                                           xmlelement("no_", ref_ba.no_)
                                                         )),
                                                         -- VALUE
                                                         xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                                         -- VALUE_CURRENCY
                                                         decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))),
                                                         -- OPEN_DATE
                                                         xmlelement("open_date", to_char(dr.pastdue_open_date, c_date_format)),
                                                         -- CLOSE_DATE
                                                         xmlelement("close_date", to_char(dr.pastdue_close_date, c_date_format))
                                                       )
                                                  FROM core.debt_remains dr,
                                                       (SELECT t.parent_id AS id,
                                                               t.no_
                                                          FROM ref.balance_account t
                                                         WHERE t.open_date <= v_report_date
                                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                 WHERE dr.credit_id = vch.id
                                                   AND dr.type_id = 56
                                                   AND dr.rep_date = v_report_date
                                                   AND dr.value IS NOT NULL
                                                   AND dr.value <> 0
                                                   AND dr.account_id = ref_ba.id (+)),
                                               -- WRITE_OFF
                                               (SELECT xmlelement("write_off",
                                                         -- BALANCE_ACCOUNT
                                                         decode(dr.account_id, null, null, xmlelement("balance_account",
                                                           -- NO_
                                                           xmlelement("no_", ref_ba.no_)
                                                         )),
                                                         -- VALUE
                                                         xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                                         -- VALUE_CURRENCY
                                                         decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))),
                                                         -- DATE
                                                         xmlelement("date", to_char(dr.write_off_date, c_date_format))
                                                       )
                                                  FROM core.debt_remains dr,
                                                       (SELECT t.parent_id AS id,
                                                               t.no_
                                                          FROM ref.balance_account t
                                                         WHERE t.open_date <= v_report_date
                                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                 WHERE dr.credit_id = vch.id
                                                   AND dr.type_id = 57
                                                   AND dr.rep_date = v_report_date
                                                   AND dr.value IS NOT NULL
                                                   AND dr.value <> 0
                                                   AND dr.account_id = ref_ba.id (+))
                                             )
                                        FROM dual
                                       WHERE EXISTS (SELECT t.* FROM core.debt_remains t WHERE t.credit_id = vch.id AND t.type_id in (55, 56, 57) AND t.rep_date = v_report_date AND t.value IS NOT NULL AND t.value <> 0)),
                                     -- DISCOUNT
                                     (SELECT xmlelement("discount",
                                               -- BALANCE_ACCOUNT
                                               decode(dr.account_id, null, null, xmlelement("balance_account",
                                                 -- NO_
                                                 xmlelement("no_", ref_ba.no_)
                                               )),
                                               -- VALUE
                                               xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                               -- VALUE_CURRENCY
                                               decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))))
                                             )
                                        FROM core.debt_remains dr,
                                             (SELECT t.parent_id AS id,
                                                     t.no_
                                                FROM ref.balance_account t
                                               WHERE t.open_date <= v_report_date
                                                 AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                       WHERE dr.credit_id = vch.id
                                         AND dr.type_id = 61
                                         AND dr.rep_date = v_report_date
                                         AND dr.value IS NOT NULL
                                         AND dr.value <> 0
                                         AND dr.account_id = ref_ba.id (+)),
                                     -- DISCOUNTED_VALUE
                                     (SELECT xmlelement("discounted_value", 
                                               xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                                             )
                                        FROM core.debt_remains dr,
                                             (SELECT t.parent_id AS id,
                                                     t.no_
                                                FROM ref.balance_account t
                                               WHERE t.open_date <= v_report_date
                                                 AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                       WHERE dr.credit_id = vch.id
                                         AND dr.type_id = 63
                                         AND dr.rep_date = v_report_date
                                         AND dr.value IS NOT NULL
                                         AND dr.value <> 0
                                         AND dr.account_id = ref_ba.id (+)),
                                     -- INTEREST
                                     (SELECT xmlelement("interest",
                                               -- CURRENT
                                               (SELECT xmlelement("current",
                                                         -- BALANCE_ACCOUNT
                                                         decode(dr.account_id, null, null, xmlelement("balance_account",
                                                           -- NO_
                                                           xmlelement("no_", ref_ba.no_)
                                                         )),
                                                         -- VALUE
                                                         xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                                         -- VALUE_CURRENCY
                                                         decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))))
                                                       )
                                                  FROM core.debt_remains dr,
                                                       (SELECT t.parent_id AS id,
                                                               t.no_
                                                          FROM ref.balance_account t
                                                         WHERE t.open_date <= v_report_date
                                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                 WHERE dr.credit_id = vch.id
                                                   AND dr.type_id = 58
                                                   AND dr.rep_date = v_report_date
                                                   AND dr.value IS NOT NULL
                                                   AND dr.value <> 0
                                                   AND dr.account_id = ref_ba.id (+)),
                                               -- PASTDUE
                                               (SELECT xmlelement("pastdue",
                                                         -- BALANCE_ACCOUNT
                                                         decode(dr.account_id, null, null, xmlelement("balance_account",
                                                           -- NO_
                                                           xmlelement("no_", ref_ba.no_)
                                                         )),
                                                         -- VALUE
                                                         xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                                         -- VALUE_CURRENCY
                                                         decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))),
                                                         -- OPEN_DATE
                                                         xmlelement("open_date", to_char(dr.pastdue_open_date, c_date_format)),
                                                         -- CLOSE_DATE
                                                         xmlelement("close_date", to_char(dr.pastdue_close_date, c_date_format))
                                                       )
                                                  FROM core.debt_remains dr,
                                                       (SELECT t.parent_id AS id,
                                                               t.no_
                                                          FROM ref.balance_account t
                                                         WHERE t.open_date <= v_report_date
                                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                 WHERE dr.credit_id = vch.id
                                                   AND dr.type_id = 59
                                                   AND dr.rep_date = v_report_date
                                                   AND dr.value IS NOT NULL
                                                   AND dr.value <> 0
                                                   AND dr.account_id = ref_ba.id (+)),
                                               -- WRITE_OFF
                                               (SELECT xmlelement("write_off",
                                                         -- VALUE
                                                         xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                                         -- VALUE_CURRENCY
                                                         decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))),
                                                         -- DATE
                                                         xmlelement("date", to_char(dr.write_off_date, c_date_format))
                                                       )
                                                  FROM core.debt_remains dr
                                                 WHERE dr.credit_id = vch.id
                                                   AND dr.type_id = 60
                                                   AND dr.rep_date = v_report_date
                                                   AND dr.value IS NOT NULL
                                                   AND dr.value <> 0)
                                             )
                                        FROM dual
                                       WHERE EXISTS (SELECT t.* FROM core.debt_remains t WHERE t.credit_id = vch.id AND t.type_id in (58, 59, 60) AND t.rep_date = v_report_date AND t.value IS NOT NULL AND t.value <> 0)),
                                     -- LIMIT
                                     (SELECT xmlelement("limit",
                                               -- BALANCE_ACCOUNT
                                               decode(dr.account_id, null, null, xmlelement("balance_account",
                                                 -- NO_
                                                 xmlelement("no_", ref_ba.no_)
                                               )),
                                               -- VALUE
                                               xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                                               -- VALUE_CURRENCY
                                               decode(dr.value_currency, null, null, xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))))
                                             )
                                        FROM core.debt_remains dr,
                                             (SELECT t.parent_id AS id,
                                                     t.no_
                                                FROM ref.balance_account t
                                               WHERE t.open_date <= v_report_date
                                                 AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                       WHERE dr.credit_id = vch.id
                                         AND dr.type_id = 102
                                         AND dr.rep_date = v_report_date
                                         AND dr.value IS NOT NULL
                                         AND dr.value <> 0
                                         AND dr.account_id = ref_ba.id (+))
                                   )
                              FROM dual
                             WHERE EXISTS (SELECT t.* FROM core.debt_remains t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.value IS NOT NULL AND t.value <> 0 AND t.type_id in (55, 56, 57, 58, 59, 60, 61, 62, 63, 102))),
                           -- CREDIT_FLOW
                           (SELECT xmlelement("credit_flow",
                                     (SELECT xmlelement("classification",
                                               -- CODE
                                               xmlelement("code", ref_cs.code)
                                             )
                                        FROM core.credit_flow cf,
                                             (SELECT t.parent_id AS id,
                                                     t.code
                                                FROM ref.classification t
                                               WHERE t.open_date <= v_report_date
                                                 AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_cs
                                       WHERE cf.credit_id = vch.id
                                         AND cf.rep_date = v_report_date
                                         AND cf.classification_id IS NOT NULL
                                         AND cf.classification_id = ref_cs.id),
                                      -- PROVISION
                                      (SELECT xmlelement("provision",
                                                -- PROVISION_KFN
                                                (SELECT xmlelement("provision_kfn",
                                                          -- BALANCE_ACCOUNT
                                                          decode(dr.account_id, null, null, xmlelement("balance_account",
                                                            -- NO_
                                                            xmlelement("no_", ref_ba.no_)
                                                          )),
                                                          -- VALUE
                                                          xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                                                        )
                                                   FROM core.debt_remains dr,
                                                        (SELECT t.parent_id AS id,
                                                                t.no_
                                                           FROM ref.balance_account t
                                                          WHERE t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                  WHERE dr.credit_id = vch.id
                                                    AND dr.type_id = 103
                                                    AND dr.rep_date = v_report_date
                                                    AND dr.value IS NOT NULL
                                                    AND dr.value <> 0
                                                    AND dr.account_id = ref_ba.id (+)),
                                                -- PROVISION_MSFO
                                                (SELECT xmlelement("provision_msfo",
                                                          -- BALANCE_ACCOUNT
                                                          decode(dr.account_id, null, null, xmlelement("balance_account",
                                                            -- NO_
                                                            xmlelement("no_", ref_ba.no_)
                                                          )),
                                                          -- VALUE
                                                          xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                                                        )
                                                   FROM core.debt_remains dr,
                                                        (SELECT t.parent_id AS id,
                                                                t.no_
                                                           FROM ref.balance_account t
                                                          WHERE t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                  WHERE dr.credit_id = vch.id
                                                    AND dr.type_id = 104
                                                    AND dr.rep_date = v_report_date
                                                    AND dr.value IS NOT NULL
                                                    AND dr.value <> 0
                                                    AND dr.account_id = ref_ba.id (+)),
                                                -- PROVISION_MSFO_OVER_BALANCE
                                                (SELECT xmlelement("provision_msfo_over_balance",
                                                          -- BALANCE_ACCOUNT
                                                          decode(dr.account_id, null, null, xmlelement("balance_account",
                                                            -- NO_
                                                            xmlelement("no_", ref_ba.no_)
                                                          )),
                                                          -- VALUE
                                                          xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                                                        )
                                                   FROM core.debt_remains dr,
                                                        (SELECT t.parent_id AS id,
                                                                t.no_
                                                           FROM ref.balance_account t
                                                          WHERE t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ba
                                                  WHERE dr.credit_id = vch.id
                                                    AND dr.type_id = 129
                                                    AND dr.rep_date = v_report_date
                                                    AND dr.value IS NOT NULL
                                                    AND dr.value <> 0
                                                    AND dr.account_id = ref_ba.id (+))
                                              )
                                         FROM dual
                                        WHERE EXISTS (SELECT t.* FROM core.debt_remains t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.value IS NOT NULL AND t.value <> 0 AND t.type_id in (103, 104, 129)))
                                   )
                              FROM dual
                             WHERE EXISTS (SELECT t.* FROM core.debt_remains t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.value IS NOT NULL AND t.value <> 0 AND t.type_id in (103, 104, 129))
                                OR EXISTS (SELECT t.* FROM core.credit_flow t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.classification_id IS NOT NULL)),
                         -- TURNOVER
                         (SELECT xmlelement("turnover",
                                   -- ISSUE
                                   xmlelement("issue",
                                     -- DEBT
                                     (SELECT xmlelement("debt",
                                               -- AMOUNT
                                               xmlelement("amount", ltrim(to_char(t.amount, c_number_format, c_nls_numeric_characters))),
                                               -- AMOUNT_CURRENCY
                                               decode(t.amount_currency, null, null, xmlelement("amount_currency", ltrim(to_char(t.amount_currency, c_number_format, c_nls_numeric_characters))))
                                             )
                                        FROM core.turnover t
                                       WHERE t.credit_id = vch.id
                                         AND t.type_id = 18
                                         AND t.rep_date = v_report_date
                                         AND t.amount IS NOT NULL
                                         AND t.amount <> 0),
                                     -- INTEREST
                                     (SELECT xmlelement("interest",
                                               -- AMOUNT
                                               xmlelement("amount", ltrim(to_char(t.amount, c_number_format, c_nls_numeric_characters))),
                                               -- AMOUNT_CURRENCY
                                               decode(t.amount_currency, null, null, xmlelement("amount_currency", ltrim(to_char(t.amount_currency, c_number_format, c_nls_numeric_characters))))
                                             )
                                        FROM core.turnover t
                                       WHERE t.credit_id = vch.id
                                         AND t.type_id = 19
                                         AND t.rep_date = v_report_date
                                         AND t.amount IS NOT NULL
                                         AND t.amount <> 0)
                                   )
                                 )
                            FROM dual
                           WHERE EXISTS (SELECT t.* FROM core.turnover t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.amount IS NOT NULL AND t.amount <> 0)))
                    FROM dual
                   WHERE EXISTS (SELECT t.* FROM core.debt_remains t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.value IS NOT NULL AND t.value <> 0)
                      OR EXISTS (SELECT t.* FROM core.credit_flow t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date)
                      OR EXISTS (SELECT t.* FROM core.turnover t WHERE t.credit_id = vch.id AND t.rep_date = v_report_date AND t.amount IS NOT NULL AND t.amount <> 0)),
                   -- CONTRACT
                   (SELECT xmlelement("contract",
                             -- NO
                             xmlelement("no", vch.contract_no),
                             -- DATE
                             xmlelement("date", to_char(vch.contract_date, c_date_format))
                           )
                      FROM dual
                     WHERE vch.contract_no IS NOT NULL
                       AND vch.contract_date IS NOT NULL),
                   -- CONTRACT_MATURITY_DATE
                   decode(vch.contract_maturity_date, null, null, xmlelement("contract_maturity_date", to_char(vch.contract_maturity_date, c_date_format))),
                   -- CREDIT_OBJECT
                   decode(vch.credit_object_id, null, null, xmlelement("credit_object",
                     xmlelement("code", ref_co.code)
                   )),
                   -- CREDIT_PURPOSE
                   decode(vch.credit_purpose_id, null, null, xmlelement("credit_purpose",
                     xmlelement("code", ref_cp.code)
                   )),
                   -- CREDIT_TYPE
                   decode(vch.type_id, null, null, xmlelement("credit_type",
                     xmlelement("code", ref_ct.code)
                   )),
                   -- CREDITOR
                   (SELECT xmlelement("creditor",
                             -- CODE
                             decode(s_vch.code, null, null, xmlelement("code", s_vch.code)),
                             -- DOCS
                             (SELECT xmlelement("docs",
                                       xmlagg(
                                         -- ITEM
                                         xmlelement("item",
                                           -- DOC_TYPE
                                           decode(ref_vcdh.type_id, null, null, xmlelement("doc_type",
                                             xmlelement("code", ref_dt.code)
                                           )),
                                           -- NO
                                           xmlelement("no", ref_vcdh.no_)
                                         )
                                       )
                                     )
                                FROM ref.creditor_doc ref_vcdh, -- MAY BE CREATE AND USE VIEW?!
                                     (SELECT t.parent_id AS id,
                                             t.code
                                        FROM ref.doc_type t
                                       WHERE t.open_date <= v_report_date
                                         AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_dt
                               WHERE ref_vcdh.creditor_id = s_vch.id
                                 AND ref_vcdh.type_id = ref_dt.id (+))
                           )
                      FROM ref.v_creditor_his s_vch
                     WHERE s_vch.id = vch.creditor_id
                       AND s_vch.open_date <= v_report_date
                       AND (s_vch.close_date > v_report_date OR s_vch.close_date is null)),
                   -- CREDITOR_BRANCH
                   (SELECT xmlelement("creditor_branch",
                           -- CODE
                           decode(ref_vch.code, null, null, xmlelement("code", ref_vch.code)),
                           -- DOCS
                           (SELECT xmlelement("docs",
                                     xmlagg(
                                       -- ITEM
                                       xmlelement("item",
                                         -- DOC_TYPE
                                         decode(ref_vcdh.type_id, null, null, xmlelement("doc_type",
                                           xmlelement("code", ref_dt.code)
                                         )),
                                         -- NO
                                         xmlelement("no", ref_vcdh.no_)
                                       )
                                     )
                                   )
                              FROM ref.creditor_doc ref_vcdh, -- MAY BE CREATE AND USE VIEW?!
                                   (SELECT t.parent_id AS id,
                                           t.code
                                      FROM ref.doc_type t
                                     WHERE t.open_date <= v_report_date
                                       AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_dt
                             WHERE ref_vch.id = ref_vcdh.creditor_id
                               AND ref_vcdh.type_id = ref_dt.id (+))
                           )
                      FROM ref.v_creditor_his ref_vch
                     WHERE ref_vch.id = vch.creditor_branch_id
                       AND ref_vch.open_date <= v_report_date
                       AND (ref_vch.close_date > v_report_date OR ref_vch.close_date IS NULL)),
                   -- CURRENCY
                   decode(vch.currency_id, null, null, xmlelement("currency",
                     xmlelement("code", ref_c.code)
                   )),
                   -- FINANCE_SOURCE
                   decode(vch.finance_source_id, null, null, xmlelement("finance_source",
                     xmlelement("code", ref_fs.code)
                   )),
                   -- HAS_CURRENCY_EARN
                   decode(vch.has_currency_earn, null, null, xmlelement("has_currency_earn", decode(vch.has_currency_earn, 1, 'true', 'false'))),
                   -- INTEREST_RATE_YEARLY
                   decode(vch.interest_rate_yearly, null, null, xmlelement("interest_rate_yearly", ltrim(to_char(vch.interest_rate_yearly, c_number_format, c_nls_numeric_characters)))),
                   -- PORTFOLIO
                   (SELECT xmlelement("portfolio",
                             decode(vch.portfolio_id, null, null, xmlelement("portfolio",
                               xmlelement("code", ref_pkfn.code)
                             )),
                             decode(vch.portfolio_msfo_id, null, null, xmlelement("portfolio_msfo",
                               xmlelement("code", ref_pmsfo.code)
                             ))
                           )
                      FROM dual
                     WHERE vch.portfolio_id IS NOT NULL
                        OR vch.portfolio_msfo_id IS NOT NULL),
                 -- PRIMARY_CONTRACT
                 xmlelement("primary_contract",
                   -- NO
                   xmlelement("no", vch.primary_contract_no),
                   -- DATE
                   xmlelement("date", to_char(vch.primary_contract_date, c_date_format))
                 ),                
                 -- SUBJECTS
                 (SELECT xmlelement("subjects",
                           xmlagg(
                             xmlelement("item",
                               -- ORGANIZATION
                               (SELECT xmlelement("organization",
                                         -- ADDRESSES
                                         (SELECT (SELECT xmlelement("addresses",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- DETAILS
                                                               decode(a.details, null, null, xmlelement("details", a.details)),
                                                               -- REGION
                                                               decode(a.region_id, null, null, xmlelement("region",
                                                                 -- CODE
                                                                 xmlelement("code", ref_a_r.code)
                                                               )),
                                                               -- TYPE
                                                               xmlelement("type", ref_at.code)
                                                             )
                                                           )
                                                         )
                                                    FROM core.address a,
                                                         ref.shared ref_at,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.region t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_a_r
                                                   WHERE a.org_id = s_voh.id
                                                     AND a.region_id = ref_a_r.id (+)
                                                     AND a.type_id = ref_at.id)
                                            FROM dual
                                           WHERE EXISTS (SELECT t.* FROM core.address t WHERE t.org_id = s_voh.id)),
                                         -- BANK_RELATIONS
                                         (SELECT (SELECT xmlelement("bank_relations",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- CREDITOR
                                                               (SELECT xmlelement("creditor",
                                                                         -- CODE
                                                                         decode(br_vch.code, null, null, xmlelement("code", br_vch.code)),
                                                                         -- DOCS
                                                                         (SELECT xmlelement("docs",
                                                                                   xmlagg(
                                                                                     -- ITEM
                                                                                     xmlelement("item",
                                                                                       -- DOC_TYPE
                                                                                       decode(ref_vcdh.type_id, null, null, xmlelement("doc_type",
                                                                                         xmlelement("code", ref_dt.code)
                                                                                       )),
                                                                                       -- NO
                                                                                       xmlelement("no", ref_vcdh.no_)
                                                                                     )
                                                                                   )
                                                                                 )
                                                                            FROM ref.creditor_doc ref_vcdh, -- MAY BE CREATE AND USE VIEW?!
                                                                                 (SELECT t.parent_id AS id,
                                                                                         t.code
                                                                                    FROM ref.doc_type t
                                                                                   WHERE t.open_date <= v_report_date
                                                                                     AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_dt
                                                                           WHERE ref_vcdh.creditor_id = br_vch.id
                                                                             AND ref_vcdh.type_id = ref_dt.id (+))
                                                                       )
                                                                  FROM ref.v_creditor_his br_vch
                                                                 WHERE br_vch.id = di.creditor_id
                                                                   AND br_vch.open_date <= v_report_date
                                                                   AND (br_vch.close_date > v_report_date OR br_vch.close_date is null)),
                                                                 xmlelement("bank_relation",
                                                                   -- CODE
                                                                   xmlelement("code", ref_br_br.code)
                                                                 )
                                                             )
                                                           )
                                                         )
                                                    FROM core.debtor_info di,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.bank_relation t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_br_br
                                                   WHERE di.org_id = s_voh.id
                                                     --AND di.creditor_id = v_creditor_id
                                                     AND di.bank_relation_id = ref_br_br.id (+)
                                                     AND di.open_date <= v_report_date
                                                     AND (di.close_date > v_report_date OR di.close_date is null))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.*
                                                           FROM core.debtor_info t
                                                          WHERE t.org_id = s_voh.id
                                                            --AND t.creditor_id = v_creditor_id
                                                            AND t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date is null))),
                                         -- CONTACTS
                                         (SELECT (SELECT xmlelement("contacts",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- CONTACT_TYPE
                                                               decode(c.type_id, null, null, xmlelement("contact_type",
                                                                 -- CODE
                                                                 xmlelement("code", ref_c_ct.code)
                                                               )),
                                                               -- DETAILS
                                                               decode(c.details, null, null, xmlelement("details", xmlelement("item", c.details)))
                                                             )
                                                           )
                                                         )
                                                    FROM core.contact c,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.contact_type t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_c_ct
                                                   WHERE c.org_id = s_voh.id
                                                     AND c.type_id = ref_c_ct.id (+))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.* FROM core.contact t WHERE t.org_id = s_voh.id)),
                                         -- COUNTRY
                                         decode(s_voh.country_id, null, null, xmlelement("country",
                                           xmlelement("code_numeric", ref_o_c.code_numeric)
                                         )),
                                         -- DOCS
                                         (SELECT (SELECT xmlelement("docs",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- DOC_TYPE
                                                               decode(vddh.type_id, null, null, xmlelement("doc_type",
                                                                 -- CODE
                                                                 xmlelement("code", ref_d_dt.code)
                                                               )),
                                                               -- NAME
                                                               decode(vddh.name, null, null, xmlelement("name", vddh.name)),
                                                               -- NO
                                                               decode(vddh.no_, null, null, xmlelement("no", vddh.no_))
                                                             )
                                                           )
                                                         )
                                                    FROM core.v_debtor_doc_his vddh,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.doc_type t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_d_dt
                                                   WHERE vddh.org_id = s_voh.id
                                                     AND vddh.type_id = ref_d_dt.id (+)
                                                     AND vddh.open_date <= v_report_date
                                                     AND (vddh.close_date > v_report_date OR vddh.close_date is null))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.*
                                                           FROM core.v_debtor_doc_his t
                                                          WHERE t.org_id = s_voh.id
                                                            AND t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date is null))),
                                         -- ECON_TRADE
                                         decode(s_voh.econ_trade_id, null, null, xmlelement("econ_trade",
                                           xmlelement("code", ref_o_et.code)
                                         )),
                                         -- ENTERPRISE_TYPE
                                         decode(s_voh.enterprise_type_id, null, null, xmlelement("enterprise_type",
                                           xmlelement("code", ref_o_t.code)
                                         )),
                                         -- HEAD
                                         (SELECT xmlelement("head",
                                                   -- DOCS
                                                   (SELECT (SELECT xmlelement("docs",
                                                                     xmlagg(
                                                                       -- ITEM
                                                                       xmlelement("item",
                                                                         -- DOC_TYPE
                                                                         decode(vddh.type_id, null, null, xmlelement("doc_type",
                                                                           -- CODE
                                                                           xmlelement("code", ref_d_dt.code)
                                                                         )),
                                                                         -- NAME
                                                                         decode(vddh.name, null, null, xmlelement("name", vddh.name)),
                                                                         -- NO
                                                                         decode(vddh.no_, null, null, xmlelement("no", vddh.no_))
                                                                       )
                                                                     )
                                                                   )
                                                              FROM core.v_debtor_doc_his vddh,
                                                                   (SELECT t.parent_id AS id,
                                                                           t.code
                                                                      FROM ref.doc_type t
                                                                     WHERE t.open_date <= v_report_date
                                                                       AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_d_dt
                                                             WHERE vddh.person_id = h_vph.id
                                                               AND vddh.type_id = ref_d_dt.id (+)
                                                               AND vddh.open_date <= v_report_date
                                                               AND (vddh.close_date > v_report_date OR vddh.close_date is null))
                                                      FROM dual
                                                     WHERE EXISTS (SELECT t.*
                                                                     FROM core.v_debtor_doc_his t
                                                                    WHERE t.person_id = h_vph.id
                                                                      AND t.open_date <= v_report_date
                                                                      AND (t.close_date > v_report_date OR t.close_date is null))),
                                                   -- NAMES
                                                   (SELECT (SELECT xmlelement("names",
                                                                     xmlagg(
                                                                       -- ITEM
                                                                       xmlelement("item",
                                                                         -- FIRSTNAME
                                                                         decode(vpnh.first_name, null, null, xmlelement("firstname", vpnh.first_name)),
                                                                         -- LASTNAME
                                                                         decode(vpnh.last_name, null, null, xmlelement("lastname", vpnh.last_name)),
                                                                         -- MIDDLENAME
                                                                         decode(vpnh.middle_name, null, null, xmlelement("middlename", vpnh.middle_name)),
                                                                         -- CODE
                                                                         xmlelement("lang", ref_l.code)
                                                                       )
                                                                     )
                                                                   )
                                                              FROM core.v_person_name_his vpnh,
                                                                   ref.shared ref_l
                                                             WHERE vpnh.person_id = h_vph.id
                                                               AND vpnh.lang_id = ref_l.id (+)
                                                               AND vpnh.open_date <= v_report_date
                                                               AND (vpnh.close_date > v_report_date OR vpnh.close_date is null))
                                                      FROM dual
                                                     WHERE EXISTS (SELECT t.*
                                                                     FROM core.v_person_name_his t
                                                                    WHERE t.person_id = h_vph.id
                                                                      AND t.open_date <= v_report_date
                                                                      AND (t.close_date > v_report_date OR t.close_date is null)))
                                                 )
                                            FROM core.v_person_his h_vph
                                           WHERE h_vph.id = s_voh.head_id
                                             AND h_vph.open_date <= v_report_date
                                             AND (h_vph.close_date > v_report_date OR h_vph.close_date is null)),
                                         -- IS_SE
                                         decode(s_voh.is_se, null, null, xmlelement("is_se", decode(s_voh.is_se, 1, 'true', 'false'))),
                                         -- LEGAL_FORM
                                         decode(s_voh.legal_form_id, null, null, xmlelement("legal_form",
                                           xmlelement("code", ref_o_lf.code)
                                         )),
                                         -- NAMES
                                         (SELECT (SELECT xmlelement("names",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- NAME
                                                               decode(vonh.name, null, null, xmlelement("name", vonh.name)),
                                                               -- CODE
                                                               xmlelement("lang", ref_l.code)
                                                             )
                                                           )
                                                         )
                                                    FROM core.v_org_name_his vonh,
                                                         ref.shared ref_l
                                                   WHERE vonh.org_id = s_voh.id
                                                     AND vonh.lang_id = ref_l.id (+)
                                                     AND vonh.open_date <= v_report_date
                                                     AND (vonh.close_date > v_report_date OR vonh.close_date is null))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.*
                                                           FROM core.v_org_name_his t
                                                          WHERE t.org_id = s_voh.id
                                                            AND t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date is null))),
                                         -- OFFSHORE
                                         decode(s_voh.offshore_id, null, null, xmlelement("offshore",
                                           xmlelement("code", ref_o_o.code)
                                         ))
                                       )
                                  FROM v_organization_his s_voh,
                                       (SELECT t.parent_id AS id,
                                               t.code
                                          FROM ref.enterprise_type t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_o_t,
                                       (SELECT t.parent_id AS id,
                                               t.code
                                          FROM ref.econ_trade t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_o_et,
                                       (SELECT t.parent_id AS id,
                                               t.code
                                          FROM ref.legal_form t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_o_lf,
                                       (SELECT t.parent_id AS id,
                                               t.code_numeric
                                          FROM ref.country t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_o_c,
                                       (SELECT t.parent_id AS id,
                                               t.code
                                          FROM ref.offshore t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_o_o
                                 WHERE s_voh.id = vdh.org_id
                                   AND s_voh.offshore_id = ref_o_o.id (+)
                                   AND s_voh.country_id = ref_o_c.id (+)
                                   AND s_voh.econ_trade_id = ref_o_et.id (+)
                                   AND s_voh.legal_form_id = ref_o_lf.id (+)
                                   AND s_voh.enterprise_type_id = ref_o_t.id (+)
                                   AND s_voh.open_date <= v_report_date
                                   AND (s_voh.close_date > v_report_date OR s_voh.close_date is null)),
                               -- PERSON
                               (SELECT xmlelement("person",
                                         -- ADDRESSES
                                         (SELECT (SELECT xmlelement("addresses",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- DETAILS
                                                               decode(a.details, null, null, xmlelement("details", a.details)),
                                                               -- REGION
                                                               decode(a.region_id, null, null, xmlelement("region",
                                                                 -- CODE
                                                                 xmlelement("code", ref_a_r.code)
                                                               )),
                                                               -- TYPE
                                                               xmlelement("type", ref_at.code)
                                                             )
                                                           )
                                                         )
                                                    FROM core.address a,
                                                         ref.shared ref_at,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.region t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_a_r
                                                   WHERE a.person_id = s_vph.id
                                                     AND a.region_id = ref_a_r.id (+)
                                                     AND a.type_id = ref_at.id)
                                            FROM dual
                                           WHERE EXISTS (SELECT t.* FROM core.address t WHERE t.person_id = s_vph.id)),
                                         -- BANK_RELATIONS
                                         (SELECT (SELECT xmlelement("bank_relations",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                                 -- CREDITOR
                                                               (SELECT xmlelement("creditor",
                                                                         -- CODE
                                                                         decode(br_vch.code, null, null, xmlelement("code", br_vch.code)),
                                                                         -- DOCS
                                                                         (SELECT xmlelement("docs",
                                                                                   xmlagg(
                                                                                     -- ITEM
                                                                                     xmlelement("item",
                                                                                       -- DOC_TYPE
                                                                                       decode(ref_vcdh.type_id, null, null, xmlelement("doc_type",
                                                                                         xmlelement("code", ref_dt.code)
                                                                                       )),
                                                                                       -- NO
                                                                                       xmlelement("no", ref_vcdh.no_)
                                                                                     )
                                                                                   )
                                                                                 )
                                                                            FROM ref.creditor_doc ref_vcdh, -- MAY BE CREATE AND USE VIEW?!
                                                                                 (SELECT t.parent_id AS id,
                                                                                         t.code
                                                                                    FROM ref.doc_type t
                                                                                   WHERE t.open_date <= v_report_date
                                                                                     AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_dt
                                                                           WHERE ref_vcdh.creditor_id = br_vch.id
                                                                             AND ref_vcdh.type_id = ref_dt.id (+))
                                                                       )
                                                                  FROM ref.v_creditor_his br_vch
                                                                 WHERE br_vch.id = di.creditor_id
                                                                   AND br_vch.open_date <= v_report_date
                                                                   AND (br_vch.close_date > v_report_date OR br_vch.close_date is null)),
                                                                 xmlelement("bank_relation",
                                                                   -- CODE
                                                                   xmlelement("code", ref_br_br.code)
                                                                 )
                                                             )
                                                           )
                                                         )
                                                    FROM core.debtor_info di,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.bank_relation t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_br_br
                                                   WHERE di.person_id = s_vph.id
                                                     AND di.bank_relation_id = ref_br_br.id (+)
                                                     AND di.open_date <= v_report_date
                                                     AND (di.close_date > v_report_date OR di.close_date is null))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.*
                                                           FROM core.debtor_info t
                                                          WHERE t.person_id = s_vph.id
                                                            AND t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date is null))),
                                         -- CONTACTS
                                         (SELECT (SELECT xmlelement("contacts",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- CONTACT_TYPE
                                                               decode(c.type_id, null, null, xmlelement("contact_type",
                                                                 -- CODE
                                                                 xmlelement("code", ref_c_ct.code)
                                                               )),
                                                               -- DETAILS
                                                               decode(c.details, null, null, xmlelement("details", xmlelement("item", c.details)))
                                                             )
                                                           )
                                                         )
                                                    FROM core.contact c,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.contact_type t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_c_ct
                                                   WHERE c.person_id = s_vph.id
                                                     AND c.type_id = ref_c_ct.id (+))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.* FROM core.contact t WHERE t.person_id = s_vph.id)),
                                         -- COUNTRY
                                         decode(s_vph.country_id, null, null, xmlelement("country",
                                           xmlelement("code_numeric", ref_p_c.code_numeric)
                                         )),
                                         -- DOCS
                                         (SELECT (SELECT xmlelement("docs",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- DOC_TYPE
                                                               decode(vddh.type_id, null, null, xmlelement("doc_type",
                                                                 -- CODE
                                                                 xmlelement("code", ref_d_dt.code)
                                                               )),
                                                               -- NAME
                                                               decode(vddh.name, null, null, xmlelement("name", vddh.name)),
                                                               -- NO
                                                               decode(vddh.no_, null, null, xmlelement("no", vddh.no_))
                                                             )
                                                           )
                                                         )
                                                    FROM core.v_debtor_doc_his vddh,
                                                         (SELECT t.parent_id AS id,
                                                                 t.code
                                                            FROM ref.doc_type t
                                                           WHERE t.open_date <= v_report_date
                                                             AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_d_dt
                                                   WHERE vddh.person_id = s_vph.id
                                                     AND vddh.type_id = ref_d_dt.id (+)
                                                     AND vddh.open_date <= v_report_date
                                                     AND (vddh.close_date > v_report_date OR vddh.close_date is null))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.*
                                                           FROM core.v_debtor_doc_his t
                                                          WHERE t.person_id = s_vph.id
                                                            AND t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date is null))),
                                         -- NAMES
                                         (SELECT (SELECT xmlelement("names",
                                                           xmlagg(
                                                             -- ITEM
                                                             xmlelement("item",
                                                               -- FIRSTNAME
                                                               decode(vpnh.first_name, null, null, xmlelement("firstname", vpnh.first_name)),
                                                               -- LASTNAME
                                                               decode(vpnh.last_name, null, null, xmlelement("lastname", vpnh.last_name)),
                                                               -- MIDDLENAME
                                                               decode(vpnh.middle_name, null, null, xmlelement("middlename", vpnh.middle_name)),
                                                               -- CODE
                                                               xmlelement("lang", ref_l.code)
                                                             )
                                                           )
                                                         )
                                                    FROM core.v_person_name_his vpnh,
                                                         ref.shared ref_l
                                                   WHERE vpnh.person_id = s_vph.id
                                                     AND vpnh.lang_id = ref_l.id (+)
                                                     AND vpnh.open_date <= v_report_date
                                                     AND (vpnh.close_date > v_report_date OR vpnh.close_date is null))
                                            FROM dual
                                           WHERE EXISTS (SELECT t.*
                                                           FROM core.v_person_name_his t
                                                          WHERE t.person_id = s_vph.id
                                                            AND t.open_date <= v_report_date
                                                            AND (t.close_date > v_report_date OR t.close_date is null))),
                                         -- OFFSHORE
                                         decode(s_vph.offshore_id, null, null, xmlelement("offshore",
                                           xmlelement("code", ref_p_o.code)
                                         ))
                                       )
                                  FROM v_person_his s_vph,
                                       (SELECT t.parent_id AS id,
                                               t.code_numeric
                                          FROM ref.country t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_p_c,
                                       (SELECT t.parent_id AS id,
                                               t.code
                                          FROM ref.offshore t
                                         WHERE t.open_date <= v_report_date
                                           AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_p_o
                                 WHERE s_vph.id = vdh.person_id
                                   AND s_vph.offshore_id = ref_p_o.id (+)
                                   AND s_vph.country_id = ref_p_c.id (+)
                                   AND s_vph.open_date <= v_report_date
                                   AND (s_vph.close_date > v_report_date OR s_vph.close_date is null)),
                               -- CREDITOR
                               (SELECT xmlelement("creditor",
                                         -- CODE
                                         decode(s_vch.code, null, null, xmlelement("code", s_vch.code)),
                                         -- DOCS
                                         (SELECT xmlelement("docs",
                                                   xmlagg(
                                                     -- ITEM
                                                     xmlelement("item",
                                                       -- DOC_TYPE
                                                       decode(ref_vcdh.type_id, null, null, xmlelement("doc_type",
                                                         xmlelement("code", ref_dt.code)
                                                       )),
                                                       -- NO
                                                       xmlelement("no", ref_vcdh.no_)
                                                     )
                                                   )
                                                 )
                                            FROM ref.creditor_doc ref_vcdh, -- MAY BE CREATE AND USE VIEW?!
                                                 (SELECT t.parent_id AS id,
                                                         t.code
                                                    FROM ref.doc_type t
                                                   WHERE t.open_date <= v_report_date
                                                     AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_dt
                                           WHERE ref_vcdh.creditor_id = s_vch.id
                                             AND ref_vcdh.type_id = ref_dt.id (+))
                                       )
                                  FROM ref.v_creditor_his s_vch
                                 WHERE s_vch.id = vdh.creditor_id
                                   AND s_vch.open_date <= v_report_date
                                   AND (s_vch.close_date > v_report_date OR s_vch.close_date is null))
                             )
                           )
                         )
                    FROM v_debtor_his vdh
                   WHERE vdh.credit_id = vch.id
                     AND vdh.type_id in (1, 7)
                     AND (vdh.person_id IS NOT NULL OR vdh.org_id IS NOT NULL OR vdh.creditor_id IS NOT NULL)
                     AND vdh.open_date <= v_report_date
                     AND (vdh.close_date > v_report_date OR vdh.close_date IS NULL)),
                 -- PLEDGES
                 (SELECT xmlelement("pledges",
                           (SELECT xmlagg(
                                     xmlelement("item",
                                       -- CONTRACT
                                       decode(vph.contract_no, null, null, xmlelement("contract", vph.contract_no)),
                                       -- PLEDGE_TYPE
                                       decode(vph.type_id, null, null, xmlelement("pledge_type",
                                         xmlelement("code", ref_p_pt.code)
                                       )),

                                       -- VALUE
                                       decode(vph.value_, null, null, xmlelement("value", ltrim(to_char(vph.value_, c_number_format, c_nls_numeric_characters))))
                                     )
                                   )
                              FROM v_pledge_his vph,
                                   (SELECT t.parent_id AS id,
                                           t.code
                                      FROM ref.pledge_type t
                                     WHERE t.open_date <= v_report_date
                                       AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_p_pt
                             WHERE vph.credit_id = vch.id
                               AND vph.type_id = ref_p_pt.id (+)
                               AND vph.open_date <= v_report_date
                               AND (vph.close_date > v_report_date OR vph.close_date is null))
                         )
                    FROM dual
                   WHERE EXISTS (SELECT t.*
                                   FROM v_pledge_his t
                                  WHERE t.credit_id = vch.id
                                    AND t.open_date <= v_report_date
                                    AND (t.close_date > v_report_date OR t.close_date is null)))
               )
               ORDER BY vch.primary_contract_no, vch.primary_contract_date
             )
           ), version '1.0" encoding="utf-8')
          INTO v_data_xml
          FROM v_credit_his vch,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.credit_type t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_ct,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.credit_object t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_co,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.credit_purpose t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_cp,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.currency t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_c,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.finance_source t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_fs,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.portfolio t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_pkfn,
               (SELECT t.parent_id AS id,
                       t.code
                  FROM ref.portfolio t
                 WHERE t.open_date <= v_report_date
                   AND (t.close_date > v_report_date OR t.close_date IS NULL)) ref_pmsfo
         WHERE vch.id IN (SELECT xci.credit_id
                            FROM core.xml_file xf,
                                 core.xml_credit_id xci
                           WHERE xf.id = xci.xml_file_id
                             AND xf.id = p_xml_file_id)
           AND vch.type_id = ref_ct.id (+)
           AND vch.credit_object_id = ref_co.id (+)
           AND vch.credit_purpose_id = ref_cp.id (+)
           AND vch.currency_id = ref_c.id (+)
           AND vch.finance_source_id = ref_fs.id (+)
           AND vch.portfolio_id = ref_pkfn.id (+)
           AND vch.portfolio_msfo_id = ref_pmsfo.id (+)
           AND vch.open_date <= v_report_date
           AND (vch.close_date > v_report_date OR vch.close_date IS NULL);

    SELECT xmlroot(xmlelement("manifest",
             xmlelement("type", '1'),
             xmlelement("name", 'data.xml'),
             xmlelement("userid", '100500'),
             xmlelement("size", (select count(*) from core.xml_credit_id xci where xci.xml_file_id = p_xml_file_id)),
             xmlelement("date", to_char(v_report_date, 'dd.MM.yyyy')),
             xmlelement("properties",
               decode(vch.code, null, null, xmlelement("property", xmlelement("name", 'CODE'), xmlelement("value", vch.code))),
               decode(vch.name, null, null, xmlelement("property", xmlelement("name", 'NAME'), xmlelement("value", vch.name))),
               (SELECT xmlelement("property",
                         xmlelement("name", 'BIN'),
                         xmlelement("value", cd.no_)
                       )
                  FROM ref.creditor_doc cd
                 WHERE cd.creditor_id = vch.id
                   AND cd.type_id = 7),
               (SELECT xmlelement("property",
                         xmlelement("name", 'BIK'),
                         xmlelement("value", cd.no_)
                       )
                  FROM ref.creditor_doc cd
                 WHERE cd.creditor_id = vch.id
                   AND cd.type_id = 15)
             )
           ), version '1.0" encoding="utf-8'),
           'XML files generated for USCI project by Alexandr Motov.' || chr(13) ||
           'Creditor ID in CR: ' || vch.id || chr(13) ||
           'Report date: ' || to_char(v_report_date, 'dd.MM.yyyy') || chr(13) ||
           'Generation date: ' || to_char(sysdate, 'dd.MM.yyyy')
      INTO v_manifest_xml, v_zip_comment
      FROM ref.v_creditor_his vch
     WHERE vch.id = v_creditor_id
       AND vch.open_date <= v_report_date
       AND (vch.close_date > v_report_date OR vch.close_date IS NULL);

    -- <manifest>
    --   <type>T</type>
    --   <name>FILE_NAME.XML</name>
    --   <userid>USER</userid>
    --   <size>S</size>
    --   <date>DD.MM.YYYY</date>
    --   <properties>
    --     <property>
    --       <name>XXX</name>
    --       <value>YYY</value>
    --     </property>
    --   </properties>
    -- </manifest>

    pkg_zip_util.add_file(v_zip, 'data.xml', v_data_xml.getBlobVal(nls_charset_id('UTF8')));
    pkg_zip_util.add_file(v_zip, 'manifest.xml', v_manifest_xml.getBlobVal(nls_charset_id('UTF8')));
    pkg_zip_util.finish_zip(v_zip, v_zip_comment);

    UPDATE xml_file xf
       SET xf.end_date = sysdate,
           xf.file_content = v_zip,
           xf.status = 'COMPLETED'
     WHERE xf.id = p_xml_file_id;

    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      BEGIN
        UPDATE xml_file xf
           SET xf.end_date = sysdate,
               xf.file_content = null,
               xf.status = 'FAILED'
         WHERE xf.id = p_xml_file_id;
      END;
  END;
  
  PROCEDURE generate_xml_v2
  (
    p_xml_file_id IN NUMBER
  )
  IS
    v_creditor_id  NUMBER;
    v_report_date  DATE;
    v_data_xml     XMLTYPE;
    v_manifest_xml XMLTYPE;
    v_zip          BLOB;
    v_zip_comment  VARCHAR2(4000 CHAR);
  BEGIN

    SELECT xf.creditor_id, xf.report_date
      INTO v_creditor_id, v_report_date
      FROM core.xml_file xf
     WHERE xf.id = p_xml_file_id;

    UPDATE core.xml_file xf
       SET xf.status = 'RUNNING'
     WHERE xf.id = p_xml_file_id;
    COMMIT;

    SELECT xmlroot(xmlelement("entities",
             xmlagg(
               xmlelement("entity",
                 xmlattributes('credit' AS "class"),
                 -- ACTUAL_ISSUE_DATE
                   xmlelement("actual_issue_date", to_char(vch.actual_issue_date, c_date_format)),
                   -- AMOUNT
                   xmlelement("amount", vch.amount),
                   -- CHANGE
                   xmlelement("change",
                     -- REMAINS
                     xmlelement("remains",
                       -- CORRECTION
                       get_debt_remains_xml(vch.id, c_drt_correction, v_report_date),
                       -- DEBT
                       xmlelement("debt",
                         -- CURRENT
                         get_debt_remains_xml(vch.id, c_drt_debt_current, v_report_date),
                         -- PASTDUE
                         get_debt_remains_xml(vch.id, c_drt_debt_pastdue, v_report_date),
                         -- WRITE_OFF
                         get_debt_remains_xml(vch.id, c_drt_debt_write_off, v_report_date)        
                       ),
                       -- DISCOUNT
                       get_debt_remains_xml(vch.id, c_drt_discount, v_report_date),
                       -- DISCOUNTED_VALUE
                       get_debt_remains_xml(vch.id, c_drt_discounted_value, v_report_date),
                       -- INTEREST
                       xmlelement("interest",
                         -- CURRENT
                         get_debt_remains_xml(vch.id, c_drt_interest_current, v_report_date),
                         -- PASTDUE
                         get_debt_remains_xml(vch.id, c_drt_interest_pastdue, v_report_date),
                         -- WRITE_OFF
                         get_debt_remains_xml(vch.id, c_drt_interest_write_off, v_report_date)        
                       ),
                       -- LIMIT
                       get_debt_remains_xml(vch.id, c_drt_limit, v_report_date)
                     ),
                     -- CREDIT_FLOW
                     get_credit_flow_xml(vch.id, v_report_date),
                     -- TURNOVER
                     xmlelement("turnover",
                       -- ISSUE
                       xmlelement("issue",
                         -- DEBT
                         get_turnover_xml(vch.id, c_tt_issue_debt, v_report_date),
                         -- INTEREST
                         get_turnover_xml(vch.id, c_tt_issue_interest, v_report_date)
                       )
                     )
                   ),
                   -- CONTRACT
                   xmlelement("contract",
                     -- NO
                     xmlelement("no", vch.contract_no),
                     -- DATE
                     xmlelement("date", to_char(vch.contract_date, c_date_format))
                   ),
                   -- CONTRACT_MATURITY_DATE
                   xmlelement("contract_maturity_date", to_char(vch.contract_maturity_date, c_date_format)),
                   -- CREDIT_OBJECT
                   get_ref_credit_object_xml(vch.credit_object_id, v_report_date),
                   -- CREDIT_PURPOSE
                   get_ref_credit_purpose_xml(vch.credit_purpose_id, v_report_date),
                   -- CREDIT_TYPE
                   get_ref_credit_type_xml(vch.type_id, v_report_date),
                   -- CREDITOR
                   get_ref_creditor_xml(vch.creditor_id, v_report_date, 'creditor'),
                   -- CREDITOR_BRANCH
                   get_ref_creditor_xml(vch.creditor_branch_id, v_report_date, 'creditor_branch'),
                   -- CURRENCY
                   get_ref_currency_xml(vch.currency_id, v_report_date),
                   -- FINANCE_SOURCE
                   get_ref_finance_source_xml(vch.finance_source_id, v_report_date),
                   -- HAS_CURRENCY_EARN
                   xmlelement("has_currency_earn", decode(vch.has_currency_earn, 1, 'true', 'false')),
                   -- INTEREST_RATE_YEARLY
                   xmlelement("interest_rate_yearly", ltrim(to_char(vch.interest_rate_yearly, c_number_format, c_nls_numeric_characters))),
                   -- PORTFOLIO
                   xmlelement("portfolio",
                     -- PORTFOLIO
                     get_ref_portfolio_xml(vch.portfolio_id, v_report_date, 'portfolio'),
                     -- PORTFOLIO_MSFO
                     get_ref_portfolio_xml(vch.portfolio_msfo_id, v_report_date, 'portfolio_msfo')
                   ),
                 -- PRIMARY_CONTRACT
                 xmlelement("primary_contract",
                   -- NO
                   xmlelement("no", vch.primary_contract_no),
                   -- DATE
                   xmlelement("date", to_char(vch.primary_contract_date, c_date_format))
                 ),
                 -- SUBJECTS
                 xmlelement("subjects",
                   (SELECT xmlagg(
                             -- ITEM
                             xmlelement("item",
                               -- ORGANIZATION
                               get_organization_xml(vdh.org_id, v_report_date, 'organization'),
                               -- PERSON
                               get_person_xml(vdh.person_id, v_report_date, 'person'),
                               -- CREDITOR
                               get_ref_creditor_xml(vdh.creditor_id, v_report_date, 'creditor')
                             )
                           )
                    FROM v_debtor_his vdh
                   WHERE vdh.credit_id = vch.id
                     AND vdh.type_id in (1, 7)
                     AND (vdh.person_id IS NOT NULL OR vdh.org_id IS NOT NULL OR vdh.creditor_id IS NOT NULL)
                     AND vdh.open_date <= v_report_date
                     AND (vdh.close_date > v_report_date OR vdh.close_date IS NULL))
                 ),
                 -- PLEDGES
                 get_pledges_xml(vch.id, v_report_date)
               )
               ORDER BY vch.primary_contract_no, vch.primary_contract_date
             )
           ), version '1.0" encoding="utf-8')
          INTO v_data_xml
          FROM v_credit_his vch,
               (SELECT xci.credit_id
                  FROM core.xml_file xf,
                       core.xml_credit_id xci
                 WHERE xf.id = xci.xml_file_id
                   AND xf.id = p_xml_file_id) ci
         WHERE vch.id = ci.credit_id
           AND vch.open_date <= v_report_date
           AND (vch.close_date > v_report_date OR vch.close_date IS NULL);

    SELECT xmlroot(xmlelement("manifest",
             xmlelement("type", '1'),
             xmlelement("name", 'data.xml'),
             xmlelement("userid", '100500'),
             xmlelement("size", (select count(*) from core.xml_credit_id xci where xci.xml_file_id = p_xml_file_id)),
             xmlelement("date", to_char(v_report_date, 'dd.MM.yyyy')),
             xmlelement("properties",
               decode(vch.code, null, null, xmlelement("property", xmlelement("name", 'CODE'), xmlelement("value", vch.code))),
               decode(vch.name, null, null, xmlelement("property", xmlelement("name", 'NAME'), xmlelement("value", vch.name))),
               (SELECT xmlelement("property",
                         xmlelement("name", 'BIN'),
                         xmlelement("value", cd.no_)
                       )
                  FROM ref.creditor_doc cd
                 WHERE cd.creditor_id = vch.id
                   AND cd.type_id = 7),
               (SELECT xmlelement("property",
                         xmlelement("name", 'BIK'),
                         xmlelement("value", cd.no_)
                       )
                  FROM ref.creditor_doc cd
                 WHERE cd.creditor_id = vch.id
                   AND cd.type_id = 15)
             )
           ), version '1.0" encoding="utf-8'),
           'XML files generated for USCI project by Alexandr Motov.' || chr(13) ||
           'Creditor ID in CR: ' || vch.id || chr(13) ||
           'Report date: ' || to_char(v_report_date, 'dd.MM.yyyy') || chr(13) ||
           'Generation date: ' || to_char(sysdate, 'dd.MM.yyyy')
      INTO v_manifest_xml, v_zip_comment
      FROM ref.v_creditor_his vch
     WHERE vch.id = v_creditor_id
       AND vch.open_date <= v_report_date
       AND (vch.close_date > v_report_date OR vch.close_date IS NULL);

    -- <manifest>
    --   <type>T</type>
    --   <name>FILE_NAME.XML</name>
    --   <userid>USER</userid>
    --   <size>S</size>
    --   <date>DD.MM.YYYY</date>
    --   <properties>
    --     <property>
    --       <name>XXX</name>
    --       <value>YYY</value>
    --     </property>
    --   </properties>
    -- </manifest>

    pkg_zip_util.add_file(v_zip, 'data.xml', v_data_xml.getBlobVal(nls_charset_id('UTF8')));
    pkg_zip_util.add_file(v_zip, 'manifest.xml', v_manifest_xml.getBlobVal(nls_charset_id('UTF8')));
    pkg_zip_util.finish_zip(v_zip, v_zip_comment);

    UPDATE xml_file xf
       SET xf.end_date = sysdate,
           xf.file_content = v_zip,
           xf.status = 'COMPLETED'
     WHERE xf.id = p_xml_file_id;

    COMMIT;
  /*EXCEPTION
    WHEN OTHERS THEN
      BEGIN
        UPDATE xml_file xf
           SET xf.end_date = sysdate,
               xf.file_content = null,
               xf.status = 'FAILED'
         WHERE xf.id = p_xml_file_id;
      END;*/
  END;

  FUNCTION get_ref_balance_account_xml
  (
    p_balance_account_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'balance_account'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_balance_account_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("no_", t.no_))
        INTO v_xml
        FROM ref.balance_account t
       WHERE t.parent_id = p_balance_account_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_credit_object_xml
  (
    p_credit_object_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'credit_object'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_credit_object_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.credit_object t
       WHERE t.parent_id = p_credit_object_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_credit_type_xml
  (
    p_credit_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'credit_type'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_credit_type_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.credit_type t
       WHERE t.parent_id = p_credit_type_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_credit_purpose_xml
  (
    p_credit_purpose_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'credit_purpose'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_credit_purpose_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.credit_purpose t
       WHERE t.parent_id = p_credit_purpose_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_currency_xml
  (
    p_currency_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'currency'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_currency_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.currency t
       WHERE t.parent_id = p_currency_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_portfolio_xml
  (
    p_portfolio_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'portfolio'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_portfolio_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.portfolio t
       WHERE t.parent_id = p_portfolio_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_finance_source_xml
  (
    p_finance_source_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'finance_source'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_finance_source_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.finance_source t
       WHERE t.parent_id = p_finance_source_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_doc_type_xml
  (
    p_doc_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'doc_type'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_doc_type_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.doc_type t
       WHERE t.parent_id = p_doc_type_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_classification_xml
  (
    p_classification_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'classification'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_classification_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.classification t
       WHERE t.parent_id = p_classification_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_country_xml
  (
    p_country_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'country'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_country_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code_numeric", t.code_numeric))
        INTO v_xml
        FROM ref.country t
       WHERE t.parent_id = p_country_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_legal_form_xml
  (
    p_legal_form_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'legal_form'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_legal_form_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.legal_form t
       WHERE t.parent_id = p_legal_form_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_offshore_xml
  (
    p_offshore_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'offshore'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_offshore_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.offshore t
       WHERE t.parent_id = p_offshore_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_econ_trade_xml
  (
    p_econ_trade_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'econ_trade'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_econ_trade_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.econ_trade t
       WHERE t.parent_id = p_econ_trade_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_enterprise_type_xml
  (
    p_enterprise_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'enterprise_type'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_enterprise_type_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.enterprise_type t
       WHERE t.parent_id = p_enterprise_type_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_contact_type_xml
  (
    p_contact_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'contact_type'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_contact_type_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.contact_type t
       WHERE t.parent_id = p_contact_type_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_region_xml
  (
    p_region_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'region'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_region_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.region t
       WHERE t.parent_id = p_region_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_bank_relation_xml
  (
    p_bank_relation_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'bank_relation'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_bank_relation_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.bank_relation t
       WHERE t.parent_id = p_bank_relation_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_pledge_type_xml
  (
    p_pledge_type_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'pledge_type'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_pledge_type_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), xmlelement("code", t.code))
        INTO v_xml
        FROM ref.pledge_type t
       WHERE t.parent_id = p_pledge_type_id 
         AND t.open_date <= p_report_date
         AND (t.close_date > p_report_date OR t.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_ref_creditor_xml
  (
    p_creditor_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'creditor'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_creditor_id IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    ELSE
      SELECT xmlelement(evalname(p_tag_name), 
               xmlelement("code", vch.code),
               xmlelement("docs",
                 (SELECT xmlagg(
                           -- ITEM
                           xmlelement("item",
                             -- DOC_TYPE
                             get_ref_doc_type_xml(vcdh.type_id, p_report_date),
                             -- NO
                             xmlelement("no", vcdh.no_)
                           )
                         )
                    FROM ref.creditor_doc vcdh -- MAY BE CREATE AND USE VIEW?!
                   WHERE vcdh.creditor_id = vch.id)
               )
             )
        INTO v_xml
        FROM ref.v_creditor_his vch
       WHERE vch.id = p_creditor_id 
         AND vch.open_date <= p_report_date
         AND (vch.close_date > p_report_date OR vch.close_date IS NULL);
    END IF;
    
    RETURN v_xml;
  END;
  
  FUNCTION get_debt_remains_xml
  (
    p_credit_id IN NUMBER,
    p_debt_remains_type_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN XMLTYPE
  IS
    v_tag_name VARCHAR2(50 char);
    v_xml xmltype;
  BEGIN
    SELECT CASE
             WHEN p_debt_remains_type_id = c_drt_debt_current THEN 'current'
             WHEN p_debt_remains_type_id = c_drt_debt_pastdue THEN 'pastdue'
             WHEN p_debt_remains_type_id = c_drt_debt_write_off THEN 'write_off'
             WHEN p_debt_remains_type_id = c_drt_interest_current THEN 'current'
             WHEN p_debt_remains_type_id = c_drt_interest_pastdue THEN 'pastdue'
             WHEN p_debt_remains_type_id = c_drt_interest_write_off THEN 'write_off'
             WHEN p_debt_remains_type_id = c_drt_discount THEN 'discount'
             WHEN p_debt_remains_type_id = c_drt_correction THEN 'correction'
             WHEN p_debt_remains_type_id = c_drt_discounted_value THEN 'discounted_value'
             WHEN p_debt_remains_type_id = c_drt_limit THEN 'limit'
             WHEN p_debt_remains_type_id = c_drt_provision_kfn THEN 'provision_kfn'
             WHEN p_debt_remains_type_id = c_drt_provision_msfo THEN 'provision_msfo'
             WHEN p_debt_remains_type_id = c_drt_provision_msfo_ob THEN 'provision_msfo_over_balance'
             ELSE null
           END
      INTO v_tag_name
      FROM dual;  
  
    BEGIN
      SELECT CASE
               -- DEBT_CURRENT
               WHEN dr.type_id = c_drt_debt_current THEN
                 xmlelement("current",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))
                 )
               -- DEBT_PASTDUE
               WHEN dr.type_id = c_drt_debt_pastdue THEN 
                 xmlelement("pastdue",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))),
                   xmlelement("open_date", to_char(dr.pastdue_open_date, 'dd.MM.yyyy')),
                   xmlelement("close_date", to_char(dr.pastdue_close_date, 'dd.MM.yyyy'))
                 )
               -- DEBT_WRITE_OFF
               WHEN dr.type_id = c_drt_debt_write_off THEN 
                 xmlelement("write_off",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))),
                   xmlelement("date", to_char(dr.write_off_date, 'dd.MM.yyyy'))
                 )
               -- INTEREST_CURRENT
               WHEN dr.type_id = c_drt_interest_current THEN
                 xmlelement("current",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))
                 )
               -- INTEREST_PASTDUE
               WHEN dr.type_id = c_drt_interest_pastdue THEN
                 xmlelement("pastdue",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))),
                   xmlelement("open_date", to_char(dr.pastdue_open_date, 'dd.MM.yyyy')),
                   xmlelement("close_date", to_char(dr.pastdue_close_date, 'dd.MM.yyyy'))
                 )
               -- INTEREST_WRITE_OFF
               WHEN dr.type_id = c_drt_interest_write_off THEN
                 xmlelement("write_off",
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters))),
                   xmlelement("date", to_char(dr.write_off_date, 'dd.MM.yyyy'))
                 )
               -- DISCOUNT
               WHEN dr.type_id = c_drt_discount THEN
                 xmlelement("discount",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))
                 )
               -- CORRECTION
               WHEN dr.type_id = c_drt_correction THEN 
                 xmlelement("correction",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))
                 )
               -- DISCOUNTED_VALUE
               WHEN dr.type_id = c_drt_discounted_value THEN
                 xmlelement("discounted_value",
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                 )
               -- LIMIT
               WHEN dr.type_id = c_drt_limit THEN
                 xmlelement("limit",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters))),
                   xmlelement("value_currency", ltrim(to_char(dr.value_currency, c_number_format, c_nls_numeric_characters)))
                 )
               WHEN dr.type_id = c_drt_provision_kfn THEN
                 xmlelement("provision_kfn",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                 )
               WHEN dr.type_id = c_drt_provision_msfo THEN
                 xmlelement("provision_msfo",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                 )
               WHEN dr.type_id = c_drt_provision_msfo_ob THEN
                 xmlelement("provision_msfo_over_balance",
                   get_ref_balance_account_xml(dr.account_id, p_report_date),
                   xmlelement("value", ltrim(to_char(dr.value, c_number_format, c_nls_numeric_characters)))
                 )
               ELSE null
             END
        INTO v_xml
        FROM core.debt_remains dr
       WHERE dr.credit_id = p_credit_id
         AND dr.type_id = p_debt_remains_type_id
         AND dr.rep_date = p_report_date;
    EXCEPTION
      WHEN no_data_found THEN
        SELECT xmlelement(evalname(v_tag_name), null)
          INTO v_xml
          FROM dual;
    END;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_turnover_xml
  (
    p_credit_id IN NUMBER,
    p_turnover_type_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN XMLTYPE
  IS
    v_tag_name VARCHAR2(50 char);
    v_xml xmltype;
  BEGIN
    SELECT CASE
             WHEN p_turnover_type_id = c_tt_issue_debt THEN 
               'debt'
             WHEN p_turnover_type_id = c_tt_issue_interest THEN 
               'interest'
             ELSE null
           END
      INTO v_tag_name
      FROM dual;  
  
    BEGIN
      SELECT xmlelement(evalname(v_tag_name),
               xmlelement("amount", ltrim(to_char(t.amount, c_number_format, c_nls_numeric_characters))),
               xmlelement("amount_currency", ltrim(to_char(t.amount_currency, c_number_format, c_nls_numeric_characters)))
             )
        INTO v_xml
        FROM core.turnover t
       WHERE t.credit_id = p_credit_id
         AND t.type_id = p_turnover_type_id
         AND t.rep_date = p_report_date;
    EXCEPTION
      WHEN no_data_found THEN
        SELECT xmlelement(evalname(v_tag_name), null)
          INTO v_xml
          FROM dual;
    END;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_credit_flow_xml
  (
    p_credit_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement("credit_flow",
               get_ref_classification_xml(cf.classification_id, p_report_date),
               xmlelement("provision",
                 -- PROVISION_KFN
                 get_debt_remains_xml(p_credit_id, c_drt_provision_kfn, p_report_date),
                 -- PROVISION_MSFO
                 get_debt_remains_xml(p_credit_id, c_drt_provision_msfo, p_report_date),
                 -- PROVISION_MSFO_OVER_BALANCE
                 get_debt_remains_xml(p_credit_id, c_drt_provision_msfo_ob, p_report_date)
               )
             )
        INTO v_xml
        FROM core.credit_flow cf
       WHERE cf.credit_id = p_credit_id
         AND cf.rep_date = p_report_date;
    EXCEPTION
      WHEN no_data_found THEN
        SELECT xmlelement("credit_flow",
                 xmlelement("classification", null),
                 xmlelement("provision",
                   -- PROVISION_KFN
                   get_debt_remains_xml(p_credit_id, c_drt_provision_kfn, p_report_date),
                   -- PROVISION_MSFO
                   get_debt_remains_xml(p_credit_id, c_drt_provision_msfo, p_report_date),
                   -- PROVISION_MSFO_OVER_BALANCE
                   get_debt_remains_xml(p_credit_id, c_drt_provision_msfo_ob, p_report_date)
                 )
               )
          INTO v_xml
          FROM dual;
    END;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_organization_xml
  (
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'organization'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_organization_id IS NOT NULL) THEN
      BEGIN
        SELECT xmlelement(evalname(p_tag_name),
                 -- ADDRESSES
                 get_addresses_xml(null, voh.id, p_report_date),
                 -- BANK_RELATIONS
                 get_bank_relations_xml(null, voh.id, p_report_date),
                 -- CONTACTS
                 get_contacts_xml(null, voh.id, p_report_date),
                 -- COUNTRY
                 get_ref_country_xml(voh.country_id, p_report_date),
                 -- DOCUMENTS
                 get_documents_xml(null, voh.id, p_report_date),
                 -- ECON_TRADE
                 get_ref_econ_trade_xml(voh.econ_trade_id, p_report_date),
                 -- ENTERPRISE_TYPE
                 get_ref_enterprise_type_xml(voh.enterprise_type_id, p_report_date),
                 -- HEAD
                 get_person_xml(voh.head_id, p_report_date, 'head', 1),
                 -- IS_SE
                 xmlelement("is_se", decode(voh.is_se, 1, 'true', 'false')),
                 -- LEGAL_FORM
                 get_ref_legal_form_xml(voh.legal_form_id, p_report_date),
                 -- NAMES
                 get_organization_names_xml(voh.id, p_report_date),
                 -- OFFSHORE
                 get_ref_offshore_xml(voh.offshore_id, p_report_date)
               )
          INTO v_xml
          FROM core.v_organization_his voh
         WHERE voh.id = p_organization_id
           AND voh.open_date <= p_report_date
           AND (voh.close_date > p_report_date OR voh.close_date is null);
      EXCEPTION
        WHEN no_data_found THEN
          v_xml := null;
      END;
    END IF;
        
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
      
    RETURN v_xml;
  END;
  
  FUNCTION get_person_xml
  (
    p_person_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'person',
    p_type IN NUMBER DEFAULT 0 -- 0 - PERSON, 1 - HEAD
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    IF (p_person_id IS NOT NULL) THEN
      BEGIN
        IF (p_type = 0) THEN
          SELECT xmlelement(evalname(p_tag_name),
                   -- ADDRESSES
                   get_addresses_xml(vph.id, null, p_report_date),
                   -- BANK_RELATIONS
                   get_bank_relations_xml(vph.id, null, p_report_date),
                   -- CONTACTS
                   get_contacts_xml(vph.id, null, p_report_date),
                   -- COUNTRY
                   get_ref_country_xml(vph.country_id, p_report_date),
                   -- DOCUMENTS
                   get_documents_xml(vph.id, null, p_report_date),
                   -- NAMES
                   get_person_names_xml(vph.id, p_report_date),
                   -- OFFSHORE
                   get_ref_offshore_xml(vph.offshore_id, p_report_date)
                 )
            INTO v_xml
            FROM core.v_person_his vph
           WHERE vph.id = p_person_id
             AND vph.open_date <= p_report_date
             AND (vph.close_date > p_report_date OR vph.close_date is null);
        ELSE
          SELECT xmlelement(evalname(p_tag_name),
                   -- DOCUMENTS
                   get_documents_xml(vph.id, null, p_report_date),
                   -- NAMES
                   get_person_names_xml(vph.id, p_report_date)
                 )
            INTO v_xml
            FROM core.v_person_his vph
           WHERE vph.id = p_person_id
             AND vph.open_date <= p_report_date
             AND (vph.close_date > p_report_date OR vph.close_date is null);
        END IF;
      EXCEPTION
        WHEN no_data_found THEN
          v_xml := null;
      END;
    END IF;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_documents_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'docs'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- DOC_TYPE
                   get_ref_doc_type_xml(vddh.type_id, p_report_date),
                   -- NO
                   xmlelement("no", vddh.no_)
                 )
               )
             )
        INTO v_xml
        FROM core.v_debtor_doc_his vddh
       WHERE ((vddh.person_id = p_person_id AND vddh.org_id IS NULL)
          OR (vddh.org_id = p_organization_id AND vddh.person_id IS NULL))
         AND vddh.open_date <= p_report_date
         AND (vddh.close_date > p_report_date OR vddh.close_date is null);
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_contacts_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'contacts'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- CONTACT_TYPE
                   get_ref_contact_type_xml(c.type_id, p_report_date, 'contact_type'),
                   -- DETAILS
                   xmlelement("details", xmlelement("item", c.details))
                 )
               )
             )
        INTO v_xml
        FROM core.contact c
       WHERE ((c.person_id = p_person_id AND c.org_id IS NULL)
          OR (c.org_id = p_organization_id AND c.person_id IS NULL));
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_person_names_xml
  (
    p_person_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'names'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- FIRSTNAME
                   xmlelement("firstname", vpnh.first_name),
                   -- LASTNAME
                   xmlelement("lastname", vpnh.last_name),
                   -- MIDDLENAME
                   xmlelement("middlename", vpnh.middle_name),
                   -- CODE
                   xmlelement("lang", ref_l.code)
                 )
               )
             )
        INTO v_xml
        FROM core.v_person_name_his vpnh,
             ref.shared ref_l
       WHERE vpnh.person_id = p_person_id
         AND vpnh.lang_id = ref_l.id (+)
         AND vpnh.open_date <= p_report_date
         AND (vpnh.close_date > p_report_date OR vpnh.close_date is null);
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;

  FUNCTION get_organization_names_xml
  (
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'names'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- NAME
                   xmlelement("name", vonh.name),
                   -- CODE
                   xmlelement("lang", ref_l.code)
                 )
               )
             )
        INTO v_xml
        FROM core.v_org_name_his vonh,
             ref.shared ref_l
       WHERE vonh.org_id = p_organization_id
         AND vonh.lang_id = ref_l.id (+)
         AND vonh.open_date <= p_report_date
         AND (vonh.close_date > p_report_date OR vonh.close_date is null);
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_addresses_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'addresses'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- DETAILS
                   xmlelement("details", a.details),
                   -- REGION
                   get_ref_region_xml(a.region_id, p_report_date),
                   -- TYPE
                   xmlelement("type", ref_at.code)
                 )
               )
             )
        INTO v_xml
        FROM core.address a,
             ref.shared ref_at
       WHERE a.type_id = ref_at.id
         AND ((a.person_id = p_person_id AND a.org_id IS NULL)
          OR (a.org_id = p_organization_id AND a.person_id IS NULL));
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_bank_relations_xml
  (
    p_person_id IN NUMBER,
    p_organization_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'bank_relations'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- BANK_RELATION
                   get_ref_bank_relation_xml(di.bank_relation_id, p_report_date),
                   -- CREDITOR
                   get_ref_creditor_xml(di.creditor_id, p_report_date, 'creditor')
                 )
               )
             )
        INTO v_xml
        FROM core.debtor_info di
       WHERE ((di.person_id = p_person_id AND di.org_id IS NULL)
          OR (di.org_id = p_organization_id AND di.person_id IS NULL))
         AND di.open_date <= p_report_date
         AND (di.close_date > p_report_date OR di.close_date is null);
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
  FUNCTION get_pledges_xml
  (
    p_credit_id IN NUMBER,
    p_report_date IN DATE,
    p_tag_name IN VARCHAR2 DEFAULT 'pledges'
  ) RETURN XMLTYPE
  IS
    v_xml xmltype;
  BEGIN
    BEGIN
      SELECT xmlelement(evalname(p_tag_name),
               xmlagg(
                 -- ITEM
                 xmlelement("item",
                   -- CONTRACT
                   xmlelement("contract", vph.contract_no),
                   -- PLEDGE_TYPE
                   get_ref_pledge_type_xml(vph.type_id, p_report_date),

                   -- VALUE
                   xmlelement("value", ltrim(to_char(vph.value_, c_number_format, c_nls_numeric_characters)))
                 )
               )
             )
        INTO v_xml
        FROM core.v_pledge_his vph
       WHERE vph.credit_id = p_credit_id
         AND vph.open_date <= p_report_date
         AND (vph.close_date > p_report_date OR vph.close_date is null);
    EXCEPTION
      WHEN no_data_found THEN
        v_xml := null;
    END;
    
    IF (v_xml IS NULL) THEN
      SELECT xmlelement(evalname(p_tag_name), null)
        INTO v_xml
        FROM dual;
    END IF;
          
    RETURN v_xml;
  END;
  
end PKG_EAV_XML_UTIL;
/
