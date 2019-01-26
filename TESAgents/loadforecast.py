import math
import csv
import sys
import json
import fncs
import re
import cmath

FileName = 'NetLoadScenarioDatabyClusterMethod2Size8.json'

def NetLoadScenarioDataJsonFormat(filename):
	NLSE = 0
	NDay = 0
	f = open(filename,'r')
	NetLoadScenarioData = json.load(f)
	NLSE = len(NetLoadScenarioData)
	NDay = [len(val) for key, val in NetLoadScenarioData[0].items()]
	return NDay[0], NLSE, NetLoadScenarioData

def loadforecast_RP(loadforecast, h, d, filename):
	time = []
	NDay, NLSE, NetLoadScenarioData = NetLoadScenarioDataJsonFormat(filename)
	ListLSENodes = []
	loadforecastRTM = [[] for i in range(3)]
	loadforecastDAM = []
	unit = 1 #1000000000
	for ele in NetLoadScenarioData:
		for k in ele:
			ListLSENodes.append(k)
	#print('ListLSENodes:', ListLSENodes)
	h1 = 0 if h == 23 else h+1
	d1 = 0 if d >= NDay else d
	d2 = d if h == 23 else d-1
	d2 = 0 if d2 >= NDay else d2
	
	print('h, d:', h, d)
	#24 hour vector -> DAM
	if(len(loadforecast) > 1):
		#print('Im here 1')
		x = (d* day_len)*unit + ((hour_len)/2)*unit - 1*unit
		for i in range(NLSE):
			for j in range(24):
				#load.append((x,'loadforecastDAM_h'+str(j), float(loadforecast[j])*1)) # replace 1 with
				load.append((x,'loadforecastDAM_LSE' +str(i+1)+ '_H'+str(j), float(NetLoadScenarioData[i][ListLSENodes[i]][d1][j])))
	else:
		#RTM
		y = (h* hour_len)*unit + (d)*(day_len)*unit + ((hour_len)/2)*unit - 1*unit #10*10*1000000000
		for i in range(NLSE):
			load.append((y, 'loadforecastRTM_'+str(i+1), float(NetLoadScenarioData[i][ListLSENodes[i]][d2][h1])))
	return None

def get_number(value):
	return float(''.join(ele for ele in value if ele.isdigit() or ele == '.'))

if len(sys.argv) == 3:
	tmax = int(sys.argv[1])
	deltaT = int(sys.argv[2])
elif len(sys.argv) == 1:
	tmax = 2 * 24 * 3600 #172800
	deltaT = 1
else:
	print ('usage: python loadforecast.py [tmax deltaT]')
	sys.exit()

fncs.initialize()
ts = 0
load = []
hour_len = 3600 #100 # in s
day_len = 24* hour_len # in s
timeSim = 0
AvgsumRealPower_am = [[0 for i in range(24)] for j in range(int(tmax/(24*3600)+1))]
AvgsumRealPower = [[0.0 for i in range(24)] for j in range(int(tmax/(24*3600)+1))]
sumdload = 0
count = 0
prev_hour = 0
denergy = 0
denergy_prev = 0
dloadvec = []
flag = 0
prev_day = 0
while ts <= tmax:
	#print ('time step: ',ts, flush = True)
	day = int(ts / day_len)# - ts % 2400 # day = 24*100s $ day_len = 2400s
	hour = int((ts - (day * day_len)) / hour_len)
	minute = (ts - (day * day_len) - hour * hour_len)/60
	#print ('day:', day, 'hour:', hour, 'minute:', minute, flush= True)

	#Receive avgsumrealpower:
	events = fncs.get_events()
	for key in events:
		title = key.decode()
		value = fncs.get_value(key).decode()

		if title.startswith('AvgsumRealPower'):
			#print('avgsumrealpower:', value, flush=True)
			AvgsumRealPower_am[day][hour] = get_number(value)
			#print('AvgsumRealPower received: ', AvgsumRealPower_am[day][hour] , flush=True)

		if title.startswith('distribution_load'):
			#print('distribution_load value received:', value, flush=True)
			valuesplit = value.split(' ')
			valuedecoded = valuesplit[0]
			valuedecoded = valuedecoded.replace('i','j')
			valuecomplex = complex(valuedecoded)
			#z = complex(valuecomplex)
			flag = 1
			dload = valuecomplex.real
			#dload = get_number(value)
			#print('distribution_load received:', dload, flush=True)

		if title.startswith('distribution_energy'):
			#print('distribution_energy received:', value, flush=True)
			valuesplit = value.split(' ')
			denergy = float(valuesplit[0])

	if flag == 1:
		dloadvec.append(dload)

	if prev_hour != hour:
		#print('Length of dload vector:', len(dloadvec), flush=True)
		#avgdload = float(sum(dloadvec[prev_hour*hour_len : (hour)*hour_len]))/3600
		#AvgsumRealPower[day][prev_hour] = avgdload
		#print('Average distribution load:', AvgsumRealPower[day][prev_hour], flush=True)
		AvgsumRealPower[prev_day][prev_hour] = float(denergy - denergy_prev)/1000000
		denergy_prev = denergy
		#print('prev_day:', prev_day, 'prev_hour:', prev_hour, 'ADE:', AvgsumRealPower[prev_day][prev_hour], flush=True)
		if hour == 23:
			#print('hour:', hour, 'day:', day, 'loadforecast_RP ADE:', AvgsumRealPower[day][0], flush=True)
			loadforecast_RP([AvgsumRealPower[day][0]], hour, day, FileName)
		elif day>0:
			#print('hour:', hour, 'day:', day, 'loadforecast_RP ADE:', AvgsumRealPower[day-1][hour+1], flush=True)
			loadforecast_RP([AvgsumRealPower[day-1][hour+1]], hour, day, FileName)

	if (day>0):
		if (ts%((day)*(day_len))) == 0:
			#print ('ts2: ',ts, flush = True)
			#print ('AvgsumRealPower1: ',AvgsumRealPower, flush = True)
			loadforecast_RP(AvgsumRealPower[day], hour, day, FileName)
	#print ('ts3: ',ts, flush = True)
	if(len(load)!=0):
		#print ('ts3: ',ts, flush = True)
		for i in range(len(load)):
			#print('ts3:', ts, 'load[i][0]:', load[i][0], flush=True)
			if(ts >= load[i][0]):
				#print ('ts4: ',ts, flush = True)
				if(ts == load[i][0]):
					print('Publishing loadforecast to AMES: ', str(load[i][0]), str(load[i][1]), load[i][2], flush = True)
					fncs.publish(str(load[i][1]), load[i][2])
			else:
				break
	if(ts < (timeSim + deltaT)) :
		ts = fncs.time_request(timeSim + deltaT)
	else:
		#print('time_granted2:', ts, flush = True)
		timeSim = timeSim + deltaT
		ts = fncs.time_request(timeSim + deltaT)

	#print('Day:', day, 'Hour:', hour, 'loadforecastEnergy:', AvgsumRealPower[day][prev_hour], 'loadforecastDload:', AvgsumRealPower_am[day][hour], flush=True)
	prev_day = day
	prev_hour = hour

#f = open("distribution_load_vs_energy.csv","w")
#old_stdout = sys.stdout
#sys.stdout = f
#writer = csv.writer(f, delimiter=',')
'''
for i in range(len(AvgsumRealPower[0])):
	#temp = str(AvgsumRealPower[0][i]) + ',' + str(AvgsumRealPower_am[0][i])
	print('Hour:', i, 'loadforecast energy:', AvgsumRealPower[0][i], 'loadforecast dload:', AvgsumRealPower_am[0][i], flush=True)
'''
#sys.stdout = old_stdout
#writer.writerows(zip([AvgsumRealPower[0][i]],[AvgsumRealPower_am[0][i]]))
#f.close()
fncs.finalize()