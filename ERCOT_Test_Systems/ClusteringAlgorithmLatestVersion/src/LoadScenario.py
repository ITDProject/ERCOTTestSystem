import json


if __name__ == "__main__":
	NDay = 3
	import xlrd
	wb = xlrd.open_workbook('../Data/Load/LoadData.08.2018.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name('LoadData.08.2018')
	LoadData = [[ws.cell(24*j + i + 1, 2).value for i in range(24)] for j in range(NDay)]
	#print('WindData 24*NDay array: ',WindData)
	
	f = open('8NodeData.json','r')
	data = json.load(f)
	LoadScenarioDatabyCluster = []
	TotalLoad = 0
	#WindName = 'Onshore Wind Turbine'
	#f.close()
	for n,NodeData in enumerate(data):
		#print('NodeData[weightdict]:', NodeData['weightdict'])
		for key,val in enumerate(NodeData['weightdict']):
			#print('key val:', key, val)
			for k,v in val.items():
				#print('k v:', k, v)
				if k == 'Load':
					#print('v:', k, v)
					TotalLoad += v
	
	for n,NodeData in enumerate(data):
		for key,val in enumerate(NodeData['weightdict']):
			for k,v in val.items():
				if k == 'Load':
					#print('checking:', k, v)
					LoadDataSharebyBus = [[round(LoadData[j][i]*(v/TotalLoad),2) for i in range(24)] for j in range(NDay)]
					#print('WindSharebyBus: ', WindSharebyBus)
					LoadScenarioDatabyCluster.append({NodeData['bus']:LoadDataSharebyBus})
	
	print('LoadScenarioDatabyCluster:' , LoadScenarioDatabyCluster)
	f2 = open('LoadScenarioDatabyCluster.json','w')
	json.dump(LoadScenarioDatabyCluster, f2)
	f2.close()