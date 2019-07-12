#!/bin/bash

export fncslibdir=/home/osboxes/grid/repository/fncs/java/build
export ITDdir=~/grid/repository/ERCOTTestSystem
export AmesVersion=AMES-V5.0
export TestSystem=TESAgents
# AMES-V5.0 AMES-v3.2
export amesdir=$ITDdir/$AmesVersion
export apidir=$ITDdir/$TestSystem
export logfilesdir=$apidir/logfiles
#export outfilesdir=$apidir/output
export inputfile=
#export inputfile=$amesdir/DATA/3BusTestCase.dat

# to redirect error and output
# > /dev/null 2>&1
#  is the same as 
# &>/dev/null

mkdir $logfilesdir &>/dev/null
#mkdir $outfilesdir &>/dev/null

#cd $amesdir
cd $apidir

export FNCS_TRACE=yes
export FNCS_FATAL=no
export FNCS_LOG_STDOUT=yes
#export FNCS_LOG_LEVEL=
fncs_broker 3 &> $logfilesdir/broker.log &

export FNCS_TRACE=no
export FNCS_FATAL=no
export FNCS_LOG_STDOUT=yes
#export FNCS_LOG_LEVEL=DEBUG4
export FNCS_CONFIG_FILE=ames.yaml
#java -jar -Djava.library.path=$fncslibdir "$amesdir/dist/$AmesVersion.jar" $inputfile &> $logfilesdir/ames.log &

#export FNCS_LOG_LEVEL=
export FNCS_CONFIG_FILE=tracer.yaml
fncs_tracer 352800s tracer.out &> $logfilesdir/tracer.log &

#export FNCS_LOG_LEVEL=
export FNCS_CONFIG_FILE=loadforecast.yaml
python3 loadforecast.py 352800 1 &> $logfilesdir/loadforecast.log &


