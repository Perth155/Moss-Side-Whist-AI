#!/usr/bin/python2
import sys
import subprocess

# Any agent whose name isn't in this list will be parsed as "Others"
KNOWN_NAMES = ["My Agent", "ALL H1GH by abraram", "GR33D by abraram", "Pr0Bz by abraram", "G0PT by abraram", "G33D_EX by abraram"]

def parse_results(count):
	wins_map = {}

	for i in range(0, count):
		results_map = {}
		output = subprocess.check_output('java -jar Moss-Side-Whist-AI.jar', shell=True)
		results = output.split('\n')[-4:-1]
		for line in results:
			split_line = line.split(':')
			agentname = split_line[0] if split_line[0] in KNOWN_NAMES else "Others"
			agentscore = split_line[1]
			results_map[agentname] = agentscore
		best_score = None
		best_agent = None
		for agent in results_map:
			score = results_map[agent]
			if score > best_score:
				best_score = score
				best_agent = agent
		print "({}/{}) Winner: {}".format(
			str(i+1).zfill(len(str(count))),
			count,
			best_agent
		)
		wins_map[best_agent] = wins_map.get(best_agent, 0) + 1

	for name in wins_map:
		print "{} won {} / {} games ({}%)".format(
			name,
			wins_map[name],
			count,
			float(wins_map[name])/float(count) * 100
		)


if __name__ == "__main__":
	try:
		count = int(sys.argv[1])
	except IndexError:
		count = 10
	parse_results(count)
