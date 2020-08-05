import json


if __name__ == "__main__":

	WindIncrementFactor = 1.05
	NDay = 6
	NNode = 8
	M = 1 # Method
	Year = 2019
	Month = 7
	Day = 23
	MarketType = 'DAM' # indicate 'DAM' or 'RTM'

	NetLoadData = [[0 for i in range(24)] for j in range(NDay)]
	
	fLoad = open('LoadScData.C' + str(NNode) + '.'+ MarketType +'.json','r')
	dataLoad = json.load(fLoad)

	fWind = open('WindScData.C' + str(NNode) + '.'+ MarketType +'.json','r')
	dataWind = json.load(fWind)

	fSolar = open('SolarScData.C' + str(NNode) + '.'+ MarketType +'.json','r')
	dataSolar = json.load(fSolar)
	
	NetLoadScenarioDatabyCluster = {k:[[0.0 for i in range(24)] for j in range(NDay)] for k in range(NNode)}

	ListNodes = []
	ListLoadNodes = []
	ListWindNodes = []
	ListSolarNodes = []


	for value in dataLoad:
		#print('item:', item)
		for node, data in value.items():
			ListLoadNodes.append(node)
			#print('node:', node)
			#print('data:', data)
			for j in range(NDay):
				for i in range(24):
					#print('i,j, ', i , j)
					#print('data: ', data[j][i])
					#print('NetLoadScenarioDatabyCluster: ',NetLoadScenarioDatabyCluster[int(node)][j][i])
					NetLoadScenarioDatabyCluster[int(node)][j][i] = NetLoadScenarioDatabyCluster[int(node)][j][i] + data[j][i]

	for item in dataWind:
		#print('item:', item)
		for node, data in item.items():
			ListWindNodes.append(node)
			#print('node:', node)
			#print('data:', data)
			for j in range(NDay):
				for i in range(24):
					#print('i,j, ', i , j)
					#print('data: ', data[j][i])
					#print('NetLoadScenarioDatabyCluster: ',NetLoadScenarioDatabyCluster[int(node)][j][i])
					NetLoadScenarioDatabyCluster[int(node)][j][i] = round(NetLoadScenarioDatabyCluster[int(node)][j][i] - WindIncrementFactor * data[j][i],2)

	for item in dataSolar:
		#print('item:', item)
		for node, data in item.items():
			ListSolarNodes.append(node)
			#print('node:', node)
			#print('data:', data)
			for j in range(NDay):
				for i in range(24):
					#print('i,j, ', i , j)
					#print('data: ', data[j][i])
					#print('NetLoadScenarioDatabyCluster: ',NetLoadScenarioDatabyCluster[int(node)][j][i])
					NetLoadScenarioDatabyCluster[int(node)][j][i] = round(NetLoadScenarioDatabyCluster[int(node)][j][i] - data[j][i],2)


	ListNodes = ListLoadNodes
	for x in ListWindNodes:
		if x not in ListLoadNodes:
			ListNodes.append(x)
	#print('ListNodes:', ListNodes)

	for x in ListSolarNodes:
		if x not in ListNodes:
			ListNodes.append(x)
	#print('ListNodes:', ListNodes)

	NetLoadScenarioDatabyClusterUpdated = [] 
	for k in ListNodes:
		temp = [[round(NetLoadScenarioDatabyCluster[int(k)][j][i],2) for i in range(24)] for j in range(NDay)]
		NetLoadScenarioDatabyClusterUpdated.append({int(k):temp})

	#print('NetLoadScenarioDatabyCluster:' , NetLoadScenarioDatabyCluster)
	f = open('NetLoadScData.C' + str(NNode) + '.'+ 'WIF' + str(WindIncrementFactor)+ '.' + MarketType +'.json','w')
	json.dump(NetLoadScenarioDatabyClusterUpdated, f)
	f.close()