package se.laeffe.mcbob;

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.util.config.Configuration;

public class BattleHandler {
	private GameInterface game;
	private int battlePeriod         = 300; //15 min
	private int buildPeriod          = 900;
	private int lastFlip             = 0;
	private boolean flipByTick       = true;
	private int startToNotifySeconds = 10;
	private int seconds              = 0;
	
	private boolean inBattle = false;
	private LinkedHashSet<Flag> flags = new LinkedHashSet<Flag>();
	private ConcurrentHashMap<Player, Flag> player2flag = new ConcurrentHashMap<Player, Flag>();
	private int battleTime = 18000;
	private int buildTime  = 6000;
	private int punishFlagCarrierAfter = 30;
	private int notifyOfFlipEvry = 30;
	
	public BattleHandler(GameInterface game) {
		this.game        = game;
		Configuration cfg = game.getConfiguration();
		
		battlePeriod = cfg.getInt("battlePeriod", battlePeriod);
		buildPeriod  = cfg.getInt("buildPeriod", buildPeriod);
		
		startToNotifySeconds   = cfg.getInt("notificationSeconds",    startToNotifySeconds);
		notifyOfFlipEvry       = cfg.getInt("notifyEvery",            notifyOfFlipEvry);
		punishFlagCarrierAfter = cfg.getInt("punishflagCarrierAfter", punishFlagCarrierAfter);
	}

	public boolean isTeamAreaRestrictionOn() {
		return !inBattle;
	}

	public boolean isBuildingAllowed() {
		return !inBattle;
	}
	
	public void tickPerSecond() {
		seconds++;
		serverTick();
		updateFlagCarriers();
	}

	private void serverTick() {
		
		boolean toggleBattle = false;
		if(flipByTick) {
			long tickFlip = inBattle?battlePeriod:buildPeriod;
			long diff = seconds-lastFlip;
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
			lastFlip = seconds;
		}
	}

	private void updateFlagCarriers() {
		for(Entry<Player, Flag> e : player2flag.entrySet()) {
			Player player = e.getKey();
			Flag flag = e.getValue();
			int diff = seconds-flag.getTakenTime();
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
		StringBuilder sb = new StringBuilder("The current score is, ");
		for(Team t : game.getTeamHandler().getTeams()) {
			sb.append(t.getName()).append(":").append(t.getScore()).append(" ");
		}
		
		game.notifyPlayers(sb.toString());
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
			Team team = game.getTeamHandler().getPlayersTeam(player);
			if(!f.isTaken()) {
				if(f.getTeam() == team) {
					player.sendMessage("Touched your flag");
					Flag careingFlag = player2flag.get(player);
					if(careingFlag != null) {
						returnFlag(player, careingFlag);
						game.notifyPlayers(player.getName()+" has harbored the flag.");
						scored(player, team);
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
		game.notifyPlayers(team.getName()+" scored!");
		team.addScore(1);
	}

	private void takeFlag(Player player, Flag f) {
		takeFlag(f);
		player2flag.put(player, f);
	}

	private void takeFlag(Flag f) {
		f.setTaken(true);
		f.getBlock().setTypeId(0);
		f.setTakenTime(seconds);
	}

	private void returnFlag(Player player, Flag careingFlag) {
		returnFlag(careingFlag);
		player2flag.remove(player);
	}

	private void returnFlag(Flag careingFlag) {
		careingFlag.setTaken(false);
		careingFlag.getBlock().setType(careingFlag.getType());
	}

	private Flag returnFlag(Player player) {
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
}
