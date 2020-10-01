library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity top is
end top;

architecture rtl of top is

component mealy_4s is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component mealy_4s_badname is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component mealy_4s_std is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component moore_4s is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component moore_4s_badname is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component safe_state is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component safe_state_badname is

	port
	(
		clk		 : in	std_logic;
		data_in	 : in	std_logic;
		reset	 : in	std_logic;
		data_out : out	std_logic_vector(1 downto 0)
	);
end component;

component user_encod is

	port 
	(
		updown	  : in std_logic;
		clock	  : in std_logic;
		lsb		  : out std_logic;
		msb		  : out std_logic
	);
	
end component;

component user_encod_badname is

	port 
	(
		updown	  : in std_logic;
		clock	  : in std_logic;
		lsb		  : out std_logic;
		msb		  : out std_logic
	);
	
end component;



signal clk,data_in,reset : std_logic;
begin

inst:mealy_4s port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst2:moore_4s port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst3:safe_state port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst4:user_encod port map(
clock=>clk,
updown=>data_in
);

inst5:mealy_4s_std port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst6:mealy_4s_badname port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst7:moore_4s_badname port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst8:safe_state_badname port map(
clk=>clk,
data_in=>data_in,
reset=>reset
);

inst9:user_encod_badname port map(
clock=>clk,
updown=>data_in
);

end rtl;