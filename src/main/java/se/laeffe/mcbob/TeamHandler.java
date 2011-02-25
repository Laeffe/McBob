package se.laeffe.mcbob;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class TeamHandler extends PlayerListener {
	ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<String, Team>();
	ConcurrentHashMap<Player, Team> player2team = new ConcurrentHashMap<Player, Team>();
	private Mcbob mcbob;

	public TeamHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
		createInitTeams();
	}

	private void createInitTeams() {
		createTeam("North", 57, -1);
		createTeam("South", 41, 1);
	}

	private void createTeam(String name, int type, int cord) {
		Team team = new Team(name, createFlag(type));
		Area area = mcbob.getAreaHandler().createArea(cord, team.getFlag());
		team.setArea(area);
		addTeam(team);
	}

	private Flag createFlag(int type) {
		Flag flag = new Flag(type);
		mcbob.getBattleHandler().addFlag(flag);
		return flag;
	}

	private void addTeam(Team team) {
		teams.put(team.getName().toLowerCase(), team);
	}

	@Override
	public void onPlayerQuit(PlayerEvent event) {
		Player player = event.getPlayer();
		removePlayerFromTeam(player);
	}
	
	@Override
	public void onPlayerJoin(PlayerEvent event) {
		Player player = event.getPlayer();
		Team team = null;
		int teamSize = Integer.MAX_VALUE;
		for(Entry<String, Team> t : teams.entrySet()) {
			int size = t.getValue().size();
			if(size <= teamSize) {
				team = t.getValue();
				teamSize = size;
			}
		}
		addPlayer2Team(player, team);
		mcbob.notifyPlayers(player.getDisplayName()+" joined team "+team.getName());
		player.teleportTo(team.getArea().getHome());
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Area teamArea = getTeamArea(player);
		player.teleportTo(teamArea.getHome());
	}
	
	private void addPlayer2Team(Player player, Team team) {
		team.add(player);
		player2team.put(player, team);
	}

	public Area getTeamArea(Player player) {
		Team team = player2team.get(player);
		return team.getArea();
	}

	public Team getTeam(String teamName) {
		return teams.get(teamName.toLowerCase());
	}

	public void changeTeam(Player player, Team team) {
		Team oldTeam = removePlayerFromTeam(player);
		addPlayer2Team(player, team);
		mcbob.notifyPlayers(player.getDisplayName()+" changed team from "+oldTeam+" to "+team);
	}

	private Team removePlayerFromTeam(Player player) {
		Team oldTeam = player2team.remove(player);
		oldTeam.remove(player);
		mcbob.getBattleHandler().playerQuitTeam(player);
		return oldTeam;
	}

	public Team getPlayersTeam(Player player) {
		return player2team.get(player);
	}

	public Set<Entry<String,Team>> getTeams() {
		return teams.entrySet();
	}
	
}
