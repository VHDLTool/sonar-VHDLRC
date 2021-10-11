library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity STD_05100_bad is
	port (
			sin : in    std_logic;

            sin_meta_l0: in    std_logic;
            sin_meta_l1: in    std_logic;
            sin_meta_l2: in    std_logic;
            clk : in    std_logic;
			dout : out    std_logic
         );
end STD_05100_bad;

architecture Behavioral of STD_05100_bad is
signal sin_ff,sin_ff2,sin_ff3 : std_logic;
signal sin_meta_l1_ff,sin_meta_l1_ff2,sin_meta_l1_ff3 : std_logic;
signal sin_meta_l2_ff,sin_meta_l2_ff2,sin_meta_l2_ff3 : std_logic;
begin

--good example
process (clk)
begin
   if rising_edge(clk) then
      sin_ff  <= sin;
      sin_ff2 <= sin_ff;
   end if;
end process;

--error step 0 see dout

--error step1
process (clk)
begin
   if rising_edge(clk) then
      sin_meta_l1_ff  <= sin_meta_l1;
      sin_meta_l1_ff2 <= sin_meta_l1_ff;
   end if;
end process; 

--error step2
process (clk)
begin
   if rising_edge(clk) then
      sin_meta_l2_ff  <= sin_meta_l2;
      sin_meta_l2_ff2 <= sin_meta_l2_ff;
   end if;
end process; 




dout<= (sin_ff2 and sin_meta_l0) or (sin_meta_l1_ff and sin_meta_l1_ff2);


end Behavioral;
