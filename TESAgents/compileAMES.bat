set ITDdir=C:\Users\swathi\Dropbox\AMESLatestVersion
set ames3dir=%ITDdir%\AMES-V5.0\
set tesdir=%ITDdir%\TESAgents

cd %ames3dir%
call ant clean
call ant jar
cd %tesdir%