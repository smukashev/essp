DECLARE
   v_fill_data number;
   v_fix_data number;
BEGIN
   v_fill_data := 1;
   v_fix_data := 1;

   --rule amount
   if v_fill_data = 1 THEN
      lx_amount_fill();
   end if;

   if v_fix_data = 1 THEN
      lx_amount_fix();
   end if;
   --end of rule amount


   -- add here another rulefix
   lx_pastdue_cd_rd_fill();
   --lx_pastdue_cd_rd_fix();

   lx_pastdue_od_vs_cd_fill();
   --lx_pastdue_od_vs_cd_fix();


end;

