CREATE OR REPLACE TRIGGER trigger_name
AFTER INSERT
   ON AUDIT_AUDITEVENT
BEGIN
  delete from AUDIT_AUDITEVENT where username like '%Бексултан Аканов%';
END;