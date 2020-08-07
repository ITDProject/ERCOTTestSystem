## README



The 'ERCOTGridComponent' directory provides relevant data, scripts, and code pertaining to constructing a user-specified number of synthetic buses for the synthetic grid.


ERCOT input data are stored in the ‘Data’ folder.  These raw data are put into required formatting by means of ‘dataprocessing.py’ and ‘utils.py’.  The file 'postprocessing.py' produces synthetic bus attributes from the processed ERCOT input data and stores these synthetic bus attributes in a file ‘NBBusData.json’.  

The ERCOT input data in the ‘Data’ folder include Zip code data as well as the type, location, and MW capacity of generators and loads.  The synthetic bus attributes produced by ‘postprocessing.py’ include the bus locations of loads, dispatchable generators, and non-dispatchable generators.


