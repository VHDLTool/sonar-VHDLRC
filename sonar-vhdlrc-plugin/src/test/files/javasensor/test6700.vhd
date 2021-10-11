architecture behave of test6700 is
   
begin

  p_CLK : process --wait
  begin
    r_CLK_TB <= not(r_CLK_TB);
    wait for c_CLK_PERIOD/2; 
  end process p_CLK;

end behave;