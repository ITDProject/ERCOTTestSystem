## README

This repository contains all the relevant codes, data files, scripts, and documentation for the ERCOT Project tasks carried out under the PNNL's contract with ISU

This repository is organized in the following way:

1. AMES-V5.0 contains the latest (unstable) version of the AMES software package
2. Documentation folder contains all the relevant documentation made for the project tasks and for the software implementation
3. ReducedOrderModelOfERCOT folder contains the relevant data, scripts, and codes to generate a generic N Clusters (nodes) of the ERCOT Transmission system's load and generation data. It also contains the data which has a 3 day load scenario for the 8 bus test case.
4. TESAgents folder contains scripts to compile AMES, run AMES along with other TESAgents (with FNCS as the network co-simulator). As of now, loadforecast.py is a TESAgent which is meant to forecast the load in the Day-Ahead Market (for the LSEs load forecast).
