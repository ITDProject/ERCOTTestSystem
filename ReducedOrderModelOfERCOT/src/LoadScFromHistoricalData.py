import json
from utils import zones

def PopulationByWZ():
	f = open('DatabyZIPCodesTexasNR.json', 'r')
	jsondata = json.load(f)
	Population = {k:0.0 for k in zones}
	for k,v in jsondata.items():
		Population[v['WeatherZone']] += v['Population']
	print('Population', Population)
	return Population

def Native_Load():
	import xlrd
	wb = xlrd.open_workbook('../Data/native_load_2018/Native_Load_2018.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name('Native Load Report')
	print(ws.cell(1,0).value)
	from datetime import datetime
	# datetime_object = datetime.strptime(ws.cell(1,0).value, '%b/%d/%Y %I:%M')
	# print(datetime_object)
	# [[ws.cell_value(r,c) for c in custom_range['Generators']] for r in range(2,ws.nrows) if ws.cell_value(r,4) == 'TX']

if __name__ == "__main__":
	
	P = PopulationByWZ()
	
	from postprocessing import postprocessdata
	d, nd = postprocessdata()
	thresholdCoordinates = [28.390267, -96.842717] #Above this => Coastal, Below this => South # This is a temporary fix
	#Should check the remaining 4 points - 3 in West Zone (Lampasas) and 1 outside Texas
	for k,v in nd.items():
		if v['Coordinates'][0] < thresholdCoordinates[0]: #Equator = 0, increases in North direction
			v['WeatherZone'] = 'South'
		if v['Coordinates'][0] > thresholdCoordinates[0]: #Equator = 0, increases in North direction
			v['WeatherZone'] = 'Coastal'
	
	#Merging two dictionaries
	CombinedData = {**d, **nd}
	
	zoneslist = ['Coastal', 'East', 'FarWest', 'North', 'NorthCentral', 'South', 'SouthCentral', 'West']
	
	# LoadPerCapitaByZones = {k:[[0.0 for i in range(24)] for j in range(3)] for k in zones if k!= 'Others'}
	# LoadPerCapitaByZones['Coastal'][0] = [1.765227732,1.685250197,1.634039235,1.608641595,1.592267514,1.607746948,1.650264613,1.687572455,1.775358331,1.874455527,1.982637569,2.078473854,2.166881964,2.264832487,2.358474865,2.43545409,2.473951267,2.46819204,2.403100559,2.294920273,2.249058505,2.180250318,2.06517713,1.929974412]
	import xlrd
	wb = xlrd.open_workbook('../Data/native_load_2018/3days.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name('Sheet2')
	LoadPerCapitaByZones = {k:[[ws.cell(24*j + i + 1, n+1).value for i in range(24)] for j in range(3)] for n,k in enumerate(zoneslist)}
	LoadScenarioData = {k: [ [v['Population']*j for j in i] for i in LoadPerCapitaByZones[v['WeatherZone']] ] for k,v in CombinedData.items() if v['WeatherZone']!= 'Others'}
	print('LoadScenarioData',LoadScenarioData)
	
	f = open('ZIPCodesByCluster.json','r')
	data = json.load(f)
	
	LoadScenarioDatabyCluster = {k:[[0.0 for i in range(24)] for j in range(3)] for k in range(8)}
	# print(LoadScenarioDatabyCluster)
	
	for k,v in data['8'].items():
		for i in v['ZIPCodes']:
			for j in range(3):
				for h in range(24):
					# print(k, i, j , h)
					# print(LoadScenarioDatabyCluster[k])
					# print(LoadScenarioData[i][j][h])
					LoadScenarioDatabyCluster[int(k)][j][h] +=  LoadScenarioData[i][j][h]/1000

	# LoadScenarioDatabyCluster = {k: [ [LoadScenarioData[i][] ]  for i in v['ZIPCodes']] for k,v in data[8].items()}
	print('LoadScenarioDatabyCluster', LoadScenarioDatabyCluster)
	f = open('LoadScenarioDatabyCluster.json','w')
	json.dump(LoadScenarioDatabyCluster, f)
	f.close()
	# for k,v in d.items():
		# v['load'] = v['Population'] * LoadPerCapitaByZones[v['WeatherZone']] * 0.001 #In MW
	# for k,v in nd.items():
		# v['load'] = v['Population'] * LoadPerCapitaByZones[v['WeatherZone']] * 0.001 #In MW