-------------------------------------------------------------------------------------------------
-- Company   : CNES
-- Author    : Mickael Carl (CNES)
-- Copyright : Copyright (c) CNES. 
-- Licensing : GNU GPLv3
-------------------------------------------------------------------------------------------------
-- Version         : V1
-- Version history : 
--    V1 : 2015-04-20 : Mickael Carl (CNES): Creation
-------------------------------------------------------------------------------------------------
-- File name          : CNE_04700_good.vhd
-- File Creation date : 2015-04-20
-- Project name       : VHDL Handbook CNES Edition 
-------------------------------------------------------------------------------------------------
-- Softwares             :  Microsoft Windows (Windows 7) - Editor (Eclipse + VEditor)
-------------------------------------------------------------------------------------------------
-- Description : Handbook example: Finite State Machine single process based: good example
--
-- Limitations : This file is an example of the VHDL handbook made by CNES. It is a stub aimed at
--               demonstrating good practices in VHDL and as such, its design is minimalistic.
--               It is provided as is, without any warranty.
--               This example is compliant with the Handbook version 1.
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

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

entity CNE_04700_good is
   port  (
      i_Clock     : in std_logic;   -- Clock input
      i_Reset_n   : in std_logic;   -- Reset input
      i_Start     : in std_logic;   -- Start counter signal
      i_Stop      : in std_logic    -- Stop counter signal
   );
end CNE_04700_good;

--CODE
architecture Behavioral of CNE_04700_good is
   constant c_Length : std_logic_vector(3 downto 0) := (others => '1'); -- How long we should count
   type t_state      is (init, loading, enabled, finished);             -- Enumerated type for state encoding
   signal sm_State   : t_state;                                         -- sm_State signal
   signal Raz        : std_logic;                                       -- Load the length value and initialize the counter
   signal Enable     : std_logic;                                       -- Counter enable signal
   signal Length     : std_logic_vector(3 downto 0);                    -- Counter length for counting
   signal End_Count  : std_logic;                                       -- End signal of counter
begin
   -- A simple counter with loading length and enable signal
   Counter:Counter
   port map (
      i_Clock     => i_Clock,
      i_Reset_n   => i_Reset_n,
      i_Raz       => Raz,
      i_Enable    => Enable,
      i_Length    => Length,
      o_Done      => End_Count
   );
   
   -- FSM process controlling the counter. Start or stop it in function of the input (i_Start & i_Stop), 
   -- load the length value, and wait for it to finish
   P_FSM:process(i_Reset_n, i_Clock)
   begin
      if (i_Reset_n='0') then
         sm_State <= init;
      elsif (rising_edge(i_Clock)) then
            -- Set the length value
            Length <= c_Length;
            case sm_State is
               when init => 
                  Raz <= '0';
                  Enable <= '0';
                  sm_State <= loading;
               when loading =>
               -- Load the counter and initialize it
                  Raz <= '1';
                  sm_State <= enabled;
               when enabled =>
               -- Start or stop counting depending on inputs until it finishes
                  Raz <= '0';
                  if (End_Count='0') then
                     Enable <= i_Start xor not i_Stop;
                     sm_State  <= Enabled;
                  else
                     Enable <= '0';
                     sm_State <= finished;
                  end if;
               when others =>
                  sm_State <= init;
            end case;
      end if;
   end process;
end Behavioral;
--CODE