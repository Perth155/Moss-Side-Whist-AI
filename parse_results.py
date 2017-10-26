#!/usr/bin/python2
import sys
import subprocess

WINS_MAP = {}
NUM_GAMES = 0

def parse_results(count):
	global NUM_GAMES
	for i in range(0, count):
		best_agent = ""
		best_score = 0 - sys.maxint

		output = subprocess.check_output('java -jar Moss-Side-Whist-AI.jar', shell=True)
		results = output.split('\n')[-4:-1]
		for line in results:
			split_line = line.split(':')
			agentname = split_line[0] if not split_line[0].startswith('RND') else "Random"
			agentscore = split_line[1]
			if agentscore > best_score:
				best_agent = agentname
				best_score = agentscore

		NUM_GAMES += 1
		print "({}/{}) Winner: {}".format(
			str(NUM_GAMES).zfill(len(str(count))),
			count,
			best_agent
		)
		WINS_MAP[best_agent] = WINS_MAP.get(best_agent, 0) + 1

def print_stats():
	for name in WINS_MAP:
		print "{} won {} / {} games ({}%)".format(
			name,
			WINS_MAP[name],
			NUM_GAMES,
			float(WINS_MAP[name])/float(NUM_GAMES) * 100
		)

if __name__ == "__main__":
	try:
		count = int(sys.argv[1])
	except IndexError:
		count = 10
	try:
		parse_results(count)
		print_stats()
	except KeyboardInterrupt:
		print_stats()
