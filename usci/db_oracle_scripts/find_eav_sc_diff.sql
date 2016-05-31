insert into oper_sc(entity_id, report_date)
select entity_id, report_date from (
select ebe.id entity_id, rd.report_date report_date
from eav_be_entities ebe
join eav_be_entity_report_dates rd on rd.entity_id = ebe.id
where ebe.class_id = 59)
left outer join (select credit_id, open_date from r_core_credit@showcase union select credit_id, open_date from r_core_credit_his@showcase) credits on entity_id = credits.credit_id and report_date = credits.open_date
where credit_id is null or open_date is null;
