-- User-Encoded State Machine

library ieee;
use ieee.std_logic_1164.all;

entity user_encod is

  port
  (
    updown  : in std_logic;
    clock   : in std_logic;
    lsb    : out std_logic;
    msb    : out std_logic
  );

end entity;

architecture rtl of user_encod is

  -- Build an enumerated type for the state machine
  type count_state is (zero, one, two, three);

  -- Registers to hold the current state and the next state
  signal sm_present_state, next_state  : count_state;

  -- Attribute to declare a specific encoding for the states
  attribute syn_encoding        : string;
  attribute syn_encoding of count_state : type is "11 01 10 00";

begin

  -- Determine what the next state will be, and set the output bits
  process (sm_present_state, updown)
  begin
    case sm_present_state is
      when zero =>
        if (updown = '0') then
          next_state <= one;
          lsb <= '0';
          msb <= '0';
        else
          next_state <= three;
          lsb <= '1';
          msb <= '1';
        end if;
      when one =>
        if (updown = '0') then
          next_state <= two;
          lsb <= '1';
          msb <= '0';
        else
          next_state <= zero;
          lsb <= '0';
          msb <= '0';
        end if;
      when two =>
        if (updown = '0') then
          next_state <= three;
          lsb <= '0';
          msb <= '1';
        else
          next_state <= one;
          lsb <= '1';
          msb <= '0';
        end if;
      when three =>
        if (updown = '0') then
          next_state <= zero;
          lsb <= '1';
          msb <= '1';
        else
          next_state <= two;
          lsb <= '0';
          msb <= '1';
        end if;
    end case;
  end process;

  -- Move to the next state
  process
  begin
    wait until rising_edge(clock);
    sm_present_state <= next_state;
  end process;

end rtl;