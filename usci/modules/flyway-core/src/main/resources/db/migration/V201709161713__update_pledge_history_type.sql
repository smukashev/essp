UPDATE EAV_M_COMPLEX_SET
SET HISTORY_TYPE = 2
WHERE CLASS_ID IN (SELECT ID
                   FROM EAV_M_CLASSES
                   WHERE name LIKE 'pledge');

