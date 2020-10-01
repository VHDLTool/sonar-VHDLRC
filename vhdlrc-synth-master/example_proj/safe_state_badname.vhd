library ieee;
use ieee.std_logic_1164.all;

entity safe_state_badname is

	port(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
	
end entity;

architecture rtl of safe_state_badname is
	
	-- Build an enumerated type for the state machine
	type state_type is (s0, s1, s2);
	
	-- Register to hold the current state
	signal state   : state_type;
	
	-- Attribute "safe" implements a safe state machine.
	-- This is a state machine that can recover from an
	-- illegal state (by returning to the reset state).
	attribute syn_encoding : string;
	attribute syn_encoding of state_type : type is "safe";
	
begin

	-- Logic to advance to the next state
	process (clk, reset)
	begin
		if reset = '1' then
			state <= s0;
		elsif (rising_edge(clk)) then
			case state is
				when s0=>
					if data_in = '1' then
						state <= s1;
					else
						state <= s0;
					end if;
				when s1=>
					if data_in = '1' then
						state <= s2;
					else
						state <= s1;
					end if;
				when s2=>
					if data_in = '1' then
						state <= s0;
					else
						state <= s2;
					end if;
			end case;
		end if;
	end process;
	
	-- Logic to determine output
	process (state)
	begin
		case state is
			when s0 =>
				data_out <= "00";
			when s1 =>
				data_out <= "01";
			when s2 =>
				data_out <= "10";
		end case;
	end process;
	
end rtl;