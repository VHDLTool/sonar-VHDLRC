---------------------------------------------------------------------
-- File Downloaded from http://www.nandland.com
---------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
 
entity bad_latch is
  port (
    i_data   : in  std_logic;
    i_enable : in  std_logic;
    o_latch  : out std_logic
    );
end bad_latch;
 
architecture rtl of bad_latch is
begin
 
  process (i_data, i_enable)
  begin
    if i_enable = '1' then
      o_latch <= i_data;
    end if;
  end process;
 
end architecture rtl;
