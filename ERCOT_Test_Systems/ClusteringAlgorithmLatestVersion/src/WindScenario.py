import json


if __name__ == "__main__":
	NDay = 3
	import xlrd
	wb = xlrd.open_workbook('../Data/Wind/WindData.08.2018.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name('WindData.08.2018')
	WindData = [[ws.cell(24*j + i + 1, 2).value for i in range(24)] for j in range(NDay)]
	#print('WindData 24*NDay array: ',WindData)
	
	f = open('8NodeData.json','r')
	data = json.load(f)
	WindScenarioDatabyCluster = []
	TotalWindCapacity = 0
	#WindName = 'Onshore Wind Turbine'
	#f.close()
	for n,NodeData in enumerate(data):
		#print('NodeData[weightdict]:', NodeData['weightdict'])
		for key,val in enumerate(NodeData['weightdict']):
			#print('key val:', key, val)
			for k,v in val.items():
				#print('k v:', k, v)
				if k == 'Onshore Wind Turbine':
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
	f2 = open('WindScenarioDatabyCluster.json','w')
	json.dump(WindScenarioDatabyCluster, f2)
	f2.close()