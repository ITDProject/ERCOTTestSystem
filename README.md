## Agent-based Modeling of Electricity Systems (AMES) V5.0

AMES V5.0 is based on both AMES V3.0 and AMES V4.0, and is currently under development.

AMES V5.0 is planned to extend previous versions in the following ways:

1. Daily Day-Ahead Market SCUC Optimization is carried out using the Pyomo Optimization Model wrapped by PSST
2. Real-Time SCED Optimization is carried out every M minutes (where M is user specified)
3. Non-dispatchable generation (NDG) can now be included as a separate NDGenAgent, where it can submit its forecast to the ISO in Day-Ahead Market. Net load = load - NDG is being considered in the DAM/RTM
4. Storage Units can now be added to AMES input test cases and passed on to the pyomo optimization model where StorageFlag sets whether these units are used or not.
	Note: As of now, pyomo optimization model contains the constraints which are flagged by the StorageFlag (read from the test case). However the storage data is yet to be initialised in the pyomo variables.
5. Non-dispatchable generation is used in two ways: either as net demand at the buses or as an explicit declaration in the test case which is flagged by NDGFlag. As of now, in the explicit case, AMES writes these NDG hourly values to the input file of pyomo. However, they are not being read into the variables of pyomo optimization model and the NDGFlag in the pyomo optimization model is made 0 (read from the test case).

#### DATA
Under the DATA folder in AMES V5.0, two test cases are given
- 8 Bus test case based on ISO-NE (8BusTestCase_8gen_nolearning.dat)
- 8 Bus test case based on ERCOT data (8BusTestCase1000Ren.dat)

Note: The SCUC input data in ERCOT's test is generated randomly.

#### COMPILE & RUN
AMES V5.0 is based on FNCS. Therefore a folder called TESAgents is being used to simulate (start, stop, postprocess the data) the TESAgents. Compile_AMES.bat and runAMES is used to compile and run the AMES. Note: TESAgents folder also contains loadforecast.py which forecasts the load profile for the LSEs.
