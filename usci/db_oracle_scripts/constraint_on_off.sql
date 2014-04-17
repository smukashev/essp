DECLARE
  p_schema_name VARCHAR2(100 CHAR) := 'CORE';
  p_action      VARCHAR2(3 CHAR) := 'OFF'; -- OFF/ON
  v_sql      VARCHAR2(4000);
  v_key_name VARCHAR2(4000);
  v_action   VARCHAR2(4000);
  CURSOR alter_cursor IS
    SELECT 'ALTER TABLE ' || OWNER || '.' || TABLE_NAME || ' ' ||
           v_action || ' CONSTRAINT ' || CONSTRAINT_NAME AS sql_string,
           CONSTRAINT_NAME
      FROM ALL_CONSTRAINTS
     WHERE CONSTRAINT_TYPE = 'R'
       AND OWNER = p_schema_name;
BEGIN
  IF UPPER(p_action) = 'ON' THEN
    v_action := 'ENABLE';
  ELSE
    v_action := 'DISABLE';
  END IF;
  OPEN alter_cursor;
  LOOP
    FETCH alter_cursor
      INTO v_sql, v_key_name;
    EXIT WHEN alter_cursor%NOTFOUND;
    EXECUTE IMMEDIATE v_sql;
  END LOOP;
EXCEPTION
  WHEN OTHERS THEN
    BEGIN
      DBMS_OUTPUT.PUT_LINE(SQLERRM);
    END;
    CLOSE alter_cursor;
END;