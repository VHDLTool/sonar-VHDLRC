--************************************************************************************--
--                                                                                    --
-- M MMMM   MMMM       MMMMM     M  MMM     MMMMM    M       M  M  MMM  M           M --
-- M      M      M   M       M   MM       M       M  M       M  MM       M         M  --
-- M      M      M  M         M  M       M           M       M  M         M       M   --
-- M      M      M  MMMMMMMMMMM  M       M           M       M  M          M     M    --
-- M      M      M  M            M       M           M       M  M           M   M     --
-- M      M      M   M       M   M        M       M  M       M  M            M M      --
-- M      M      M     MMMMM     M          MMMMM      MMMM  M  M             M       --
--                                                                           M        --
--                                     M                                 MMM          --
--                MMM  M      M  MMM  MMMM   MMM  MMMM MMM   MMM                      --
--               M      M    M  M      M    M   M M   M   M M                         --
--                MMM    M  M    MMM   M    MMMMm M   M   M  MMM                      --
--                   M    MM        M  M    M     M   M   M     M  MMM MMM            --
--                MMM     M      MMM    MMM  MMM  M   M   M  MMM    M  MMM            --
--                     MM                                                             --
--                                                                                    --
--                               http://www.mrcy.com                                  --
--************************************************************************************--
--**********************           COPYRIGHT NOTICE     ******************************--
--************************************************************************************--
--****              MERCURY MISSION SYSTEMS INTERNATIONAL S.A.                    ****--
--****            Copyright (C) 2018 MERCURY All Rights Reserved                  ****--
--**** -------------------------------------------------------------------------- ****--
--****   This file is owned and controlled by MERCURY and must be used solely     ****--
--****   for design, simulation, implementation and creation of design files      ****--
--****   limited to MERCURY technologies.                                         ****--
--****   Unauthorized copying of this file, via any medium is strictly prohibited.****--
--****   Use with non-MERCURY designs or technologies is strictly prohibited.     ****--
--************************************************************************************--
-- Author                  : PMS
--
-- Creation Date           : 27.12.2018
--
-- Last Modification Date  : 24.12.2018, 10:25:46
--
-- Design Name             : function_pkg
--
-- Project Name            : 
--
-- Current Revision        : 0.1
--
--************************************************************************************--
--
-- Description             : 
--
--************************************************************************************--
--
-- Revision history:
--====================================================================================--
--  Revision |  Date       |  Author |     Description
--====================================================================================--
--     0.1   | 27.12.2018  |   PMS   | Initial revision
--           |             |         | 
--************************************************************************************--

library ieee;
use     ieee.std_logic_1164.all;
use     ieee.numeric_std.all;

package function_pkg is

   function log2      (x : natural) return natural;
   function ceil_log2 (t : natural) return natural;

end;


package body function_pkg is

   --************************************************************************
   -- Comment line 1
   --   Comment line 2
   --************************************************************************
   function log2 (x : natural) return natural is 
   begin
      if x <= 1 then
         return 0;
      else
         return log2 (x / 2) + 1;
      end if;
   end function log2;


   --------------------------------------------------------------------------
   -- Comment 3
   -- (ceiling value) Comment 4
   -- If decimal part of result is non null, then adds 1; comment 5
   -- else returns the log2 value comment 6
   --------------------------------------------------------------------------
   function ceil_log2 (t : natural) return natural is
        variable v_ret:  natural;
----------------
-- Comment line 7
------------
    begin
      v_ret := integer(log2(t));
      if (t > (2**v_ret)) then
         return(v_ret + 1); -- comment 8
      else -- comment 9
         return(v_ret); --****___
		 --
		 ----- Comment line 10
      end if;
   end function ceil_log2;

end;
