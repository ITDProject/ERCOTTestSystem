import json
import csv
import numpy as np
import matplotlib.pyplot as plt 
import networkx as nx
import math 

if __name__ == '__main__':
#	name,bus1,bus2,kV,length[miles],#parallel,r1[Ohms/mile],x1[Ohms/mile],b1[MVAR/mile],ampacity,capacity[MW]
	dlines = np.genfromtxt('RetainedLines.csv', dtype=str, skip_header=1, delimiter=',') # ['U',int,int,float,float,int,float,float,float,float,float], skip_header=1, delimiter=',')
#	bus,lon,lat,load,gen,diff
	dbuses = np.genfromtxt('RetainedBuses.csv', dtype=[int, float, float, float, float, float], skip_header=1, delimiter=',')

	lbl345 = {}
	lbl138 = {}
	e345 = set()
	e138 = set()
	n345 = set()
	n138 = set()
	graph = nx.Graph()
	for e in dlines:
		if '//' not in e[0]:
			n1 = int(e[1])
			n2 = int(e[2])
			graph.add_edge (n1, n2)
			if float(e[3]) > 200.0:
				n138.discard (n1)
				n138.discard (n2)
				n345.add (n1)
				n345.add (n2)
				lbl345[(n1, n2)] = e[0]
				e345.add ((n1, n2))
			else:
				lbl138[(n1, n2)] = e[0]
				n138.add (n1)
				n138.add (n2)
				e138.add ((n1, n2))

	xy = {}
	lblbus345 = {}
	lblbus138 = {}
	for b in dbuses:
		xy[b[0]] = [b[1], b[2]]
		if b[0] in n345:
			lblbus345[b[0]] = str(b[0]) + ':' + str(int(b[5]))
		else:
			lblbus138[b[0]] = str(b[0]) + ':' + str(int(b[5]))

	nx.draw_networkx_nodes (graph, xy, nodelist=list(n345), node_color='r', node_size=80, alpha=0.3)
#	nx.draw_networkx_nodes (graph, xy, nodelist=list(n138), node_color='b', node_size=20, alpha=0.3)
	nx.draw_networkx_edges (graph, xy, edgelist=list(e345), edge_color='r', width=2, alpha=0.8)
#	nx.draw_networkx_edges (graph, xy, edgelist=list(e138), edge_color='b', width=1, alpha=0.8)
	nx.draw_networkx_labels (graph, xy, lblbus345, font_size=8, font_color='g')
#	nx.draw_networkx_labels (graph, xy, lblbus138, font_size=8, font_color='g')
	nx.draw_networkx_edge_labels (graph, xy, edge_labels=lbl345, label_pos=0.5, font_color='m', font_size=6)
#	nx.draw_networkx_edge_labels (graph, xy, edge_labels=lbl138, label_pos=0.5, font_color='k', font_size=6)

	plt.title ('Graph of Retained EHV and HV Lines')
	plt.xlabel ('Longitude [deg]')
	plt.ylabel ('Latitude [deg N]')
	plt.grid(linestyle='dotted')
	plt.show()

