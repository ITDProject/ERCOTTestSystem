import json
from data2html import Map, generateHTML
from utils import gentypes, LoadPerCapitaByZones, LoadPerCapitaOfERCOT, zones
import numpy as np

def postprocessdata():
	f = open('DatabyZIPCodesTexas.json', 'r')
	data = json.load(f)
	f.close()
	nd = {k:v for k,v in data.items() if v['WeatherZone'] == ''} # items where no weather zone is asssigned
	d = {k:v for k,v in data.items() if v['WeatherZone'] != ''}
	f = open('DatabyZIPCodesTexasNR.json', 'w')
	json.dump(d,f)
	f.close()
	return d, nd

def postprocessingmain(Case, bgen):
	d, nd = postprocessdata()
	thresholdCoordinates = [28.390267, -96.842717] #Above this => Coastal, Below this => South # This is a temporary fix
	#Should check the remaining 4 points - 3 in West Zone (Lampasas) and 1 outside Texas

	for k,v in nd.items():
		if v['Coordinates'][0] < thresholdCoordinates[0]: #Equator = 0, increases in North direction
			v['WeatherZone'] = 'South'
		if v['Coordinates'][0] > thresholdCoordinates[0]: #Equator = 0, increases in North direction
			v['WeatherZone'] = 'Coastal'

	# Assigning load to each node by scaling population with LoadPerCapita
	if Case == 'WeatherZones':
		for k,v in d.items():
			v['load'] = v['Population'] * LoadPerCapitaByZones[v['WeatherZone']] * 0.001 #In MW
		for k,v in nd.items():
			v['load'] = v['Population'] * LoadPerCapitaByZones[v['WeatherZone']] * 0.001 #In MW
	elif Case == 'ERCOT':
		for k,v in d.items():
			v['load'] = v['Population'] * LoadPerCapitaOfERCOT * 0.001 #In MW
		for k,v in nd.items():
			v['load'] = v['Population'] * LoadPerCapitaOfERCOT * 0.001 #In MW

	print('Length of d: ', len(d))
	print('Length of nd: ', len(nd))

	#Merging two dictionaries
	CombinedData = {**d, **nd}
	print('Length of Combined data: ', len(CombinedData))
	CombinedDataERCOT = {k:v for k,v in CombinedData.items() if v['WeatherZone'] != 'Others'}
	return CombinedDataERCOT

def plotmap(filename, CombinedData, bgen):
	pointslistbyZIPCodes = [{'name': k, 'coordinates': [v['Coordinates'][0], v['Coordinates'][1]], 'type': 'aggr', 'power' : v['load'], 'zone': v['WeatherZone'], 'county': v['County']} for k,v in CombinedData.items() if v['WeatherZone'] != 'Others'] # Previously gentype was 'aggr', now it's changed to v['WeatherZone']

	print('Length of points list by ZIP Codes: ', len(pointslistbyZIPCodes))
	if (bgen):
		from data2html import data2htmlmain
		pointslistGen = data2htmlmain()
		pointslistbyZIPCodes = pointslistbyZIPCodes + pointslistGen

	map = Map(gentypes)
	generateHTML(pointslistbyZIPCodes, map, filename)

	# #Code to generate NR points on a map:
	# pointslistNR = [{'name': k, 'coordinates': [v['Coordinates'][0], v['Coordinates'][1]], 'type': 'aggr', 'power' : 10, 'zone': 'North'} for k,v in nd.items()]
	# filename = 'NRZIPCodes.html'
	# map = Map(gentypes)
	# generateHTML(pointslistNR, map, filename)

def ClusterByWeatherZones(CombinedDataERCOT):
	from data2html import Cluster
	data = {z: [[v['Coordinates'][0], v['Coordinates'][1], v['load'], 'Load', k] for k,v in CombinedDataERCOT.items() if z == v['WeatherZone']] for z in zones if z != 'Others'}
	Output = {n : {'Coordinates': [0.0, 0.0], 'load': 0.0, 'County': 'None', 'WeatherZone': k} for n,k in enumerate(zones) if k != 'Others'}
	# For each zone:
	# Compute center using closed form solution:
	for n,z in enumerate(zones):
		if z != 'Others':
			Output[n]['Coordinates'] = [sum([l[i]*l[2] for l in data[z]])/sum([l[2] for l in data[z]]) for i in range(2)]
			Output[n]['load'] = sum([l[2] for l in data[z]])
	Result = {'WZ'+str(k) : v for k,v in Output.items()}
	return Result

if __name__ == '__main__': # main function
	Case = 'WeatherZones'
	bgen = False

	CombinedLoadDataERCOT = postprocessingmain(Case, bgen)

	# To cluster the load data over ERCOT for a Cluster Number:
	from data2html import case_2, generateHTML, Map
	LoadDataList = [[v['Coordinates'][0], v['Coordinates'][1], v['load'], 'Load', k] for k,v in CombinedLoadDataERCOT.items() if v['load'] != 0 if v['WeatherZone'] != 'Others'] # Passing k to the list, however it is not used currently in Cluster data structure
	from data2html import data2htmlmain
	pointslistGen = data2htmlmain()
	gentypessubllist = ['Onshore Wind Turbine', 'Natural Gas Fired Combined Cycle', 'Conventional Steam Coal', 'Solar Photovoltaic', 'Natural Gas Steam Turbine', 'Nuclear', 'Natural Gas Internal Combustion Engine', 'Natural Gas Fired Combustion Turbine']

	GenDataList = [[i['coordinates'][0], i['coordinates'][1], i['power'], i['type']] for i in pointslistGen if i['power'] != 0 if i['zone'] != 'Others' if i['type'] in gentypessubllist]

	CombinedLoadGenList = LoadDataList + GenDataList
	# CombinedLoadGenList = GenDataList

	#print('Printing GenDataList: ', GenDataList, flush = True)
	print('Input data to GenDataList:', len(GenDataList))
	print('Input data to LoadDataList:', len(LoadDataList))
	print('Input data to case_2:', len(CombinedLoadGenList))
	#print('Printing CombinedLoadGenList: ', CombinedLoadGenList, flush = True)

	clusterno = [1800-100*i for i in range(17)] # + [50, 30, 20, 10, 8] # [8] - M1 # range(18) 1915 - Preprocessed (Removed overlapping gen data in Clustering Algorithm i.e., retained only plants)

	print('Clusterno:', clusterno)

	metric='euclidean'
	#cdata = case_2(CombinedLoadGenList, metric, clusterno)
	ZIPCodesByCluster, ClusterDataJsonFormat = case_2(CombinedLoadGenList, metric, clusterno)
	f = open('ZIPCodesByCluster.json','w')
	json.dump(ZIPCodesByCluster,f)
	f.close()
	f = open('ABC.json','w')
	json.dump(ClusterDataJsonFormat,f)
	f.close()

	# from data2html import Cluster, clustertopointslist # temporary
	# # r2 = [pointslistGen] + [clustertopointslist(i) for i in cdata] # temporarily added 1st term
	# r2 = [clustertopointslist(i) for i in cdata]
	# # print('cdata[23] ', cdata[23])
	# filenames = ['outputcase2_'+str(i)+'.html' for i in range(len(r2))]

	# #Initialize Maps for case 2
	# from utils import gentypes
	# MapObj = [Map(gentypes) for i in range(len(filenames))]
	# #Generate HTML for all the files in case 2:
	# for i in range(len(filenames)):
	# 	generateHTML(r2[i], MapObj[i], filenames[i])

	# metric='euclidean'
	# cdata = case_2(CombinedLoadGenList, metric, clusterno)
	# r2 = [clustertopointslist(i) for i in cdata]
	# print('r2', r2)

	# # Plotting Map using Combined Data and bgen flag (flag to include generators in the map)
	# filename = 'LoadbyZIPCodes.html'
	# # plotmap(filename, CombinedLoadDataERCOT, bgen) # Direct plot of load
	# plotmap(filename, CombinedLoadDataERCOT, bgen) # Clustered load data by weather zones # Alternatively use CombinedLoadGenList or r2 (Previously) instead of CombinedLoadDataERCOT
