
from pyomo.environ import *


def initialize_NDG(model, NDG_names=None, NDG_at_bus=None, NDGdemand=None):

    model.NonDispatchableGenerators = Set(initialize=NDG_names)
    model.NonDispatchableGeneratorsAtBus = Set(model.Buses, initialize=NDG_at_bus)

    model.NondispatchablePower = Param(model.NonDispatchableGenerators, model.TimePeriods, initialize=NDGdemand, default=0.0, mutable=True)

