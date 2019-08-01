##Agent-based Modeling of Electricity Systems (AMES) V5.0

AMES V5.0 is based on both AMES V3.0 and AMES V4.0, and is currently under development.

AMES V5.0 is planned to extend previous versions in the following ways:

1. Daily Day-Ahead Market SCUC Optimization is carried out using the Pyomo Optimization Model wrapped by PSST
2. Real-Time SCED Optimization is carried out every M minutes (where M is user specified)
3. Non-dispatchable generation (NDG) can now be included as a separate NDGenAgent, where it can submit its forecast to the ISO in Day-Ahead Market. Net load = load - NDG is being considered in the DAM/RTM

Note: The SCUC input data in ERCOT's test is generated randomly.

#### COMPILE & RUN
AMES V5.0 is based on FNCS. Therefore a folder called TESAgents is being used to simulate (start, stop, postprocess the data) the TESAgents. Compile_AMES.bat and runAMES is used to compile and run the AMES. Note: TESAgents folder also contains loadforecast.py which forecasts the load profile for the LSEs.
