import os
import click


generator_data_str_format = '{bus}\t{Pg}\t{Qg}\t{Qmax}\t{Qmin}\t{Vg}\t{mBase}\t{status}\t{Pmax}\t{Pmin}\t{Pc1}\t{Pc2}\t{Qc1min}\t{Qc1max}\t{Qc2min}\t{Qc2max}\t{ramp_agc}\t{ramp_10}\t{ramp_30}\t{ramp_q}\t{apf}'.format

current_directory = os.path.realpath(os.path.dirname(__file__))
#click.echo("printing current_directory: " + current_directory)


def int_else_float_except_string(s):
    try:
        f = float(s.replace(',', '.'))
        i = int(f)
        return i if i==f else f
    except ValueError:
        return s


def has_number(string):
    return any(c.isdigit() for c in string)


def dict_to_repr(d):
    string = ''
    for i, (k, v) in enumerate(d.items()):
        if i == 0:
            string = string + '{}={}'.format(k, v)
        else:
            string = string + ', {}={}'.format(k, v)
    return string


def make_interpolater(domain_min, domain_max, range_min, range_max):
    # Figure out how 'wide' each range is
    domain_span = domain_max - domain_min
    range_span = range_max - range_min

    try:
        # Compute the scale factor between left and right values
        scale_factor = float(range_span) / float(domain_span)
    except ZeroDivisionError:
        scale_factor = 0

    # create interpolation function using pre-calculated scaleFactor
    def interp_fn(value):
        return range_min + (value-domain_min)*scale_factor

    return interp_fn



def create_gen_data(**kwargs):
    gen_data = dict()

    gen_data['bus'] = kwargs.pop('bus', 0)
    gen_data['Pg'] = kwargs.pop('Pg', 0)
    gen_data['Qg'] = kwargs.pop('Qg', 0)
    gen_data['Qmax'] = kwargs.pop('Qmax', 0)
    gen_data['Qmin'] = kwargs.pop('Qmin', 0)
    gen_data['Vg'] = kwargs.pop('Vg', 0)
    gen_data['mBase'] = kwargs.pop('mBase', 0)
    gen_data['status'] = kwargs.pop('status', 0)
    gen_data['Pmax'] = kwargs.pop('Pmax', 0)
    gen_data['Pmin'] = kwargs.pop('Pmin', 0)
    gen_data['Pc1'] = kwargs.pop('Pc1', 0)
    gen_data['Pc2'] = kwargs.pop('Pc2', 0)
    gen_data['Qc1min'] = kwargs.pop('Qc1min', 0)
    gen_data['Qc1max'] = kwargs.pop('Qc1max', 0)
    gen_data['Qc2min'] = kwargs.pop('Qc2min', 0)
    gen_data['Qc2max'] = kwargs.pop('Qc2max', 0)
    gen_data['ramp_agc'] = kwargs.pop('ramp_agc', 0)
    gen_data['ramp_10'] = kwargs.pop('ramp_10', 0)
    gen_data['ramp_30'] = kwargs.pop('ramp_30', 0)
    gen_data['ramp_q'] = kwargs.pop('ramp_q', 0)
    gen_data['apf'] = kwargs.pop('apf', 0)

    return gen_data


def read_unit_commitment(uc):

    with open(uc) as f:
        data = f.read()

    uc_dict = dict()
    for l in data.splitlines():
        if l.startswith('#'):
            continue
        l = l.strip()

        if l == '1' or l=='0':
            uc.append(l)
        else:
            uc = []
            uc_dict[l] = uc
    return uc_dict


def find_generators(data):
    DIRECTIVE = r'set ThermalGenerators :='
    for l in data.splitlines():
        if l.startswith(DIRECTIVE):
            return l.split('=')[1].strip('; ').split()

def find_buses(data):
    for l in data.splitlines():
        DIRECTIVE = 'set Buses := '
        if l.startswith(DIRECTIVE):
            return l.split(DIRECTIVE)[1].strip(';').split()

def read_model(model_data):

    click.echo("In psst utils.py read_model method")
    with open(model_data) as f:
        data = f.read()
        #click.echo("printing data:" + data)

    from .case import PSSTCase
    c = PSSTCase(os.path.join(current_directory, '../cases/case.m'))
    #click.echo("printing file path: " + os.path.join(current_directory, '../cases/case.m'))
    #click.echo("printing c: " + str(c))

    click.echo("1 printing c.gencost: " + str(c.gencost))
    ag = find_generators(data)
    for g in ag:
        c.gen.loc[g] = c.gen.loc['GenCo0']
        c.gencost.loc[g] = c.gencost.loc['GenCo0']

    if 'GenCo0' not in ag:
        c.gen.drop('GenCo0', inplace=True)
        c.gencost.drop('GenCo0', inplace=True)

    DIRECTIVE = 'set ThermalGeneratorsAtBus'
    for l in data.splitlines():
        if l.startswith(DIRECTIVE):
            bus, gen = l.split(DIRECTIVE)[1].split(':=')
            bus = bus.replace(']', '').replace('[', '').strip()
            gen = gen.replace(';', '').strip()
            c.gen.loc[gen, 'GEN_BUS'] = bus

    for l in data.splitlines():
        if l.startswith('param StorageFlag'):
            c.StorageFlag = float(l.split(':=')[1].split(';')[0].replace(' ', ''))
        if l.startswith('param NDGFlag'):
            c.NDGFlag = float(l.split(':=')[1].split(';')[0].replace(' ', ''))

    #click.echo("2 printing c.gencost: " + str(c.gencost))

    READ = False
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l == 'param: PowerGeneratedT0 UnitOnT0State MinimumPowerOutput MaximumPowerOutput MinimumUpTime MinimumDownTime NominalRampUpLimit NominalRampDownLimit StartupRampLimit ShutdownRampLimit ColdStartCost HotStartCost ShutdownCostCoefficient :=':
            READ = True
            continue

        if READ is True:
            g, pg, status, min_g, max_g, min_up_time, min_down_time, ramp_up_rate, ramp_down_rate, startup_ramp_rate, shutdown_ramp_rate, coldstartcost, hotstartcost, shutdowncostcoefficient = l.split()
            c.gen.loc[g, 'PMAX'] = float(max_g.replace(',', '.'))  # Handle europe number format TODO: use better fix!
            c.gen.loc[g, 'PG'] = float(pg.replace(',', '.'))
            c.gen.loc[g, 'UnitOnT0State'] = int(status.replace(',','.'))
            #click.echo("In psst - UnitOnT0State :" + str(c.gen.loc[g, 'UnitOnT0State']))
            c.gen.loc[g, 'PMIN'] = float(min_g.replace(',', '.'))
            c.gen.loc[g, 'MINIMUM_UP_TIME'] = int(min_up_time)
            c.gen.loc[g, 'MINIMUM_DOWN_TIME'] = int(min_down_time)
            ramp = float(ramp_up_rate.replace(',', '.'))
            c.gen.loc[g, 'RAMP_10'] = float(max_g.replace(',', '.')) if ramp == 0 else ramp
            startup_ramp = float(startup_ramp_rate.replace(',', '.'))
            c.gen.loc[g, 'STARTUP_RAMP'] = float(max_g.replace(',', '.')) if startup_ramp == 0 else startup_ramp
            shutdown_ramp = float(shutdown_ramp_rate.replace(',', '.'))
            c.gen.loc[g, 'SHUTDOWN_RAMP'] = float(max_g.replace(',', '.')) if shutdown_ramp == 0 else shutdown_ramp
            cold_start_cost = float(coldstartcost.replace(',', '.'))
            c.gencost.loc[g, "STARTUP_COLD"] = float(max_g.replace(',', '.')) if cold_start_cost == 0 else cold_start_cost
            #click.echo("Reading model data - printing STARTUP or cold_start_cost:" + str(c.gen.loc[g, 'STARTUP']) + str(cold_start_cost))
            hot_start_cost = float(hotstartcost.replace(',', '.'))
            c.gencost.loc[g, "STARTUP_HOT"] = float(max_g.replace(',', '.')) if hot_start_cost == 0 else hot_start_cost
            shutdown_costcoefficient = float(shutdowncostcoefficient.replace(',', '.'))
            c.gencost.loc[g, "SHUTDOWN_COEFFICIENT"] = float(max_g.replace(',', '.')) if shutdown_costcoefficient == 0 else shutdown_costcoefficient


    branch_number = 1
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l == 'param: BusFrom BusTo ThermalLimit Reactance :=':
            READ = True
            continue

        if READ is True:
            _, b1, b2, tl, r = l.split()
            c.branch.loc[branch_number] = c.branch.loc[0]
            c.branch.loc[branch_number, 'F_BUS'] = b1
            c.branch.loc[branch_number, 'T_BUS'] = b2
            c.branch.loc[branch_number, 'BR_X'] = float(r.replace(',', '.'))
            c.branch.loc[branch_number, 'RATE_A'] = float(tl.replace(',', '.'))
            branch_number = branch_number + 1

    c.branch.drop(0, inplace=True)

    ag = find_buses(data)
    for b in ag:
        c.bus.loc[b] = c.bus.loc['Bus1']

    if 'Bus1' not in ag:
        c.bus.drop('Bus1', inplace=True)

    READ = False
    DIRECTIVE = 'param: NetDemand :='
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l.strip() == '':
            continue

        if l == 'param: NetDemand :=':
            READ = True
            continue

        if READ is True:
            b, t, v = l.split()
            c.load.loc[t, b] = float(v.replace(',', '.'))

    c.load = c.load.fillna(0)
    c.load.drop(0, inplace=True)
    c.load.index = range(0, len(c.load.index))

    # Make Bus1 slack
    c.bus.loc['Bus1', 'TYPE'] = 3.0
    #click.echo("3 printing c.gencost: " + str(c.gencost))

    READ = False
    DIRECTIVE = 'param: ProductionCostA0 ProductionCostA1 ProductionCostA2 :='
    for l in data.splitlines():
        if l.strip() == ';':
            READ = False

        if l.strip() == '':
            continue

        if l == DIRECTIVE:
            READ = True
            continue

        if READ is True:
            g, a0, a1, a2 = l.split()
            c.gencost.loc[g, "COST_2"] = float(a2.replace(',', '.'))
            c.gencost.loc[g, "COST_1"] = float(a1.replace(',', '.'))
            c.gencost.loc[g, "COST_0"] = float(a0.replace(',', '.'))
            c.gencost.loc[g, "N_COST"] = 3 # Does DK intend to keep NCOST instead of N_COST?
            c.gencost.loc[g, "NCOST"] = 3 # Added NCOST to 3 - Swathi

    #click.echo("printing c.gencost.loc: " + str(c.gencost.loc))
    #click.echo("printing c.gencost: " + str(c.gencost))
    #click.echo("printing c: " + str(c))
    return c
