WORK_DIR=build_dir
VHDL_DIR=../

mkdir $WORK_DIR
cd $WORK_DIR

#clean working folder
ghdl --clean
rm unisim-obj93.cf
rm work-obj93.cf

#additionnal library xilinx to allow elaboration
ghdl -a --work=unisim  -fexplicit -fsynopsys $VHDL_DIR/xil_lib/unisim_VPKG.vhd
ghdl -a --work=unisim  -fexplicit -fsynopsys $VHDL_DIR/xil_lib/unisim_VCOMP.vhd

#project files
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/mlite_pack.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/ddr_ctrl.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/plasma.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/uart.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/eth_dma.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/alu.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/ram_xilinx.vhd
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/bus_mux.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/control.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/mem_ctrl.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/mult.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/pipeline.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/pc_next.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/reg_bank.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/shifter.vhd 
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/cache.vhd
ghdl -a  -fexplicit -fsynopsys $VHDL_DIR/mlite_cpu.vhd

#elaborate design need to be done in yosys with command ghdl -fexplicit -fsynopsys plasma
ghdl -e -fexplicit -fsynopsys plasma
