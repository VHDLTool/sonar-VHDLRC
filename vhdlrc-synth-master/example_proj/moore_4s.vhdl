-- A Moore machine's outputs are dependent only on the current sm_state_moore.
-- The output is written only when the sm_state_moore changes.  (sm_state_moore
-- transitions are synchronous.)

library ieee;
use ieee.std_logic_1164.all;

entity moore_4s is

	port(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out	 : out	std_logic_vector(1 downto 0)
	);
	
end entity;

architecture rtl of moore_4s is

	-- Build an enumerated type for the sm_state_moore machine
	type sm_state_moore_type is (s0, s1, s2, s3);
	
	-- Register to hold the current sm_state_moore
	signal sm_state_moore   : sm_state_moore_type;

begin
	-- Logic to advance to the next sm_state_moore
	process (clk, reset)
	begin
		if reset = '1' then
			sm_state_moore <= s0;
		elsif (rising_edge(clk)) then
			case sm_state_moore is
				when s0=>
					if data_in = '1' then
						sm_state_moore <= s1;
					else
						sm_state_moore <= s0;
					end if;
				when s1=>
					if data_in = '1' then
						sm_state_moore <= s2;
					else
						sm_state_moore <= s1;
					end if;
				when s2=>
					if data_in = '1' then
						sm_state_moore <= s3;
					else
						sm_state_moore <= s2;
					end if;
				when s3 =>
					if data_in = '1' then
						sm_state_moore <= s0;
					else
						sm_state_moore <= s3;
					end if;
			end case;
		end if;
	end process;
	
	-- Output depends solely on the current sm_state_moore
	process (sm_state_moore)
	begin
	
		case sm_state_moore is
			when s0 =>
				data_out <= "00";
			when s1 =>
				data_out <= "01";
			when s2 =>
				data_out <= "10";
			when s3 =>
				data_out <= "11";
		end case;
	end process;
	
end rtl;
