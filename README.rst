## README

The below published paper is the main documentation for the ERCOT Test System.  The appendices of this paper provide detailed implementation instructions for the ERCOT Test System.
 
Swathi Battula, Leigh Tesfatsion, and Thomas E. McDermott, "An ERCOT Test System for Market Design Studies,” Energy, Vol. 275, October, 2020.  DOI:10.1016/j.apenergy.2020.115182. Working Paper Preprint:  https://lib.dr.iastate.edu/econ_workingpapers/79/
 
Abstract: An open source test system is developed that permits the dynamic modeling of centrally-managed wholesale power markets operating over high-voltage transmission grids. In default mode, the test system models basic operations in the Electric Reliability Council of Texas (ERCOT): namely, centrally-managed day-ahead and real-time markets operating over successive days, with congestion handled by locational marginal pricing. These basic operational features characterize all seven U.S. energy regions organized as centrally-managed wholesale power markets. Modeled participants include dispatchable generators, load-serving entities, and non-dispatchable generation such as unfirmed wind and solar power. Users can configure a broad variety of parameters to study basic market and grid features under alternative system conditions. Users can also easily extend the test system's Java/Python software classes to study modified or newly envisioned market and grid features. Finally, the test system is integrated with a high-level simulation framework that permits it to function as a software component within larger systems, such as multi-country systems or integrated transmission and distribution systems. Detailed test cases with 8-bus and 200-bus transmission grids are reported to illustrate these test system capabilities.
 
This repository contains all the relevant codes, data files, scripts, and documentation for the ERCOT Project tasks carried out under Iowa State University's "ERCOT Contract" with the Pacific Northwest National Laboratory (PNNL).

As described in the above published paper, the ERCOT Test System has grid and market components.
This repository provides code and data for these components, organized in the following way:

1. The 'ERCOTGridComponent' directory provides relevant data, scripts, and code pertaining to constructing a user-specified number of synthetic buses for the synthetic grid.

The user needs to navigate to the 'ClusteringAlgorithm' folder located in the 'SyntheticBusConstructionMethod' subdirectory and specify NB, the desired number of synthetic buses, by executing the following command: 

python postprocessing.py NB

The above command outputs a 'NBBusData.json' file that contains bus attribute data, such as the bus location of load, dispatchable generators, and non-dispatchable generators. 

As explained in the above published paper, Delaunay Triangulation is used to construct synthetic lines for the synthetic grid. 

2. TThe `ERCOTTestCases’ directory contains relevant data, scripts, and code pertaining to sample test cases developed and conducted using the ERCOT Test System.
The user needs to navigate to the 'src' subdirectory and execute the following commands to produce profiles for load and non-dispatchable generation.

TTo produce a load profile, the user needs to execute the following command:  python LoadScenarioMethod.py NB FileName Month NDays
To produce a wind profile, the user needs to execute the following command:  python WindScenarioMethod.py NB FileName Month NDays
To produce a solar profile, the user needs to execute the following command:  python SolarScenarioMethod.py NB FileName Month NDays

The above commands depend on the following user-specified parameters:   NB - The desired number of buses for the synthetic grid;  FileName – The name of the Excel file that contains system-wide ERCOT data;  Month – The name of the Excel sheet ???? that represents the month of the year in numeric value; and NDays – The number of days for which load and generation profiles are required.

Note: The user needs to make sure that the input Excel files that contain system-wide ERCOT data (i) have their sheets named in accordance with the numerical value of the month of the year, and (ii) have data for a number of days that is at least as large as NDays. Sample input Excel sheets are provided in the 'Data' subdirectory.
The output files generated in Steps 1 and 2, above, are used to construct input test case files for AMES V5.0, the market component of the ERCOT Test System described in Steps 3 and 4, below.  Sample input test case files are provided in the 'InputTestCaseFiles' subdirectory.

3. The ERCOT Test System requires AMES V5.0 to be installed.  The `AMES V5.0’ directory provides instructions and necessary files for this installation.  In addition, the `AMES V5.0’ directory includes a `TESAgents’ subdirectory that provides scripts to compile AMES V5.0, and to run AMES V5.0 along with other TES Agents, using the Framework for Network Co-Simulation (FNCS).   NetLoadForecastDAM.py and NetLoadForecastRTM.py are Python implementations for TES Agents that can provide load forecasts for the AMES V5.0 Day-Ahead Market (DAM) and Real-Time Market (RTM), respectively.

4. The License folder describes the BSD 3-Clause ("New" or "Revised") License under which the materials at this repository are being released as Open Source Software.
