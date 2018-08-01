set ITDdir=C:\Users\rohit\Dropbox\ERCOT\ToUpload
set amesdir=%ITDdir%\AMES-V5.0\
set tesdir=%ITDdir%\TESAgents

cd %amesdir%
call ant clean
call ant jar
cd %tesdir%