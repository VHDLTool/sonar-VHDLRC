----------------------------------------------------------------------------------
-- Company: CNES
-- Student: T.VANDERMEIRSSCHE 
-- 
-- Create Date:    02/15/2017 
-- Design Name: 
-- Module Name:    hex_to_ascii - Behavioral 
-- Project Name: 	 Boitier_USB2-Generik-USER_IP
-- Target Devices: XC3S400A ou XC3S200A
-- Tool versions:  ISE 14.7
-- Description:    module de conversion hexa to ascii combinatoire
-- Revision
-- 15/02/2017 (v1.0)		: Creation du fichier
----------------------------------------------------------------------------------
entity STD_04200_good is
   generic (g_Width : positive := 4);
   port (
      -- A clock domain (Source)
      i_ClockA     : in  std_logic;     -- First clock signal
      i_ResetA_n   : in  std_logic;     -- Reset signal
      i_Data       : in  std_logic_vector(g_Width-1 downto 0);  -- Data from source
      i_Request    : in  std_logic;     -- Request from source
      o_Grant      : out std_logic;     -- Acknowledge synced to source
      -- B clock domain (Destination)
      i_ClockB     : in  std_logic;     -- Second clock signal
      i_ResetB_n   : in  std_logic;     -- Reset signal
      o_Data       : out std_logic_vector(g_Width-1 downto 0);  -- Data to destination
      o_Request_r2 : out std_logic;     -- Request synced to destination
      i_Grant      : in  std_logic      -- Acknowledge from destination

      );
end STD_04200_good;

architecture Behavioral of STD_04200_good is
	signal Request_r1 : std_logic;       -- Starts with tab
	signal Request_r2 : std_logic;       -- Starts with tab
	signal Grant_r1   : std_logic;       -- Starts with tab
   signal Grant_r2   : std_logic;       -- Grant signal registered 2 times
begin
   P_Source_Domain : process(i_ResetA_n, i_ClockA)
   begin
      if (i_ResetA_n = '0') then
         Grant_r1 <= '0';
         Grant_r2 <= '0';
      elsif (rising_edge(i_ClockA)) then
            -- Synchronize i_Grant to i_ClockA domain
            Grant_r1 <= i_Grant;
            Grant_r2 <= Grant_r1;
      end if;
   end process;

   P_Destination_Domain : process(i_ResetB_n, i_ClockB)
   begin
      if (i_ResetB_n = '0') then
         Request_r1 <= '0';
         Request_r2 <= '0';
      elsif (rising_edge(i_ClockB)) then
            -- Synchronize i_Request to i_ClockB domain
            -- Data is valid when Request_r2 is asserted
            Request_r1 <= i_Request;
            Request_r2 <= Request_r1;
      end if;
   end process;

   o_Request_r2 <= Request_r2;
   o_Data       <= i_Data;
   o_Grant      <= Grant_r2;
end Behavioral;