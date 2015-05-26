#!/bin/bash

declare -a arr=("de.tum.in.dp.bluetooth" "de.tum.in.dp.bluetooth.machine" "de.tum.in.dp.cache" "de.tum.in.dp.commons" "de.tum.in.dp.dependencies")
b="_build.xml"
c="/"
home_dir="/Users/AMIT/IoT_IDP/Pi/"

for i in "${arr[@]}"
do
   echo "Generating Deployment Package for $i"
   ant -d -buildfile  $home_dir$i$c$i$b
done