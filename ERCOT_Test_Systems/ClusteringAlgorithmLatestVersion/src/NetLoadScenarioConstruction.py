import json


if __name__ == "__main__":

	NDay = 3
	NNode = 200
	Method = 2

	NetLoadData = [[0 for i in range(24)] for j in range(NDay)]
	
	fLoad = open('LoadScenarioDatabyCluster' + 'Method' + str(Method) + 'Size' + str(NNode) + '.json','r')
	dataLoad = json.load(fLoad)

	fWind = open('WindScenarioDatabyClusterSize' + str(NNode) + '.json','r')
	dataWind = json.load(fWind)

	NetLoadScenarioDatabyCluster = {k:[[0.0 for i in range(24)] for j in range(NDay)] for k in range(NNode)}

	ListNodes = []
	ListLoadNodes = []
	ListWindNodes = []


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
					NetLoadScenarioDatabyCluster[int(node)][j][i] = round(NetLoadScenarioDatabyCluster[int(node)][j][i] - data[j][i],2)

	ListNodes = ListLoadNodes
	for x in ListWindNodes:
		if x not in ListLoadNodes:
			ListNodes.append(x)
	#print('ListNodes:', ListNodes)

	NetLoadScenarioDatabyClusterUpdated = [] 
	for k in ListNodes:
		temp = [[round(NetLoadScenarioDatabyCluster[int(k)][j][i],2) for i in range(24)] for j in range(NDay)]
		NetLoadScenarioDatabyClusterUpdated.append({int(k):temp})

	#print('NetLoadScenarioDatabyCluster:' , NetLoadScenarioDatabyCluster)
	f = open('NetLoadScenarioDatabyClusterMethod' + str(Method) +'Size' + str(NNode) + '.json','w')
	json.dump(NetLoadScenarioDatabyClusterUpdated, f)
	f.close()