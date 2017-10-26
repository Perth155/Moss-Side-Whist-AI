#!/usr/bin/python2
import sys
import subprocess

TARGET_TIME = 200

def check_time(output):
	worst_time = 0
	for line in output.split('\n'):
		if line.startswith('Execution Time'):
			time = float(line.split(':')[1])
			if time > worst_time:
				worst_time = time

	if worst_time > TARGET_TIME:
		raise Exception("{} target exceeded. Turn took {}".format(TARGET_TIME, worst_time))

def parse_results(count):
	wins_map = {}

	for i in range(0, count):
		results_map = {}
		output = subprocess.check_output('java -cp out MossSideWhist', shell=True)
		try:
			check_time(output)
		except Exception as e:
			print(repr(e))
			print_stats(wins_map, i)
			return

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

	print_stats(wins_map, count)

def print_stats(wins_map, count):
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