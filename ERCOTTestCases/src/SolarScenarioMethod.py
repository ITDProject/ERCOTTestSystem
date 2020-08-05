import json
import sys

if __name__ == "__main__":

	NNode = sys.argv[1]
	FileName = sys.argv[2]
	sheetName = sys.argv[3]
	NDay = int(sys.argv[4])

	import xlrd
	wb = xlrd.open_workbook('../Data/Solar/'+ FileName+ '.xlsx', on_demand = True)
	# Loads only current sheets to memory
	ws = wb.sheet_by_name(sheetName)
	SolarData = [[ws.cell(24*j + i + 1, 3).value for i in range(24)] for j in range(NDay)]
	
	f = open('../../ERCOTGridComponent/SyntheticBusConstructionMethod/src/'+str(NNode)+'BusData.json','r')
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
	f = open(FileName +  '.NB' + str(NNode) +'.json','w')
	json.dump(SolarScenarioDatabyCluster, f)
	f.close()