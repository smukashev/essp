begin
  delete from eav_a_creditor_user;

  insert into eav_a_creditor_user(user_id, creditor_id)
    select u.user_id, (select id from v_creditor_map where creditor_id = jt.creditor_id) as creditor_id
    from maintenance.portal_user@credits u,
      maintenance.portal_user_creditor@credits jt
    where u.id = jt.portal_user_id;
end;