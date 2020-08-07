
# coding: utf-8

# In[2]:


import xlrd


# In[55]:


# Open a workbook 
wb = xlrd.open_workbook('../Data/EIA/3_1_Generator_Y2016.xlsx', on_demand = True)


# In[4]:


# Loads only current sheets to memory
ws = wb.sheet_by_name('Operable')


# In[56]:


# Open another workbook 
wb2 = xlrd.open_workbook('../Data/EIA/2___Plant_Y2016.xlsx', on_demand = True)

# Loads only current sheets to memory
ws2 = wb2.sheet_by_name('Plant')


# In[58]:


print(ws.cell(0,0).value)


# In[59]:


print('Sheet:',ws.name, 'nrows', ws.nrows, 'ncols', ws.ncols)


# In[60]:


print(ws2.cell(0,0).value)


# In[61]:


print('Sheet:',ws2.name, 'nrows', ws2.nrows, 'ncols', ws2.ncols)


# In[62]:


custom_range = {}
custom_range['Generators'] = [0, 1, 2, 3, 4, 5, 6, 7, 15, 16, 17, 18, 19, 23]
custom_range['Plants'] = [0, 1, 2, 3, 6, 7, 8, 9, 10]
print('Length of generators data array:', len(custom_range['Generators'])+3)


# In[63]:


content={}
content['Generators'] = [[ws.cell_value(r,c) for c in custom_range['Generators']] for r in range(2,ws.nrows) if ws.cell_value(r,4) == 'TX']
content['Plants'] = [[ws2.cell_value(r,c) for c in custom_range['Plants']] for r in range(2,ws2.nrows) if ws2.cell_value(r,6) == 'TX']
#print(content['Plants'])


# In[64]:


names_generators_file = [ws.cell_value(1,c) for c in custom_range['Generators']]
names_plants_file = [ws2.cell_value(1,c) for c in custom_range['Plants']]


# In[65]:


print(names_generators_file)
print(names_plants_file)


# In[66]:


PlantCodetoLocation = {i[2]: {'Zip': i[5], 'Latitude' : i[7], 'Longitude': i[8]} for i in content['Plants']}


# In[67]:


print(PlantCodetoLocation[content['Generators'][0][2]])
print(PlantCodetoLocation[56233.0])


# In[68]:


print(max([i[8] for i in content['Generators']])) #Printing maximum nameplate capacity out of all generators in Texas


# In[69]:


#ZonesClass = {}
#ZonesClass['CongestionZones'] = {}
#ZonesClass['CongestionZones']['West'] = ['Culberson', 'Jeff Davis', 'Presidio', 'Brewster', 'Reeves', 'Pecos', 'Terrell', 'Hall', 'Childress', 'Hardeman', 'Motley', 'Cottle', 'Foard', 'Wilbarger', 'Wichita', 'Clay', 'Crosby', 'Dickens', 'King', 'Knox', 'Baylor', 'Archer', 'Kent', 'Stonewall', 'Haskell', 'Throckmorton', 'Young', 'Dawson', 'Borden', 'Scurry', 'Fisher', 'Jones', 'Shackelford', 'Stephens', 'Andrews', 'Martin', 'Howard', 'Mitchell', 'Nolan', 'Taylor', 'Callahan', 'Eastland', 'Loving', 'Winkler', 'Ector', 'Midland', 'Glasscock', 'Sterling', 'Coke','Runnels', 'Coleman', 'Ward', 'Crane', 'Upton', 'Reagan', 'Irion', 'Tom Green', 'Concho', 'Crockett', 'Schleicher', 'Sutton']
#ZonesClass['CongestionZones']['Houston'] = ['Waller', 'Montgomery', 'Harris', 'Fort Bend', 'Chambers', 'Brazoria', 'Galveston']
#ZonesClass['CongestionZones']['Northeast'] = ['Cooke', 'Grayson', 'Fannin', 'Lamar', 'Red River', 'Delta', 'Hopkins', 'Franklin', 'Titus']
#ZonesClass['CongestionZones']['North'] = ['Montague', 'Jack', 'Wise', 'Denton', 'Collin', 'Hunt', 'Palo Pinto', 'Parker', 'Tarrant', 'Dallas', 'Rockwall', 'Rains', 'Kaufman', 'Van Zandt', 'Erath', 'Hood', 'Somervell', 'Johnson', 'Ellis', 'Henderson', 'Smith', 'Rusk', 'Brown', 'Comanche', 'San Saba', 'Lampasas', 'Mills', 'Hamilton', 'Bosque', 'Coryell', 'Bell', 'McLennan', 'Hill', 'Falls', 'Limestone', 'Navarro', 'Freestone', 'Anderson', 'Cherokee', 'Nacogdoches', 'Angelina', 'Houston', 'Madison', 'Grimes', 'Brazos', 'Leon', 'Robertson']
#ZonesClass['CongestionZones']['South'] = ['Cameron', 'Hidalgo', 'Starr', 'Zapata', 'Jim Hogg', 'Brooks', 'Kenedy', 'Willacy', 'Kleberg', 'Nueces', 'Jim Wells', 'Duval', 'Webb', 'San Patricio', 'Live Oak', 'McMullen', 'La Salle', 'Dimmit', 'Aransas', 'Refugio', 'Bee', 'Maverick', 'Zavala', 'Frio', 'Atascosa', 'Karnes', 'Goliad', 'Victoria', 'Calhoun', 'Kinney', 'Val Verde', 'Edwards', 'Real', 'Uvalde', 'Kimble', 'Menard', 'McCulloch', 'Mason', 'Llano', 'Gillespie', 'Kerr', 'Bandera', 'Medina', 'Burnet', 'Blanco', 'Kendall', 'Bexar', 'Wilson', 'DeWitt', 'Jackson', 'Matagorda', 'Wharton', 'Austin', 'Washington', 'Burleson', 'Milam', 'Williamson', 'Travis', 'Hays', 'Bastrop', 'Lee', 'Fayette', 'Colorado', 'Lavaca', 'Caldwell', 'Comal', 'Guadalupe', 'Gonzales']
#ZonesClass['CongestionZones']['Others'] = ['El Paso', 'Hudspeth', 'Gaines', 'Yoakum', 'Terry', 'Lynn', 'Cochran', 'Hockley', 'Lubbock', 'Hale', 'Lamb', 'Bailey', 'Parmer', 'Castro', 'Swisher', 'Floyd', 'Garza', 'Briscoe', 'Deaf Smith', 'Randall', 'Armstrong', 'Donley', 'Collingsworth', 'Wheeler', 'Gray', 'Carson', 'Potter', 'Oldham', 'Hartley', 'Moore', 'Hutchinson', 'Roberts', 'Hemphill', 'Lipscomb', 'Ochiltree', 'Hansford', 'Sherman', 'Dallam', 'Bowie', 'Morris', 'Cass', 'Camp', 'Upshur', 'Marion', 'Gregg', 'Harrison', 'Panola', 'Shelby', 'San Augustine', 'Sabine', 'Trinity', 'Polk', 'Tyler', 'Jasper', 'Newton', 'San Jacinto', 'Hardin', 'Liberty', 'Jefferson', 'Orange', 'Wood', 'Walker']

#ZonesClass['WeatherZones'] = {}
#ZonesClass['WeatherZones']['FarWest'] = ['Culberson', 'Jeff Davis', 'Presidio', 'Brewster', 'Reeves', 'Pecos', 'Terrell', 'Crockett', 'Ward', 'Crane', 'Upton', 'Reagan', 'Loving', 'Winkler', 'Ector', 'Midland', 'Glasscock', 'Andrews', 'Martin', 'Howard', 'Dawson', 'Borden']
#ZonesClass['WeatherZones']['West'] = ['Val Verde', 'Kinney', 'Uvalde', 'Edwards', 'Real', 'Kerr', 'Sutton', 'Kimble', 'Gillespie', 'Schleicher', 'Menard', 'Mason', 'Llano', 'Irion', 'Tom Green', 'Concho', 'McCulloch', 'San Saba', 'Sterling', 'Coke', 'Runnels', 'Coleman', 'Mitchell', 'Nolan', 'Taylor', 'Scurry', 'Fisher', 'Jones']
#ZonesClass['WeatherZones']['South'] = ['Cameron', 'Hidalgo', 'Willacy', 'Starr', 'Zapata', 'Jim Hogg', 'Brooks', 'Kenedy', 'Webb', 'Duval', 'Jim Wells', 'Kleberg', 'Nueces', 'San Patricio', 'Live Oak', 'McMullen', 'La Salle', 'Dimmit', 'Maverick', 'Zavala', 'Frio', 'Atascosa', 'Bee', 'Refugio', 'Aransas', 'Goliad']
#ZonesClass['WeatherZones']['Coastal'] = ['Calhoun', 'Victoria', 'Jackson', 'Matagorda', 'Wharton', 'Brazoria', 'Fort Bend', 'Galveston', 'Harris', 'Waller', 'Montgomery', 'Chambers']
#ZonesClass['WeatherZones']['East'] = ['Grimes', 'Brazos', 'Robertson', 'Madison', 'Leon', 'Houston', 'Freestone', 'Anderson', 'Henderson', 'Van Zandt', 'Rains', 'Hopkins', 'Delta', 'Franklin', 'Titus', 'Smith', 'Cherokee', 'Angelina', 'Nacogdoches', 'Rusk']
#ZonesClass['WeatherZones']['NorthCentral'] = ['Throckmorton', 'Young', 'Jack', 'Wise', 'Denton', 'Collin', 'Hunt', 'Shackelford', 'Stephens', 'Palo Pinto', 'Parker', 'Tarrant', 'Dallas', 'Rockwall', 'Kaufman', 'Callahan', 'Eastland', 'Erath', 'Hood', 'Johnson', 'Ellis', 'Somervell', 'Brown', 'Comanche', 'Bosque', 'Hill', 'Navarro', 'Mills', 'Hamilton', 'McLennan', 'Limestone', 'Coryell', 'Bell', 'Falls']
#ZonesClass['WeatherZones']['SouthCentral'] = ['Medina', 'Bandera', 'Bexar', 'Kendall', 'Blanco', 'Burnet', 'Comal', 'Wilson', 'Guadalupe', 'Hays', 'Travis', 'Williamson', 'Karnes', 'DeWitt', 'Gonzales', 'Caldwell', 'Bastrop', 'Milam', 'Lee', 'Fayette', 'Lavaca', 'Colorado', 'Austin', 'Washington', 'Burleson', 'Milam']
#ZonesClass['WeatherZones']['North'] = ['Briscoe', 'Hall', 'Childress', 'Floyd', 'Motley', 'Cottle', 'Crosby', 'Dickens', 'King', 'Garza', 'Kent', 'Stonewall', 'Hardeman', 'Foard', 'Knox', 'Haskell', 'Wilbarger', 'Baylor', 'Wichita', 'Archer', 'Clay', 'Montague', 'Cooke', 'Grayson', 'Fannin', 'Lamar', 'Red River']
#ZonesClass['WeatherZones']['Others'] = ['El Paso', 'Hudspeth', 'Gaines', 'Yoakum', 'Terry', 'Lynn', 'Cochran', 'Hockley', 'Lubbock', 'Hale', 'Lamb', 'Bailey', 'Parmer', 'Castro', 'Swisher', 'Deaf Smith', 'Randall', 'Armstrong', 'Donley', 'Collingsworth', 'Wheeler', 'Gray', 'Carson', 'Potter', 'Oldham', 'Hartley', 'Moore', 'Hutchinson', 'Roberts', 'Hemphill', 'Lipscomb', 'Ochiltree', 'Hansford', 'Sherman', 'Dallam', 'Bowie', 'Morris', 'Cass', 'Camp', 'Upshur', 'Marion', 'Gregg', 'Harrison', 'Panola', 'Shelby', 'San Augustine', 'Sabine', 'Trinity', 'Polk', 'Tyler', 'Jasper', 'Newton', 'San Jacinto', 'Hardin', 'Liberty', 'Jefferson', 'Orange', 'Wood', 'Walker']

import json
f = open('../Data/ZonesToCounties.json','r')
ZonesClass = json.load(f)
f.close()


# In[70]:


print('No. of counties in ERCOT:', len(ZonesClass['CongestionZones']['West']+ ZonesClass['CongestionZones']['Northeast'] + ZonesClass['CongestionZones']['South'] + ZonesClass['CongestionZones']['Houston'] + ZonesClass['CongestionZones']['North']), 'Others:', len(ZonesClass['CongestionZones']['Others']))
print('No. of counties in ERCOT (by weather zones):', len(ZonesClass['WeatherZones']['West'] + ZonesClass['WeatherZones']['East'] + ZonesClass['WeatherZones']['South'] + ZonesClass['WeatherZones']['Coastal'] + ZonesClass['WeatherZones']['North'] + ZonesClass['WeatherZones']['FarWest'] + ZonesClass['WeatherZones']['SouthCentral'] + ZonesClass['WeatherZones']['NorthCentral']), 'Others:', len(ZonesClass['WeatherZones']['Others']))


# In[71]:


Zones = {}
for k,v in ZonesClass['WeatherZones'].items(): #replacing with weather zones
    for i in v:
        Zones[i] = k


# In[72]:


data = {z : [] for z in ZonesClass['WeatherZones']} #'West':[], 'Houston': [], 'Northeast' : [], 'North' : [], 'South' : [], 'Others' : []}
#datawithlocations = data


# In[73]:


GenTypes = list(set([i[7] for i in content['Generators']]))


# In[74]:


print(GenTypes)


# In[75]:


for i in content['Generators']:
    ##print(i)
    ##print(i+[v for k,v in PlantCodetoLocation[i[2]].items()])
    ##data[Zones[i[5]]].append(i)
    templist = [v for k,v in PlantCodetoLocation[i[2]].items()]
    q = i + templist
    #print(q)
    ##datawithlocations[Zones[i[5]]].append(i+[v for k,v in PlantCodetoLocation[i[2]].items()])
    data[Zones[i[5]]].append(q)


# In[76]:


GenCapacityData = {}
GenCapacityData['total'] = {}
GenCapacityData['West'] = {}
GenCapacityData['North'] = {}
#GenCapacityData['Northeast'] = {}
GenCapacityData['South'] = {}
#GenCapacityData['Houston'] = {}
GenCapacityData['NorthCentral'] = {}
GenCapacityData['SouthCentral'] = {}
GenCapacityData['Coastal'] = {}
GenCapacityData['FarWest'] = {}
GenCapacityData['East'] = {}

for i in GenTypes:
    GenCapacityData['total'][i] = 0.0
    GenCapacityData['West'][i] = 0.0
    GenCapacityData['North'][i] = 0.0
    #GenCapacityData['Northeast'][i] = 0.0
    GenCapacityData['South'][i] = 0.0
    GenCapacityData['East'][i] = 0.0
    GenCapacityData['NorthCentral'][i] = 0.0
    GenCapacityData['SouthCentral'][i] = 0.0
    GenCapacityData['Coastal'][i] = 0.0
    GenCapacityData['FarWest'][i] = 0.0
    #GenCapacityData['Houston'][i] = 0.0


# In[77]:


print(data['West'][1])
#print(datawithlocations['West'][100])


# In[78]:


cgnames = ['West', 'North', 'Northeast', 'Houston', 'South']
weathernames = ['West', 'FarWest', 'Coastal', 'South', 'SouthCentral', 'NorthCentral', 'East', 'North']
#for cg in cgnames+['total']:
#    for i in data[cg]:
#        GenCapacityData[cg][i[7]] += i[8]
for z in weathernames:
    for i in data[z]:
        GenCapacityData[z][i[7]] += i[8]


# In[88]:


import json
outfile = open('../Data/GenCapacitybyZones.json', 'w') 
json.dump(GenCapacityData, outfile)
outfile.close()


# In[80]:


print(GenCapacityData['West'])


# In[81]:


print('ERCOT Gens:', len(data['West'] + data['Coastal'] + data['North']+ data['NorthCentral'] + data['SouthCentral']+ data['South']+ data['FarWest']+ data['East']), 'Others:', len(data['Others'])) 


# In[82]:


#print('West:', len(data['West']), 'North:', len(data['North']), 'South:', len(data['South']), 'Northeast:', len(data['Northeast']), 'Houston:', len(data['Houston']), 'Others:', len(data['Others']))


# In[86]:


import json
outfile = open('../Data/GenData.json', 'w') 
json.dump(data, outfile)
outfile.close()


# In[87]:


print('JSON data -> In each list : [0] Utility ID, [1] Utility Name, [2] Plant Code, [3] Plant Name, [4] State, [5] County, [6] Generator ID, [7] Technology, [8] Nameplate Capacity (MW), [9] Nameplate Power Factor, [10] Summer Capacity (MW), [11] Winter Capacity (MW), [12] Minimum Load (MW), [13] Status, [14] ZipCode, [15] Latitude, [16] Longitude')

