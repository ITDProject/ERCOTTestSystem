import numpy as np
import pypower.api as pp
import tesp_support.api as tesp;

ppc = tesp.load_json_case ('ppcase.json') # ('ercot_200.json')
ppopt_regular = pp.ppoption(VERBOSE=0, OUT_ALL=1, PF_DC=0)
rpf = pp.runpf (ppc, ppopt_regular)
#print (rpf)
#pp.printpf (rpf)

