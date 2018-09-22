import numpy as np
import pypower.api as pp
import tesp_support.api as tesp;

# ppc = tesp.load_json_case ('ppcase.json')
ppc = tesp.load_json_case ('ercot_200.json')
ppopt_regular = pp.ppoption(VERBOSE=1, OUT_ALL=1, OUT_ALL_LIM=2, PF_DC=0, PF_ALG=1, OUT_RAW=1)
rpf = pp.runpf (ppc, ppopt_regular)
#print (rpf)
#pp.printpf (rpf)

