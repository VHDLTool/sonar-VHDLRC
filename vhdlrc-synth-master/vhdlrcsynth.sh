files_regex=".*\.\(vhdl\|vhd\)"

ghdl --clean
if [ -z $2 ]
	then
	ghdl -a `find ./ -regex $files_regex| tr '\n' ' '`
	else
	files=${@:2}
	ghdl -a $files
	fi
ghdl -e $1

yosys -m ghdl -p "ghdl; setattr -set fsm_encoding \"auto\"; fsm -norecode -nomap -export"