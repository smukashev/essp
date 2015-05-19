create or replace procedure                                                                                               REPORT_USERS_EMAIL_ADDRESSES(p_cur        out utility.TCursor,
                                                         p_user_id    in number,
                                                         p_has_access in number) is
begin
  open p_cur for
    select pu.last_name || ' ' || pu.first_name || ' ' || pu.middle_name as fio,
           (case
             when count(puc.creditor_id) > 1 then
              'Несколько организаций'
             when count(puc.creditor_id) = 0 then
              'Нет доступа к организациям'
             else
              (select cr.name
                 from CORE.eav_a_creditor_user puc2, CORE.r_ref_creditor cr
                where puc2.user_id = pu.user_id
                  and puc2.creditor_id = cr.id)
           end) as creditor_name,
           pu.email

      from CORE.EAV_A_USER pu, CORE.EAV_A_CREDITOR_USER puc
     where pu.user_id = puc.user_id
     and pu.user_id = p_user_id
     and pu.is_active = 1
     group by pu.id,
              pu.last_name,
              pu.first_name,
              pu.middle_name,
              pu.email
     order by fio;
end REPORT_USERS_EMAIL_ADDRESSES;