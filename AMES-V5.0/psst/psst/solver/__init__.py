
from pyomo.environ import SolverFactory
import warnings
import os
import click
from .results import PSSTResults


PSST_WARNING = os.getenv('PSST_WARNING', 'ignore')


def solve_model(model, solver='glpk', solver_io=None, keepfiles=True, verbose=True, symbolic_solver_labels=True, is_mip=True, mipgap=0.01):
    #click.echo("solver  "+str(solver))
    if solver == 'xpress':
        solver = SolverFactory(solver, solver_io=solver_io, is_mip=is_mip)
    else:
        solver = SolverFactory(solver, solver_io=solver_io)
    #click.echo("In solver 1 "+str(solver_io))
    model.preprocess()
    if is_mip:
        solver.options['mipgap'] = mipgap
    solver.options['seconds'] = 10 # Maximum Time Limit
    #solver.options['solution'] = 'qwerty.txt'   #NA

    with warnings.catch_warnings():
        warnings.simplefilter(PSST_WARNING)
        resultsPSST = solver.solve(model, suffixes=['dual'], tee=verbose, keepfiles=keepfiles, symbolic_solver_labels=symbolic_solver_labels)
        #click.echo("solver msg 2 "+str(resultsPSST.solver))
        #click.echo("solver msg 3 "+str(resultsPSST.solver.status))
        #click.echo("solver msg 4 "+ str(resultsPSST.solver.termination_condition))
        #click.echo("solver msg " + str(resultsPSST))

    #click.echo("End")
    return model
