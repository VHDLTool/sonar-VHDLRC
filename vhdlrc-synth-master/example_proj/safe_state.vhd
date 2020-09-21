library ieee;
use ieee.std_logic_1164.all;

entity safe_state is

	port(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
	
end entity;

architecture rtl of safe_state is
	
	-- Build an enumerated type for the sm_state machine
	type state_type is (s0, s1, s2);
	
	-- Register to hold the current sm_state
	signal sm_state   : state_type;
	
	-- Attribute "safe" implements a safe sm_state machine.
	-- This is a sm_state machine that can recover from an
	-- illegal sm_state (by returning to the reset sm_state).
	attribute syn_encoding : string;
	attribute syn_encoding of state_type : type is "safe";
	
begin

	-- Logic to advance to the next sm_state
	process (clk, reset)
	begin
		if reset = '1' then
			sm_state <= s0;
		elsif (rising_edge(clk)) then
			case sm_state is
				when s0=>
					if data_in = '1' then
						sm_state <= s1;
					else
						sm_state <= s0;
					end if;
				when s1=>
					if data_in = '1' then
						sm_state <= s2;
					else
						sm_state <= s1;
					end if;
				when s2=>
					if data_in = '1' then
						sm_state <= s0;
					else
						sm_state <= s2;
					end if;
			end case;
		end if;
	end process;
	
	-- Logic to determine output
	process (sm_state)
	begin
		case sm_state is
			when s0 =>
				data_out <= "00";
			when s1 =>
				data_out <= "01";
			when s2 =>
				data_out <= "10";
		end case;
	end process;
	
end rtl;