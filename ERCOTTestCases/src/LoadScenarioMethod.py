import json
import sys

if __name__ == "__main__":

	NNode = sys.argv[1]
	FileName = sys.argv[2]
	sheetName = sys.argv[3]
	NDay = int(sys.argv[4])

	import xlrd
	wb = xlrd.open_workbook('../Data/Load/'+ FileName +'.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name(sheetName)
	LoadData = [[ws.cell(24*j + i + 1, 3).value for i in range(24)] for j in range(NDay)]  #Col 3 for scaled demand
	
	f = open('../../ERCOTGridComponent/SyntheticBusConstructionMethod/src/'+str(NNode)+'BusData.json','r')
	data = json.load(f)
	LoadScenarioDatabyCluster = []
	TotalLoad = 0
	
	for n,NodeData in enumerate(data):
		#print('NodeData[weightdict]:', NodeData['weightdict'])
		for key,val in enumerate(NodeData['weightdict']):
			#print('key val:', key, val)
			for k,v in val.items():
				#print('k v:', k, v)
				if k == 'Load':
					#print('v:', k, v)
					TotalLoad += v
	
	print('TotalLoad:', TotalLoad)
	
	for n,NodeData in enumerate(data):
		for key,val in enumerate(NodeData['weightdict']):
			for k,v in val.items():
				if k == 'Load':
					#print('checking:', k, v)
					LoadDataSharebyBus = [[round(LoadData[j][i]*(v/TotalLoad),2) for i in range(24)] for j in range(NDay)]
					#print('WindSharebyBus: ', WindSharebyBus)
					LoadScenarioDatabyCluster.append({NodeData['bus']:LoadDataSharebyBus})
	
	#print('LoadScenarioDatabyCluster:' , LoadScenarioDatabyCluster)
	f = open(FileName+ '.NB' + str(NNode) +'.json','w')
	json.dump(LoadScenarioDatabyCluster, f)
	f.close()