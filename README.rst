## README

The below published paper is the main documentation for the ERCOT Test System.  The appendices of this paper provide detailed implementation instructions for the ERCOT Test System.
 
Swathi Battula, Leigh Tesfatsion, and Thomas E. McDermott, "An ERCOT Test System for Market Design Studies,” Energy, Vol. 275, October, 2020.  DOI:10.1016/j.apenergy.2020.115182. Working Paper Preprint:  https://lib.dr.iastate.edu/econ_workingpapers/79/
 
Abstract: An open source test system is developed that permits the dynamic modeling of centrally-managed wholesale power markets operating over high-voltage transmission grids. In default mode, the test system models basic operations in the Electric Reliability Council of Texas (ERCOT): namely, centrally-managed day-ahead and real-time markets operating over successive days, with congestion handled by locational marginal pricing. These basic operational features characterize all seven U.S. energy regions organized as centrally-managed wholesale power markets. Modeled participants include dispatchable generators, load-serving entities, and non-dispatchable generation such as unfirmed wind and solar power. Users can configure a broad variety of parameters to study basic market and grid features under alternative system conditions. Users can also easily extend the test system's Java/Python software classes to study modified or newly envisioned market and grid features. Finally, the test system is integrated with a high-level simulation framework that permits it to function as a software component within larger systems, such as multi-country systems or integrated transmission and distribution systems. Detailed test cases with 8-bus and 200-bus transmission grids are reported to illustrate these test system capabilities.
 
This repository contains all the relevant codes, data files, scripts, and documentation for the ERCOT Project tasks carried out under Iowa State University's "ERCOT Contract" with the Pacific Northwest National Laboratory (PNNL).

This repository is organized in the following way:

1. The ERCOT_Test_Systems folder contains relevant data, scripts, and code pertaining to the development of ERCOT Test Systems.

2. The ERCOT Test System requires AMES V5.0 to be installed.  The `AMES V5.0’ directory provides instructions and necessary files for this installation.  In addition, the `AMES V5.0’ directory includes a `TESAgents’ subdirectory that provides scripts to compile AMES V5.0, and to run AMES V5.0 along with other TES Agents, using the Framework for Network Co-Simulation (FNCS).   NetLoadForecastDAM.py and NetLoadForecastRTM.py are Python implementations for TES Agents that can provide load forecasts for the AMES V5.0 Day-Ahead Market (DAM) and Real-Time Market (RTM), respectively.

3. The License folder describes the BSD 3-Clause ("New" or "Revised") License under which the materials at this repository are being released as Open Source Software.
