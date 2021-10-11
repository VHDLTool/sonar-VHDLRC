architecture rtl of test6900 is

procedure DISPLAY_MUX
 (ALARM_TIME, CURRENT_TIME : in digit;
  SHOW_A              : in std_ulogic;
  signal DISPLAY_TIME : out digit) is 
begin
  if (SHOW_A = '1') then
    DISPLAY_TIME <= ALARM_TIME;  -- procedure
  else
    DISPLAY_TIME <= CURRENT_TIME;
  end if;
end DISPLAY_MUX;

end rtl;