files_regex=".*\.\(vhdl\|vhd\)"

cd ./build_dir

ghdl --clean
# Analyze the list of files passed as parameters. If invoked with no list of files, all files with vhd or vhdl extension in the project will be analyzed.
if [ -z $3 ]
    then
    ghdl -a $2 `find ../ -regex $files_regex| tr '\n' ' '`
    else
    files=${@:3}
    ghdl -a $2 $files
    fi
ghdl -e $2 $1