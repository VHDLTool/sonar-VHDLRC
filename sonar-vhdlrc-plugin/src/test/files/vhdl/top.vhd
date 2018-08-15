----------------------------------------------------------------------------------
-- Entreprise	: CNES - DCT/TV/AV & IN
-- Auteur	: Laurent MARY & Florent MANNI
--
-- Nom du projet	: Boitier_USB2-Generik-USER_IP
-- Cible		: XC3S400A ou XC3S200A
-- Outil		: Xilinx ISE 14.7
-- Module	: top.vhd
-- Version	: 1.0
-- Date creation	: 18/08/2016
-- Description	: 	Module de communication USB - GENERIK pour acces PEEK-POKE Registre et DDR2
--
-- Modification 	:
-- 02/02/2011 (V0.1) 	-> Creation du projet
-- 27/10/2011 (V0.8)		-> Mise en place des premieres fonctionnalites "essentielles"
-- 16/08/2012 (V0.9)		-> Premiere version fonctionnelle
-- 17/09/2015 				-> Ajout de la possibilite de lire le cpt ms via le registre du FPGA
--								-> Modification de la gestion des horloges pour fournir une CLK a 80MHz au controlleur DDR2
--									(au lieu de 133MHz, qui pouvait gnrer des erreurs en accs)
-- 29/04/2016 		    	-> conversion en brick gnrique "BoitierFinal_Generik_DDR2"
-- 18/08/2016 (V1.0)		-> Separation de la partie "USER" dans un fichier VHD et nouveau projet "Boitier_USB2-Generik-USER_IP"
------------------------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;



entity top is
	 Generic
			(  use_ddr2 : std_logic := '0'; --A METTRE A UN SI UTILISATION DDR2 !!
				Brick_ID : std_logic_vector(31 downto 0) := "00000000000000000000000000000001"
			);
    Port ( 	sys_clk_in : in  STD_LOGIC; --Horloge d'entree du FPGA
				rst : in  STD_LOGIC;		--rst actif  l'etat bas
			  --Signaux I/F FTDI
				FTDI_Data_io : inout  STD_LOGIC_VECTOR (7 downto 0);
				ftdi_wr : out  STD_LOGIC;
				ftdi_rd : out  STD_LOGIC;
				ftdi_rxe : in  STD_LOGIC;
				ftdi_txe : in  STD_LOGIC;
				ftdi_si : out  STD_LOGIC;
			  --LED Boitier
			   led_data_usb : out  STD_LOGIC;
				led_2 : out STD_LOGIC;
				led_3 : out  STD_LOGIC;
				led_4 : out  STD_LOGIC;
				led_5 : out std_logic;
				led_6 : out STD_LOGIC;
				led_fpga_ok : out  STD_LOGIC;
			 --Switchs
				sw_led : in STD_LOGIC;
				sw_2 : in std_logic;
				sw_3 : in std_logic;
			 --SIGNAUX UTILISATEURS

			 --I2C
				scl : in std_logic;
				sda : inout std_logic;

			 --END SIGNAUX UTILISATEURS
			 --DEBUG
				o_state : out std_logic_vector ( 3 downto 0 );
				debug_4 : out std_logic;
				debug_3 : out std_logic;
				debug_2 : out std_logic;
				debug_1 : out std_logic;
				debug : out std_logic_vector(31 downto 0);
			 --LED Module
            led_dlp : out  STD_LOGIC
				);
end top;

architecture Behavioral of top is

--CONSTANTES
-- ... temporelles
constant TPS_250MS : std_logic_vector(26 downto 0) := "000100110001001011010000000" ; --250 ms a 40MHz
constant TPS_10MS  : std_logic_vector(26 downto 0) := "000000001100001101010000000" ; --10 ms a 40MHz
constant TPS_500US : std_logic_vector(26 downto 0) := "000000000000100111000011111" ; --500us a 40MHz

constant TPS_50NS : std_logic_vector(3 downto 0) 				:= "0010"; --50ns  40MHz
constant TPS_25NS : std_logic_vector(3 downto 0) 				:= "0001"; --25ns  40MHz


-- Decodage des ordres USB
constant CMD_PEEK : std_logic_vector(7 downto 0) 	:= "11101001" ;  --0xE9
constant REP_PEEK : std_logic_vector(7 downto 0) 	:= "11011001" ;  --0xD9
constant CMD_POKE : std_logic_vector(7 downto 0) 	:= "11101000" ;  --0xE8

constant CMD_RAW_WR : std_logic_vector(7 downto 0) 	:= "11011000" ;  --0xD8
constant REP_RAW_PKT : std_logic_vector(7 downto 0) 	:= "11011001" ;  --0xD9

constant REP_RAW_DATA : std_logic_vector(7 downto 0) 	:= "11010000" ;  --0xD0

-- Encodage taille des paquets
constant TAILLE_PKT_PEEK			: std_logic_vector(15 downto 0) := "0000000000001000";

----DECLARATION DES COMPOSANTS DDR2 ==> A Decommenter si utilisation DDR2 !!!
--ATTRIBUTE X_CORE_INFO          : STRING;
--ATTRIBUTE CORE_GENERATION_INFO : STRING;
--
--ATTRIBUTE X_CORE_INFO of Behavioral : ARCHITECTURE  IS "mig_v2_3_ddr2_sp3, Coregen 10.1.02";
--ATTRIBUTE CORE_GENERATION_INFO of Behavioral  : ARCHITECTURE IS "ddr2_sp3,mig_v2_3,{component_name=ddr2_sp3, DATA_WIDTH=8, memory_width=8, clk_width=1, bank_address=2, row_address=13, column_address=10, no_of_cs=1, cke_width=1, registered=0, data_mask=1, mask_enable=0, load_mode_register=0010100110010, ext_load_mode_register=0000001000100}";
--
--component DDR2_FULL is
--    Port (
--				--Signaux de controle
--				 iCLK133MHZ : in std_logic; --clock 133MHZ for DDR2
--				 iRST_n : in std_logic;   -- active low global reset
--
--				 iCLK_USER : in std_logic; --clock  interface that will be used for this interface (shall be inferior or equal to 133MHz)
--				 iRST_USER_n : in  std_logic; -- reset interface active low
--
--				  -----------------------------------------------------
--				  -- user interface  for DDR2 control (clock is the same as MIG clk_tb)
--				  -----------------------------------------------------
--				 iVALID : in std_logic; --pulse value 1 when we want to read  or write
--				 iRdWrn : in std_logic; --value 1 when we want to read value 0 if we want to write (acquired on RdWrn signal
--				 iADDR  : in std_logic_vector(((ROW_ADDRESS + COLUMN_ADDRESS + BANK_ADDRESS)- 1) downto 0); --targetted address
--				 iDATA  : in std_logic_vector(31 downto 0); --value to be written
--
--				 oVALID : out std_logic; --pulse indicating that data read is available
--				 oDATA  : out std_logic_vector(31 downto 0); -- value read from  memory
--				 oBUSY  : out std_logic; -- value 1 when busy (no new action should be issued)
--				--Signaux DDR2
--				 ddr2_a                : out   std_logic_vector((ROW_ADDRESS-1) downto 0);
--				 ddr2_ba               : out   std_logic_vector((BANK_ADDRESS-1) downto 0);
--				 ddr2_ras_n            : out   std_logic;
--				 ddr2_cas_n            : out   std_logic;
--				 ddr2_we_n             : out   std_logic;
--				 ddr2_cs_n             : out   std_logic;
--				 ddr2_odt              : out   std_logic;
--				 ddr2_cke              : out   std_logic;
--				 ddr2_ck               : out   std_logic_vector((CLK_WIDTH-1) downto 0);
--				 ddr2_ck_n             : out   std_logic_vector((CLK_WIDTH-1) downto 0);
--				 ddr2_dq               : inout std_logic_vector((DATA_WIDTH-1) downto 0);
--				 ddr2_dqs              : inout std_logic_vector((DATA_STROBE_WIDTH-1) downto 0);
--				 ddr2_dqs_n            : inout std_logic_vector((DATA_STROBE_WIDTH-1) downto 0);
--				 --ddr2_dm               : out   std_logic_vector((DATA_MASK_WIDTH-1) downto 0);
--				 ddr2_rst_dqs_div_in : in std_logic;
--				 ddr2_rst_dqs_div_out : out std_logic
--	 );
--end component;

COMPONENT pll_80M
PORT(
	CLKIN_IN : IN std_logic;
	RST_IN : IN std_logic;
	CLKFX_OUT : OUT std_logic;
	CLKIN_IBUFG_OUT : OUT std_logic;
	CLK0_OUT : OUT std_logic;
	--CLK2X_OUT : OUT std_logic;
	LOCKED_OUT : OUT std_logic
	);
END COMPONENT;

--Fifo d'interface avec FTDI First Word Fall Through
component fifo_ftdi IS
	port (
	din: IN std_logic_VECTOR(7 downto 0);
	rd_en: IN std_logic;
	srst: IN std_logic;
	clk: IN std_logic;
	wr_en: IN std_logic;
	dout: OUT std_logic_VECTOR(7 downto 0);
	empty: OUT std_logic;
	full: OUT std_logic);
END component;

--Fifo d'interface pour les modes RAW DATA
component fifo_raw_data IS
	port (
	din: IN std_logic_VECTOR(7 downto 0);
	rd_en: IN std_logic;
	rst: IN std_logic;
	clk: IN std_logic;
	wr_en: IN std_logic;
	dout: OUT std_logic_VECTOR(7 downto 0);
	empty: OUT std_logic;
	full: OUT std_logic);
END component;

--COMPONENTS UTLISATEURS
component user_ip IS
	port (
		--Generic signals
		clk_user : in std_logic;
		reset_ip : in std_logic;
		--Register Access
		WrRegUser : in std_logic;
		AddRegUser : in std_logic_vector(31 downto 0);
		DataRegUser_Rd : out std_logic_vector(31 downto 0);
		DataRegUser_Wr : in std_logic_vector(31 downto 0);
		--RAW FIFO Access
		empty_raw_to_user_ip :  in std_logic;
		full_raw_from_user_ip :  in std_logic;
		empty_raw_from_user_ip :  in std_logic;
		raw_from_user_ip : out std_logic_vector(7 downto 0);
		raw_to_user_ip : in std_logic_vector(7 downto 0);
		raw_from_user_ip_wr_ok : out std_logic;
		raw_to_user_ip_rd_ok : out std_logic;
		--FONCTIONNAL SIGNALS
		--
		--I2C
		scl : in std_logic;
		sda : inout std_logic;
		active_raw_to_usb : in std_logic;
		--
		--USER SWITCH
		user_sw2 : in std_logic;
		user_sw3 : in std_logic;
		--LDE EVENT PULSE
		led_2_i_event : out std_logic;
		led_3_i_event : out std_logic;
		led_4_i_event : out std_logic;
		led_5_i_event : out std_logic;
		led_6_i_event : out std_logic;
		--Debug
		debug : out std_logic_vector(31 downto 0)
		);
end component;

--END COMPONENT UTILISATEURS

--TYPE des machines d'etat

type STATE_FTDI is (ST_FTDI_INIT, ST_FTDI_READ_1, ST_FTDI_READ_2, ST_FTDI_READ_3, ST_FTDI_TRANSIT, ST_FTDI_WRITE_1, ST_FTDI_WRITE_2, ST_FTDI_WRITE_3);

type STATE_USB is (ST_INIT, ST_WAIT_CMD,ST_BYTE1_1, ST_BYTE1_2, ST_BYTE2_1, ST_BYTE2_2, ST_BYTE2_3, ST_BYTE3_1, ST_BYTE3_2, ST_BYTE3_3, ST_BYTE4_1, ST_BYTE4_2, ST_BYTE4_3,	ST_FIRST_ANALYSE_CMD,
						ST_GET_ADD, ST_GET_ADD_1_2, ST_GET_ADD_1_3, ST_GET_ADD_2_1, ST_GET_ADD_2_2, ST_GET_ADD_2_3, ST_GET_ADD_3_1, ST_GET_ADD_3_2, ST_GET_ADD_3_3, ST_GET_ADD_4_1, ST_GET_ADD_4_2,
						ST_ANALYSE_CMD, ST_PEEK, ST_POKE, ST_WR_REG, ST_RD_REG, ST_RD_REG_USB, ST_RD_REG_USER, ST_RD_REG_USER_2,
						ST_WR_REG_BYTE_1_1, ST_WR_REG_BYTE_1_2, ST_WR_REG_BYTE_2_1, ST_WR_REG_BYTE_2_2, ST_WR_REG_BYTE_2_3, ST_WR_REG_BYTE_3_1, ST_WR_REG_BYTE_3_2, ST_WR_REG_BYTE_3_3, ST_WR_REG_BYTE_4_1, ST_WR_REG_BYTE_4_2, ST_WR_REG_BYTE_4_3, ST_WR_REG_WR, ST_WR_REG_WR_END, ST_RD_DMA1553,
						ST_PEEK_PKT, ST_PEEK_PKT_2, ST_PEEK_PKT_3, ST_PEEK_PKT_4, ST_PEEK_PKT_5, ST_PEEK_PKT_6, ST_PEEK_PKT_7, ST_PEEK_PKT_8, ST_PEEK_PKT_9, ST_PEEK_PKT_10, ST_PEEK_PKT_11, ST_PEEK_PKT_12, ST_PEEK_PKT_13, ST_PEEK_PKT_14, ST_PEEK_PKT_15, ST_PEEK_PKT_16, ST_PEEK_PKT_17, ST_PEEK_PKT_18, ST_PEEK_PKT_19, ST_PEEK_PKT_20, ST_PEEK_PKT_21, ST_PEEK_PKT_22, ST_PEEK_PKT_23, ST_PEEK_PKT_24, ST_PEEK_PKT_END,
						ST_WR_DDR2, ST_ANALYSE_POKE, ST_POKE_GET_DATA, ST_POKE_GET_DATA_1_1, ST_POKE_GET_DATA_1_2, ST_POKE_GET_DATA_2_1, ST_POKE_GET_DATA_2_2, ST_POKE_GET_DATA_2_3, ST_POKE_GET_DATA_3_1, ST_POKE_GET_DATA_3_2, ST_POKE_GET_DATA_3_3, ST_POKE_GET_DATA_4_1, ST_POKE_GET_DATA_4_2, ST_POKE_GET_DATA_4_3, ST_WR_DDR2_CMD_1, ST_WR_DDR2_CMD_1_END, ST_WR_DDR2_CMD_2, ST_WR_DDR2_CMD_2_END,
						ST_RD_DDR2, ST_RD_DDR2_TRANSIT, ST_RD_DDR2_2, ST_RD_DDR2_END,
						ST_GET_RAW_DATA, ST_RAW_TRANSFER_1, ST_RAW_TRANSFER_2
						);

type STATE_ARBITRE is (ST_ARB_INIT, ST_ARB_WAIT,
							  ST_REQ_USB, ST_REQ_USB_WR, ST_REQ_USB_WR_CMD, ST_REQ_USB_WR_ACK, ST_REQ_USB_WR_END, ST_REQ_USB_RD, ST_REQ_USB_RD_CMD, ST_REQ_USB_RD_ACK, ST_REQ_USB_RD_END);

type STATE_RAW  is (GEST_RAW_INIT, GEST_RAW_TRANSFERT, GEST_RAW_TRANSFERT_2, GEST_RAW_END);

signal etat_arbitre : STATE_ARBITRE := ST_ARB_INIT;

--Signaux de smachines d'etat
signal State : STATE_FTDI := ST_FTDI_INIT;
signal etat_usb : STATE_USB := ST_INIT;

signal etat_ctrl_raw : STATE_RAW := GEST_RAW_INIT;

-- Signaux buffer data FTDI
signal	i_T_FTDI_n			:	std_logic;					-- 1 : HighZ   0 : OutPut
signal	i_FTDI_Data_Out	:	std_logic_vector(7 downto 0);		-- Data_Out FTDI
signal	i_FTDI_Data_In		:	std_logic_vector(7 downto 0);		-- Data_In FTDI
signal	i_FTDI_Rd_n_o		:	std_logic;					-- Signal RD FIFO du FTDI
signal   i_FTDI_Wr_n_o		:	std_logic;
signal 	i_FTDI_Rxe			:  std_logic;
signal 	i_FTDI_Txe			:  std_logic;

signal ftdi_rxe_dd, ftdi_rxe_d, ftdi_txe_dd, ftdi_txe_d : std_logic;
signal cpt_tempo : std_logic_vector(3 downto 0);

--Signaux des 2 Fifo dp FTDI
signal data_from_IP, data_to_ftdi : std_logic_vector(7 downto 0);
signal ftdi_rd_ok, IP_wr_ok, empty_computer, full_ftdi : std_logic;

signal data_from_ftdi, data_to_IP : std_logic_vector(7 downto 0);
signal IP_rd_ok, ftdi_wr_ok, empty_ftdi, full_computer : std_logic;

--Signaux de controle du MUX ntre RAW et CMD USB
signal data_from_IP_cmd, data_from_IP_raw : std_logic_vector(7 downto 0);
signal IP_wr_ok_cmd, IP_wr_ok_raw : std_logic;
signal active_raw_to_usb : std_logic;

--Signaux clk et reset
signal reset_hard, rst_pll : std_logic;
signal clk_40M, clk_20M, clk_66M_ibuf_out, clk_80M, clk_ddr : std_logic;
signal cpt_rst_fpga : std_logic_vector(29 downto 0) ;

--Signaux donnees USB
signal cmd_address, cmd_data : std_logic_vector(31 downto 0);
signal cmd_longueur : std_logic_vector(15 downto 0);
signal cmd_tag, cmd_cfg : std_logic_vector(7 downto 0);
signal data_peek : std_logic_vector(31 downto 0);

--Signaux instance DDR2
signal ddr2_reset, ddr2_valid_in, ddr2_write_n, ddr2_valid_out, ddr2_busy : std_logic;
signal add_ddr2 : std_logic_vector(24 downto 0);
signal ddr2_data_in, ddr2_data_out : std_logic_vector(31 downto 0);
signal cpt_pre_init_ddr2 : std_logic_vector(26 downto 0);

--Signaux pour horloges
signal cpt_clk_1KHz : std_logic_vector(26 downto 0);
signal clk_1Hz, clk_1KHz, clk_1KHz_h : std_logic;
signal cpt_ms : std_logic_vector(9 downto 0);
signal cpt_s : std_logic_vector(5 downto 0);

--Signaux LED
signal led_2_i, led_3_i, led_4_i, led_5_i, led_6_i, led_data_usb_i : std_logic;
signal led_2_i_event, led_3_i_event, led_4_i_event, led_5_i_event, led_6_i_event : std_logic;
signal cpt_event_led_2, cpt_event_led_3, cpt_event_led_4, cpt_event_led_5, cpt_event_led_6 : std_logic_vector(26 downto 0);
signal flag_led_data_usb : std_logic;

--Signaux Switchs
signal etat_sw_led, etat_sw_2, etat_sw_3  : std_logic := '0';
signal cpt_filt_sw_led_high,cpt_filt_sw_led_low : std_logic_vector(29 downto 0) ;
signal cpt_filt_sw_2_high,cpt_filt_sw_2_low : std_logic_vector(29 downto 0) ;
signal cpt_filt_sw_3_high,cpt_filt_sw_3_low : std_logic_vector(29 downto 0) ;

--Signaux machine d'etat TX
signal dmausb_req_n, dmausb_ready_n, dmausb_write_n : std_logic;
signal dmausb_address : std_logic_vector(22 downto 0);
signal usb_ddr2_data : std_logic_vector(31 downto 0);
signal dmausb_data_in, dmausb_data_out : std_logic_vector(31 downto 0);

--Signaux etat FPGA
signal status_fpga : std_logic_vector(7 downto 0);
signal data_conf_fpga : std_logic_vector(31 downto 0);
signal data_conf_fpga_wr : std_logic_vector(7 downto 0);
signal data_conf_fpga_rd : std_logic_vector(23 downto 0);

--Signaux pour les FIFO RAW RD/WR
signal raw_from_usb, raw_to_fpga, raw_from_fpga, raw_to_usb : std_logic_vector(7 downto 0);
signal full_raw_fpga, empty_raw_fpga, full_raw_usb, empty_raw_usb : std_logic;
signal raw_fpga_rd_ok, rawusb_wr_ok : std_logic;
signal rawusb_rd_ok, raw_fpga_wr_ok : std_logic;

--Access to register bank (0x00000001 to 0x0000000F)
signal WrREg : std_logic; -- pulse value 1 when a write in regsiter is wanted
signal SelectRegWr    : std_logic_vector(30 downto 0); --select register
signal Reg2Write    :std_logic_vector(31 downto 0); -- data to be written inside register
signal Reg2Read     :std_logic_vector(31 downto 0); -- data to be read inside register
signal SelectRegRead     : std_logic_vector(30 downto 0); --save address of the register to be read

--Access to User Register Bank (0x00000010 to 0x7FFFFFFF)
signal WrRegUser : std_logic;
signal AddRegUser : std_logic_vector(31 downto 0);
signal DataRegUser_Wr, DataRegUser_Rd : std_logic_vector(31 downto 0);

type T_RegisterBank is array  (0 to 31) of std_logic_vector(31 downto 0);
signal RegisterBank : T_RegisterBank;


begin

------------------------------------------------------------------------
--	Affectations permanentes
------------------------------------------------------------------------

--debug_4 <= vz_cmd_debug;
--debug_3 <= debug(2); --vz_emi
--debug_2 <= debug(1); --addr1(0)
--debug_1 <= debug(0); --cs1


------------------------------------------------------------------------
--	Generation Reset / CLK
------------------------------------------------------------------------

process(reset_hard,clk_40M)
begin
	if (reset_hard = '1' or reset_hard = '1') then
		clk_20M <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		clk_20M <= not(clk_20M);
	end if;
end process;

--Filtrage du reset hard d'entree
process(clk_66M_ibuf_out)
begin
	if (clk_66M_ibuf_out'event) and (clk_66M_ibuf_out = '1') then
		if rst = '1' then
			reset_hard <= '0';
			cpt_rst_fpga <= (others => '0');
		elsif cpt_rst_fpga = "1111111111111111" then
			reset_hard <= '1';
		elsif rst = '0' then
			cpt_rst_fpga <= cpt_rst_fpga + 1;
			reset_hard <= '0';
		else
			cpt_rst_fpga <= (others => '0');
			reset_hard <= '0';
		end if;
	end if;
end process;
ddr2_reset <= not(reset_hard);

--Process de reset propre la pll.
rst_pll <= '0';

------------------------------------------------------------------------
--	Gestion de la CLK 40M du systeme et de la clk 80M pour la DDR2
------------------------------------------------------------------------

process(reset_hard,clk_80M)
begin
if reset_hard = '1' then
	clk_40M <= '0';
elsif clk_80M'event and clk_80M = '1' then
	clk_40M <= not(clk_40M);
end if;
end process;

--Clk pour DDR2
clk_ddr <= clk_80M;

------------------------------------------------------------------------
--	LEDs + Filtrage switch
------------------------------------------------------------------------

led_dlp <= clk_1Hz;
led_fpga_ok <= '0';

led_data_usb <= '0' when etat_sw_led = '1' else not(led_data_usb_i);
led_2 <= '0' when etat_sw_led = '1' else not(led_2_i);
led_3 <= '0' when etat_sw_led = '1' else not(led_3_i);
led_4 <= '0' when etat_sw_led = '1' else not(led_4_i);
led_5 <= '0' when etat_sw_led = '1' else not(led_5_i);
led_6 <= '0' when etat_sw_led = '1' else not(led_6_i);

--Process pour allumer les led selon le besoin

--led_2_i <= led_2_i_event; --RegisterBank(1)(1);
--led_3_i <= led_3_i_event; --RegisterBank(1)(2) ;
--led_4_i <= led_4_i_event; --RegisterBank(1)(3) ;
--led_5_i <= led_5_i_event; --RegisterBank(1)(4) ;
--led_6_i <= led_6_i_event; --RegisterBank(1)(5) ;

process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		led_2_i <= '0';
		led_3_i <= '0';
		led_4_i <= '0';
		led_5_i <= '0';
		led_6_i <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		--Led 2
		if led_2_i_event = '1' then
			cpt_event_led_2 <= (others => '0');
			led_2_i <= '1';
		elsif cpt_event_led_2 = TPS_250MS then
			led_2_i <= '0';
			cpt_event_led_2 <= cpt_event_led_2;
		else
			cpt_event_led_2 <= cpt_event_led_2 + 1;
			led_2_i <= led_2_i;
		end if;
		--Led 3
		if led_3_i_event = '1' then
			cpt_event_led_3 <= (others => '0');
			led_3_i <= '1';
		elsif cpt_event_led_3 = TPS_250MS then
			led_3_i <= '0';
			cpt_event_led_3 <= cpt_event_led_3;
		else
			cpt_event_led_3 <= cpt_event_led_3 + 1;
			led_3_i <= led_3_i;
		end if;
		--Led 4
		if led_4_i_event = '1' then
			cpt_event_led_4 <= (others => '0');
			led_4_i <= '1';
		elsif cpt_event_led_4 = TPS_250MS then
			led_4_i <= '0';
			cpt_event_led_4 <= cpt_event_led_4;
		else
			cpt_event_led_4 <= cpt_event_led_4 + 1;
			led_4_i <= led_4_i;
		end if;
		--Led 5
		if led_5_i_event = '1' then
			cpt_event_led_5 <= (others => '0');
			led_5_i <= '1';
		elsif cpt_event_led_5 = TPS_250MS then
			led_5_i <= '0';
			cpt_event_led_5 <= cpt_event_led_5;
		else
			cpt_event_led_5 <= cpt_event_led_5 + 1;
			led_5_i <= led_5_i;
		end if;
		--Led 6
		if led_6_i_event = '1' then
			cpt_event_led_6 <= (others => '0');
			led_6_i <= '1';
		elsif cpt_event_led_6 = TPS_250MS then
			led_6_i <= '0';
			cpt_event_led_6 <= cpt_event_led_6;
		else
			cpt_event_led_6 <= cpt_event_led_6 + 1;
			led_6_i <= led_6_i;
		end if;
	end if;
end process;

--Process de gestion de l'etat des switchs avec filtrage 10ms
process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		cpt_filt_sw_led_high <= (others => '0');
		cpt_filt_sw_led_low <= (others => '0');
		etat_sw_led <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		if cpt_filt_sw_led_high = TPS_10MS then
			--etat_sw_led <= '1';
			etat_sw_led <= '0';
		elsif cpt_filt_sw_led_low = TPS_10MS then
			--etat_sw_led <= '0';
			etat_sw_led <= '1';
		end if;
		if sw_led = '1' then
			cpt_filt_sw_led_low <= (others => '0');
			cpt_filt_sw_led_high <= cpt_filt_sw_led_high + 1;
		else
			cpt_filt_sw_led_high <= (others => '0');
			cpt_filt_sw_led_low <= cpt_filt_sw_led_low + 1;
		end if;
	end if;
end process;

process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		cpt_filt_sw_2_high <= (others => '0');
		cpt_filt_sw_2_low <= (others => '0');
		etat_sw_2 <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		if cpt_filt_sw_2_high = TPS_10MS then
			etat_sw_2 <= '0';
		elsif cpt_filt_sw_2_low = TPS_10MS then
			etat_sw_2 <= '1';
		end if;
		if sw_2 = '1' then
			cpt_filt_sw_2_low <= (others => '0');
			cpt_filt_sw_2_high <= cpt_filt_sw_2_high + 1;
		else
			cpt_filt_sw_2_high <= (others => '0');
			cpt_filt_sw_2_low <= cpt_filt_sw_2_low + 1;
		end if;
	end if;
end process;

process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		cpt_filt_sw_3_high <= (others => '0');
		cpt_filt_sw_3_low <= (others => '0');
		etat_sw_3 <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		if cpt_filt_sw_3_high = TPS_10MS then
			etat_sw_3 <= '0';
		elsif cpt_filt_sw_3_low = TPS_10MS then
			etat_sw_3 <= '1';
		end if;
		if sw_3 = '1' then
			cpt_filt_sw_3_low <= (others => '0');
			cpt_filt_sw_3_high <= cpt_filt_sw_3_high + 1;
		else
			cpt_filt_sw_3_high <= (others => '0');
			cpt_filt_sw_3_low <= cpt_filt_sw_3_low + 1;
		end if;
	end if;
end process;


------------------------------------------------------------------------
--	Generation Top 1Hz, 1Khz et compteur de millisecondes
------------------------------------------------------------------------

process(clk_40M, reset_hard)
begin
	if (reset_hard = '1') then
		cpt_clk_1KHz <= (others => '0');
		--clk_1Hz <= '0';
		clk_1KHz <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		if cpt_clk_1KHz = TPS_500US then
			clk_1KHz <= not(clk_1KHz);
			cpt_clk_1KHz <= (others => '0');
		else
			cpt_clk_1KHz <= cpt_clk_1KHz + 1;
		end if;
	end if;
end process;

process(clk_40M, reset_hard)
begin
	if (reset_hard = '1') then
		cpt_ms <= (others => '0');
		cpt_s <= (others => '0');
		clk_1Hz <= '0';
		clk_1KHz_h <= '0';
	elsif (clk_40M'event) and (clk_40M = '1') then
		clk_1KHz_h <= clk_1KHz;
		if clk_1KHz_h = '1' and clk_1KHz = '0' then --Utilisation front descendant ?
			if cpt_ms = 999 then
				clk_1Hz <= '0';
				cpt_s <= cpt_s + 1;
				cpt_ms <= (others => '0');
			elsif cpt_ms = 499 then
				clk_1Hz <= '1';
				cpt_ms <= cpt_ms + 1;
			else
				cpt_ms <= cpt_ms + 1;
			end if;
		else
			cpt_ms <= cpt_ms;
		end if;
	end if;
end process;

------------------------------------------------------------------------
--	Machine a etat de gestion des commandes USB PEEK POKE
------------------------------------------------------------------------

process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		etat_usb <= ST_INIT; --etat de la machine de reception des octets usb
		cmd_address <= (others => '0');
		cmd_data <= (others => '0');
		cmd_longueur <= (others => '0');
		cmd_tag <= (others => '0');
		cmd_cfg <= (others => '0');
		data_peek <= (others => '0');
		IP_wr_ok_cmd <= '0';
		dmausb_req_n <= '0';
		dmausb_write_n <= '0';
		dmausb_address <= (others => '0');
		dmausb_data_in <= (others => '0');
		usb_ddr2_data <= (others => '0');
		WrREg<='0';
		SelectRegWr<=(others=>'0');
		Reg2Write<=(others=>'0');

	elsif ((clk_40M'event) and (clk_40M = '1')) then
			WrREg<='0';
			SelectRegWr<=(others=>'0');
		   Reg2Write<=(others=>'0');
			WrRegUser <= '0';
			AddRegUser <= (others => '0');
			DataRegUser_Wr <= (others => '0');
			case etat_usb is
				when ST_INIT =>
					cmd_address <= (others => '0');
					cmd_data <= (others => '0');
					cmd_longueur <= (others => '0');
					cmd_tag <= (others => '0');
					cmd_cfg <= (others => '0');
					data_peek <= (others => '0');
					IP_wr_ok_cmd <= '0';
					dmausb_req_n <= '1';
					dmausb_write_n <= '1';
					dmausb_address <= (others => '0');
					dmausb_data_in <= (others => '0');
					usb_ddr2_data <= (others => '0');
					etat_usb <= ST_WAIT_CMD;
				--Etat d'attente des 4 octets de commande/addresse
				when ST_WAIT_CMD =>
					data_peek <= (others => '0');
					IP_wr_ok_cmd <= '0';
					dmausb_req_n <= '1';
					dmausb_write_n <= '1';
					dmausb_address <= (others => '0');
					dmausb_data_in <= (others => '0');
					usb_ddr2_data <= (others => '0');
					if (empty_ftdi = '0') then
						etat_usb <= ST_BYTE1_1;
					end if;
				--Protocole PEEK / POKE : L(lsb) + L(msb) + Tag + CFG / STAT + ADD( 32 bits) + DATA (...)
				--Etat recuperation octet n1
				when ST_BYTE1_1 =>
					cmd_longueur(7 downto 0)	<= data_to_IP(7 downto 0); --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_BYTE1_2;
				--Repos et attente octet n2
				when ST_BYTE1_2 =>
					IP_rd_ok <= '0';
					etat_usb <= ST_BYTE2_1;
				--Attente octet n2
				when ST_BYTE2_1 =>
					if (empty_ftdi = '0') then
						etat_usb <= ST_BYTE2_2;
					else
						etat_usb <= etat_usb;
					end if;
				--Recuperation octet n2
				when ST_BYTE2_2 =>
						cmd_longueur(15 downto 8) <= data_to_IP(7 downto 0); --octet utile
						IP_rd_ok <= '1';
						etat_usb <= ST_BYTE2_3;
				--Repos et attente octet n3
				when ST_BYTE2_3 =>
						IP_rd_ok <= '0';
						etat_usb <= ST_BYTE3_1;
				--Attente octet n3
				when ST_BYTE3_1 =>
					if cmd_longueur = "0000000000000000" then
						etat_usb <= ST_WAIT_CMD;
					elsif (empty_ftdi = '0') then
						etat_usb <= ST_BYTE3_2;
					else
						etat_usb <= etat_usb;
					end if;
				--Recuperation octet n3
				when ST_BYTE3_2 =>
						cmd_tag <= data_to_IP; --octet utile
						IP_rd_ok <= '1';
						etat_usb <= ST_BYTE3_3;
				--Repos et attente octet 4
				when ST_BYTE3_3 =>
						IP_rd_ok <= '0';
						etat_usb <= ST_BYTE4_1;
				--Attente octet n4
				when ST_BYTE4_1 =>
					if (empty_ftdi = '0') then
						etat_usb <= ST_BYTE4_2;
					else
						etat_usb <= etat_usb;
					end if;
				--Recuperation octet n4 et decision
				when ST_BYTE4_2 =>
						cmd_cfg <= data_to_IP;
						IP_rd_ok <= '1';
						etat_usb <= ST_BYTE4_3;
				--Repos et attente octet 4
				when ST_BYTE4_3 =>
						IP_rd_ok <= '0';
						etat_usb <= ST_FIRST_ANALYSE_CMD;
				when ST_FIRST_ANALYSE_CMD =>
					IP_rd_ok <= '0';
					if cmd_longueur = "0000000000000010" then --taille de 2
						etat_usb <= ST_WAIT_CMD; --pour l'instant
					elsif cmd_tag = CMD_PEEK and active_raw_to_usb = '0' then --PEEK = lecture de registre uniquement si on envoie pas de RAW vers l'USB
						etat_usb <= ST_GET_ADD;
					elsif cmd_tag = CMD_POKE then --POKE = ecriture registre OK
						etat_usb <= ST_GET_ADD;
					elsif cmd_tag = CMD_RAW_WR then --RAW WR = Envoie de donnes vers l'utilisateur FPGA
						etat_usb <= ST_GET_RAW_DATA;
					else
						etat_usb <= ST_WAIT_CMD; --Commande erronee on revient au debut
					end if;

				--Recuperation des octets d'addresse
				when ST_GET_ADD => --Recuperation des 4 octets d'addresse
					if (empty_ftdi = '0') then
						etat_usb <= ST_GET_ADD_1_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_GET_ADD_1_2 =>
					cmd_address(7 downto 0) <= data_to_IP;--FM changed to have full 32 bits addressing  data_to_IP(7 downto 2) & "00"; --octet utile --FIXME disable this 32 addressing convrted from 8Bits
					IP_rd_ok <= '1';
					etat_usb <= ST_GET_ADD_1_3;
				when ST_GET_ADD_1_3 =>
					IP_rd_ok <= '0';
					etat_usb <= ST_GET_ADD_2_1;

				when ST_GET_ADD_2_1 =>
					if (empty_ftdi = '0') then
						etat_usb <= ST_GET_ADD_2_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_GET_ADD_2_2 =>
					cmd_address(15 downto 8) <= data_to_IP; --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_GET_ADD_2_3;
				when ST_GET_ADD_2_3 =>
					IP_rd_ok <= '0';
					etat_usb <= ST_GET_ADD_3_1;
				when ST_GET_ADD_3_1 => --Recuperation des 4 octets d'addresse
					if (empty_ftdi = '0') then
						etat_usb <= ST_GET_ADD_3_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_GET_ADD_3_2 =>
					cmd_address(23 downto 16) <= data_to_IP; --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_GET_ADD_3_3;
				when ST_GET_ADD_3_3 =>
					IP_rd_ok <= '0';
					etat_usb <= ST_GET_ADD_4_1;
				when ST_GET_ADD_4_1 => --Recuperation des 4 octets d'addresse
					if (empty_ftdi = '0') then
						etat_usb <= ST_GET_ADD_4_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_GET_ADD_4_2 =>
					cmd_address(31 downto 24) <= data_to_IP; --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_ANALYSE_CMD;
				when ST_ANALYSE_CMD => --Analyse de la commande PEEK ou POKE
					IP_rd_ok <= '0';
					if cmd_tag = CMD_PEEK then --peek = lecture
						etat_usb <= ST_PEEK;
					elsif cmd_tag = CMD_POKE then --poke = ecriture
						etat_usb <= ST_POKE;
					else
						etat_usb <= ST_WAIT_CMD; --Commande erronee on revient au debut
					end if;

				--Etat PEEK : on verifie si addrese ou registre
				when ST_PEEK =>
					if ((cmd_address(31) = '1') and (use_ddr2 = '1')) then --access to DDR
						etat_usb <= ST_RD_DDR2;
					else --access to regsitres
						--etat_usb <= ST_RD_REG;
						if cmd_address(30 downto 1) = "000000000000000000000000000000" then
							SelectRegRead <= cmd_address(30 downto 0); --save address to be read from
							etat_usb <= ST_RD_REG_USB;
						else
							AddRegUser <= '0' & cmd_address(30 downto 0);
							etat_usb <= ST_RD_REG_USER;
						end if;
					end if;
				-- Etat POKE : on recupere une data
				when ST_POKE =>
					etat_usb <= ST_POKE_GET_DATA;

				-- Etat de recuperation des data pour le poke
				when ST_POKE_GET_DATA => --On commencer par recuprer quatre (32 bits) nouveaux octets de donnes !
					IP_rd_ok <= '0';
					dmausb_req_n <= '1';
					dmausb_write_n <= '0'; --Ecriture
					if (empty_ftdi = '0') then
						etat_usb <= ST_POKE_GET_DATA_1_1;
					else
						etat_usb <= etat_usb;
					end if;

				when ST_POKE_GET_DATA_1_1 => --Recuperation octet n1
					cmd_data(7 downto 0) <= data_to_IP(7 downto 0); --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_POKE_GET_DATA_1_2;
				when ST_POKE_GET_DATA_1_2 =>  --Repos et attente octet n2
					IP_rd_ok <= '0';
					etat_usb <= ST_POKE_GET_DATA_2_1;
				when ST_POKE_GET_DATA_2_1 =>  --Recuperation octet n2
					IP_rd_ok <= '0';
					if (empty_ftdi = '0') then
						etat_usb <= ST_POKE_GET_DATA_2_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_POKE_GET_DATA_2_2 =>
					cmd_data(15 downto 8) <= data_to_IP(7 downto 0); --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_POKE_GET_DATA_2_3;
				when ST_POKE_GET_DATA_2_3 =>
					IP_rd_ok <= '0';
					etat_usb <= ST_POKE_GET_DATA_3_1;
				when ST_POKE_GET_DATA_3_1 =>  --Recuperation octet n3
					IP_rd_ok <= '0';
					if (empty_ftdi = '0') then
						etat_usb <= ST_POKE_GET_DATA_3_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_POKE_GET_DATA_3_2 =>
					cmd_data(23 downto 16) <= data_to_IP(7 downto 0); --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_POKE_GET_DATA_3_3;
				when ST_POKE_GET_DATA_3_3 =>  --Repos
					IP_rd_ok <= '0';
					etat_usb <= ST_POKE_GET_DATA_4_1;
				when ST_POKE_GET_DATA_4_1 =>  --Recuperation octet n4
					IP_rd_ok <= '0';
					if (empty_ftdi = '0') then
						etat_usb <= ST_POKE_GET_DATA_4_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_POKE_GET_DATA_4_2 =>
					cmd_data(31 downto 24) <= data_to_IP(7 downto 0); --octet utile
					IP_rd_ok <= '1';
					etat_usb <= ST_POKE_GET_DATA_4_3;
				when ST_POKE_GET_DATA_4_3 =>  --Repos
					IP_rd_ok <= '0';
					etat_usb <= ST_ANALYSE_POKE;
				when ST_ANALYSE_POKE =>
					if ((cmd_address(31) = '1') and (use_ddr2 = '1')) then -- access DDR
						etat_usb <= ST_WR_DDR2_CMD_1;
						usb_ddr2_data <= cmd_data;
					else --access registre
						etat_usb <= ST_WR_REG_WR;
					end if;

				--Ecriture DDR2
				when ST_WR_DDR2_CMD_1 => --Requete d'ecriture DDR2
					dmausb_address <= cmd_address(24 downto 2);
					dmausb_data_in <= usb_ddr2_data;
					dmausb_req_n <= '0';
					dmausb_write_n <= '0';
					if dmausb_ready_n = '0' then --La requete est termin
						etat_usb <= ST_WR_DDR2_CMD_1_END;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_WR_DDR2_CMD_1_END =>
					dmausb_req_n <= '1';
					dmausb_write_n <= '0';
					if dmausb_ready_n = '1' then
						etat_usb <= ST_WR_DDR2_CMD_2;
						dmausb_address <= cmd_address(24 downto 2) ;
					else
						etat_usb <= etat_usb;
						dmausb_address <= dmausb_address;
					end if;
				when ST_WR_DDR2_CMD_2 => --Requete d'ecriture DDR2
					dmausb_address <= dmausb_address;
					dmausb_data_in <= usb_ddr2_data;
					dmausb_req_n <= '0';
					dmausb_write_n <= '0';
					if dmausb_ready_n = '0' then --La requete est termin
						etat_usb <= ST_WR_DDR2_CMD_2_END;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_WR_DDR2_CMD_2_END =>
					dmausb_req_n <= '1';
					dmausb_write_n <= '1';
					etat_usb <= ST_WAIT_CMD;
				--Ecriture registre
				when ST_WR_REG_WR =>
				  --On regarde si il s'agit d'une ecriture locale ( Add = 0x0000 000x) ou IP utilisateur
				  if cmd_address(30 downto 1) = "000000000000000000000000000000" then
					  WrREg<='1';
					  SelectRegWr<=cmd_address(30 downto 0);
					  Reg2Write<=cmd_data;
					  etat_usb <= ST_WR_REG_WR_END;
				  else
						WrRegUser <= '1';
						AddRegUser <= '0' & cmd_address(30 downto 0);
						DataRegUser_Wr <= cmd_data;
						etat_usb <= ST_WR_REG_WR_END;
				  end if;
				when ST_WR_REG_WR_END =>
					etat_usb <= ST_WAIT_CMD;

				--Etat lecture d'un des deux registre de l'ip (Interne ou User)
				when ST_RD_REG_USB =>
						data_peek <= Reg2Read;
						etat_usb <= ST_PEEK_PKT;

				when ST_RD_REG_USER =>
						AddRegUser <= AddRegUser;
						etat_usb <= ST_RD_REG_USER_2;
				when ST_RD_REG_USER_2 =>
						data_peek <= DataRegUser_Rd;
						etat_usb <= ST_PEEK_PKT;

				--Etat lecture de la DDR2
				when ST_RD_DDR2 =>
					dmausb_address <= cmd_address(24 downto 2);
					dmausb_req_n <= '0'; --Requete
					dmausb_write_n <= '1'; --Lecture
					if dmausb_ready_n = '0' then --La requete est termin
						etat_usb <= ST_RD_DDR2_TRANSIT;
						usb_ddr2_data <= dmausb_data_out;
					else
						etat_usb <= etat_usb;
						usb_ddr2_data <= usb_ddr2_data;
					end if;
				when ST_RD_DDR2_TRANSIT =>
					dmausb_req_n <= '1';
					if dmausb_ready_n = '1' then
						etat_usb <= ST_RD_DDR2_2;
						dmausb_address <= cmd_address(24 downto 2);
					else
						etat_usb <= etat_usb;
					end if;
				when ST_RD_DDR2_2 =>
					dmausb_address <= cmd_address(24 downto 2);
					dmausb_req_n <= '0'; --Requete
					dmausb_write_n <= '1'; --Lecture
					if dmausb_ready_n = '0' then --La requete est termin
						etat_usb <= ST_RD_DDR2_END;
						usb_ddr2_data <= dmausb_data_out;
					else
						etat_usb <= etat_usb;
						usb_ddr2_data <= usb_ddr2_data;
					end if;
				when ST_RD_DDR2_END =>
					etat_usb <= ST_PEEK_PKT;
					dmausb_req_n <= '1';
					data_peek <= usb_ddr2_data;

				--Etat creation du paquet  renvoyer !
				when ST_PEEK_PKT => --On envoie les octets vers la FIFO USB FTDI avec le meme protocole
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= TAILLE_PKT_PEEK(7 downto 0);
						etat_usb <= ST_PEEK_PKT_2;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_2 => --1er octet taille
					IP_wr_ok_cmd <= '1';
					data_from_IP_cmd <= TAILLE_PKT_PEEK(7 downto 0);
					etat_usb <= ST_PEEK_PKT_3;
				when ST_PEEK_PKT_3 => --2eme octet taille
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= TAILLE_PKT_PEEK(15 downto 8);
						etat_usb <= ST_PEEK_PKT_4;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_4 => --2eme octet taille
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= TAILLE_PKT_PEEK(15 downto 8);
						etat_usb <= ST_PEEK_PKT_5;
					else
						etat_usb <= etat_usb;
					end if;

				when ST_PEEK_PKT_5 => --3eme octet tag
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= REP_PEEK;
						etat_usb <= ST_PEEK_PKT_6;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_6 => --3eme octet tag
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= REP_PEEK;
						etat_usb <= ST_PEEK_PKT_7;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_7 => --4eme octet status
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= status_fpga;
						etat_usb <= ST_PEEK_PKT_8;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_8 => --4eme octet status
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= status_fpga;
						etat_usb <= ST_PEEK_PKT_9;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_9 => --5eme octet : on repete l'addresse
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= cmd_address(7 downto 0);
						etat_usb <= ST_PEEK_PKT_10;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_10 => --5eme octet : adresse
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= cmd_address(7 downto 0);
						etat_usb <= ST_PEEK_PKT_11;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_11 => --6eme octet : adresse
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= cmd_address(15 downto 8);
						etat_usb <= ST_PEEK_PKT_12;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_12 =>--6eme octet : adresse
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= cmd_address(15 downto 8);
						etat_usb <= ST_PEEK_PKT_13;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_13 => --7eme octet : adresse
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= cmd_address(23 downto 16);
						etat_usb <= ST_PEEK_PKT_14;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_14 => --7eme octet : adresse
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= cmd_address(23 downto 16);
						etat_usb <= ST_PEEK_PKT_15;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_15 => --8eme octet : adresse
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= cmd_address(31 downto 24);
						etat_usb <= ST_PEEK_PKT_16;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_16 => --8eme octet : adresse
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= cmd_address(31 downto 24);
						etat_usb <= ST_PEEK_PKT_17;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_17 => --9eme octet : data
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						data_from_IP_cmd <= data_peek(7 downto 0);
						etat_usb <= ST_PEEK_PKT_18;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_18 => --9eme octet : data
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= data_peek(7 downto 0);
						etat_usb <= ST_PEEK_PKT_19;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_19 =>
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						etat_usb <= ST_PEEK_PKT_20;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_20 => --10eme octet
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= data_peek(15 downto 8);
						etat_usb <= ST_PEEK_PKT_21;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_21 =>
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						etat_usb <= ST_PEEK_PKT_22;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_22 => --11eme octet
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= data_peek(23 downto 16);
						etat_usb <= ST_PEEK_PKT_23;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_23 =>
					IP_wr_ok_cmd <= '0';
					if full_ftdi = '0' then
						etat_usb <= ST_PEEK_PKT_24;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_24 => --12eme octet
					if full_ftdi = '0' then
						IP_wr_ok_cmd <= '1';
						data_from_IP_cmd <= data_peek(31 downto 24);
						etat_usb <= ST_PEEK_PKT_END;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_PEEK_PKT_END =>
					IP_wr_ok_cmd <= '0';
					etat_usb <= ST_WAIT_CMD;

				--Mode envoi de donnees brutes vers le FPGA
				when ST_GET_RAW_DATA => --On tranfre la fifo de donnees FTDI vers la FIFO RAW
						IP_rd_ok <= '0';
						rawusb_wr_ok <= '0';
					if cmd_longueur = 2 then -- c'est fini !
						etat_usb <= ST_INIT;
					elsif full_raw_fpga = '0' and empty_ftdi = '0' then
						cmd_longueur <= cmd_longueur - 1;
						etat_usb <= ST_RAW_TRANSFER_1;
						raw_from_usb <= data_to_IP;
					else
						etat_usb <= etat_usb;
					end if;
				when ST_RAW_TRANSFER_1 =>
						IP_rd_ok <= '1';
						rawusb_wr_ok <= '1';
						raw_from_usb <= data_to_IP;
						etat_usb <= ST_RAW_TRANSFER_2;
				when ST_RAW_TRANSFER_2 =>
						IP_rd_ok <= '0';
						rawusb_wr_ok <= '0';
						raw_from_usb <= raw_from_usb;
						etat_usb <= ST_GET_RAW_DATA;

				when others =>
					etat_usb <= ST_INIT;
			end case;
	end if;
end process;

------------------------------------------------------------------------
-- MUX DE GESTION DU MODE RAW
------------------------------------------------------------------------

process(active_raw_to_usb)
begin
		if active_raw_to_usb = '1' then
			data_from_IP <= data_from_IP_raw;
			ip_wr_ok <= ip_wr_ok_raw;
		else
			data_from_IP <= data_from_ip_cmd;
			ip_wr_ok <= ip_wr_ok_cmd;
		end if;
end process;

--Process de transfert automatique des donnees de la FIFO RAW vers la FIFO FTDI
process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		etat_ctrl_raw <= GEST_RAW_INIT;
		ip_wr_ok_raw <= '0';
		rawusb_rd_ok <= '0';
	elsif(clk_40M'event) and (clk_40M = '1') then
		case etat_ctrl_raw is
			when GEST_RAW_INIT =>
				ip_wr_ok_raw <= '0';
				rawusb_rd_ok <= '0';
				if active_raw_to_usb = '1' then
					etat_ctrl_raw <= GEST_RAW_TRANSFERT;
				else
					etat_ctrl_raw <= etat_ctrl_raw;
				end if;
			when GEST_RAW_TRANSFERT =>
				if full_ftdi = '0' and empty_raw_fpga = '0' then
					etat_ctrl_raw <= GEST_RAW_TRANSFERT_2;
					data_from_IP_raw <= raw_to_usb;
				else
					etat_ctrl_raw <= GEST_RAW_INIT;
				end if;

			when GEST_RAW_TRANSFERT_2 =>
				data_from_IP_raw <= raw_to_usb;
				ip_wr_ok_raw <= '1';
				rawusb_rd_ok <= '1';
				etat_ctrl_raw <= GEST_RAW_END;

			when GEST_RAW_END =>
				ip_wr_ok_raw <= '0';
				rawusb_rd_ok <= '0';
				data_from_IP_raw <= data_from_IP_raw;
				etat_ctrl_raw <= GEST_RAW_INIT;

		end case;
	end if;
end process;

------------------------------------------------------------------------
-- REGISTRES PERMANENTS DE CONFIGURATION
------------------------------------------------------------------------

--Registre 0x00 : IDENTIFIANT BRIQUE : RD ONLY
RegisterBank(0) <= Brick_ID;

--Registre 0x01 : GESTION BRIQUE : RD/WR ONLY
active_raw_to_usb <= RegisterBank(1)(0);

------------------------------------------------------------------------
--	Gestion des RD / WR des registres "BOITIERS USB2"
------------------------------------------------------------------------

	P_L_REgister_WR:
	for i in 1 to 15 generate
		P_Register_WR : process(reset_hard,clk_40M)
			begin
				if (reset_hard = '1') then
					RegisterBank(i)<=(others=>'0');
				elsif ((clk_40M'event) and (clk_40M = '1')) then
					if (SelectRegWr = conv_std_logic_vector(i,SelectRegWr'length)) then
						-- good address
							if (WrREg='1') then
								--write selected
								RegisterBank(i)<=Reg2Write;
							end if;
					end if;
				end if;
			end process;
	end generate;

	Reg2Read <= RegisterBank(conv_integer(SelectRegRead));

------------------------------------------------------------------------
-- Arbitre DMA vers DDR2 entre ordre machine d'etat / dma demande par l'ip
------------------------------------------------------------------------

----Process arbitrage memoire DDR2 ==> a decommenter si utilisation DDR2 !!!
--process(reset_hard,clk_40M)
--begin
--	if (reset_hard = '1') then
--		dmausb_ready_n <= '1';
--		ddr2_valid_in <= '0';
--		ddr2_write_n <= '0';
--		add_ddr2 <= (others => '0');
--		ddr2_data_in <= (others => '0');
--		etat_arbitre <= ST_ARB_INIT;
--	elsif(clk_40M'event) and (clk_40M = '1') then
--		case etat_arbitre is
--			when ST_ARB_INIT => --Attente DDR2 ready (phase d'init)
--				dmausb_ready_n <= '1';
--				ddr2_valid_in <= '0';
--				ddr2_write_n <= '0';
--				add_ddr2 <= (others => '0');
--				ddr2_data_in <= (others => '0');
--				if ddr2_busy = '0' then
--					etat_arbitre <= ST_ARB_WAIT;
--				else
--					etat_arbitre <= ST_ARB_INIT;
--				end if;
--			when ST_ARB_WAIT => --Attente d'une requete IP ou USB ==> A INTEGRER SI IP NECESSITANT LA DDR2
--				dmausb_ready_n <= '1';
--				ddr2_valid_in <= '0';
--				ddr2_write_n <= '0';
--				add_ddr2 <= (others => '0');
--				ddr2_data_in <= (others => '0');
--            if dmausb_req_n = '0' then --Requete provenant de l'USB
--					etat_arbitre <= ST_REQ_USB;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--
--			--traitement USB
--			when ST_REQ_USB =>
--				if dmausb_write_n = '0' then --requete en ecriture
--					etat_arbitre <= ST_REQ_USB_WR;
--					ddr2_write_n <= '0';
--				else --requete en lecture
--					etat_arbitre <= ST_REQ_USB_RD;
--					ddr2_write_n <= '1';
--				end if;
--
--			when ST_REQ_USB_WR => --Ecriture DDR2
--				ddr2_data_in <= dmausb_data_in;
--				ddr2_write_n <= '0';
--				add_ddr2 <= dmausb_address(20 downto 0) & "00" & dmausb_address(22 downto 21);
--				if ddr2_busy = '0' then
--					ddr2_valid_in <= '1';
--					etat_arbitre <= ST_REQ_USB_WR_CMD;
--				else
--					ddr2_valid_in <= '0';
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_WR_CMD =>
--				ddr2_data_in <= dmausb_data_in;
--				ddr2_write_n <= '0';
--				add_ddr2 <= dmausb_address(20 downto 0) & "00" & dmausb_address(22 downto 21);
--				ddr2_valid_in <= '0';
--				if ddr2_busy = '1' then
--					etat_arbitre <= ST_REQ_USB_WR_ACK;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_WR_ACK =>
--				ddr2_valid_in <= '0';
--				ddr2_write_n <= '0';
--				ddr2_data_in <= ddr2_data_in;
--				add_ddr2 <= add_ddr2;
--				if ddr2_busy = '0' then
--					etat_arbitre <= ST_REQ_USB_WR_END;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_WR_END =>
--				dmausb_ready_n <= '0';
--				if dmausb_req_n = '1' then
--					etat_arbitre <= ST_ARB_WAIT;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_RD => --Lecture DDR2
--				ddr2_write_n <= '1';
--				add_ddr2 <= dmausb_address(20 downto 0) & "00" & dmausb_address(22 downto 21);
--				if ddr2_busy = '0' then
--					ddr2_valid_in <= '1';
--					etat_arbitre <= ST_REQ_USB_RD_CMD;
--				else
--					ddr2_valid_in <= '0';
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_RD_CMD =>
--				ddr2_valid_in <= '0';
--				ddr2_write_n <= '1';
--				add_ddr2 <= dmausb_address(20 downto 0) & "00" & dmausb_address(22 downto 21);
--				if ddr2_valid_out = '1' then
--					dmausb_data_out <= ddr2_data_out;
--					etat_arbitre <= ST_REQ_USB_RD_ACK;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_RD_ACK =>
--				ddr2_valid_in <= '0';
--				dmausb_data_out <= ddr2_data_out;
--				if ddr2_busy = '0' then
--					etat_arbitre <= ST_REQ_USB_RD_END;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when ST_REQ_USB_RD_END =>
--				dmausb_ready_n <= '0';
--				dmausb_data_out <= dmausb_data_out;
--				if dmausb_req_n = '1' then
--					etat_arbitre <= ST_ARB_WAIT;
--				else
--					etat_arbitre <= etat_arbitre;
--				end if;
--			when others =>
--				etat_arbitre <= ST_ARB_INIT;
--		end case;
--	end if;
--end process;

------------------------------------------------------------------------
--	Machine a etat avec le FDTI et FIFO
------------------------------------------------------------------------

ftdi_si <= '1';
ftdi_rd <= i_FTDI_Rd_n_o;
ftdi_wr <= i_FTDI_Wr_n_o;

--Cross Domain
i_FTDI_Txe <= ftdi_txe_dd;
i_FTDI_Rxe <= ftdi_rxe_dd;
process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		ftdi_txe_d <= '1';
		ftdi_txe_dd <= '1';
		ftdi_rxe_d <= '1';
		ftdi_rxe_dd <= '1';
	elsif(clk_40M'event) and (clk_40M = '1') then
		ftdi_txe_d <= ftdi_txe;
		ftdi_txe_dd <= ftdi_txe_d;
		ftdi_rxe_d <= ftdi_rxe;
		ftdi_rxe_dd <= ftdi_rxe_d;
	end if;
end process;

--INSTANTIATION DES BUFFERS I/O
--Buffer I/O :
-- i_T_FTDI_n = '1' ==> Input
-- i_T_FTDI_n = '0' ==> Output

--Buffer I/O :
-- i_T_FTDI_n = '1' ==> Input
-- i_T_FTDI_n = '0' ==> Output
process(reset_hard,clk_40M)
begin
	if (reset_hard = '1') then
		i_FTDI_Rd_n_o	<= '1';
		i_FTDI_Wr_n_o	<= '1';
		i_T_FTDI_n	<= '1';
		cpt_tempo <= (others => '0');
		i_FTDI_Data_Out	<= x"00";
		ftdi_wr_ok <= '0';
		ftdi_rd_ok <= '0';
		flag_led_data_usb	<= '0';
		State <=	ST_FTDI_INIT;
	elsif(clk_40M'event) and (clk_40M = '1') then
		case State is
			When ST_FTDI_INIT => -- Etat origine
				i_FTDI_Rd_n_o	<= '1';
				i_FTDI_Wr_n_o	<= '1';
				ftdi_wr_ok <= '0';
				ftdi_rd_ok <= '0';
				flag_led_data_usb <= '0';
				cpt_tempo <= (others => '0');
				if (i_FTDI_Rxe = '0' and full_computer = '0') then --on peut lire une data a envoyer vers le FPGA
					State <= ST_FTDI_READ_1;
					i_T_FTDI_n <= '1'; -- Changement du sens des buffers
				elsif (i_FTDI_Txe = '0' and empty_computer = '0') then --on crit une donnee recue sur le FPGA
					i_T_FTDI_n <= '0'; -- Changement du sens des buffers
					i_FTDI_Data_Out <= i_FTDI_Data_Out;
					State <= ST_FTDI_WRITE_1;
				else
					State <= State;
					i_T_FTDI_n <= i_T_FTDI_n;
				end if;
			When ST_FTDI_READ_1 => --On descend le RD, le sens I/O est OK
				i_FTDI_Rd_n_o	<= '0';
				i_FTDI_Wr_n_o	<= '1';
				i_T_FTDI_n	<= '1';
				flag_led_data_usb <= '1';
				State <= ST_FTDI_READ_2;
			When ST_FTDI_READ_2 => --Il faut attendre 50ns au moins, donc 50ns de plus (Ca fait 60ns en tout)
				i_FTDI_Rd_n_o	<= '0';
				i_FTDI_Wr_n_o	<= '1';
				i_T_FTDI_n	<= '1';
				if cpt_tempo = TPS_50NS then --On a attendu, la data est valide et on l'crit en FIFO FPGA
					State <= ST_FTDI_READ_3;
					data_from_ftdi <= i_FTDI_Data_In;
					ftdi_wr_ok <= '1';
				else
					State <= State;
					cpt_tempo <= cpt_tempo + 1;
				end if;
			When ST_FTDI_READ_3 => --On remonte le RD on attend le RXE  '1' + Sens buffer I/O
				i_FTDI_Rd_n_o	<= '1';
				i_FTDI_Wr_n_o	<= '1';
				i_T_FTDI_n	<= '0';
				ftdi_wr_ok <= '0';
				if i_FTDI_Rxe = '1' then
					State <= ST_FTDI_TRANSIT;
				else
					State <= State;
				end if;
			When ST_FTDI_TRANSIT => --Phase basculement vers ecriture ou retour idle
				i_FTDI_Rd_n_o	<= '1';
				ftdi_wr_ok <= '0';
				cpt_tempo <= (others => '0');
				i_FTDI_Wr_n_o	<= '1';
				i_T_FTDI_n	<= '0';
				if (i_FTDI_Txe = '0' and empty_computer = '0') then -- on crit une donnee recue par le FPGA
					State <= ST_FTDI_WRITE_1;
					i_FTDI_Data_Out <= i_FTDI_Data_Out; --On met la data en sortie en avance
				else
					State <= ST_FTDI_INIT;
				end if;
			When ST_FTDI_WRITE_1 => -- On laisse la data et on valide la lecutre de la FIFO
				flag_led_data_usb <= '1';
				i_FTDI_Rd_n_o	<= '1';
				i_FTDI_Wr_n_o	<= '1';
				i_T_FTDI_n	<= '0';
				ftdi_rd_ok <= '1';
				i_FTDI_Data_Out <= data_to_ftdi;
				State <= ST_FTDI_WRITE_2;
			When ST_FTDI_WRITE_2 => -- on passe le wr ftdi a '0' et il faut le laisser au moins 30ns
				i_FTDI_Rd_n_o	<= '1';
				i_FTDI_Wr_n_o	<= '0';
				i_T_FTDI_n	<= '0';
				ftdi_rd_ok <= '0';
				i_FTDI_Data_Out <= i_FTDI_Data_Out;
				if cpt_tempo = TPS_50NS then
					State <= ST_FTDI_WRITE_3;
				elsif i_FTDI_Txe = '1' then --On commence  compter les 20ns quand le TXE est repass  '1'
					State <= State;
					cpt_tempo <= cpt_tempo + 1;
				else
					State <= State;
				end if;
			When ST_FTDI_WRITE_3 => -- On remonte le WR
				i_FTDI_Rd_n_o	<= '1';
				i_FTDI_Wr_n_o	<= '1';
				i_T_FTDI_n	<= '0';
				i_FTDI_Data_Out <= i_FTDI_Data_Out;
				State <= ST_FTDI_INIT;
			When others =>
				State <= ST_FTDI_INIT;
			end case;
		end if;
end process;

FTDI_D0 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(0),
      IO 	=> FTDI_Data_io(0),
      I 	=> i_FTDI_Data_Out(0),
      T 	=> i_T_FTDI_n
   );

FTDI_D1 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(1),
      IO 	=> FTDI_Data_io(1),
      I 	=> i_FTDI_Data_Out(1),
      T 	=> i_T_FTDI_n
   );

   FTDI_D2 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(2),
      IO 	=> FTDI_Data_io(2),
      I 	=> i_FTDI_Data_Out(2),
      T 	=> i_T_FTDI_n
   );

   FTDI_D3 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(3),
      IO 	=> FTDI_Data_io(3),
      I 	=> i_FTDI_Data_Out(3),
      T 	=> i_T_FTDI_n
   );

   FTDI_D4 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(4),
      IO 	=> FTDI_Data_io(4),
      I 	=> i_FTDI_Data_Out(4),
      T 	=> i_T_FTDI_n
   );

   FTDI_D5 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(5),
      IO 	=> FTDI_Data_io(5),
      I 	=> i_FTDI_Data_Out(5),
      T 	=> i_T_FTDI_n
   );

   FTDI_D6 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(6),
      IO 	=> FTDI_Data_io(6),
      I 	=> i_FTDI_Data_Out(6),
      T 	=> i_T_FTDI_n
   );

   FTDI_D7 : IOBUF
   generic map (
      DRIVE => 12,
      IBUF_DELAY_VALUE => "0",
      IFD_DELAY_VALUE => "AUTO",
      IOSTANDARD => "LVCMOS33",
      SLEW => "FAST")
   port map (
      O 	=> i_FTDI_Data_In(7),
      IO 	=> FTDI_Data_io(7),
      I 	=> i_FTDI_Data_Out(7),
      T 	=> i_T_FTDI_n
   );

------------------------------------------------------------------------
--	Instanciation des composants principaux
------------------------------------------------------------------------

----Instanciation de la pll
Inst_pll_80M : pll_80M PORT MAP(
		CLKIN_IN => sys_clk_in,
		RST_IN => rst_pll,
		CLKFX_OUT => clk_80M,
		CLKIN_IBUFG_OUT => clk_66M_ibuf_out,
		CLK0_OUT => open,
		LOCKED_OUT => open
	);

inst_fifo_IP_to_ftdi : fifo_ftdi
	port map (
	din => data_from_IP,
	rd_en=> ftdi_rd_ok,
	srst=> reset_hard,
	clk=> clk_40M,
	wr_en=> IP_wr_ok,
	dout=> data_to_ftdi,
	empty => empty_computer,
	full => full_ftdi
	);

inst_fifo_ftdi_to_IP : fifo_ftdi
	port map (
	din => data_from_ftdi,
	rd_en=> IP_rd_ok,
	srst=> reset_hard,
	clk => clk_40M,
	wr_en=> ftdi_wr_ok,
	dout=> data_to_IP,
	empty => empty_ftdi,
	full => full_computer
	);

inst_fifo_rawdata_to_fpga : fifo_raw_data
	port map (
	din => raw_from_usb,
	rd_en=> raw_fpga_rd_ok,
	rst=> reset_hard,
	clk => clk_40M,
	wr_en=> rawusb_wr_ok,
	dout=> raw_to_fpga,
	empty => empty_raw_usb,
	full => full_raw_fpga
	);

inst_fifo_rawdata_from_fpga : fifo_raw_data
	port map (
	din => raw_from_fpga,
	rd_en=> rawusb_rd_ok,
	rst=> reset_hard,
	clk => clk_40M,
	wr_en=> raw_fpga_wr_ok,
	dout=> raw_to_usb,
	empty => empty_raw_fpga,
	full => full_raw_usb
	);

--INSTANCIATION COMPONENTS UTLISATEURS
inst_user_ip : User_IP
	port map (
		clk_user => clk_40M,
		--clk_user => clk_80M,
		reset_ip => reset_hard,
		--Register Access
		WrRegUser => WrRegUser,
		AddRegUser => AddRegUser,
		DataRegUser_Rd => DataRegUser_Rd,
		DataRegUser_Wr => DataRegUser_Wr,
		--RAW FIFO Access
		empty_raw_to_user_ip => empty_raw_usb,
		full_raw_from_user_ip => full_raw_usb,
		empty_raw_from_user_ip => empty_raw_fpga,
		raw_from_user_ip => raw_from_fpga,
		raw_to_user_ip => raw_to_fpga,
		raw_from_user_ip_wr_ok => raw_fpga_wr_ok,
		raw_to_user_ip_rd_ok => raw_fpga_rd_ok,
		--FONCTIONNAL SIGNALS
		--
		--I2C
		scl => scl,
		sda => sda,
		active_raw_to_usb=>active_raw_to_usb,
		--
		--USER SWITCH
		user_sw2 => etat_sw_2,
		user_sw3 => etat_sw_3,
		--LDE EVENT PULSE
		led_2_i_event => led_2_i_event,
		led_3_i_event => led_3_i_event,
		led_4_i_event => led_4_i_event,
		led_5_i_event => led_5_i_event,
		led_6_i_event => led_6_i_event,
		--Debug
		debug => debug
		);

--END INSTANCIATION COMPONENT UTILISATEURS

----Instanciation de la DDR2 ==> A decommenter si utilisation DDR2 !!!!
--inst_ctrl_ddr2 : DDR2_FULL
--	port map (
--	--Signaux de controle
--	 iCLK133MHZ => clk_ddr,
--	 iRST_n 		=> ddr2_reset,
--
--	 iCLK_USER => clk_40M,
--	 iRST_USER_n => ddr2_reset,
--
--	 iVALID 		=> ddr2_valid_in,
--	 iRdWrn 		=> ddr2_write_n,
--	 iADDR  		=> add_ddr2,
--	 iDATA  		=> ddr2_data_in,
--	 oVALID 		=> ddr2_valid_out,
--	 oDATA  		=> ddr2_data_out,
--	 oBUSY  		=> ddr2_busy,
--	--Signaux DDR2
--	ddr2_dq  			=> ddr2_dq,
--	ddr2_a				=> ddr2_a,
--	ddr2_ba        	=> ddr2_ba,
--	ddr2_cke       	=> ddr2_cke,
--	ddr2_cs_n     		=> ddr2_cs_n,
--	ddr2_ras_n     	=> ddr2_ras_n,
--	ddr2_cas_n     	=> ddr2_cas_n,
--	ddr2_we_n      	=> ddr2_we_n,
--	ddr2_odt       	=> ddr2_odt,
--	ddr2_dqs       	=> ddr2_dqs,
--	ddr2_dqs_n     	=> ddr2_dqs_n,
--	ddr2_ck        	=> ddr2_ck,
--	ddr2_ck_n      	=> ddr2_ck_n,
--	ddr2_rst_dqs_div_in 	=> rst_dqs_div_in,
--	ddr2_rst_dqs_div_out	=> rst_dqs_div_out
--	);

end Behavioral;
