import json
import sys

if __name__ == "__main__":

	NNode = sys.argv[1]
	FileName = sys.argv[2]
	sheetName = sys.argv[3]
	NDay = int(sys.argv[4])

	import xlrd
	wb = xlrd.open_workbook('../Data/Wind/'+ FileName+ '.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name(sheetName)
	WindData = [[ws.cell(24*j + i + 1, 3).value for i in range(24)] for j in range(NDay)]
	
	f = open('../../ERCOTGridComponent/SyntheticBusConstructionMethod/src/'+str(NNode)+'BusData.json','r')
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
	f = open(FileName +  '.NB' + str(NNode) +'.json','w')
	json.dump(WindScenarioDatabyCluster, f)
	f.close()