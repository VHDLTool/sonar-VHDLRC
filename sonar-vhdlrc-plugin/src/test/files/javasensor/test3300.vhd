entity counter is
        port (Incr, Load, Clock: in     bit; -- buffer
              Carry:             out    bit;
              Data_Out:          buffer bit_vector(7 downto 0);
              Data_In:           in     bit_vector(7 downto 0));
 end counter;