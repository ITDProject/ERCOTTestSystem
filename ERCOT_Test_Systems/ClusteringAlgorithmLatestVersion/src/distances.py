from utils import euclidean

EightNodeNumberMap = {0:1, 276:2, 447:3, 481:4, 525:5, 694:6, 933:7, 1480:8}

EightNodeLocationData = {'0_None': { 'location': { 'lat': 32.48495926252003, 'lng': -96.70957688462417 }}, '276_None': {'location': { 'lat': 29.65967776090361, 'lng': -95.42729030608442 }}, '447_None': {'location': {'lat': 33.95412407455294, 'lng': -99.98991164554926}}, '481_Onshore Wind Turbine': { 'location': { 'lat': 32.07814367480595, 'lng': -101.27130182090112 }},'525_None': { 'location': { 'lat': 29.8003227514715, 'lng': -97.92643466798783 } },'694_None': {'location': {'lat':29.270701714288283, 'lng': -100.24506894025335}},'933_None': {'location': {'lat': 27.361603784152422,'lng': -97.7250449632622 }},'1480_None': {'location': {'lat': 29.97418558573035,'lng': -103.99866183928118}}}

Distance = [[euclidean(v['location']['lat'], v['location']['lng'], v1['location']['lat'], v1['location']['lng']) for k,v in EightNodeLocationData.items()] for k1,v1 in EightNodeLocationData.items()]

AdjacencyMatrix = [[0,1,1,1,1,0,0,0], [1,0,0,0,1,0,1,0], [1,0,0,1,0,0,0,0], [1,0,1,0,1,1,0,1], [1,1,0,1,0,1,1,0], [0,0,0,1,1,0,1,0], [0,1,0,0,1,1,0,0], [0,0,0,1,0,0,0,0]]

DistanceMatrix = {str(i+1)+str(j+1) : Distance[i][j] * AdjacencyMatrix[i][j] for i in range(8) for j in range(8) if i < j and AdjacencyMatrix[i][j] == 1}

Reactances = {k: 0.584*v for k,v in DistanceMatrix.items()}

print('Distance Matrix:', DistanceMatrix)
print('Reactances:', Reactances)

GenData = {'1':
{
'None': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 19825.78954,
'type': 'None',
'county': 'None'
},

'Natural Gas Internal Combustion Engine': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 40.900000000000006,
'type': 'Natural Gas Internal Combustion Engine',
'county': 'None'
},

'Solar Photovoltaic': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 7.0,
'type': 'Solar Photovoltaic',
'county': 'None'
},

'Natural Gas Steam Turbine': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 5594.1,
'type': 'Natural Gas Steam Turbine',
'county': 'None'
},

'Natural Gas Fired Combustion Turbine': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 677.9000000000001,
'type': 'Natural Gas Fired Combustion Turbine',
'county': 'None'
},

'Natural Gas Fired Combined Cycle': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 13665.9,
'type': 'Natural Gas Fired Combined Cycle',
'county': 'None'
},

'Onshore Wind Turbine': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 1674.7999999999997,
'type': 'Onshore Wind Turbine',
'county': 'None'
},

'Nuclear': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 2430.0,
'type': 'Nuclear',
'county': 'None'
},

'Conventional Steam Coal': {
'location': {'lat': 32.48495926252003, 'lng': -96.70957688462417 },
'capacity': 11664.8,
'type': 'Conventional Steam Coal',
'county': 'None'
}},

'2':{
'None': {
'location': {'lat': 29.65967776090361, 'lng': -95.42729030608442 },
'capacity': 14270.053190000002,
'type': 'None',
'county': 'None'
},

'Natural Gas Fired Combustion Turbine': {
'location': {'lat': 29.65967776090361, 'lng': -95.42729030608442 },
'capacity': 3469.7999999999997,
'type': 'Natural Gas Fired Combustion Turbine',
'county': 'None'
},

'Natural Gas Fired Combined Cycle': {
'location': {'lat': 29.65967776090361, 'lng': -95.42729030608442 },
'capacity': 12551.2,
'type': 'Natural Gas Fired Combined Cycle',
'county': 'None'
},

'Natural Gas Steam Turbine': {
'location': {'lat': 29.65967776090361, 'lng': -95.42729030608442 },
'capacity': 4740.7,
'type': 'Natural Gas Steam Turbine',
'county': 'None'
},

'Conventional Steam Coal': {
'location': {'lat': 29.65967776090361, 'lng': -95.42729030608442 },
'capacity': 3190.3,
'type': 'Conventional Steam Coal',
'county': 'None'
},

'Nuclear': {
'location': {'lat': 29.65967776090361, 'lng': -95.42729030608442 },
'capacity': 2708.6,
'type': 'Nuclear',
'county': 'None'
}},

'3':{
'None': {
'location': {'lat': 33.95412407455294, 'lng': -99.98991164554926 },
'capacity': 387.7724579999999,
'type': 'None',
'county': 'None'
},

'Natural Gas Fired Combined Cycle': {
'location': {'lat': 33.95412407455294, 'lng': -99.98991164554926 },
'capacity': 80.0,
'type': 'Natural Gas Fired Combined Cycle',
'county': 'None'
},

'Onshore Wind Turbine': {
'location': {'lat': 33.95412407455294, 'lng': -99.98991164554926 },
'capacity': 2242.2,
'type': 'Onshore Wind Turbine',
'county': 'None'
},

'Conventional Steam Coal': {
'location': {'lat': 33.95412407455294, 'lng': -99.98991164554926 },
'capacity': 720.0,
'type': 'Conventional Steam Coal',
'county': 'None'
},

'Solar Photovoltaic': {
'location': {'lat': 33.95412407455294, 'lng': -99.98991164554926 },
'capacity': 100.0,
'type': 'Solar Photovoltaic',
'county': 'None'
}},

'4':{
'Onshore Wind Turbine': {
'location': {'lat': 32.07814367480595, 'lng': -101.27130182090112 },
'capacity': 8730.3,
'type': 'Onshore Wind Turbine',
'county': 'None'
},

'None': {
'location': {'lat': 32.07814367480595, 'lng': -101.27130182090112 },
'capacity': 1674.1437620000002,
'type': 'None',
'county': 'None'
},

'Natural Gas Internal Combustion Engine': {
'location': {'lat': 32.07814367480595, 'lng': -101.27130182090112 },
'capacity': 3.3,
'type': 'Natural Gas Internal Combustion Engine',
'county': 'None'
},

'Natural Gas Fired Combined Cycle': {
'location': {'lat': 32.07814367480595, 'lng': -101.27130182090112 },
'capacity': 2087.1,
'type': 'Natural Gas Fired Combined Cycle',
'county': 'None'
},

'Natural Gas Fired Combustion Turbine': {
'location': {'lat': 32.07814367480595, 'lng': -101.27130182090112 },
'capacity': 1347.8,
'type': 'Natural Gas Fired Combustion Turbine',
'county': 'None'
},

'Solar Photovoltaic': {
'location': {'lat': 32.07814367480595, 'lng': -101.27130182090112 },
'capacity': 190.2,
'type': 'Solar Photovoltaic',
'county': 'None'
}},

'5':{
'None': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 8751.107714000002,
'type': 'None',
'county': 'None'
},

'Conventional Steam Coal': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 5728.1,
'type': 'Conventional Steam Coal',
'county': 'None'
},

'Natural Gas Fired Combined Cycle': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 5901.5,
'type': 'Natural Gas Fired Combined Cycle',
'county': 'None'
},

'Natural Gas Steam Turbine': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 3201.0,
'type': 'Natural Gas Steam Turbine',
'county': 'None'
},

'Natural Gas Fired Combustion Turbine': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 1234.6,
'type': 'Natural Gas Fired Combustion Turbine',
'county': 'None'
},

'Solar Photovoltaic': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 127.5,
'type': 'Solar Photovoltaic',
'county': 'None'
},

'Natural Gas Internal Combustion Engine': {
'location': {'lat': 29.8003227514715, 'lng': -97.92643466798783 },
'capacity': 252.60000000000008,
'type': 'Natural Gas Internal Combustion Engine',
'county': 'None'
}},

'6':{
'None': {
'location': {'lat': 29.270701714288283, 'lng': -100.24506894025335 },
'capacity': 351.229579,
'type': 'None',
'county': 'None'
},

'Solar Photovoltaic': {
'location': {'lat': 29.270701714288283, 'lng': -100.24506894025335 },
'capacity': 139.6,
'type': 'Solar Photovoltaic',
'county': 'None'
},

'Onshore Wind Turbine': {
'location': {'lat': 29.270701714288283, 'lng': -100.24506894025335 },
'capacity': 99.8,
'type': 'Onshore Wind Turbine',
'county': 'None'
}},

'7':{
'Natural Gas Fired Combined Cycle': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 5853.900000000001,
'type': 'Natural Gas Fired Combined Cycle',
'county': 'None'
},

'None': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 4209.7054419999995,
'type': 'None',
'county': 'None'
},

'Natural Gas Fired Combustion Turbine': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 581.6,
'type': 'Natural Gas Fired Combustion Turbine',
'county': 'None'
},

'Conventional Steam Coal': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 622.4,
'type': 'Conventional Steam Coal',
'county': 'None'
},

'Natural Gas Steam Turbine': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 725.1,
'type': 'Natural Gas Steam Turbine',
'county': 'None'
},

'Onshore Wind Turbine': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 3562.2000000000003,
'type': 'Onshore Wind Turbine',
'county': 'None'
},

'Natural Gas Internal Combustion Engine': {
'location': {'lat': 27.361603784152422, 'lng': -97.7250449632622 },
'capacity': 224.39999999999995,
'type': 'Natural Gas Internal Combustion Engine',
'county': 'None'
}},

'8':{
'None': {
'location': {'lat': 29.97418558573035, 'lng': -103.99866183928118 },
'capacity': 32.829978,
'type': 'None',
'county': 'None'
},

'Solar Photovoltaic': {
'location': {'lat': 29.97418558573035, 'lng': -103.99866183928118 },
'capacity': 10.0,
'type': 'Solar Photovoltaic',
'county': 'None'
}}
}

# print('GenData', GenData)