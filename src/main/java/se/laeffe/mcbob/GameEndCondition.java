package se.laeffe.mcbob;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.text.StrBuilder;

public class GameEndCondition {
	private int nrOfFlips = 0;
	private int winningScore = 0;
	private int maxScore = 0;
	private WeakReference<AbstractGame> weakGameRef;
	private int maxSeconds;

	public GameEndCondition(GameConfiguration cfg, AbstractGame game) {
		weakGameRef = new WeakReference<AbstractGame>(game);
		nrOfFlips = cfg.getInt("nrOfFlips", nrOfFlips);
		maxSeconds = cfg.getInt("maxSeconds", maxSeconds);
		winningScore = cfg.getInt("winningScore", winningScore);
		maxScore = cfg.getInt("maxScore", maxScore);
	}

	public boolean check() {
		BattleHandler bh = getGame().getBattleHandler();
		if(nrOfFlips > 0 && bh.getNrOfFlips() >= nrOfFlips) {
			getGame().endGame("no. of state flips achieved");
			return true;
		}
		
		if(winningScore > 0) {
			for(Entry<Team, AtomicInteger> s : bh.getScores().entrySet()) {
				if(s.getValue().get() >= winningScore) {
					getGame().endGame("winning score achieved");
					return true;
				}
			}
		}

		if(maxScore > 0) {
			int scoreSum = 0;
			for(AtomicInteger s : bh.getScores().values()) {
				scoreSum += s.get();
			}
			if(scoreSum >= maxScore) {
				getGame().endGame("max score achieved");
				return true;
			}
		}
		
		if(maxSeconds > 0) {
			if(bh.getSeconds() >= maxSeconds) {
				getGame().endGame("max seconds achieved");
				return true;
			}
		}
		return false;
	}
	
	public String getEndConditionSummary() {
		StringBuilder sb = new StringBuilder("This game ends when");
		if(nrOfFlips > 0) {
			sb.append(", the mode has flipped from building to battle: ").append(nrOfFlips).append(" times");
		}
		if(winningScore > 0) {
			sb.append(", a team reaches a score of: ").append(winningScore);
		}
		if(maxScore>0){
			sb.append(", the maximum score among the teams are: ").append(maxScore);
		}
		if(maxSeconds>0) {
			sb.append(", the play time reaches: ").append(maxSeconds).append(" seconds");
		}
		return sb.toString();
	}

	AbstractGame getGame() {
		return weakGameRef.get();
	}
}
