# !/bin/bash

nxjc SumoCarUni.java 
a=$?
nxjlink -o SumoCarUni.nxj SumoCarUni
c=$?
if [ $a -eq 0 ] && [ $c -eq 0 ]; then
	sudo nxjupload SumoCarUni.nxj
fi
