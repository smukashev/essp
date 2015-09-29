 declare
 l_errors  NUMBER;
 begin

begin
DBMS_REDEFINITION.can_redef_table(USER, 'EAV_BE_ENTITY_REPORT_DATES');

exception when others then
  dbms_redefinition.abort_redef_table(uname      => USER,
    orig_table => 'EAV_BE_ENTITY_REPORT_DATES',
    int_table  => 'EAV_BE_ENTITY_REPORT_DATES_NEW');

end;

DBMS_REDEFINITION.can_redef_table(USER, 'EAV_BE_ENTITY_REPORT_DATES');

execute immediate 'create table EAV_BE_ENTITY_REPORT_DATES_NEW
(
  id                   NUMBER(14),
  entity_id            NUMBER(14),
  report_date          DATE,
  integer_values_count NUMBER(14),
  date_values_count    NUMBER(14),
  string_values_count  NUMBER(14),
  boolean_values_count NUMBER(14),
  double_values_count  NUMBER(14),
  complex_values_count NUMBER(14),
  simple_sets_count    NUMBER(14),
  complex_sets_count   NUMBER(14),
  is_closed            NUMBER(1)
)
Partition by range(report_date) (
Partition p30 values less than (to_date(''01.04.2013'', ''dd.mm.yyyy'')),
Partition p29 values less than (to_date(''01.05.2013'', ''dd.mm.yyyy'')),
Partition p28 values less than (to_date(''01.06.2013'', ''dd.mm.yyyy'')),
Partition p27 values less than (to_date(''01.07.2013'', ''dd.mm.yyyy'')),
Partition p26 values less than (to_date(''01.08.2013'', ''dd.mm.yyyy'')),
Partition p25 values less than (to_date(''01.09.2013'', ''dd.mm.yyyy'')),
Partition p24 values less than (to_date(''01.10.2013'', ''dd.mm.yyyy'')),
Partition p23 values less than (to_date(''01.11.2013'', ''dd.mm.yyyy'')),
Partition p22 values less than (to_date(''01.12.2013'', ''dd.mm.yyyy'')),
Partition p21 values less than (to_date(''01.01.2014'', ''dd.mm.yyyy'')),
Partition p20 values less than (to_date(''01.02.2014'', ''dd.mm.yyyy'')),
Partition p19 values less than (to_date(''01.03.2014'', ''dd.mm.yyyy'')),
Partition p18 values less than (to_date(''01.04.2014'', ''dd.mm.yyyy'')),
Partition p17 values less than (to_date(''01.05.2014'', ''dd.mm.yyyy'')),
Partition p16 values less than (to_date(''01.06.2014'', ''dd.mm.yyyy'')),
Partition p15 values less than (to_date(''01.07.2014'', ''dd.mm.yyyy'')),
Partition p14 values less than (to_date(''01.08.2014'', ''dd.mm.yyyy'')),
Partition p13 values less than (to_date(''01.09.2014'', ''dd.mm.yyyy'')),
Partition p12 values less than (to_date(''01.10.2014'', ''dd.mm.yyyy'')),
Partition p11 values less than (to_date(''01.11.2014'', ''dd.mm.yyyy'')),
Partition p10 values less than (to_date(''01.12.2014'', ''dd.mm.yyyy'')),
Partition p9 values less than (to_date(''01.01.2015'', ''dd.mm.yyyy'')),
Partition p8 values less than (to_date(''01.02.2015'', ''dd.mm.yyyy'')),
Partition p7 values less than (to_date(''01.03.2015'', ''dd.mm.yyyy'')),
Partition p6 values less than (to_date(''01.04.2015'', ''dd.mm.yyyy'')),
Partition p5 values less than (to_date(''01.05.2015'', ''dd.mm.yyyy'')),
Partition p4 values less than (to_date(''01.06.2015'', ''dd.mm.yyyy'')),
Partition p3 values less than (to_date(''01.07.2015'', ''dd.mm.yyyy'')),
Partition p2 values less than (to_date(''01.08.2015'', ''dd.mm.yyyy'')),
Partition p1 values less than (to_date(''01.09.2015'', ''dd.mm.yyyy'')))';

 DBMS_REDEFINITION.start_redef_table(
    uname      => USER,
    orig_table => 'EAV_BE_ENTITY_REPORT_DATES',
    int_table  => 'EAV_BE_ENTITY_REPORT_DATES_NEW');


  dbms_redefinition.sync_interim_table(
    uname      => USER,
    orig_table => 'EAV_BE_ENTITY_REPORT_DATES',
    int_table  => 'EAV_BE_ENTITY_REPORT_DATES_NEW');

DBMS_REDEFINITION.copy_table_dependents(
    uname            => USER,
    orig_table       => 'EAV_BE_ENTITY_REPORT_DATES',
    int_table        => 'EAV_BE_ENTITY_REPORT_DATES_NEW',
    copy_indexes     => DBMS_REDEFINITION.cons_orig_params,
    copy_triggers    => TRUE,
    copy_constraints => TRUE,
    copy_privileges  => TRUE,
    ignore_errors    => FALSE,
    num_errors       => l_errors,
    copy_statistics  => FALSE,
    copy_mvlog       => FALSE);

  DBMS_OUTPUT.put_line('Errors=' || l_errors);

DBMS_STATS.gather_table_stats(USER, 'EAV_BE_ENTITY_REPORT_DATES_NEW', cascade => TRUE);

 dbms_redefinition.finish_redef_table(
    uname      => USER,
    orig_table => 'EAV_BE_ENTITY_REPORT_DATES',
    int_table  => 'EAV_BE_ENTITY_REPORT_DATES_NEW');

execute immediate 'DROP TABLE EAV_BE_ENTITY_REPORT_DATES_NEW';

DBMS_STATS.gather_table_stats(USER, 'EAV_BE_ENTITY_REPORT_DATES', cascade => TRUE);
 end;