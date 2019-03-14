# -*- coding: utf-8 -*-

import os
import click
import pandas as pd

from .utils import read_unit_commitment, read_model
from .model import build_model

import numpy as np

np.seterr(all='raise')

SOLVER = os.getenv('PSST_SOLVER', 'cplex')
#click.echo("Printing PSST_SOLVER:"+SOLVER)
print("Printing PSST_SOLVER:"+SOLVER)

NL = 4

@click.group()
@click.version_option('0.1.0', '--version')
def cli():
    pass


@cli.command()
@click.option('--data', default=None, type=click.Path(), help='Path to model data')
@click.option('--output', default='./xfertoames.dat', type=click.Path(), help='Path to output file')
@click.option('--solver', default=SOLVER, help='Solver')
def scuc(data, output, solver):
    click.echo("Running SCUC using PSST TrailVersion")

    #click.echo("printing " + data + " : " + data.strip("'"))
    #click.echo("printing output: " + output + " solver: " + solver)
    click.echo("SCUC Data is being read")
    c = read_model(data.strip("'"))
    click.echo("SCUC Data is read")
    ##click.echo("printing c:" + c)
    config = {"segments":(NL+1), 'reserve_up_factor':0.0, 'reserve_down_factor':0.0}
    model = build_model(c,config=config)
    click.echo("Model is built")
    model.solve(solver=solver)
    click.echo("Model is solved by "+solver)
    click.echo("Model results ")
    click.echo(" : "+ str(model.results.lmp))
    
    uc = "temp.dat"
    with open(uc, 'w') as outfile:
        instance = model._model
        results = {}
        resultsPowerGen = {}
        for g in instance.Generators.value:
            for t in instance.TimePeriods:
                results[(g, t)] = instance.UnitOn[g, t]
                resultsPowerGen[(g, t)] = instance.PowerGenerated[g, t]

        for g in sorted(instance.Generators.value):
            outfile.write("%s\n" % str(g).ljust(8))
            for t in sorted(instance.TimePeriods):
                outfile.write("% 1d \n" % (int(results[(g, t)].value + 0.5)))

    uc_df = pd.DataFrame(read_unit_commitment(uc.strip("'")))
    c.gen_status = uc_df.astype(int)
    config = {"segments":(NL+1), 'reserve_up_factor':0.0, 'reserve_down_factor':0.0}
    model = build_model(c,config=config)
    model.solve(solver=solver)
    click.echo("Model is solved by "+solver)
    click.echo("Model results ")
    click.echo(" : "+ str(model.results.lmp))
    with open('./DAMLMP.dat', 'w') as outfile:
        outfile.write("DAMLMP\n")        			
        for h, r in model.results.lmp.iterrows():
            bn = 1
            for _, lmp in r.iteritems():
                if lmp is None:
                    lmp = 0
                outfile.write(str(bn) + ' : ' + str(h + 1) +' : ' + str(lmp) +"\n")
                bn = bn + 1
        outfile.write("END_LMP\n")

    with open(output, 'w') as outfile:
        instance = model._model
        results = {}
        resultsPowerGen = {}
        for g in instance.Generators.value:
            for t in instance.TimePeriods:
                results[(g, t)] = instance.UnitOn[g, t]
                resultsPowerGen[(g, t)] = instance.PowerGenerated[g, t]

        for g in sorted(instance.Generators.value):
            outfile.write("%s\n" % str(g).ljust(8))
            for t in sorted(instance.TimePeriods):
                outfile.write("% 1d %6.2f %6.2f %6.2f\n" % (int(results[(g, t)].value + 0.5), resultsPowerGen[(g, t)].value, 0.0, 0.0)) # not sure why DK added 0.0, 0.0

    with open('./Results.dat', 'w') as outfile:
        instance = model._model
        SlackVariablePower = {}
        for b in instance.Buses.value:
            for t in instance.TimePeriods:
                SlackVariablePower[(b, t)] = instance.LoadGenerateMismatch[b, t]

        for b in sorted(instance.Buses.value):
            outfile.write("%s\n" % str(b).ljust(8))
            for t in sorted(instance.TimePeriods):
                outfile.write(" %6.2f \n" % (SlackVariablePower[(b, t)].value)) 



@cli.command()
@click.option('--uc', default=None, type=click.Path(), help='Path to unit commitment file')
@click.option('--data', default=None, type=click.Path(), help='Path to model data')
@click.option('--output', default='./output.dat', type=click.Path(), help='Path to output file')
@click.option('--solver', default=SOLVER, help='Solver')
def sced(uc, data, output, solver):

    click.echo("Running SCED using PSST")

    #click.echo("printing " + data + ":" + data.strip("'"))
    #click.echo("printing output:" + output + " solver:" + solver + " uc:" + uc)
    # TODO : Fixme
    uc_df = pd.DataFrame(read_unit_commitment(uc.strip("'")))

    #click.echo("SCED Data is being read") 
    c = read_model(data.strip("'"))
    #click.echo("SCED Data is read")
    c.gen_status = uc_df.astype(int)

    config = {"segments":(NL+1), 'reserve_up_factor':0.0, 'reserve_down_factor':0.0}
    model = build_model(c,config=config)
    model.solve(solver=solver)
    #click.echo("SCED Model is solved by "+solver)
    Interval = 1

    with open('./ResultsSCED.dat', 'w') as outfile:
        instance = model._model
        SlackVariablePower = {}
        for b in instance.Buses.value:
            for t in instance.TimePeriods:
                SlackVariablePower[(b, t)] = instance.LoadGenerateMismatch[b, t]

        for b in sorted(instance.Buses.value):
            outfile.write("%s\n" % str(b).ljust(8))
            for t in sorted(instance.TimePeriods):
                outfile.write(" %6.2f \n" % (SlackVariablePower[(b, t)].value)) 
    with open(output.strip("'"), 'w') as f:
        f.write("LMP\n")
        #click.echo("..." + str(model.results.lmp))  
        for h, r in model.results.lmp.iterrows():
            bn = 1
            for _, lmp in r.iteritems():
                if lmp is None:
                    lmp = 0
                f.write(str(bn) + ' : ' + str(h + 1) +' : ' + str(lmp) +"\n")
                bn = bn + 1
        f.write("END_LMP\n")

        f.write("GenCoResults\n")
        instance = model._model
        #Interval = instance.Interval
        #click.echo("Interval: " + str(Interval))
        #for i in instance.Interval:
         #   f.write("Interval: {}\n".format(str(i + 1)))
        for g in instance.Generators.value:
            f.write("%s\n" % str(g).ljust(8))
            for t in instance.TimePeriods:
                f.write("Hour: {}\n".format(str(t + 1)))
                f.write("\tPowerGenerated: {}\n".format(instance.PowerGenerated[g, t].value))
                f.write("\tProductionCost: {}\n".format(instance.ProductionCost[g, t].value))
                f.write("\tStartupCost: {}\n".format(instance.StartupCost[g, t].value))
                f.write("\tShutdownCost: {}\n".format(instance.ShutdownCost[g, t].value))
        f.write("END_GenCoResults\n")
        f.write("VOLTAGE_ANGLES\n")
        for bus in sorted(instance.Buses):
            for t in instance.TimePeriods:
                f.write('{} {} : {}\n'.format(str(bus), str(t + 1), str(instance.Angle[bus, t].value)))
        f.write("END_VOLTAGE_ANGLES\n")
        # Write out the Daily LMP
        f.write("DAILY_BRANCH_LMP\n")
        f.write("END_DAILY_BRANCH_LMP\n")
        # Write out the Daily Price Sensitive Demand
        f.write("DAILY_PRICE_SENSITIVE_DEMAND\n")
        f.write("END_DAILY_PRICE_SENSITIVE_DEMAND\n")
        # Write out which hour has a solution
        f.write("HAS_SOLUTION\n")
        h = 0
        max_hour = 24  # FIXME: Hard-coded number of hours.
        while h < max_hour:
            f.write("1\t")  # FIXME: Hard-coded every hour has a solution.
            h += 1
        f.write("\nEND_HAS_SOLUTION\n")


if __name__ == "__main__":
    cli()
