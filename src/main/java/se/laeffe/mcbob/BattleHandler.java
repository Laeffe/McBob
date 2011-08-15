package se.laeffe.mcbob;

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

public class BattleHandler extends BlockListener {

	private static final long TPS = 20;
	private long startToNotifySeconds = 10;

	private long seconds = 0;
	
	private Mcbob mcbob;
	private long tickFlipBattle = 2000;
	private long tickFlipBuild = 2000;
	private long lastFlip = 0;
	private boolean flipByTick = true;
	
	private boolean inBattle = false;
	private LinkedHashSet<Flag> flags = new LinkedHashSet<Flag>();
	private ConcurrentHashMap<Player, Flag> player2flag = new ConcurrentHashMap<Player, Flag>();
	private long battleTime = 18000;
	private long buildTime  = 6000;
	
	public BattleHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
	}

	public boolean isTeamAreaRestrictionOn() {
		return !inBattle;
	}

	public boolean isBuildingAllowed() {
		return !inBattle;
	}
	
	private void tickPerSecond() {
		seconds++;
		serverTick();
		updateFlagCarriers();
	}

	private void serverTick() {
		boolean toggleBattle = false;
		if(flipByTick) {
			long tickFlip = inBattle?tickFlipBattle:tickFlipBuild;
			long diff = seconds-lastFlip;
			System.out.println("BattleHandler.serverTick(), "+diff);
			if(diff>=tickFlip) {
				toggleBattle = true;
			} else if(tickFlip-diff < startToNotifySeconds) {
				long seconds = (tickFlip-diff);
				mcbob.notifyPlayers("There will be "+(inBattle?"peace":"war")+" in "+seconds+"s.");
			}
		} else {
			long time = mcbob.getWorld().getTime();
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
			long diff = seconds-flag.getTakenTime();
			System.out.println("BattleHandler.updateFlagCarriers(), "+player.getDisplayName()+" flag "+flag.getTakenTime()+" diff "+diff);
			if(diff % 10 == 0) {
				mcbob.notifyPlayers(player.getDisplayName()+" is still holding "+flag.getTeam().getName()+"'s flag, punished he will be.");
				mcbob.getWorld().strikeLightning(player.getLocation());
			}
		}
	}

	public Runnable get20TickTask() {
		return new Runnable() {
			@Override
			public void run() {
				tickPerSecond();
			}
		};
	}

	public boolean isBattle() {
		return inBattle;
	}

	public void setBattleState(boolean state) {
		inBattle = state;
		if(inBattle)
			mcbob.notifyPlayers("We are now in BATTLE!!!!");
		else {
			mcbob.notifyPlayers("The war time is over, returned flags has become.");
			for(Player e : player2flag.keySet()) {
				returnFlag(e);
			}
		}
		
		notifyScore();
	}

	private void notifyScore() {
		StringBuilder sb = new StringBuilder("The current score is, ");
		for(Entry<String, Team> t : mcbob.getTeamHandler().getTeams()) {
			sb.append(t.getKey()).append(":").append(t.getValue().getScore()).append(" ");
		}
		
		mcbob.notifyPlayers(sb.toString());
	}

	public void addFlag(Flag flag) {
		flags.add(flag);
	}
	
	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		for(Flag f : flags) {
			if(!f.getBlock().getLocation().equals(event.getBlock().getLocation())) {
				continue;
			}
			
			Player player = event.getPlayer();
			Team team = mcbob.getTeamHandler().getPlayersTeam(player);
			if(!f.isTaken()) {
				if(f.getTeam() == team) {
					player.sendMessage("Touched your flag");
					Flag careingFlag = player2flag.get(player);
					if(careingFlag != null) {
						returnFlag(player, careingFlag);
						mcbob.notifyPlayers(player.getName()+" has harbored the flag.");
						scored(player, team);
					}
				} else {
					player.sendMessage("Touched other flag");
					takeFlag(player, f);
					mcbob.notifyPlayers(player.getName()+" has captured the flag.");
				}
			}
		}
	}

	private void scored(Player player, Team team) {
		mcbob.notifyPlayers(team.getName()+" scored!");
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
			mcbob.notifyPlayers(flag.getTeam().getName()+"'s flag returned since "+player.getDisplayName()+" left the team.");
	}

	public void playerDied(Player player) {
		Flag flag = returnFlag(player);
		if(flag != null)
			mcbob.notifyPlayers(flag.getTeam().getName()+"'s flag returned since "+player.getDisplayName()+" DIED!!");
	}

	public void setPeriod(long buildPeriod, long battlePeriod) {
	    tickFlipBuild = buildPeriod;
	    tickFlipBattle = battlePeriod;
	    flipByTick = true;
	}

	public void setTime(long buildTime, long battleTime) {
		this.buildTime = buildTime; 
		this.battleTime = battleTime;
	    flipByTick = false;
	}
}
