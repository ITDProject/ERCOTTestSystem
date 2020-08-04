import json

if __name__ == "__main__":

	NDay = 6
	NNode = 8
	Year = 2019
	Month = 7
	Day = 23
	MarketType = 'RTM' # indicate 'DAM' or 'RTM'
	Name = 'WindData.' + str(Year)
	sheetName = str(Month) # + '.' + str(Day)
	import xlrd
	wb = xlrd.open_workbook('../Data/Wind/'+ Name+ '.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name(sheetName)
	WindData = [[ws.cell(24*j + i + 1, 6).value for i in range(24)] for j in range(NDay)] # 5 - for DAM, 6 - for RTM
	
	f = open(str(NNode)+'NodeData.json','r')
	data = json.load(f)
	WindScenarioDatabyCluster = []
	TotalWindCapacity = 0
	
	for n,NodeData in enumerate(data):
		#print('NodeData[weightdict]:', NodeData['weightdict'])
		for key,val in enumerate(NodeData['weightdict']):
			#print('key val:', key, val)
			for k,v in val.items():
				#print('k v:', k, v)
				if k == 'Onshore Wind Turbine':
					#print('v:', k, v)
					TotalWindCapacity += v
	
	print('TotalWindCapacity:', TotalWindCapacity)
	
	for n,NodeData in enumerate(data):
		for key,val in enumerate(NodeData['weightdict']):
			for k,v in val.items():
				if k == 'Onshore Wind Turbine':
					#print('checking:', k, v)
					WindSharebyBus = [[round(WindData[j][i]*(v/TotalWindCapacity),2) for i in range(24)] for j in range(NDay)]
					#print('WindSharebyBus: ', WindSharebyBus)
					WindScenarioDatabyCluster.append({NodeData['bus']:WindSharebyBus})
	
	#print('WindScenarioDatabyCluster:' , WindScenarioDatabyCluster)
	f = open('WindScData.C' + str(NNode) + '.'+ MarketType +'.json','w')
	json.dump(WindScenarioDatabyCluster, f)
	f.close()