from xml.dom.minidom import parseString

def parsekmltxcounties():
	filename = '../Data/Map/kmlfiles/texas_counties.kml'
	#Read KML file as a string
	file = open(filename)
	strdata = file.read()
	file.close()

	#Parse that string into a DOM
	dom = parseString(strdata)
	data = {}
	
	# #Iterate through a collection of coordinates elements
	for n in dom.getElementsByTagName('name'):
		namearray = n.firstChild.data.split()
		cname = namearray[:-1]
		countyname = " ".join(cname)
		data[countyname] = []
		if countyname != 'Texas':
			for d in dom.getElementsByTagName('coordinates'):
				#Break them up into latitude and longitude
				coords = d.firstChild.data.split(',')
				data[countyname].append([float(coords[1]), float(coords[0])])
	#print(data['Wise'])
	return data


# def parsekmlpoints():
	# filename = '../Data/Map/kmlfiles/texas_counties.kml'
	# #Read KML file as a string
	# file = open(filename)
	# strdata = file.read()
	# file.close()

	# #Parse that string into a DOM
	# dom = parseString(strdata)
	# data = {}
	# #County names
	# CountyNames = []
	
	# # #Iterate through a collection of coordinates elements
	# for n in dom.getElementsByTagName('name'):
		# namearray = n.firstChild.data.split()
		# cname = namearray[:-1]
		# countyname = " ".join(cname)
		# if countyname != 'Texas':
			# data[countyname] = []
			# CountyNames.append(countyname)
			# #print(countyname)
			# for d in dom.getElementsByTagName('coordinates'):
				# #Break them up into latitude and longitude
				# coords = d.firstChild.data.split(',')
				# data[countyname].append([float(coords[1]), float(coords[0])]) #In texas_counties.kml file, [long, lat] is the order of the coordinates
	# #print(data['Wise'])
	# return data, CountyNames


def parsekmlpoints():
	filename = '../Data/Map/kmlfiles/texas_counties.kml'
	#Read KML file as a string
	file = open(filename)
	strdata = file.read()
	file.close()

	#Parse that string into a DOM
	dom = parseString(strdata)
	data = {}
	#County names
	CountyNames = []
	
	# #Iterate through a collection of coordinates elements
	for folder in dom.getElementsByTagName('Folder'):
		for n in folder.getElementsByTagName('name'):
			namearray = n.firstChild.data.split()
			cname = namearray[:-1]
			countyname = " ".join(cname)
			if countyname != 'Texas':
				data[countyname] = []
				CountyNames.append(countyname)
				#print(countyname)
				for d in folder.getElementsByTagName('coordinates'):
					#Break them up into latitude and longitude
					coords = [float(j) for i in d.firstChild.data.split(',') for j in i.split() if j!= '0']
					data[countyname] = [[coords[i+1], coords[i]] for i in range(0,len(coords),2)]
					#data[countyname].append([float(coords[1]), float(coords[0])]) #In texas_counties.kml file, [long, lat] is the order of the coordinates
	#print(data['Wise'])
	return data, CountyNames

def parsekmlregion():
	filename = '../Data/Map/kmlfiles/ercot_boundary.kml'
	#Read KML file as a string
	file = open(filename)
	strdata = file.read()
	file.close()

	#Parse that string into a DOM
	dom = parseString(strdata)
	data = []
	
	# #Iterate through a collection of coordinates elements
	for folder in dom.getElementsByTagName('Folder'):
		#for n in folder.getElementsByTagName('name'):
		#namearray = n.firstChild.data.split()
		#cname = namearray[:-1]
		#countyname = " ".join(cname)
		#if countyname != 'Texas':
		#data[countyname] = []
		#CountyNames.append(countyname)
		#print(countyname)
		for d in folder.getElementsByTagName('coordinates'):
			#Break them up into latitude and longitude
			coords = [float(j) for i in d.firstChild.data.split(',') for j in i.split() if j!= '0']
			data = [[coords[i+1], coords[i]] for i in range(0,len(coords),2)]
			#data[countyname].append([float(coords[1]), float(coords[0])]) #In texas_counties.kml file, [long, lat] is the order of the coordinates
	return data

if __name__ == "__main__":
	#data = parsekmltxcounties()
	#data, CountyNames = parsekmlpoints()
	data = parsekmlregion()
	print('data:', data)
	print('Len of data:', len(data))