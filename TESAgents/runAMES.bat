set fncslibdir=C:\tesp\src\java
set ITDdir=C:\Users\rohit\Dropbox\ERCOT\ToUpload
set ames3dir=%ITDdir%\AMES-V5.0\
set apidir=%ITDdir%\TESAgents
set logfilesdir=%apidir%\logfiles

md %logfilesdir% 2> nul
md %apidir%\output 2> nul

cd %ames3dir%

set FNCS_FATAL=no
set FNCS_LOG_STDOUT=yes
set FNCS_LOG_LEVEL=DEBUG4
set FNCS_TRACE=no
set FNCS_CONFIG_FILE=%apidir%/ames.yaml

start /b cmd /c java -jar -Djava.library.path=%fncslibdir% "%ames3dir%/dist/AMES-V5.0.jar"^ > %logfilesdir%/ames.log 2^>^&1

cd %apidir%

set FNCS_FATAL=no
set FNCS_LOG_STDOUT=yes
set FNCS_LOG_LEVEL=
set FNCS_TRACE=yes
start /b cmd /c fncs_broker 3 ^>%logfilesdir%/broker.log 2^>^&1

set FNCS_CONFIG_FILE=tracer.yaml
start /b cmd /c fncs_tracer 352800s tracer.out ^>%logfilesdir%/tracer.log 2^>^&1

set FNCS_LOG_LEVEL=
set FNCS_CONFIG_FILE=loadforecast.yaml
start /b cmd /c python loadforecast.py 352800 1 ^>%logfilesdir%/loadforecast.log 2^>^&1