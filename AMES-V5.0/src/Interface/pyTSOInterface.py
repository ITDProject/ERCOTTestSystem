from py4j.java_gateway import (JavaGateway, GatewayParameters)
from py4j.tests.java_gateway_test import gateway

import numpy as np

def transfer1DNumpyArray2JavaDblAry(gateway, pyArray, size):
    dblAry = gateway.new_array(gateway.jvm.double, size)
    i = 0
    for x in pyArray:
        dblAry[i] = float(x)
        i = i + 1
    return dblAry

def transfer2DNumpyArray2JavaDblAry(gateway, pyArray, x_size,y_size):
    dblAry = gateway.new_array(gateway.jvm.double, x_size,y_size)
    for i in range(x_size):
        for j in range(y_size):
            dblAry[i][j] = pyArray[i][j] 
    return dblAry

def transfer1DJavaArray2NumpyArray(ary) :
    size1 = len(ary)
    np_ary = np.zeros(size1)
    for i in range(size1):
        np_ary[i] = ary[i]
    return np_ary

def transfer2DJavaArray2NumpyArray(ary) :
    size1 = len(ary)
    size2 = len(ary[0])
    np_ary = np.zeros((size1,size2))
    for i in range(size1):
        for j in range(size2):
            np_ary[i,j] = ary[i][j]

    return np_ary



server_port_num = 26000
## start up the AMES Server
import os
myCmd = 'java -jar AMES_V5_TESP.jar'+" "+server_port_num

os.system(myCmd)

## set up the connection via Py4j

gateway = JavaGateway(gateway_parameters=GatewayParameters(port=server_port_num, auto_convert=True))

ames = gateway.entry_point


ames_case_file ="C:\Users\huan289\Qiuhua\FY2016_Project_Transactive_system\FY18\ERCOTTestSystem\AMES-V5.0\DATA\AMESTestInput.dat"


ames.readBaseCase(case_file)  #load the data

ames.startMarket()  # initialize the market



hours_one_day = 24
total_gen_num = 16
wind_plants_num = 8
total_bus_num = 8

unRespMW = np.zeros(total_bus_num, hours_one_day)
respMaxMW = np.zeros(total_bus_num, hours_one_day)
respC2 = np.zeros(total_bus_num, hours_one_day)
respC1 = np.zeros(total_bus_num, hours_one_day)
resp_deg = np.zeros(total_bus_num, hours_one_day)

## wind forecast power??
forecast_wind_power = np.zeros(wind_plants_num, hours_one_day)


## Results
DALMPs = np.zeros(total_bus_num, hours_one_day)
RTLMPs = np.zeros(total_bus_num, hours_one_day)
DAUnitSchedule = np.zeros(total_gen_num, hours_one_day)
RTHourlyUnitCommitments = np.zeros(total_gen_num, 1)

min = 0
interval = 0
hour = 1
day =1
dayMax =2    
M =app.getRTMarketIntervalInMins()
NIH = 60/M


market_done = False

while ( not market_done):
    # get data
    tomorrow = day + 1
    interval = (hour-1)*NIH + min/M
     # hour = 0, collect data for day-ahead market
    if (hour ==2 and min == 0):

        # TODO # collect data for the next-day DA Market
        #ames.collectBidsOffersDAMarket(tomorrow)
        unRespMW_4j  = transfer2DNumpyArray2JavaDblAry(unRespMW)
        respMaxMW_4j = transfer2DNumpyArray2JavaDblAry(respMaxMW)
        respC2_4j    = transfer2DNumpyArray2JavaDblAry(respC2) 
        respC1_4j    = transfer2DNumpyArray2JavaDblAry(respC1)
        resp_deg_4j  = transfer2DNumpyArray2JavaDblAry(resp_deg)
        forecast_wind_power_4j = transfer2DNumpyArray2JavaDblAry(forecast_wind_power)
        
        ames.prepareDayAheadSCUCCase(unRespMW_4j, respMaxMW_4j, respC2_4j, respC1_4j, resp_deg_4j, forecast_wind_power_4j)


    # hour = 2, start DA market
    if(hour ==16 and min == 0):

        ames.runDAMarket(tomorrow)
        
        # get the Day-ahead market solution results, LMPs and Unit schedules
        DA_LMPs = transfer2DJavaArray2NumpyArray(ames.getDALMPs())
        DA_Unit_Schedule = transfer2DJavaArray2NumpyArray(ames.getDAUnitSchedule())
        
        print("DA LMPs: \n", DA_LMPs)
        print("DAUnitSchedule: \n",DA_Unit_Schedule)

   
    if(day>1):
        
        unRespMW_4j  = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num))
        respMaxMW_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num))
        respC2_4j    = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num)) 
        respC1_4j    = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num))
        resp_deg_4j  = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num))
        forecast_wind_power_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(wind_plants_num))
        
        ames.prepareRealTimeSCEDCase(unRespMW_4j, respMaxMW_4j, respC2_4j, respC1_4j, resp_deg_4j, forecast_wind_power_4j)
        ames.runOneStepRTMarket(min, interval, hour, day)
        
        RT_LMPs = transfer1DJavaArray2NumpyArray(ames.getRealTimeLMPs())
        gen_dispatches = transfer1DJavaArray2NumpyArray(ames.getRealTimeGeneratorPowerinMW());
        
        print("RT LMPs: \n",RTLMPs)
        print("RT Gen dispatches: \n", gen_dispatches)
    
    # update AMES internal timer
    ames.updateAMESInternalTimer()
    
    # python side time update
    min = min + M
            
    if (min % 60 == 0):    
        hour = hour + 1
        min = 0
        if (hour == 25):
            min = 0
            hour = 1
            day = day + 1
            

             
    if ((hour == 24) and (day == dayMax)): 
         market_done = true
