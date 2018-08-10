This test case contains the 8 Bus test case of the ERCOT transmission network. The following settings are considered in building the test case for AMES V3.1:

- The effective MaxCap is set to 1000 MW for the branches between Bus i and Bus j, for all i and j ($i \neq j$) and the effective reactances are calculated and set accordingly
- Generator Cost functions' data is taken from the fuel costs considered in the 8-Bus ISO-NE test system
- An LSE is considered at each bus and it's fixed demand bid is computed based on real data i.e., the actual ERCOT load profile by weather zone
- LSE's price sensitive data is not based on any ERCOT data
- In this test case, only fixed demand bids are considered. Therefore, hybrid demand flags are set to 1
- Generator Learning is not used in this test case. Therefore M1's are set to 1. The other generator learning data used here is not based on ERCOT data
