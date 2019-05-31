from pyomo.environ import *
import click
# def _zone_generator_map(m, g):
    # raise NotImplementedError("Zonal reserves not implemented yet")

def _zone_generator_map(m, g):
    for bus in m.GeneratorsAtBus:
        GenArray = m.GeneratorsAtBus[bus]
        for gen in GenArray:
            if g == gen:
                BusFound = bus
    for zone in m.BusesAtEachZone:
        busArray = m.BusesAtEachZone[zone]
        for bus in busArray:
            if BusFound == bus:
                ZoneFound = zone
    #click.echo('g, zone: '+ g + ' : ' + ZoneFound)
    return ZoneFound

# def _form_generator_reserve_zones(m,rz):
    # return (g for g in m.Generators if m.ReserveZoneLocation[g]==rz)

def _form_generator_reserve_zones(m,rz):
    ListOfGenCos = []
    for g in m.Generators:
        if m.ReserveZoneLocation[g]==rz:
            ListOfGenCos.append(g)
            #click.echo('g, rz:'+ g + ' : ' + rz)
    #click.echo('ListOfGenCos: '+ str(ListOfGenCos))
    return ListOfGenCos
    #zz = (g for g in m.Generators if m.ReserveZoneLocation[g]==rz)
    #click.echo('zz:' + str(zz))
    #return (g for g in m.Generators if m.ReserveZoneLocation[g]==rz)


# def _reserve_requirement_rule(m, t):
    # return m.ReserveFactor * sum(value(m.Demand[b,t]) for b in m.Buses)
def _reserve_up_requirement_rule(m, t):
    return m.ReserveUpSystemPercent * sum(value(m.Demand[b,t]) for b in m.Buses)
def _reserve_down_requirement_rule(m, t):
    return m.ReserveDownSystemPercent * sum(value(m.Demand[b,t]) for b in m.Buses)

def initialize_global_reserves(model, ReserveDownSystemPercent=None, ReserveUpSystemPercent=None, reserve_up_requirement=_reserve_up_requirement_rule, reserve_down_requirement=_reserve_down_requirement_rule):

    #model.ReserveFactor = Param(within=Reals, initialize=reserve_factor, mutable=True)
    model.ReserveDownSystemPercent = Param(within=Reals, initialize=ReserveDownSystemPercent, mutable=True)
    model.ReserveUpSystemPercent = Param(within=Reals, initialize=ReserveUpSystemPercent, mutable=True)
    #model.ReserveRequirement = Param(model.TimePeriods, initialize=reserve_requirement, within=NonNegativeReals, default=0.0, mutable=True)
    model.ReserveUpRequirement = Param(model.TimePeriods, initialize=reserve_up_requirement, within=NonNegativeReals, default=0.0, mutable=True)
    model.ReserveDownRequirement = Param(model.TimePeriods, initialize=reserve_down_requirement, within=NonNegativeReals, default=0.0, mutable=True)


def initialize_regulating_reserves(model):
    model.RegulatingReserveUpAvailable = Var(model.Generators, model.TimePeriods, initialize=0.0, within=NonNegativeReals)

def initialize_zonal_reserves(model, zone_names=None, buses_at_each_zone=None, ReserveDownZonalPercent=None, ReserveUpZonalPercent=None, generator_reserve_zones=_form_generator_reserve_zones, zone_generator_map=_zone_generator_map):

    model.ReserveZones = Set(initialize=zone_names)
    model.BusesAtEachZone = Set(model.ReserveZones, initialize=buses_at_each_zone)

    model.ReserveZoneLocation = Param(model.Generators, initialize=zone_generator_map)
    model.GeneratorsInReserveZone = Set(model.ReserveZones, initialize=generator_reserve_zones)

    model.ReserveDownZonalPercent = Param(model.ReserveZones, initialize=ReserveDownZonalPercent, within=NonNegativeReals, default=0.0, mutable=True)
    model.ReserveUpZonalPercent = Param(model.ReserveZones, initialize=ReserveUpZonalPercent, within=NonNegativeReals, default=0.0, mutable=True)

# def initialize_zonal_reserves(model, buses=None, generator_reserve_zones=_form_generator_reserve_zones, zone_generator_map=_zone_generator_map):
    # if buses is None:
        # buses = model.Buses
    # model.ReserveZones = Set(initialize=buses)
    # model.ZonalReserveRequirement = Param(model.ReserveZones, model.TimePeriods, default=0.0, mutable=True, within=NonNegativeReals)
    # model.ReserveZoneLocation = Param(model.Generators, initialize=zone_generator_map)

    # model.GeneratorsInReserveZone = Set(model.ReserveZones, initialize=generator_reserve_zones)