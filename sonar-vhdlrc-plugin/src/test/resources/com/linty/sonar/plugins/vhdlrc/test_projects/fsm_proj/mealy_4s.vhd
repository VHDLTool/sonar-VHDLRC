-- A Mealy machine has outputs that depend on both the sm_state_mealy and
-- the inputs. When the inputs change, the outputs are updated
-- immediately, without waiting for a clock edge. The outputs
-- can be written more than once per sm_state_mealy or per clock cycle.

library ieee;
use ieee.std_logic_1164.all;

entity mealy_4s is

  port
  (
    clk   : in  std_logic;
    data_in : in  std_logic;
    reset  : in  std_logic;
    data_out : out std_logic_vector(1 downto 0)
  );

end entity;

architecture rtl of mealy_4s is

  -- Build an enumerated type for the sm_state_mealy machine
  type sm_state_mealy_type is (s0, s1, s2, s3);

  -- Register to hold the current sm_state_mealy
  signal sm_state_mealy : sm_state_mealy_type;

begin
  process (clk, reset)
  begin
    if reset = '1' then
      sm_state_mealy <= s0;
    elsif (rising_edge(clk)) then
      -- Determine the next sm_state_mealy synchronously, based on
      -- the current sm_state_mealy and the input
      case
      sm_state_mealy is
        when s0=>
          if data_in = '1' then
            sm_state_mealy <= s1;
          else
            sm_state_mealy <= s0;
          end if;
        when s1=>
          if data_in = '1' then
            sm_state_mealy <= s2;
          else
            sm_state_mealy <= s1;
          end if;
        when s2=>
          if data_in = '1' then
            sm_state_mealy <= s3;
          else
            sm_state_mealy <= s2;
          end if;
        when s3=>
          if data_in = '1' then
            sm_state_mealy <= s3;
          else
            sm_state_mealy <= s2;
          end if;
      end case;

    end if;
  end process;

  -- Determine the output based only on the current sm_state_mealy
  -- and the input (do not wait for a clock edge).
  process (sm_state_mealy, data_in)
  begin
    case sm_state_mealy is
      when s0=>
        if data_in = '1' then
          data_out <= "00";
        else
          data_out <= "01";
        end if;
      when s1=>
        if data_in = '1' then
          data_out <= "01";
        else
          data_out <= "11";
        end if;
      when s2=>
        if data_in = '1' then
          data_out <= "10";
        else
          data_out <= "10";
        end if;
      when s3=>
        if data_in = '1' then
          data_out <= "11";
        else
          data_out <= "10";
        end if;
    end case;
  end process;

end rtl;