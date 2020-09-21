top_module="top"
files_regex=".*\.\(vhdl\|vhd\)"
yosys $MODULE -m ghdl -p "ghdl `find ./ -regex $files_regex| tr '\n' ' '` -e $top_module; setattr -set fsm_encoding \"auto\" ; fsm -norecode -nomap -export"

kiss2array=$(find ./ -regex .*\.kiss2)
fsm_regex="^state_(.)*"
touch ./report_CNE_02000.xml
echo '<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<rc:ReportRule xmlns:rc="RULECHECKER" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<rc:RuleCheckerVersion>RC-Synth-1.00</rc:RuleCheckerVersion>
<rc:RuleName>CNE_02000</rc:RuleName>
<rc:ExecutionDate></rc:ExecutionDate>
</rc:ReportRule>' > report_CNE_02000.xml
touch ./report_STD_03900.xml
echo '<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<rc:ReportRule xmlns:rc="RULECHECKER" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<rc:RuleCheckerVersion>RC-Synth-1.00</rc:RuleCheckerVersion>
<rc:RuleName>STD_03900</rc:RuleName>
<rc:ExecutionDate></rc:ExecutionDate>
</rc:ReportRule>' > report_STD_03900.xml
for kiss2 in $kiss2array
do
    filename="${kiss2##*/}" 
	statename="${filename##*\\}"
	statename="${statename%\$*}"
	modulename="${filename%-*}"
	file=$(find ./ -regex .*$modulename".vhd\(l\)?")
	line=$(grep -n -E "signal(\s|\n|\t|\r)*$statename"  $file)
	statetype=$(echo $line | cut -d : -f 3)
	if [[ `echo $statetype` == std* ]]||[[ `echo $statetype` == ieee* ]]
	then
		linenumber=$(echo $line | cut -d : -f 1)
		xmlstarlet ed --inplace --subnode "rc:ReportRule" --type elem -n 'rc:RuleFailureTmp' report_STD_03900.xml
		xmlstarlet ed --inplace --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:File' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:Line' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:Entity' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:Architecture' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:STD_03900' report_STD_03900.xml
		xmlstarlet ed --inplace --subnode "rc:ReportRule/rc:RuleFailureTmp/rc:STD_03900" --type elem -n 'rc:SonarQubeMsg' report_STD_03900.xml
		xmlstarlet ed --inplace --subnode "rc:ReportRule/rc:RuleFailureTmp/rc:STD_03900/rc:SonarQubeMsg" --type elem -n 'rc:SonarError' --subnode "rc:ReportRule/rc:RuleFailureTmp/rc:STD_03900/rc:SonarQubeMsg" --type elem -n 'rc:SonarRemediationMsg' report_STD_03900.xml
		xmlstarlet ed --inplace -u '/rc:ReportRule/rc:RuleFailureTmp/rc:Line' -v "$linenumber"  -u '/rc:ReportRule/rc:RuleFailureTmp/rc:Entity' -v "$modulename" -u '/rc:ReportRule/rc:RuleFailureTmp/rc:File' -v "$file" -u '/rc:ReportRule/rc:ExecutionDate' -v "`date`" -u '/rc:ReportRule/rc:RuleFailureTmp/rc:STD_03900/rc:SonarQubeMsg/rc:SonarError' -v "State machine signal $statename uses wrong type." -u '/rc:ReportRule/rc:RuleFailureTmp/rc:STD_03900/rc:SonarQubeMsg/rc:SonarRemediationMsg' -v "Use enumerated type instead." report_STD_03900.xml 
		xmlstarlet ed --inplace -r "rc:ReportRule/rc:RuleFailureTmp" -v "RuleFailure" report_STD_03900.xml
	fi

	if [[ ! $statename =~ $fsm_regex ]]
	then
		linenumber=$(echo $line | cut -d : -f 1)
		xmlstarlet ed --inplace --subnode "rc:ReportRule" --type elem -n 'rc:RuleFailureTmp' report_CNE_02000.xml
		xmlstarlet ed --inplace --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:File' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:Line' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:Entity' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:Architecture' --subnode "rc:ReportRule/rc:RuleFailureTmp" --type elem -n 'rc:CNE_02000' report_CNE_02000.xml
		xmlstarlet ed --inplace --subnode "rc:ReportRule/rc:RuleFailureTmp/rc:CNE_02000" --type elem -n 'rc:SonarQubeMsg' report_CNE_02000.xml
		xmlstarlet ed --inplace --subnode "rc:ReportRule/rc:RuleFailureTmp/rc:CNE_02000/rc:SonarQubeMsg" --type elem -n 'rc:SonarError' --subnode "rc:ReportRule/rc:RuleFailureTmp/rc:CNE_02000/rc:SonarQubeMsg" --type elem -n 'rc:SonarRemediationMsg' report_CNE_02000.xml
		xmlstarlet ed --inplace -u '/rc:ReportRule/rc:RuleFailureTmp/rc:Line' -v "$linenumber"  -u '/rc:ReportRule/rc:RuleFailureTmp/rc:Entity' -v "$modulename" -u '/rc:ReportRule/rc:RuleFailureTmp/rc:File' -v "$file" -u '/rc:ReportRule/rc:ExecutionDate' -v "`date`" -u '/rc:ReportRule/rc:RuleFailureTmp/rc:CNE_02000/rc:SonarQubeMsg/rc:SonarError' -v "State machine $statename is miswritten." -u '/rc:ReportRule/rc:RuleFailureTmp/rc:CNE_02000/rc:SonarQubeMsg/rc:SonarRemediationMsg' -v "Change signal name $statename to comply with $fsm_regex" report_CNE_02000.xml 
		xmlstarlet ed --inplace -r "rc:ReportRule/rc:RuleFailureTmp" -v "RuleFailure" report_CNE_02000.xml
	fi
done