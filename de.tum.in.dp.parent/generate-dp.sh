#*******************************************************************************
# Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*******************************************************************************
#!/bin/bash

declare -a arr=("de.tum.in.dp.bluetooth" "de.tum.in.dp.bluetooth.machine" "de.tum.in.dp.cache" "de.tum.in.dp.commons" "de.tum.in.dp.dependencies")
b="_build.xml"
c="/"
home_dir="/Users/AMIT/IoT_IDP/Pi/"

for i in "${arr[@]}"
do
   echo "Generating Deployment Package for $i"
   /usr/local/bin/ant -d -buildfile  $home_dir$i$c$i$b
done