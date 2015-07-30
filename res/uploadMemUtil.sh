#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/default-java
export AWS_CLOUDWATCH_HOME=/home/ubuntu/Downloads/CloudWatch-1.0.20.0
export AWS_CREDENTIAL_FILE=$AWS_CLOUDWATCH_HOME/awscreds.conf
export AWS_CLOUDWATCH_URL=https://monitoring.us-east-1.amazonaws.com
export PATH=$AWS_CLOUDWATCH_HOME/bin:$PATH
echo java_home=$JAVA_HOME

# get ec2 instance id
instanceid=`wget -q -O - http://169.254.169.254/latest/meta-data/instance-id`
#echo $instanceid

#Lets declare all the JVM processes we are interested in
declare -A MYMAP
MYMAP[AlfrescoMemUtil]=/home/ubuntu/alfresco-4.2.c/tomcat/temp/catalina.pid
MYMAP[JasperMemUtil]=/home/ubuntu/jasperreports-server-cp-5.0.0/apache-tomcat/temp/catalina.pid
MYMAP[TomcatMemUtil]=/home/ubuntu/apache-tomcat-7.0.53/logs/catalina-daemon.pid
MYMAP[ActiveMQMemUtil]=/home/ubuntu/WSO2/apache-activemq-5.10.0/data/activemq-youtility.youtility.in.pid
MYMAP[WSO2ISMemUtil]=/home/ubuntu/WSO2/wso2is-4.0.0/wso2carbon.pid
MYMAP[WSO2DSSMemUtil]=/home/ubuntu/WSO2/wso2dss-3.0.0/wso2carbon.pid
MYMAP[WSO2ESBMemUtil]=/home/ubuntu/WSO2/wso2esb-4.7.0/wso2carbon.pid
MYMAP[WSO2ASMemUtil]=/home/ubuntu/WSO2/wso2as-5.0.1/wso2carbon.pid
#echo "${!MYMAP[*]}"


#Lets get the PID,s for all the above processes
declare -A pids
for K in "${!MYMAP[@]}"; do 
	pids[$K]=`cat ${MYMAP[$K]}`
done
#echo 'pids array contains:' ${pids[*]}

#Now lets get the utilization of all these JVM's in a single call to java 
pidlist=`echo ${pids[*]}`
#echo 'pidlist contains:' "$pidlist"

#declare -A utilizations
jvmutils=`/home/ubuntu/Downloads/jvmtop/dist/getjvmmemutil.sh $pidlist`
echo 'values returned by java are:'$jvmutils'END'
#lets trim the string returned by java
jvmutils="$(echo -e "${jvmutils}" | sed -e 's/[[:space:]]*$//')"
#echo 'jvmutils after trim is:'$jvmutils'END'

#lets get these into an associative array
eval "declare -A utilizations=($jvmutils)"
#for key in "${!utilizations[@]}"; do
#	echo 'utilizations' $key '=' ${utilizations[$key]}
#done

#Now lets upload these to Amazon CloudWatch
for K in "${!MYMAP[@]}"; do
	metric="$K"
	echo 'metric='$K 'pid='${pids[$K]} 'and utilization='${utilizations[${pids[$K]}]}
	val=${utilizations[${pids[$K]}]}
	#echo $metric "=" $val
	mon-put-data --show-table --show-request --metric-name "$metric" --namespace "System/Linux" --dimensions "InstanceId=$instanceid" --value "$val" --unit "Percent"
done

