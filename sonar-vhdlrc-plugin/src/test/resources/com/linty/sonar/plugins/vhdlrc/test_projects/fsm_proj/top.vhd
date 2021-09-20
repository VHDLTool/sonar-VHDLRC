--CODE
-------------------------------------------------------------------------------------------------
-- Company   : CNES
-- Author    : Mickael Carl (CNES)
-- Copyright : Copyright (c) CNES.
-- Licensing : GNU GPLv3
-------------------------------------------------------------------------------------------------
-- Version         : V1
-- Version history :
--    V1 : 2015-04-02 : Mickael Carl (CNES): Creation
-------------------------------------------------------------------------------------------------
-- File name          : STD_02200_good.vhd
-- File Creation date : 2015-04-02
-- Project name       : VHDL Handbook CNES Edition
-------------------------------------------------------------------------------------------------
-- Softwares             :  Microsoft Windows (Windows 7) - Editor (Eclipse + VEditor)
-------------------------------------------------------------------------------------------------
-- Description: Handbook example: Version control in header of file: good example
--
-- Limitations : This file is an example of the VHDL handbook made by CNES. It is a stub aimed at
--               demonstrating good practices in VHDL and as such, its design is minimalistic.
--               It is provided as is, without any warranty.
--
-------------------------------------------------------------------------------------------------
-- Naming conventions:
--
-- i_Port: Input entity port
-- o_Port: Output entity port
-- b_Port: Bidirectional entity port
-- g_My_Generic: Generic entity port
--
-- c_My_Constant: Constant definition
-- t_My_Type: Custom type definition
--
-- My_Signal_n: Active low signal
-- v_My_Variable: Variable
-- sm_My_Signal: FSM signal
-- pkg_Param: Element Param coming from a package
--
-- My_Signal_re: Rising edge detection of My_Signal
-- My_Signal_fe: Falling edge detection of My_Signal
-- My_Signal_rX: X times registered My_Signal signal
--
-- P_Process_Name: Process
--
-------------------------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity top is
end top;

architecture rtl of top is

component mealy_4s is -- procedure

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component mealy_4s_badname is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component mealy_4s_std is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component moore_4s is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component moore_4s_badname is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component safe_state is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component safe_state_badname is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );
end component;

component user_encod is

  port
  (
    updown  : in std_logic;
    clock   : in std_logic;
    lsb    : out std_logic;
    msb    : out std_logic
  );

end component;

component user_encod_badname is

  port
  (
    updown  : in std_logic;
    clock   : in std_logic;
    lsb    : out std_logic;
    msb    : out std_logic
  );

end component;



signal clk,data_in,reset : std_logic;
begin

inst:mealy_4s port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst2:moore_4s port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst3:safe_state port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst4:user_encod port map(
clock=>clk,
updown=>data_in
);

inst5:mealy_4s_std port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst6:mealy_4s_badname port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst7:moore_4s_badname port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst8:safe_state_badname port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst9:user_encod_badname port map(
clock=>clk,
updown=>data_in
);

end rtl;