from py4j.java_gateway import (JavaGateway, GatewayParameters)
from py4j.tests.java_gateway_test import gateway
import time
import numpy as np
import os


def transfer1DNumpyArray2JavaDblAry(pyArray):
    sz = pyArray.shape
    dblAry = gateway.new_array(gateway.jvm.double, sz[0])
    for i in range(sz[0]):
        dblAry[i] = pyArray[i]
    return dblAry


def transfer2DNumpyArray2JavaDblAry(pyArray):
    sz = pyArray.shape
    dblAry = gateway.new_array(gateway.jvm.double, sz[0], sz[1])
    for i in range(sz[0]):
        for j in range(sz[1]):
            dblAry[i][j] = pyArray[i][j]
    return dblAry


def transfer1DJavaArray2NumpyArray(ary):
    size1 = len(ary)
    np_ary = np.zeros(size1)
    for i in range(size1):
        np_ary[i] = ary[i]
    return np_ary


def transfer2DJavaArray2NumpyArray(ary):
    size1 = len(ary)
    size2 = len(ary[0])
    np_ary = np.zeros((size1, size2), dtype=float)
    for i in range(size1):
        for j in range(size2):
            np_ary[i, j] = ary[i][j]
    return np_ary


# can place it into batch/shell file
server_port_num = "26000"
# ames_case_file ="C:\Users\huan289\Qiuhua\FY2016_Project_Transactive_system\FY18\ERCOTTestSystem\AMES-V5.0\DATA\AMESTestInput.dat"
ames_case_file = "/home/osboxes/grid/repository/ERCOTTestSystem/AMES-V5.0/DATA/AMESTestInput.dat"
ames_jar_file = "\"/home/osboxes/grid/repository/ERCOTTestSystem/AMES-V5.0/dist/AMES-V5.0.jar\""

## start up the AMES Server
myCmd = "java -jar " + ames_jar_file + " " + server_port_num + " " + ames_case_file + " &"
#os.system(myCmd)

## set up the connection via Py4j
#gateway = JavaGateway(gateway_parameters=GatewayParameters(port=server_port_num, auto_convert=False))

found = False
while not found:
  try:
    gateway = JavaGateway()
    ames = gateway.entry_point
    # ames.readBaseCase(case_file)  #load the data
    ames.startMarket()  # initialize the market
    found = True
  except :
    pass


hours_one_day = 24
total_gen_num = 16
wind_plants_num = 8
total_bus_num = 8

unRespMW = np.zeros([total_bus_num, hours_one_day], dtype=float)
respMaxMW = np.zeros([total_bus_num, hours_one_day], dtype=float)
respC2 = np.zeros([total_bus_num, hours_one_day], dtype=float)
respC1 = np.zeros([total_bus_num, hours_one_day], dtype=float)
resp_deg = np.zeros([total_bus_num, hours_one_day], dtype=float)

## wind forecast power??
forecast_wind_power = np.zeros([wind_plants_num, hours_one_day], dtype=float)

## Results
DALMPs = np.zeros([total_bus_num, hours_one_day], dtype=float)
RTLMPs = np.zeros([total_bus_num, hours_one_day], dtype=float)
# DAUnitSchedule = np.zeros([total_gen_num, hours_one_day])
# RTHourlyUnitCommitments = np.zeros([total_gen_num, 1])

mn = 0
interval = 0
hour = 1
day = 1
dayMax = 2
M = ames.getRTMarketIntervalInMins()
NIH = 60 // M

market_done = False

while (not market_done):
    # get data
    tomorrow = day + 1
    interval = (hour - 1) * NIH + mn // M
    # hour = 0, collect data for day-ahead market
    if (hour == 2 and mn == 0):
        # TODO # collect data for the next-day DA Market
        # ames.collectBidsOffersDAMarket(tomorrow)
        unRespMW_4j = transfer2DNumpyArray2JavaDblAry(unRespMW)  #LSE
        respMaxMW_4j = transfer2DNumpyArray2JavaDblAry(respMaxMW)  #price sens
        respC2_4j = transfer2DNumpyArray2JavaDblAry(respC2)
        respC1_4j = transfer2DNumpyArray2JavaDblAry(respC1)
        resp_deg_4j = transfer2DNumpyArray2JavaDblAry(resp_deg)
        forecast_wind_power_4j = transfer2DNumpyArray2JavaDblAry(forecast_wind_power)

        ames.prepareDayAheadSCUCCase(unRespMW_4j, respMaxMW_4j, respC2_4j, respC1_4j, resp_deg_4j,
                                     forecast_wind_power_4j)

    # hour = 2, start DA market
    if (hour == 16 and mn == 0):
        ames.runDAMarket(tomorrow)

        # get the Day-ahead market solution results, LMPs and Unit schedules
        DA_LMPs = transfer2DJavaArray2NumpyArray(ames.getDALMPs())
        DA_Unit_Schedule = transfer2DJavaArray2NumpyArray(ames.getDAUnitSchedule())
        DA_gen_dispatches = transfer2DJavaArray2NumpyArray(ames.getDAUnitPower())

        print("DA LMPs: \n", DA_LMPs)
        print("DA Unit Schedule: \n", DA_Unit_Schedule)
        print("DA Gen dispatches: \n", DA_gen_dispatches)

    if (day > 1):
        unRespMW_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num, dtype=float))
        respMaxMW_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num, dtype=float))
        respC2_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num, dtype=float))
        respC1_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num, dtype=float))
        resp_deg_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(total_bus_num, dtype=float))
        forecast_wind_power_4j = transfer1DNumpyArray2JavaDblAry(np.zeros(wind_plants_num, dtype=float))

        ames.prepareRealTimeSCEDCase(unRespMW_4j, respMaxMW_4j, respC2_4j, respC1_4j, resp_deg_4j,
                                     forecast_wind_power_4j)
        ames.runOneStepRTMarket(mn, interval, hour, day)

        RT_LMPs = transfer1DJavaArray2NumpyArray(ames.getRealTimeLMPs())
        gen_dispatches = transfer1DJavaArray2NumpyArray(ames.getRealTimeGeneratorPowerinMW());

        print("RT LMPs: \n", RT_LMPs)
        print("RT Gen dispatches: \n", gen_dispatches)

    # update AMES internal timer
    ames.updateAMESInternalTimer()

    # python side time update
    mn = mn + M

    if (mn % 60 == 0):
        hour = hour + 1
        mn = 0
        if (hour == 25):
            mn = 0
            hour = 1
            day = day + 1

    if (hour == 24) and (day == dayMax):
        market_done = True

ames.closeMarket()
