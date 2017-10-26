#!/usr/bin/python2
import sys
import subprocess

def parse_results(count):
	wins_map = {}

	for i in range(0, count):
		results_map = {}
		output = subprocess.check_output('java -cp out MossSideWhist', shell=True)

		results = output.split('\n')[-4:-1]
		for line in results:
			split_line = line.split(':')
			agentname = split_line[0] if not split_line[0].startswith('RND') else "Random"
			agentscore = split_line[1]
			results_map[agentname] = int(agentscore)
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