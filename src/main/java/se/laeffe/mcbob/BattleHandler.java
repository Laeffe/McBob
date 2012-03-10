package se.laeffe.mcbob;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;

public class BattleHandler {
	private AbstractGame game;
	private long battlePeriod         = 300; //15 min
	private long buildPeriod          = 900;
	private long lastFlip             = 0;
	private boolean flipByTick       = true;
	private long startToNotifySeconds = 10;
	private AtomicInteger nrOfFlips  = new AtomicInteger(0);
	
	private boolean inBattle = false;
	private LinkedHashSet<Flag> flags = new LinkedHashSet<Flag>();
	private ConcurrentHashMap<Player, Flag> player2flag = new ConcurrentHashMap<Player, Flag>();
	private Scores scores               = new Scores();

	private long battleTime             = 18000;
	private long buildTime              = 6000;
	private long firstBuildTime         = 12000;
	private long punishFlagCarrierAfter = 30;
	private long notifyOfFlipEvry       = 30;

	private ConcurrentHashMap<Player,Location> playersLastDeathLocation = new ConcurrentHashMap<Player, Location>();

	private GameEndCondition endCondition;
	
	public BattleHandler(AbstractGame game) {
		this.game        = game;
		GameConfiguration cfg = game.getConfiguration();
		
		battlePeriod    = cfg.getLong("battlePeriod",   battlePeriod);
		buildPeriod     = cfg.getLong("buildPeriod",    buildPeriod);
		firstBuildTime  = cfg.getLong("firstBuildTime", firstBuildTime);
		
		startToNotifySeconds   = cfg.getLong("notificationSeconds",    startToNotifySeconds);
		notifyOfFlipEvry       = cfg.getLong("notifyEvery",            notifyOfFlipEvry);
		punishFlagCarrierAfter = cfg.getLong("punishflagCarrierAfter", punishFlagCarrierAfter);
		
		endCondition = new GameEndCondition(cfg, game);
	}

	public void init() {
		scores.init(game.getTeamHandler().getTeams());
	}

	public boolean isTeamAreaRestrictionOn() {
		return !inBattle;
	}

	public boolean isBuildingAllowed() {
		return !inBattle;
	}
	
	public void tickPerSecond() {
		serverTick();
		updateFlagCarriers();
		checkEndConditions();
	}

	private void serverTick() {
		
		boolean toggleBattle = false;
		if(flipByTick) {
			long tickFlip = getNextFlipTime();
			long diff = game.getSeconds()-lastFlip;
			if(diff % 5 == 0)
				System.out.println("BattleHandler.serverTick(), "+diff);
			if(diff>=tickFlip) {
				toggleBattle = true;
			} else {
				long secondsLeft = tickFlip-diff;
				if(secondsLeft < startToNotifySeconds || secondsLeft % notifyOfFlipEvry == 0) {
					game.notifyPlayers("There will be "+(inBattle?"peace":"war")+" in "+secondsLeft+"s.");
				}
			}
		} else {
			long time = game.getWorld().getTime();
			System.out.println("BattleHandler.serverTick(), "+time);
			long timeFlip = inBattle?buildTime:battleTime;
			if(timeFlip>=time) {
				toggleBattle = true;
			}
		}
		
		if(toggleBattle) {
			setBattleState(!inBattle);
			lastFlip = game.getSeconds();
			nrOfFlips.incrementAndGet();
		}
	}

	private long getNextFlipTime() {
		if(nrOfFlips.get()>0)
		{
			return inBattle?battlePeriod:buildPeriod;
		}
		return firstBuildTime;
	}

	private void updateFlagCarriers() {
		for(Entry<Player, Flag> e : player2flag.entrySet()) {
			Player player = e.getKey();
			Flag flag = e.getValue();
			long diff = game.getSeconds()-flag.getTakenTime();
			System.out.println("BattleHandler.updateFlagCarriers(), "+player.getDisplayName()+" flag "+flag.getTakenTime()+" diff "+diff);
			if(diff % punishFlagCarrierAfter == 0) {
				game.notifyPlayers(player.getDisplayName()+" is still holding "+flag.getTeam().getName()+"'s flag, punished he will be.");
				game.getWorld().strikeLightning(player.getLocation());
			}
		}
	}

	public boolean isBattle() {
		return inBattle;
	}

	public void setBattleState(boolean state) {
		inBattle = state;
		if(inBattle)
			game.notifyPlayers("We are now in BATTLE!!!!");
		else {
			game.notifyPlayers("The war time is over, returned flags has become.");
			for(Player e : player2flag.keySet()) {
				returnFlag(e);
			}
		}
		
		notifyScore();
	}

	private void notifyScore() {
		String scoreSummary = getScoreSummary();
		game.notifyPlayers("The current score is, "+scoreSummary);
	}

	public String getScoreSummary() {
		StringBuilder sb = new StringBuilder();
		for(Team t : game.getTeamHandler().getTeams()) {
			sb.append(t.getName()).append(":").append(scores.get(t)).append(" ");
		}
		return sb.toString();
	}

	public void addFlag(Flag flag) {
		flags.add(flag);
	}
	
	public void onBlockDamage(BlockDamageEvent event) {
		for(Flag f : flags) {
			if(!f.getBlock().getLocation().equals(event.getBlock().getLocation())) {
				continue;
			}
			
			Player player = event.getPlayer();
			Team team = game.getTeamHandler().getTeam(player);
			if(!f.isTaken()) {
				if(f.getTeam() == team) {
					player.sendMessage("Touched your flag");
					Flag careingFlag = player2flag.get(player);
					if(careingFlag != null) {
						returnFlag(player, careingFlag);
						game.notifyPlayers(player.getName()+" has harbored the flag.");
						scored(player, team);
					}
					else
					{
						player.sendMessage(getScoreSummary());	
					}
				} else {
					player.sendMessage("Touched other flag");
					takeFlag(player, f);
					game.notifyPlayers(player.getName()+" has captured the flag.");
				}
			}
		}
	}

	private void scored(Player player, Team team) {
//		game.notifyPlayers(team.getName()+" scored!");
		scores.incrementScore(team);
		if(checkEndConditions())
			return;
		notifyScore();
	}

	private boolean checkEndConditions() {
		return endCondition.check();
	}

	private void takeFlag(Player player, Flag f) {
		takeFlag(f);
		player2flag.put(player, f);
	}

	private void takeFlag(Flag f) {
		f.setTaken(true);
		f.getBlock().setTypeId(0);
		f.setTakenTime(game.getSeconds());
	}

	private void returnFlag(Player player, Flag careingFlag) {
		returnFlag(careingFlag);
		player2flag.remove(player);
	}

	private void returnFlag(Flag careingFlag) {
		careingFlag.setTaken(false);
		careingFlag.getBlock().setType(careingFlag.getType());
	}

	public Flag returnFlag(Player player) {
		Flag flag = player2flag.remove(player);
		if(flag != null)
			returnFlag(flag);
		return flag;
	}
	
	public void playerQuitTeam(Player player) {
		Flag flag = returnFlag(player);
		if(flag != null)
			game.notifyPlayers(flag.getTeam().getName()+"'s flag returned since "+player.getDisplayName()+" left the team.");
	}

	public void playerDied(Player player) {
		Flag flag = returnFlag(player);
		if(flag != null)
			game.notifyPlayers(flag.getTeam().getName()+"'s flag returned since "+player.getDisplayName()+" DIED!!");
		playersLastDeathLocation.put(player, player.getLocation());
		game.log("playerDied, lastDeathLocation: ", player.getLocation());
	}

	public void setPeriod(int buildPeriod, int battlePeriod) {
	    this.buildPeriod = buildPeriod;
	    this.battlePeriod = battlePeriod;
	    this.flipByTick = true;
	}

	public void setTime(int buildTime, int battleTime) {
		this.buildTime = buildTime; 
		this.battleTime = battleTime;
	    flipByTick = false;
	}
	
	public int getNrOfFlips() {
		return nrOfFlips.get();
	}
	
	public Map<Team, Integer> getScoresAsMap() {
		return scores.getScoreMap();
	}
	
	public Scores getScores() {
		return scores;
	}
	
	public Team getWinners() {
		Team winners = null;
		int winningScore = Integer.MIN_VALUE;
		for(Entry<Team, Integer> s : getScoresAsMap().entrySet()) {
			int score = s.getValue();
			if(score > winningScore) {
				winners = s.getKey();
				winningScore = score;
			} else if(score == winningScore) {
				winners = null;
			}
		}
		return winners;
	}
	
	public GameEndCondition getEndCondition() {
		return endCondition;
	}

	public Location getLastDeathLocation(Player player) {
		return playersLastDeathLocation.get(player);
	}
}
