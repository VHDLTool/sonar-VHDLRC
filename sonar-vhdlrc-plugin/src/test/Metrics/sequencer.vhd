library ieee;
	use ieee.std_logic_1164.all;
	use ieee.std_logic_unsigned.all;
	use IEEE.numeric_std.all;

entity sequencer is
 	port(i_clk : in std_logic; --clock signal
		 i_rst_n : in std_logic; --reset
		 i_vz_cmd : in std_logic; --vz signal for command
		 i_vz_param : in std_logic; --vz signal for parameter
		 i_done : in std_logic; --done signal from read_write block
		 i_cmd : in std_logic_vector(7 downto 0); --command from NINANO
		 i_size_data : in std_logic_vector(7 downto 0); --size of the data received

		 o_wr : out std_logic; --write or read to read_write block
		 o_enable : out std_logic; --enable signal to read_write block
 	    o_vz_ackno : out std_logic; --vz signal to emission module for acknoledge
		 o_sel : out std_logic_vector(6 downto 0); --select data and adress in Aiguillage block
		 o_cs_a : out std_logic; --chip select to register
		 o_size_read : out std_logic_vector(7 downto 0) ); --size of data to return
end sequencer;

architecture rtl of sequencer is

type state_type is (
	init,
	comm,
	selection,
	lecture,
	action,
	new_sel,
	ecriture,
	ackno,
	att_ec,
	att_le,
	att_a,
	aigui_le,
	aigui_ec
);


signal state : state_type;


signal compteur : std_logic_vector(7 downto 0);
signal size_data : std_logic_vector(7 downto 0);
signal cmd : std_logic_vector(7 downto 0);
signal sel : std_logic_vector(6 downto 0);
signal sel_comb : std_logic_vector(6 downto 0);
signal size_read_comb : std_logic_vector(7 downto 0);
signal nombre_le : std_logic_vector(7 downto 0);
signal nombre_ec : std_logic_vector(7 downto 0);

begin



--Machine d'�tat
out_state : process(i_clk, i_rst_n)
begin

if (i_rst_n = '0' or i_rst_n = '0') then

	o_wr <= '0';
	o_enable <= '0';
	o_vz_ackno <= '0';
	o_cs_a <= '0';
	compteur <= x"00";
	sel <= "0000000";
	nombre_ec <= x"00";
	nombre_le <= x"00";
	state <= init;
   cmd <= x"00";
   size_data <= x"00";

elsif (i_clk'event and i_clk='1') then

	o_wr <= '0';
	o_enable <= '0';
	o_vz_ackno <= '0';
	o_cs_a <= '0';

	case state is
		when init =>
			compteur <= x"00";
			sel <= "0000000";
			nombre_ec <= x"00";
			nombre_le <= x"00";
         size_data <= x"00";
			if (i_vz_cmd='1') then
				state <= comm;
			else
				state <= init;
			end if;
		when comm =>
			cmd <= i_cmd;
			if (i_vz_param='1') then
				state <= selection;
            size_data <= i_size_data;
			else
				state <= comm;
			end if;
		when selection =>
			nombre_ec <=  std_logic_vector(unsigned('0' & size_data(7 downto 1))-1);
			nombre_le <= '0' & size_read_comb(7 downto 1);
			sel <= sel_comb;
			if (sel(6 downto 5)="10") then
				state <= ecriture;
			elsif (sel(6 downto 5)="11") then
				state <= lecture;
			elsif (sel(6 downto 5)="01") then
				state <= action;
			else
				state <= selection; --ca ne doit pas arriver
			end if;
		when ecriture =>
			o_wr <= '1';
			o_enable <= '1';
			state <= att_ec;
			compteur <= compteur + 1;
		when att_ec =>
			o_wr <= '1';
			if (i_done = '0') then
				state <= att_ec;
			else
				state <= aigui_ec;
			end if;
		when aigui_ec =>
			if (compteur < nombre_ec) then
				state <= new_sel;
			else
				state <= ackno;
			end if;
		when lecture =>
			o_enable <= '1';
			state <= att_le;
			compteur <= compteur + '1';
		when att_le =>
			if (i_done = '0') then
				state <= att_le;
			else
				state <= aigui_le;
			end if;
		when aigui_le =>
			if (compteur < nombre_le) then
				state <= new_sel;
			else
				state <= init;
			end if;
		when action =>
			o_cs_a <= '1';
			state <= att_a;
			compteur <= compteur + 1;
		when att_a =>
			if (compteur < 4) then
				state <= new_sel;
			else
				state <= ackno;
			end if;
		when ackno =>
			o_vz_ackno <= '1';
			state <= init;
			--state <= ackno;
		when new_sel =>
			if (sel(6 downto 5)="10") then
				state <= ecriture;
			elsif (sel(6 downto 5)="11") then
				state <= lecture;
			elsif (sel(6 downto 5)="01") then
				state <= action;
			else
				state <= init; --ca ne doit pas arriver
			end if;
			sel <= sel + '1';
		when others =>
			state <= init;
	end case;

end if;
end process out_state;


cmd_comb : process(cmd)
begin

case cmd is
	when x"43" => --action reset
		sel_comb <= "0100000";
		size_read_comb <= x"01";
	when x"45" => --read RX register
		sel_comb <= "1100000";
		size_read_comb <= x"26";
	when x"52" => --read one register
		sel_comb <= "1111111";
		size_read_comb <= x"02";
	when x"57" => --write one register
		sel_comb <= "1000000";
		size_read_comb <= x"01";
	when x"56" => --read FPGA identifer
		sel_comb <= "1111000";
		size_read_comb <= x"0C";
	when others =>
		sel_comb <= "0000000";
		size_read_comb <= x"00";
end case;

end process cmd_comb;

o_sel <= sel;
o_size_read <= size_read_comb;

end rtl;
