package se.laeffe.mcbob;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TeamHandler extends PlayerListener {
	ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<String, Team>();
	ConcurrentHashMap<Player, Team> player2team = new ConcurrentHashMap<Player, Team>();
	private Mcbob mcbob;

	public TeamHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
	}
	
	public void init() {
		createInitTeams();
	}

	private void createInitTeams() {
		createTeam("North", Material.DIAMOND_BLOCK, new Vector(-1, 0, 0));
		createTeam("South", Material.GOLD_BLOCK,    new Vector( 1, 0, 0));
	}

	private void createTeam(String name, Material material, Vector modifier) {
		Team team = new Team(name, createFlag(material));
		Area area = mcbob.getAreaHandler().createArea(modifier, team);
		team.setArea(area);
		addTeam(team);
	}

	private Flag createFlag(Material material) {
		Flag flag = new Flag(material);
		mcbob.getBattleHandler().addFlag(flag);
		return flag;
	}

	private void addTeam(Team team) {
		teams.put(team.getName().toLowerCase(), team);
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		removePlayerFromTeam(player);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
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
		player.teleport(team.getArea().getHome());
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		System.out.println("TeamHandler.onPlayerRespawn()");
		final Player player = event.getPlayer();
		final Area teamArea = getTeamArea(player);
		//Let's wait a second before TP the player home, else it might not work ;)
		mcbob.getServer().getScheduler().scheduleSyncDelayedTask(mcbob, new Runnable() {
			@Override
			public void run() {
				player.teleport(teamArea.getHome());
			}
		}, 20);
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
		player.teleport(team.getArea().getHome());
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
	
	public Collection<Team> getTeamsList() {
		return teams.values();
	}

	public void setChestContents(Chest chest, Team team) {
		Inventory inventory = chest.getInventory();
		inventory.clear();
		inventory.addItem(new ItemStack(Material.LOG, 64));
		inventory.addItem(new ItemStack(Material.COBBLESTONE, 64));
		inventory.addItem(new ItemStack(Material.SULPHUR, 64));
	}
	
}
