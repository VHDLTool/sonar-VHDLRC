files_regex=".*\.\(vhdl\|vhd\)"

ghdl --clean
# Analyze the list of files passed as parameters. If invoked with no list of files, all files with vhd or vhdl extension in the project will be analyzed.
if [ -z $2 ]
	then
	ghdl -a `find ./ -regex $files_regex| tr '\n' ' '`
	else
	files=${@:2}
	ghdl -a $files
	fi
ghdl -e $1

yosys -m ghdl -p "ghdl; setattr -set fsm_encoding \"auto\"; fsm -norecode -nomap -export"
