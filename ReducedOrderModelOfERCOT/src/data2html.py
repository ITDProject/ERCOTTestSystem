import numpy as np

class Map(object):
	def __init__(self, gentypes):
		#gentypes is not being used now
		from utils import BeginningOfString, EndOfString, colormapbygen
		self._points = []
		self._BS = BeginningOfString
		self._ES = EndOfString
		#todo: change colors according to the overall generator capacity proportions
		self.colormap = colormapbygen #self.buildcolormap(gentypes)

	def add_point(self, point):
		self._points.append(point)

	def __str__(self):
		varCodeElements = ",\n".join(
			["""
			'{name}': {{
			location: {{lat: {lat}, lng: {lng} }},
			capacity: {capacity},
			type: '{Type}',
			county: '{County}'
			}}""".format(name=i['name'], lat=i['coordinates'][0], lng=i['coordinates'][1], capacity=i['power'], Type=i['type'], County=i['county']) for i in self._points if i['zone'] != 'Others']) #excluded other zones here for case 1, #New update: Added County, should add to the gen data as well
		varCode = "var genmap = {\n" + varCodeElements + "\n };"

		forloop = """
			for (var gen in genmap) {
			// Add the circle for this city to the map.
				var genCircle = new google.maps.Circle({
				strokeColor: colormap[genmap[gen].type],
				strokeOpacity: 0.8,
				strokeWeight: 2,
				fillColor: colormap[genmap[gen].type],
				fillOpacity: 0.35, //1
				map: map,
				center: genmap[gen].location,
				radius: Math.sqrt(genmap[gen].capacity) * 600 //Math.sqrt(genmap[gen].capacity) * 1000
			});
			}
		"""
		return "\n".join([self._BS, varCode, self.colormap, forloop, self._ES])

def generateHTML(pointslist, map, filename):
	#Add points to Map:
	for i in pointslist:
		map.add_point(i)
	#Write to HTML file:
	with open("./Output/"+filename, "w") as out:
		print(map, file=out)

class Cluster():
	# data is in list format:
	# [latitude, longitude, gencapacity/capacity/weight, gentype]
	def __init__(self, n, data):
		#self.points = data[0:2]
		from utils import gentypes, loadtypes
		self.center = np.array(data[0:2])
		self.weight = np.array(data[2])
		self.identity = {'ID': n, 'InitialType': {'load': data[3] if data[3] in loadtypes else '', 'gen': data[3] if data[3] in gentypes else '' }}
		# if (data[3] in gentypes):
		# 	self.datatype = 'gen'
		# elif data[3] == 'None':
		# 	self.datatype = 'load'
		# if self.datatype == 'gen':
		self.weightdict = {k:0.0 for k in gentypes + loadtypes}
		self.weightdict[data[3]] = data[2]
		if data[3] in loadtypes:
			self.zipcodelist = [data[4]]
		else:
			self.zipcodelist = []
		# print(self.weightdict)

	def __updateCenter(self, other):
		self.center = (self.weight * self.center + other.weight * other.center)/(self.weight + other.weight)

	def __updateWeight(self, other):
		self.weight = self.weight + other.weight
		# if self.datatype == 'gen':
			# from collections import Counter
			# self.weightdict = dict(Counter(self.weightdict) + Counter(other.weightdict))
		from collections import Counter
		self.weightdict = dict(Counter(self.weightdict) + Counter(other.weightdict))
		# self.weightdict = {k1:v1+v2 for k1,v1 in self.weightdict.items() for k2,v2 in other.weightdict.items() if k1==k2}

	def __updateZIPCodeList(self, other):
		self.zipcodelist = self.zipcodelist + other.zipcodelist

	def updateCluster(self, other):
		self.__updateCenter(other)
		self.__updateWeight(other)
		self.__updateZIPCodeList(other)

	def __str__(self):
		return str(self.identity['ID'])
		# return str(self.weightdict)

	def __eq__(self, other):
		return self.__dict__ == other.__dict__

	def distance(self, other, type):
		from utils import euclidean
		import haversine
		if type == 'euclidean':
			return euclidean(self.center[0], self.center[1], other.center[0], other.center[1])
		if type == 'haversine':
			pa = (self.center[0], self.center[1])
			pb = (other.center[0], other.center[1])
			return haversine(pa,pb)

def preprocess(DistanceMatrix, deldict):
	check = np.zeros([1, DistanceMatrix.shape[0]])
	dd = {}
	d = {}
	for k,v in deldict.items():
		# Preprocess data to find indices where distances are zeros except the distance with itself
		val = [i for i in v if k != i]
		dd[k] = val

	# Temporarily store the indices in index dictionary
	index = {k:1 if len(v) == 0 else 0 for k,v in dd.items()}

	for k,v in dd.items():
		if check[0][k] == 0:
			d[k] = v
		for i in v:
			check[0][i] = 1
	#Test:
	#print(d)
	# count = 0
	# for k,v in d.items():
		# count += len(v)
	# print("Count:", 1050-count)
	return d, index


def ClusteringAlgorithm(inputdata, metric, clusterno):
	#Initialize Cluster Size
	ClusterSizeOrig = len(inputdata)
	ClusterSize = ClusterSizeOrig
	#Initialize each point to be cluster
	ClustersArray = np.array([Cluster(n,i) for n,i in enumerate(inputdata)])
	#Initialize Distance Matrix
	DistanceMatrix = np.array([np.array([i.distance(j,metric) for j in ClustersArray]) for i in ClustersArray])
	#for j in DistanceMatrix[0]:
	#	print(j)

	# Preprocessing Clusters - Removing overlapping data points
	# for each i, check the distancematrix for entries with zero and store the indices into d[i]
	d = {i : np.where(DistanceMatrix[i] == 0)[0].tolist() for i in range(DistanceMatrix.shape[0])}
	# Get the deldict from preprocess def, where DistanceMatrix and d are given as input
	deldict, index = preprocess(DistanceMatrix, d)
	#print(deldict)
	#print(len(deldict))
	print('ClustersArray:', ClustersArray.shape[0])

	# Computing sumval:
	# sumval = [i.weightdict['None'] for i in ClustersArray]
	sumval = sum([i.weightdict['None'] for i in ClustersArray if 'None' in i.weightdict])
	print('Sum of load:', sumval)
	# for i in range(len(ClustersArray)):
	# 	print('i=', i, 'data:', str(ClustersArray[i]))
	# 	print(' ')

	# Updating ClustersArray to account for overlaps:

	# New array:
	Clustersdata = []
	result = []
	deletearray = []
	#Merging similar clusters (plant locations):

	for k,v in deldict.items():
		if len(v) > 0:
			for i in v:
				ClustersArray[k].updateCluster(ClustersArray[i])
			Clustersdata.append(ClustersArray[k])
		if index[k] == 1:
			Clustersdata.append(ClustersArray[k])

	Clustersdata = np.array(Clustersdata)
	print('Clustersdata:', Clustersdata.shape[0])

	ClusterSize = Clustersdata.shape[0]
	ClusterSizeOrig = ClusterSize # temporary update to ClusterSizeOrig to the val ClusterSize

	# # Computing sumval:
	# val = [1 if 'None' in i.weightdict else 0 for i in Clustersdata]
	# valid = [n for n,i in enumerate(val) if i == 0]
	# sumval = sum(val)
	sumval = format(sum([i.weightdict['None'] for i in Clustersdata if 'None' in i.weightdict]), '.4f')
	print('Sum of load:', sumval)
	# for i in range(len(Clustersdata)):
	# 	print('i=', i, 'data:', str(Clustersdata[i]))
	# 	print(' ')

	# DistanceMatrix = np.asmatrix(np.array([np.array([i.distance(j,metric) for j in Clustersdata]) for i in Clustersdata]))
	# Check more about this conversion - from npmatrix to nparray
	DistanceMatrix = np.array([np.array([i.distance(j,metric) for j in Clustersdata]) for i in Clustersdata])
	DistMaxVal = DistanceMatrix.max()
	returnindex=0
	print("Initialized Clustering")
	flag = 1

	ZIPCodesByCluster = {k:{} for k in clusterno}

	# clusterno is assumed to be in increasing order
	while ClusterSize > clusterno[-1]:
		minval = np.min(np.min(np.where(DistanceMatrix==0, DistanceMatrix.max(), DistanceMatrix), axis=0))
		minindex = np.where(DistanceMatrix==minval)[0]
		Clustersdata[minindex[0]].updateCluster(Clustersdata[minindex[1]])
		# print('minindex', minindex)
		if(minindex[1] in deletearray or minindex[0] in deletearray):
			print('ERROR in deletearray')

		deletearray.append(minindex[1])

		# # Deleting minindex[1] from the Clustersdata
		# Clustersdata = np.delete(Clustersdata, minindex[1])
		# #print("Clustersize:", Clusterdata.shape[0])
		# # Recomputing DistanceMatrix
		# DistanceMatrix = np.asmatrix(np.array([np.array([i.distance(j,metric) for j in Clustersdata]) for i in Clustersdata]))

		# print('dist matrix: ', np.array([Clustersdata[minindex[0]].distance(j,metric) for j in Clustersdata]))
		# print('dist matrix: ', DistanceMatrix[:, minindex[0]])
		# print('dist matrix: ', DistanceMatrix)

		# Should improvise the speed of the following lines of code
		DistanceMatrix[:, minindex[0]] = np.array([0 if n in deletearray else Clustersdata[minindex[0]].distance(j,metric) for n,j in enumerate(Clustersdata)])
		DistanceMatrix[minindex[0], :] = DistanceMatrix[:, minindex[0]]
		DistanceMatrix[:, minindex[1]] = np.zeros(ClusterSizeOrig)
		DistanceMatrix[minindex[1], :] = np.zeros(ClusterSizeOrig)

		ClusterSize = ClusterSize - 1
		# print("Clustersize:", ClusterSize)
		MapObj = [0.0 for i in range(len(clusterno))]
		# ClusterSize = Clustersdata.shape[0]
		#if len(clusterno) > 1:
		if ClusterSize == clusterno[returnindex]:
			# Additional processing before appending Clustersdata to the result variable
			# print('DistanceMatrix sum: ', DistanceMatrix.sum(0))
			# print('DistanceMatrix.shape', DistanceMatrix.shape)

			# # Note: Rows and Columns that are supposed to be deleted are supposed to have zeros in the distance matrix
			# DistanceSumMatrix = np.array(DistanceMatrix.sum(0))
			# deletedarray = [n for n, i in enumerate(np.where(DistanceSumMatrix==0, 1, 0).tolist()) if i==1]
			# if(set(deletearray) != set(deletedarray)):
			# 	print('dELETE ARRAY NOT EQUAL')

			# temp = np.array([i for n,i in enumerate(Clustersdata) if n not in deletedarray])
			temp = np.array([i for n,i in enumerate(Clustersdata) if n not in deletearray])

			# result.append(temp)

			# Printing to files - intermediate:
			# ClusterType = [0 for i in len]
			# for i in temp:
			# 	for k,v in i.weightdict.items():
			# 		if v == 0:
			# 			print('Error in temp')
			# ClusterType = ['' for i in range(len(temp))]
			# for n,i in enumerate(temp):
			#

			count = 0
			for n,i in enumerate(temp):
				# for k,v in i.weightdict.items():
					# if v != 0.0:
						# ZIPCodesByCluster[n].append({'coordinates': i.center.tolist(), 'power': v, 'type': k, 'name' : str(i)+'_'+k, 'zone': 'None', 'county': 'None'}) # as of now hardcoded both zone and county with the value: None
				ZIPCodesByCluster[ClusterSize][n] = {'coordinates': i.center.tolist(), 'ZIPCodes': i.zipcodelist}
				count = count + len(i.zipcodelist)
			if ClusterSize == 8:
				print('ZIPCodes Count', count)
				print('ZIPCodesByCluster', ZIPCodesByCluster[ClusterSize])

			r2 = clustertopointslist(temp)
			retval = FindClusterTypes(r2)
			print('[L, G, H]: ', retval)
			print('Sum of L,G,H: ', sum(retval))
			# print('r2', r2)
			filenames = 'outputcase2_'+str(returnindex)+'.html'

			#Initialize Maps for case 2
			from utils import gentypes
			MapObj[returnindex] = Map(gentypes)
			#Generate HTML for the file in case 2:
			generateHTML(r2, MapObj[returnindex], filenames)

			# # Checking temp array:
			# if(CheckResult([i for n,i in enumerate(Clustersdata) if n not in deletedarray], sumval) == False):
			# 	print('ERROR')

			print("Clusters at i:", returnindex)
			returnindex += 1

		#print(c1, c2)
	print("Finalized Clustering")
	return ZIPCodesByCluster # None # result # Clustersdata


def FindClusterTypes(r2):
	names = []
	CountH = 0
	CountL = 0
	CountG = 0
	from utils import gentypes

	for i in r2:
		s = i['name'].split('_')
		names.append(int(s[0]))
	names = set(names)
	# print(names)
	d = {k:[] for k in names}

	for i in r2:
		s = i['name'].split('_')
		if i['power'] > 0:
			d[int(s[0])].append(s[1])

	# print(d)
	Type = {i:'L' for i in names if d[i]}
	for i in names:
		if len(set(gentypes) & set(d[i])) > 0 and 'None' in d[i]:
			CountH += 1
		elif 'None' in d[i]:
			CountL += 1
		elif len(set(gentypes) & set(d[i])) > 0:
			CountG += 1

	return [CountL, CountG, CountH]

# def CheckResult(l, sumval):
# 	sumofload = format(sum([i.weightdict['None'] for i in l if 'None' in i.weightdict]), '.4f')
# 	if sumofload != sumval:
# 		print('Actual (Expected)', sumofload, sumval)
# 	return True if sumofload == sumval else False

def gennparray(pointslist):
	return np.array([ [i['coordinates'][0], i['coordinates'][1], i['power']] for i in pointslist ])

def generatedata(pointslist, zones, gentypes):
	databyzones = [[] for i in zones]
	for n,i in enumerate(zones):
		pointsbyzone = [q for q in pointslist if q['zone'] == i]
		databyzones[n] = gennparray(pointsbyzone)
	#print('Printing databyzones: ', databyzones)
	outputbyzones = np.array(databyzones)

	databyGenTypes = [[] for i in gentypes]
	for n,i in enumerate(gentypes):
		pointsbyGenType = [q for q in pointslist if q['type'] == i]
		databyGenTypes[n] = gennparray(pointsbyGenType)
	#print('Printing databyGenTypes: ', databyGenTypes)
	outputbyGenTypes = np.array(databyGenTypes)

	return outputbyzones, outputbyGenTypes, gennparray(pointslist)

#data by zones
def case_1(databyzones):
	weighteddatabyzones = databyzones
	averagelocations = np.zeros([9, 3]) #9 zones, with each zone containing avg lat, avg lng, total power
	for i in range(databyzones.shape[0]): #for each zone
		for j in range(databyzones[i].shape[0]): #for each generator in a zone
			weighteddatabyzones[i][j,0:2] = databyzones[i][j,2]*databyzones[i][j,0:2] # Scale both lat, log by their power (weight)
		weighteddatabyzones[i][:,0:2] = weighteddatabyzones[i][:,0:2]/sum(databyzones[i][:,2]) # Divide by total power (weight)
		averagelocations[i][0:2] = sum(weighteddatabyzones[i][:,0:2]) # Weighted average locations for zone i
		averagelocations[i][2] = sum(databyzones[i][:,2]) # Total power (weight) for zone i
	return averagelocations.tolist()

#Overall data with generic clustering algorithm:
def case_2(data, metric, clusterno):
	#print("Clustering at i:", n)
	return ClusteringAlgorithm(data, metric, clusterno)

	# weighteddatabyzones = databyzones
	# averagelocations = np.zeros([9, 3]) #9 zones, with each zone containing lat, lng, cap
	# for i in range(databyzones.shape[0]): #for each zone
		# for j in range(databyzones[i].shape[0]): #for each generator in a zone
			# weighteddatabyzones[i][j,0:2] = databyzones[i][j,2]*databyzones[i][j,0:2]
		# weighteddatabyzones[i][:,0:2] = weighteddatabyzones[i][:,0:2]/sum(databyzones[i][:,2])
		# averagelocations[i][0:2] = sum(weighteddatabyzones[i][:,0:2])
		# averagelocations[i][2] = sum(databyzones[i][:,2])
	# return averagelocations.tolist()


def pointslistformat(pl, zones):
	r = []
	for n,i in enumerate(pl):
		if len(i) == 3:
			r.append({'coordinates': [i[0], i[1]], 'power': i[2], 'type': 'aggr', 'name' : zones[n], 'zone': zones[n], 'county': 'None'}) # Check type -> as of now hard-coded with aggr, also county is hardcoded with the value None. Is this def for a specific purpose?
	return r

def clustertopointslist(cl):
	r = []
	for i in cl:
		# temporary fix: if condition
		# TODO: Remove or update dependency on datatype
		# if i.datatype == 'gen':
		for k,v in i.weightdict.items():
			if v != 0.0:
				r.append({'coordinates': i.center.tolist(), 'power': v, 'type': k, 'name' : str(i)+'_'+k, 'zone': 'None', 'county': 'None'}) # as of now hardcoded both zone and county with the value: None
	return r


def data2htmlmain():
	#Initializing parameters:
	metric='euclidean'

	# point dictionary format:  {'name':'G1', 'coordinates': [39.908715, 116.397389], 'gentype' : 'type1', 'gencapacity' : 1000}
	#Get points from GenData.json file
	from utils import getpoints, zones, gentypes
	pointslist = getpoints()
	print('Points list length:', len(pointslist))
	#distances = euclideandistance(pointslist)
	return pointslist #for use in postprocessing

	# #Initialize Map for case 0
	# map0 = Map(gentypes)
	# #Generate HTML with the points list / r1:
	# generateHTML(pointslist, map0, 'GeneratorsData.html')

	# # Other cases for solely clustering/plotting generators data:

	# #Generating np array from the input data: 1050 x 3 vector. [0] -> latitude, [1] -> longitude, [2] -> weight / gen capacity
	# databyzones, databytypes, data_t = generatedata(pointslist, zones, gentypes)
	# #print("databytypes.shape():", databytypes.shape)

	# # #Case 1:
	# # result_1 = case_1(databyzones)
	# # r1 = pointslistformat(result_1, zones)
	# # #print(r1)

	# #Initialize Map for case 1
	# map1 = Map(gentypes)
	# #Generate HTML with the points list / r1:
	# generateHTML(r1, map1, 'GeneratorsDatabyZone.html')

	# # #Case 2:
	# # data = [[i['coordinates'][0], i['coordinates'][1], i['power'], i['type']] for i in pointslist if i['zone'] != 'Others']
	# # #print(data)
	# # clusterno = [300-10*i for i in range(30)]
	# # #cdata = [case_2(data, metric, i, n) for n,i in enumerate(clusterno)]
	# # cdata = case_2(data, metric, clusterno)
	# # r2 = [clustertopointslist(i) for i in cdata]
	# # filenames = ['outputcase2_'+str(i)+'.html' for i in range(len(r2))]

	# # #Initialize Maps for case 2
	# # map = [Map(gentypes) for i in range(len(filenames))]
	# # #Generate HTML for all the files in case 2:
	# # for i in range(len(filenames)):
		# # generateHTML(r2[i], map[i], filenames[i])

if __name__ == "__main__":
	data2htmlmain()
