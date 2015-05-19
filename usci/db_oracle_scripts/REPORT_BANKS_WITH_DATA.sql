create or replace procedure     REPORT_BANKS_WITH_DATA(p_cur        OUT utility.TCursor,
                                                   p_user_id    IN NUMBER,
                                                   p_has_access in NUMBER,
                                                   --   first_row_num in number,
                                                   -- last_row_num  in number,
                                                   p_report_date IN DATE) is
begin
  open p_cur for
    select r.id           AS "ID",
           c.name         AS "NAME",
           r.total_count  as "TOTAL-COUNT",
           r.actual_count as "ACTUAL-COUNT",
           r.beg_date     as "BEGIN-DATE",
           r.end_date     as "END-DATE",
           REPORTER.GET_REPORT_STATUS_CODE(r.status_id)         as "STATUS-CODE",
           REPORTER.GET_REPORT_STATUS_NAME_RU(r.status_id)      as "STATUS-NAME",
           c.id           as "CREDITOR-ID",
           r.username     as "USERNAME"
      from CORE.R_REF_CREDITOR              c,
           CORE.EAV_REPORT                  r,
           CORE.EAV_A_USER          pu,
           CORE.EAV_A_CREDITOR_USER puc
     where c.id = r.creditor_id
       and r.report_date = p_report_date
       and pu.user_id = p_user_id
       and pu.USER_ID = puc.USER_ID
       and puc.creditor_id = c.id
     order by decode(REPORTER.GET_REPORT_STATUS_CODE(r.status_id),
                     'ORGANIZATION_APPROVED',
                     0,
                     'WOE',
                     1,
                     'RECIPIENCY_COMPLETED',
                     2,
                     'WE',
                     3,
                     'RECIPIENCY_IN_PROGRESS',
                     4,
                     100),
              c.name;
end REPORT_BANKS_WITH_DATA;

