import json
import pykml
from shapely.geometry import Point, Polygon
import random

zones = ['North', 'East', 'SouthCentral', 'FarWest', 'NorthCentral', 'West', 'South', 'Coastal', 'Others']
gentypes = ['Onshore Wind Turbine', 'Natural Gas Fired Combined Cycle', 'Conventional Steam Coal', 'Solar Photovoltaic', 'Petroleum Coke', 'Petroleum Liquids', 'Natural Gas Steam Turbine', 'Nuclear', 'Wood/Wood Waste Biomass', 'Other Gases', 'All Other', 'Other Waste Biomass', 'Natural Gas Internal Combustion Engine', 'Conventional Hydroelectric', 'Batteries', 'Natural Gas Fired Combustion Turbine','Landfill Gas', 'aggr']

loadtypes = ['None']

LoadPerCapitaByZones = {'North': 1.859, 'East': 1.563, 'SouthCentral': 2.045, 'FarWest': 1.854, 'NorthCentral': 2.438, 'West': 2.368, 'South': 1.843, 'Coastal': 2.362, 'Others': 0.0}
LoadPerCapitaOfERCOT = 2.00

def checkdata():
	f = open('data.json','r')
	data = json.load(f)
	#print(data)
	count = 0
	for k,v in data.items():
		for i in v:
			if len(i) != 17:
				count += 1
	print('Possible error count:', count)
	f.close()

def getpoints():
	pointslist = []
	zoneslist = []
	gentypes = []
	f = open('../Data/GenData.json', 'r')
	s = json.load(f)
	for k,v in s.items():
		zoneslist.append(k)
		for i in v:
			gentypes.append(i[7])
			pointslist.append({'name': str(i[6])+str(int(i[8]))+str(random.uniform(1,10)), 'coordinates': [i[15], i[16]], 'type': i[7], 'power' : i[8], 'zone': k, 'county': 'None'})
			# Note: Randomizing the name of the point => multiple calls to getpoints() generate different names
	types = list(set(gentypes))
	zones = list(set(zoneslist))
	return pointslist


def euclidean(lat1, lon1, lat2, lon2):
	from math import radians, cos, sin, asin, sqrt
	R = 6371  # radius of the earth in km
	R = R*0.621371 # in miles
	lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
	x = (lon2 - lon1) * cos( 0.5*(lat2+lat1) )
	y = lat2 - lat1
	d = R * sqrt( x*x + y*y )
	return d

def getDataFromZIPCodes(case):
	f = open('../Data/Gaz_zcta_national.txt','r')
	next(f)
	if case == 'Texas':
		data = {int(line.strip().split('\t')[0]) : {'Population': int(line.strip().split('\t')[1]), 'Coordinates': [float(line.strip().split('\t')[7]), float(line.strip().split('\t')[8])], 'WeatherZone':'', 'load': 0.0, 'County': ''}  for line in f if int(line.strip().split('\t')[0]) > 75000 and int(line.strip().split('\t')[0]) < 80000}
	elif case == 'USA':
		data = {int(line.strip().split('\t')[0]) : {'Population': int(line.strip().split('\t')[1]), 'Coordinates': [float(line.strip().split('\t')[7]), float(line.strip().split('\t')[8])], 'WeatherZone':'', 'load': 0.0, 'County': ''}  for line in f}
	f.close()
	return data

def ZipCodesToCounties(Case):
	from kmlparser import parsekmlpoints
	data, counties = parsekmlpoints()
	#print('data:', data)
	DatabyCounties = {k:{'Coordinates':v, 'WeatherZone': '', 'CongestionZone':'', 'Population':0} for k,v in data.items()}

	f = open('../Data/ZonesToCounties.json', 'r')
	ZonesToCounties = json.load(f)
	f.close()

	f = open('temp.json','w')
	json.dump(DatabyCounties, f)
	f.close()

	for k,v in DatabyCounties.items():
		for zone, cnames in ZonesToCounties['WeatherZones'].items():
			if(k in cnames):
				v['WeatherZone'] = zone

	DatabyZIPCodes = getDataFromZIPCodes(Case)

	count = 0
	for ZIP,v in DatabyZIPCodes.items():
		count += 1
		print('ITER:', count)
		# Need to check v['Coordinates'] presence inside the list of all counties' coordinates
		for k in DatabyCounties:
			#print('Coordinates:', DatabyCounties[k]['Coordinates'])
			if(PointInPolygon(v['Coordinates'], DatabyCounties[k]['Coordinates'])):
				v['WeatherZone'] = DatabyCounties[k]['WeatherZone']
				v['County'] = k

	if Case == 'Texas':
		return DatabyZIPCodes
	elif Case == 'USA':
		return {k:v for k,v in DatabyZIPCodes.items() if v['WeatherZone'] != ''} #Hardcode -> WeatherZone

def PointInPolygon(pointlist, linearringlist):
	#Convert the data appropriately into shapely library:
	point = Point(tuple(pointlist))
	linearringtuplelist = [tuple(i) for i in linearringlist]
	polygon = Polygon(linearringtuplelist)
	return polygon.contains(point)

def checkZIPCodes():
	from kmlparser import parsekmlregion
	data = parsekmlregion()
	DatabyZIPCodes = getDataFromZIPCodes('USA')

	count = 0
	for ZIP,v in DatabyZIPCodes.items():
		count += 1
		print('ITER:', count)
		if(PointInPolygon(v['Coordinates'], data)):
			#v['WeatherZone'] = DatabyCounties[k]['WeatherZone']
			v['WeatherZone'] = 'ERCOT'
			v['County'] = 'None'

	temp = {k:v for k,v in DatabyZIPCodes.items() if v['WeatherZone'] != ''}
	print('# ZIP Codes in ERCOT (from ERCOT Boundary):', len(temp))
	return None

# String variables
BeginningOfString = """ <!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <title>KML Layers</title>
    <style>
      /* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
      #map {
        height: 100%;
      }
      /* Optional: Makes the sample page fill the window. */
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
    </style>
  </head>
  <body>
    <div id="map"></div>
    <script>

      function initMap() {
        var styledMapType = new google.maps.StyledMapType
		(
				[
				  {
					"elementType": "labels",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  },
				  {
					"featureType": "administrative.neighborhood",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  },
				  {
					"featureType": "poi",
					"elementType": "labels.text",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  },
				  {
					"featureType": "poi.business",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  },
				  {
					"featureType": "road",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  },
				  {
					"featureType": "road",
					"elementType": "labels.icon",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  },
				  {
					"featureType": "transit",
					"stylers": [
					  {
						"visibility": "off"
					  }
					]
				  }
				],
			{name: 'Styled Map'}
		);

        var map = new google.maps.Map(document.getElementById('map'), {
          /*zoom: 11,
          center: {lat: 41.876, lng: -87.624}*/
          zoom: 11,
          center: {lat: 31.599632, lng: -99.067722},
          mapTypeControlOptions: {
            mapTypeIds: ['roadmap', 'satellite', 'hybrid', 'terrain', 'styled_map']
          }
        });
"""

EndOfString = """
        var ercotBoundaryLayer = new google.maps.KmlLayer({
          /*url: 'http://googlemaps.github.io/js-v2-samples/ggeoxml/cta.kml',*/
          /*url: 'http://ercot.com/content/cdr/static/texas_counties.kml',*/
          url: 'https://raw.githubusercontent.com/rohithreddy95/kmlfiles/master/ercot_boundary2.kml',
          map: map
        });

		var texasCountiesLayer = new google.maps.KmlLayer({
          /*url: 'http://googlemaps.github.io/js-v2-samples/ggeoxml/cta.kml',*/
          /*url: 'http://ercot.com/content/cdr/static/texas_counties.kml',*/
          url: 'https://raw.githubusercontent.com/rohithreddy95/kmlfiles/master/texas_county11.kml',
          map: map
        });

        map.mapTypes.set('styled_map', styledMapType);
        map.setMapTypeId('styled_map');
      }
    </script>
    <script async defer
    src="https://maps.googleapis.com/maps/api/js?key=AIzaSyB610aVb6R_ck6S5RFQsG63b8P2tcxm-CA&callback=initMap">
    </script>
  </body>
</html>
"""

colormap = """
var colormap = {
	'type1' : '#FF0000',
	'type2' : '#FF0000',
	'type3' : '#FF0000',
	'type4' : '#FF0000'
	};
"""

colormapbygen ="""
var colormap = {
'Natural Gas Fired Combined Cycle' : '#C9560A',
'Conventional Steam Coal' : '#77492B',
'Onshore Wind Turbine' : '#07078C',
'Natural Gas Steam Turbine' : '#66AD70',
'Natural Gas Fired Combustion Turbine' : '#FFCD3A',
'Nuclear' : '#2E4F33',
'Solar Photovoltaic' : '#28668C',
'All Other' : '#87afa6',
'Petroleum Coke' : '#0e5a9a',
'Landfill Gas' : '#57caff',
'Wood/Wood Waste Biomass' : '#1292b0',
'Batteries' : '#f28c55',
'Other Waste Biomass' : '#2ce83a',
'Natural Gas Internal Combustion Engine' : '#1eafbb',
'Conventional Hydroelectric' : '#025ea1',
'Other Gases' : '#253734',
'Petroleum Liquids' : '#5367c9',
'aggr' : '#FF13CB',
'None' : '#FF0F00'
/*
'North' : '#FF13CB',
'East' : '#0D090A',
'SouthCentral' : '#13C6FF',
'FarWest' : '#5E373E',
'NorthCentral' : '#FFF333',
'West' : '#D9850E',
'South' : '#890301',
'Coastal' : '#EA00E6'*/
};
"""
# 'North', 'East', 'SouthCentral', 'FarWest', 'NorthCentral', 'West', 'South', 'Coastal', 'Others'

if __name__ == "__main__":
	#Checking data.json file correctness - output from dataprocessing.py - generators data
	#checkdata()

	# case = 'USA'
	# DatabyZIPCodes1 = ZipCodesToCounties(case)
	# print('# ZIP Codes in Texas (case: USA):', len(DatabyZIPCodes1))
	# f = open('DatabyZIPCodesUSA.json', 'w')
	# json.dump(DatabyZIPCodes1, f)
	# f.close()

	case = 'Texas'
	DatabyZIPCodes2 = ZipCodesToCounties(case)

	# # Counting counties in all weather zones including 'Others'
	# count = {k:0 for k in zones if k!= 'Others'}
	# count['Empty'] = 0
	# print('Count:', count)
	# for k,v in DatabyZIPCodes2.items():
		# if v['WeatherZone'] == '':
			# count['Empty'] += 1
		# elif v['WeatherZone'] != 'Others':
			# count[v['WeatherZone']] += 1
	# print('Count:', count)
	# print('# ZIP Codes in ERCOT region:', sum([v for k,v in count.items()]))

	print('# ZIP Codes in Texas (case: Texas):', len(DatabyZIPCodes2))

	f = open('DatabyZIPCodesTexas.json', 'w')
	json.dump(DatabyZIPCodes2, f)
	f.close()

	#checkZIPCodes()
