import math
import csv
import sys
import json
import fncs
import re
import cmath

def loadforecast_RP(loadforecast, h, d):
	time = []
	LSE2 = [200, 176.8,	161.47,	153.73,	146.13,	149.93,	153.73,	169.2, 207.6,	238.4,	246.13,	249.93,	246.13,	238.4,	234.6,	234.6, 249.93,	284.53,	269.2,	265.26,	261.47,	253.73,	234.6,	211.53]
	LSE1 = [250.00,	222.93,	205.04,	196.02,	187.16,	191.59,	196.02,	214.07, 258.86,	294.8,	303.82,	308.25,	303.82,	294.8,	290.37,	290.37, 308.25,	348.62,	330.73,	326.14,	321.71,	312.69,	290.37,	263.46]
	LSE3 = [287.50,	259.89,	244.06,	230.63,	224.44,	226.52,	240.97,	265.73, 291.54,	317.35,	338.00,	346.26,	345.24,	342.15,	342.15,	352.45, 360.72,	368.95,	376.15,	377.17,	363.78,	348.31,	332.17,	302.98]
	loadforecastRTM = [[] for i in range(3)]
	loadforecastDAM = []
	unit = 1 #1000000000
	if h == 23:
		h1 = 0
	else:
		h1 = h +1
	#24 hour vector -> DAM
	if(len(loadforecast) > 1):
		x = (d* day_len)*unit + ((hour_len)/2)*unit - 1*unit
		for j in range(24):
			load.append((x,'loadforecastDAM_h'+str(j), float(loadforecast[j])*1)) # replace 1 with
	else:
		#RTM
		y = (h* hour_len)*unit + (d)*(day_len)*unit + ((hour_len)/2)*unit - 1*unit #10*10*1000000000
		load.append((y, 'loadforecastRTM_'+str(2), + float(LSE2[h1]+loadforecast[0])*1))
		load.append((y, 'loadforecastRTM_'+str(1), float(LSE1[h1])))
		load.append((y, 'loadforecastRTM_'+str(3), float(LSE3[h1])))
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
	print ('time step: ',ts, flush = True)
	day = int(ts / day_len)# - ts % 2400 # day = 24*100s $ day_len = 2400s
	hour = int((ts - (day * day_len)) / hour_len)
	minute = (ts - (day * day_len) - hour * hour_len)/60
	print ('day:', day, 'hour:', hour, 'minute:', minute, flush= True)

	#Receive avgsumrealpower:
	events = fncs.get_events()
	for key in events:
		title = key.decode()
		value = fncs.get_value(key).decode()

		if title.startswith('AvgsumRealPower'):
			print('avgsumrealpower:', value, flush=True)
			AvgsumRealPower_am[day][hour] = get_number(value)
			print('AvgsumRealPower received: ', AvgsumRealPower_am[day][hour] , flush=True)

		if title.startswith('distribution_load'):
			print('distribution_load value received:', value, flush=True)
			valuesplit = value.split(' ')
			valuedecoded = valuesplit[0]
			valuedecoded = valuedecoded.replace('i','j')
			valuecomplex = complex(valuedecoded)
			#z = complex(valuecomplex)
			flag = 1
			dload = valuecomplex.real
			#dload = get_number(value)
			print('distribution_load received:', dload, flush=True)

		if title.startswith('distribution_energy'):
			print('distribution_energy received:', value, flush=True)
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
		print('prev_day:', prev_day, 'prev_hour:', prev_hour, 'ADE:', AvgsumRealPower[prev_day][prev_hour], flush=True)
		if hour == 23:
			print('hour:', hour, 'day:', day, 'loadforecast_RP ADE:', AvgsumRealPower[day][0], flush=True)
			loadforecast_RP([AvgsumRealPower[day][0]], hour, day)
		elif day>0:
			print('hour:', hour, 'day:', day, 'loadforecast_RP ADE:', AvgsumRealPower[day-1][hour+1], flush=True)
			loadforecast_RP([AvgsumRealPower[day-1][hour+1]], hour, day)

	#if (day>0):
	#	if (ts%((day)*(day_len))) == 0:
	#		#print ('ts2: ',ts, flush = True)
	#		#print ('AvgsumRealPower1: ',AvgsumRealPower, flush = True)
	#		loadforecast_RP(AvgsumRealPower[day], hour, day)
	#print ('ts3: ',ts, flush = True)
	if(len(load)!=0):
		#print ('ts3: ',ts, flush = True)
		for i in range(len(load)):
			#print('ts3:', ts, 'load[i][0]:', load[i][0], flush=True)
			if(ts >= load[i][0]):
				#print ('ts4: ',ts, flush = True)
				if(ts == load[i][0]):
					print('Publishing loadforecast to AMES: ', str(load[i][1]), load[i][2], flush = True)
					fncs.publish(str(load[i][1]), load[i][2])
			else:
				break
	if(ts < (timeSim + deltaT)) :
		ts = fncs.time_request(timeSim + deltaT)
	else:
		#print('time_granted2:', ts, flush = True)
		timeSim = timeSim + deltaT
		ts = fncs.time_request(timeSim + deltaT)

	print('Day:', day, 'Hour:', hour, 'loadforecastEnergy:', AvgsumRealPower[day][prev_hour], 'loadforecastDload:', AvgsumRealPower_am[day][hour], flush=True)
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