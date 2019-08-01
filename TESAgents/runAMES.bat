set fncslibdir=C:\tesp\src\java
set ITDdir=C:\Users\swathi\Dropbox\AMESLatestVersion
set AmesVersion=AMES-V5.0
rem AMES-V5.0 AMES-v3.2
set amesdir=%ITDdir%\%AmesVersion%\
set apidir=%ITDdir%\TESAgents
set logfilesdir=%apidir%\logfiles

md %logfilesdir% 2> nul
rem md %apidir%\output 2> nul

cd %ames3dir%

set FNCS_FATAL=no
set FNCS_LOG_STDOUT=yes
set FNCS_LOG_LEVEL=DEBUG4
set FNCS_TRACE=no
set FNCS_CONFIG_FILE=%apidir%/YamlFiles/ames.yaml

start /b cmd /c java -jar -Djava.library.path=%fncslibdir% "%amesdir%/dist/%AmesVersion%.jar"^ > %logfilesdir%/ames.log 2^>^&1

cd %apidir%

set FNCS_FATAL=no
set FNCS_LOG_STDOUT=yes
set FNCS_LOG_LEVEL=
set FNCS_TRACE=no
start /b cmd /c fncs_broker 3 ^>%logfilesdir%/broker.log 2^>^&1

set FNCS_CONFIG_FILE=YamlFiles/tracer.yaml
start /b cmd /c fncs_tracer 352800s ^>%logfilesdir%/tracer.log 2^>^&1

set FNCS_CONFIG_FILE=YamlFiles/loadforecast.yaml
set FNCS_LOG_LEVEL=DEBUG4
start /b cmd /c python loadforecast.py 352800 1 ^>%logfilesdir%/loadforecast.log 2^>^&1