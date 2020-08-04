import json

if __name__ == "__main__":

	NDay = 6
	NNode = 8
	Year = 2019
	Month = 7
	Day = 23
	MarketType = 'DAM' # indicate 'DAM' or 'RTM'
	Name = 'SolarData.' + str(Year)
	sheetName = str(Month) # Need to change this if we want to use 5-min RTM values
	import xlrd
	wb = xlrd.open_workbook('../Data/Solar/'+ Name+ '.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name(sheetName)
	SolarData = [[ws.cell(24*j + i + 1, 2).value for i in range(24)] for j in range(NDay)] # 5 - for DAM, 6 - for RTM
	
	f = open(str(NNode)+'NodeData.json','r')
	data = json.load(f)
	SolarScenarioDatabyCluster = []
	TotalSolarCapacity = 0
	
	for n,NodeData in enumerate(data):
		#print('NodeData[weightdict]:', NodeData['weightdict'])
		for key,val in enumerate(NodeData['weightdict']):
			#print('key val:', key, val)
			for k,v in val.items():
				#print('k v:', k, v)
				if k == 'Solar Photovoltaic':
					#print('v:', k, v)
					TotalSolarCapacity += v
	
	print('TotalSolarCapacity:', TotalSolarCapacity)
	
	for n,NodeData in enumerate(data):
		for key,val in enumerate(NodeData['weightdict']):
			for k,v in val.items():
				if k == 'Solar Photovoltaic':
					#print('checking:', k, v)
					SolarSharebyBus = [[round(SolarData[j][i]*(v/TotalSolarCapacity),2) for i in range(24)] for j in range(NDay)]
					#print('SolarSharebyBus: ', SolarSharebyBus)
					SolarScenarioDatabyCluster.append({NodeData['bus']:SolarSharebyBus})
	
	#print('SolarScenarioDatabyCluster:' , SolarScenarioDatabyCluster)
	f = open('SolarScData.C' + str(NNode) + '.'+ MarketType +'.json','w')
	json.dump(SolarScenarioDatabyCluster, f)
	f.close()