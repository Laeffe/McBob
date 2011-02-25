package se.laeffe.mcbob;

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;

public class BattleHandler extends BlockListener {

	private Mcbob mcbob;
	private long tickCount = 0;
	private long tickFlip = 2000;
	private boolean inBattle = false;
	private LinkedHashSet<Flag> flags = new LinkedHashSet<Flag>();
	private ConcurrentHashMap<Player, Flag> player2flag = new ConcurrentHashMap<Player, Flag>();
	
	public BattleHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
	}

	public boolean isTeamAreaRestrictionOn() {
		return !inBattle;
	}

	public boolean isBuildingAllowed() {
		return !inBattle;
	}

	private void serverTick() {
		tickCount+=100;
		if(tickCount>tickFlip) {
			tickCount = 0;
			setBattleState(!inBattle);
		}
	}
	
	public Runnable getTask() {
		return new Runnable() {
			@Override
			public void run() {
				serverTick();
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
		else
			mcbob.notifyPlayers("The war time is over.");
		
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
	public void onBlockRightClick(BlockRightClickEvent event) {
		Player player = event.getPlayer();
		Team team = mcbob.getTeamHandler().getPlayersTeam(player);
		for(Flag f : flags) {
			if(!f.isTaken() &&  f.getBlock().getLocation().equals(event.getBlock().getLocation())) {
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
	}

	private void returnFlag(Player player, Flag careingFlag) {
		returnFlag(careingFlag);
		player2flag.remove(player);
	}

	private void returnFlag(Flag careingFlag) {
		careingFlag.setTaken(false);
		careingFlag.getBlock().setTypeId(careingFlag.getType());
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
}
