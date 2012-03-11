package se.laeffe.mcbob;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import se.laeffe.mcbob.observer.AbstractObservable;

public class Scores extends AbstractObservable {
	private ConcurrentHashMap<Team, AtomicInteger>	scores	= new ConcurrentHashMap<Team, AtomicInteger>();

	public void init(Collection<Team> teams) {
		for(Team t : teams) {
			scores.put(t, new AtomicInteger(0));
		}
		notifyObservers();
	}

	/**
	 * Will return the score for a team, or -1 if the team is non existing in
	 * the score table.
	 * 
	 * @param t
	 * @return
	 */
	public int get(Team t) {
		AtomicInteger atomicInteger = scores.get(t);
		if(atomicInteger != null) {
			return atomicInteger.get();
		}
		return -1;
	}

	/**
	 * Will increment the score for a team, and return the new score, or -1 if
	 * the team is missing.
	 * 
	 * @param team
	 * @return
	 */
	public int incrementScore(Team team) {
		AtomicInteger atomicInteger = scores.get(team);
		if(atomicInteger != null) {
			int i = atomicInteger.incrementAndGet();
			notifyObservers();
			return i;
		}
		return -1;
	}

	public Map<Team, Integer> getScoreMap() {
		HashMap<Team, Integer> map = new HashMap<Team, Integer>();
		for(Entry<Team, AtomicInteger> entry : scores.entrySet()) {
			map.put(entry.getKey(), entry.getValue().get());
		}
		return map;
	}
}
