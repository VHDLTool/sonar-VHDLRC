library ieee;
	use ieee.std_logic_1164.all;
	use ieee.std_logic_unsigned.all;
 
entity read_write is
 	port(i_clk : in std_logic; --clock signal
		 i_rst : in std_logic; --reset
		 i_wr : in std_logic; --write or read from sequencer
		 i_enable : in std_logic; --enable signal from sequencer
		 
 	     o_vz_emi : out std_logic; --vz signal to emission module
		 o_wr1 : out std_logic;  --write or read to DPRAM
		 o_cs1 : out std_logic; --chip select to DPRAM
		 o_done : out std_logic; --done signal to sequencer
		 o_addr1_0 : out std_logic ); --address LSB to DPRAM
end read_write;

architecture rtl of read_write is

type state_type is (
	init,
	lecture,
	emission,
	ecriture,
	impl,
	fin
);

attribute state_vector : string;
attribute state_vector of rtl : architecture is "current_state";

signal current_state : state_type;
signal next_state : state_type;

signal cpt : integer;



begin

--Process registration
clocked_proc : process(i_clk, i_rst)
begin
if (i_rst = '1') then
	current_state <= init;
elsif (i_clk'event and i_clk='1') then
	current_state <= next_state;
end if;
end process clocked_proc;


--Next state process
nextstate_proc : process(current_state, i_enable, i_wr, cpt)
begin

o_done <= '0';
o_addr1_0 <= '0';
o_cs1 <= '0';
o_vz_emi <= '0';
o_wr1<= '0';

case current_state is
	when init => 
		cpt <= 0;
		if (i_enable='1' and i_wr='1') then
			next_state <= ecriture;
		elsif (i_enable='1' and i_wr='0') then
			next_state <= lecture;
		else
			next_state <= init;
		end if;
	when ecriture =>
		if (cpt=0) then
			o_addr1_0 <= '0';
			next_state <= impl;			
		else
			o_addr1_0 <= '1';
			next_state <= fin;
		end if;
		o_wr1<= '1';
		o_cs1 <= '1';
	when lecture =>
		if (cpt=0) then
			o_addr1_0 <= '0';	
		else
			o_addr1_0 <= '1';
		end if;
		o_cs1 <= '1';
		o_vz_emi <= '1';
		next_state <= emission;
	when emission =>
		o_cs1 <= '1';
		if (cpt=0) then
			next_state <= impl;
			o_addr1_0 <= '0';
		else
			next_state <= fin;
			o_addr1_0 <= '1';
		end if;
	when impl =>
		o_addr1_0 <= '1';
		cpt <= 1;
		if (i_wr='1') then
			next_state <= ecriture;
		else 
			next_state <= lecture;
		end if;
	when fin =>
		o_done <= '1';
		next_state <= init;
	when others =>
		next_state <= init;
end case;
end process nextstate_proc;





end rtl;
		
		
		
		
	
		


		
		

	
