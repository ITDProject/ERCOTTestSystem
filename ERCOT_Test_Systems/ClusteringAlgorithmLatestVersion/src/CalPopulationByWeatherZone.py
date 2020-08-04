import json
import numpy as np

PopulationByWeatherZone = {'North': 0, 'East': 0, 'SouthCentral': 0, 'FarWest': 0, 'NorthCentral': 0, 'West': 0, 'South': 0, 'Coastal': 0, 'Others': 0.0}

def postprocessdata():
	f = open('DatabyZIPCodesTexas.json', 'r')
	data = json.load(f)
	f.close()
	nd = {k:v for k,v in data.items() if v['WeatherZone'] == ''} # items where no weather zone is asssigned
	d = {k:v for k,v in data.items() if v['WeatherZone'] != ''}
	f = open('DatabyZIPCodesTexasNR.json', 'w')
	json.dump(d,f)
	f.close()
	return d, nd

def postprocessingmain(Case, bgen):
	d, nd = postprocessdata()
	thresholdCoordinates = [28.390267, -96.842717] #Above this => Coastal, Below this => South # This is a temporary fix
	#Should check the remaining 4 points - 3 in West Zone (Lampasas) and 1 outside Texas

	for k,v in nd.items():
		if v['Coordinates'][0] < thresholdCoordinates[0]: #Equator = 0, increases in North direction
			v['WeatherZone'] = 'South'
		if v['Coordinates'][0] > thresholdCoordinates[0]: #Equator = 0, increases in North direction
			v['WeatherZone'] = 'Coastal'

	# Assigning load to each node by scaling population with LoadPerCapita
	if Case == 'WeatherZones':
		for k,v in d.items():
			PopulationByWeatherZone[v['WeatherZone']] = PopulationByWeatherZone[v['WeatherZone']] + v['Population']
		for k,v in nd.items():
			PopulationByWeatherZone[v['WeatherZone']] = PopulationByWeatherZone[v['WeatherZone']] + v['Population']

	print( 'PopulationByWeatherZone:' , PopulationByWeatherZone)
	print('Length of d: ', len(d))
	print('Length of nd: ', len(nd))

	#Merging two dictionaries
	CombinedData = {**d, **nd}
	print('Length of Combined data: ', len(CombinedData))
	CombinedDataERCOT = {k:v for k,v in CombinedData.items() if v['WeatherZone'] != 'Others'}
	return CombinedDataERCOT


if __name__ == '__main__': # main function
	Case = 'WeatherZones'
	bgen = False

	CombinedLoadDataERCOT = postprocessingmain(Case, bgen)
