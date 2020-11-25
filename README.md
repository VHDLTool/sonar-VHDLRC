# VHDL-RC  

[SonarQube](https://www.sonarqube.org) plugin for VHDL using [CNES](https://cnes.fr/en) static analyzer [Zamiacad-Rulechecker](https://github.com/VHDLTool/Zamiacad-Rulechecker/wiki) and [Yosys-framework](http://www.clifford.at/yosys/) associated with [ghdl-yosys-plugin](https://github.com/ghdl/ghdl-yosys-plugin).  
  
VHDL-RC is a ready to use SonarQube plugin  
Call `rc-scanner` instead of `sonar-scanner`.  
The external tool is wrapped in rc-scanner and is fully taken care of.  

## Features  
* Automated analysis  
* Issues import from [Zamiacad-Rulechecker](https://github.com/VHDLTool/Zamiacad-Rulechecker/wiki) analysis  
* 130 guidline rules with examples and diagrams, 31 implemented rules  
* Effective rule parameters  
* Loc and comments metrics computation   
* Library and Top entity configuration  

## Build Plugin
Without integration tests:
```
mvn clean package
```

With integration tests on SonarQube 7.9.4 version:
```
mvn clean verify -Pits -Dsonar.runtimeVersion=7.9.4
```

## Quickstart
1. Setup a SonarQube 7.9 LTS instance  
2. [Install the plugin](https://github.com/Linty-Services/VHDL-RC/wiki/Try-it-in-3-clics)  
3. [Install the rc-scanner](https://github.com/Linty-Services/VHDL-RC/wiki/Install-The-Scanner) 
4. [Run an analysis](https://github.com/Linty-Services/VHDL-RC/wiki/Run-Analysis)  
