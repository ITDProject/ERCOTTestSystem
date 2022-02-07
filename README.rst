## README

The Applied Energy article cited below, together with the accompanying linked working paper version of this article, constitute the main documentation for the ERCOT Test System.  The article and working paper appendices provide detailed implementation instructions for the ERCOT Test System.
 
Swathi Battula, Leigh Tesfatsion, and Thomas E. McDermott (2020), "An ERCOT Test System for Market Design Studies,” Applied Energy, Vol. 275, October.  DOI:10.1016/j.apenergy.2020.115182. Working Paper Preprint:  https://lib.dr.iastate.edu/econ_workingpapers/79/
 
Abstract: An open source test system is developed that permits the dynamic modeling of centrally-managed wholesale power markets operating over high-voltage transmission grids. In default mode, the test system models basic operations in the Electric Reliability Council of Texas (ERCOT): namely, centrally-managed day-ahead and real-time markets operating over successive days, with congestion handled by locational marginal pricing. These basic operational features characterize all seven U.S. energy regions organized as centrally-managed wholesale power markets. Modeled participants include dispatchable generators, load-serving entities, and non-dispatchable generation such as unfirmed wind and solar power. Users can configure a broad variety of parameters to study basic market and grid features under alternative system conditions. Users can also easily extend the test system's Java/Python software classes to study modified or newly envisioned market and grid features. Finally, the test system is integrated with a high-level simulation framework that permits it to function as a software component within larger systems, such as multi-country systems or integrated transmission and distribution systems. Detailed test cases with 8-bus and 200-bus transmission grids are reported to illustrate these test system capabilities.
 
This repository contains all relevant code, data files, scripts, and documentation for the ERCOT Project tasks carried out under Iowa State University's "ERCOT Contract" with the Pacific Northwest National Laboratory (PNNL).

As described at greater length in the above cited and linked documentation, the ERCOT Test System has grid and market components.  This repository provides code and data for these components, organized in the following way:

* The 'ERCOTGridComponent' directory provides relevant data, scripts, and code pertaining to constructing a user-specified number of synthetic buses for the synthetic grid.

  The user needs to navigate to the 'ClusteringAlgorithm' folder located in the 'SyntheticBusConstructionMethod' subdirectory and specify NB, the desired number of synthetic buses, by executing the following command: 

  python postprocessing.py NB

  ERCOT input data are stored in the ‘Data’ folder.  These raw data are put into required formatting by means of ‘dataprocessing.py’ and ‘utils.py’.  The file 'postprocessing.py' produces synthetic bus attributes from the processed ERCOT input data and stores these synthetic bus attributes in a file ‘NBBusData.json’.  

  The ERCOT input data in the ‘Data’ folder include Zip code data as well as the type, location, and MW capacity of generators and loads.  The synthetic bus attributes produced by ‘postprocessing.py’ include the bus locations of loads, dispatchable generators, and non-dispatchable generators.

  As explained in the above published paper, Delaunay Triangulation is used to construct synthetic lines for the synthetic grid. 

* The `ERCOTTestCases’ directory contains relevant data, scripts, and code pertaining to sample test cases developed and conducted using the ERCOT Test System.

  The user needs to navigate to the 'src' subdirectory and execute the following commands to produce profiles for load and non-dispatchable generation.

  To produce a load profile, the user needs to execute the following command:  
  python LoadScenarioMethod.py NB FileName Month NDays

  To produce a wind profile, the user needs to execute the following command:  
  python WindScenarioMethod.py NB FileName Month NDays

  To produce a solar profile, the user needs to execute the following command: 
  python SolarScenarioMethod.py NB FileName Month NDays

  The above commands depend on the following user-specified parameters:   NB - The desired number of buses for the synthetic grid;  FileName – The name of the Excel file that contains system-wide ERCOT data;  Month – The name of the Excel sheet that represents the month of the year in numeric value; and NDays – The number of days for which load and generation profiles are required.

  Note: The user needs to make sure that the input Excel files that contain system-wide ERCOT data (i) have their sheets named in accordance with the numerical value of the month of the year, and (ii) have data for a number of days that is at least as large as NDays. Sample input Excel sheets are provided in the 'Data' subdirectory.
  The output files generated in Steps 1 and 2, above, are used to construct input test case files for AMES V5.0, the market component of the ERCOT Test System described in Steps 3 and 4, below.  Sample input test case files are provided in the 'InputTestCaseFiles' subdirectory.

* The ERCOT Test System requires AMES V5.0 to be installed.  The `AMES V5.0’ directory provides instructions and necessary files for this installation.  In addition, the `AMES V5.0’ directory includes a `TESAgents’ subdirectory that provides scripts to compile and run AMES V5.0.  Moreover, if additional user-specified components have been provided for a larger software system that includes AMES V5.0, this subdirectory provides scripts to compile and run all of these components using the Framework for Network Co-Simulation (FNCS).  NetLoadForecastDAM.py and NetLoadForecastRTM.py are Python implementations for TES Agents that can provide load forecasts for the AMES V5.0 Day-Ahead Market (DAM) and Real-Time Market (RTM), respectively.

* The License folder describes the BSD 3-Clause ("New" or "Revised") License under which the materials at this repository are being released as Open Source Software.
