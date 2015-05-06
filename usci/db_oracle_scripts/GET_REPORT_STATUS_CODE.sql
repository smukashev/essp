CREATE OR REPLACE FUNCTION GET_REPORT_STATUS_CODE (status_id in NUMBER) RETURN VARCHAR2 AS
BEGIN
  IF status_id = 90 then
    return 'RECIPIENCY_IN_PROGRESS';
  ELSIF status_id = 91 then
    return 'CROSS_CHECK_ERROR';
  ELSIF status_id = 92 then
    return 'RECIPIENCY_COMPLETED';
  ELSIF status_id = 74 then
    return 'IR';
  ELSIF status_id = 75 then
    return 'CR';
  ELSIF status_id = 76 then
    return 'WE';
  ELSIF status_id = 77 then
    return 'WOE';
  ELSIF status_id = 128 then
    return 'ORGANIZATION_APPROVED';
  END IF;
END GET_REPORT_STATUS_CODE;