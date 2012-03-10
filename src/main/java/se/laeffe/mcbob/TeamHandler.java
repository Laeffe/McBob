package se.laeffe.mcbob;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class TeamHandler {
	ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<String, Team>();
	ConcurrentHashMap<Player, Team> player2team = new ConcurrentHashMap<Player, Team>();
	ConcurrentHashMap<Player, Long> playerRespawnTime = new ConcurrentHashMap<Player, Long>();
	
	private AbstractGame game;
	private long respawnTime = 30;

	public TeamHandler(AbstractGame game) {
		this.game = game;
		game.getConfiguration().getLong("spawn.respawntime", respawnTime);
	}
	
	public void init() {
		createInitTeams();
	}

	private void createInitTeams() {
		createTeam("North", Material.DIAMOND_BLOCK, new Vector(-1, 0, 0));
		createTeam("South", Material.GOLD_BLOCK,    new Vector( 1, 0, 0));
	}

	private void createTeam(String name, Material material, Vector modifier) {
		Team team = new Team(name, createFlag(material), modifier);
		Location teamHome = game.getAreaHandler().createTeamBase(team);
		team.setHome(teamHome);
		addTeam(team);
	}

	private Flag createFlag(Material material) {
		Flag flag = new Flag(material);
		game.getBattleHandler().addFlag(flag);
		return flag;
	}

	private void addTeam(Team team) {
		teams.put(team.getName().toLowerCase(), team);
	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		removePlayerFromTeam(player);
		playerRespawnTime.remove(player);
	}
	
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
		game.notifyPlayers(player.getDisplayName()+" joined team "+team.getName());
		player.teleport(team.getHome());
		
		for(Player playerInRespawn : playerRespawnTime.keySet()) {
			player.hidePlayer(playerInRespawn);
		}
	}

	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		Location lastDeathLocation = game.getBattleHandler().getLastDeathLocation(player);
		if(lastDeathLocation != null) {
			game.log("onPlayerRespawn, lastDeathLocation: ", lastDeathLocation);
			event.setRespawnLocation(lastDeathLocation);
			game.hidePlayer(player);
			playerRespawnTime.put(player, game.getSeconds()+respawnTime);
			player.sendMessage("You will respawn for realz after "+respawnTime+" seconds");
		}
		else
		{
			game.log("Player does not have a lastDeathLocation");
		}
//		final Team team = getTeam(player);
//		event.setRespawnLocation(team.getHome());
		//Let's wait a second before TP the player home, else it might not work ;)
//		game.getServer().getScheduler().scheduleSyncDelayedTask(game.getMcbob(), new Runnable() {
//			@Override
//			public void run() {
//				player.teleport(team.getHome());
//			}
//		}, 20);
	}
	
	private void doRealRespawn(Player player) {
		if(playerRespawnTime.remove(player) == null)
			game.log("Player respawned.. but was not in respawn timer map thingy");
		Team team = getTeam(player);
		player.teleport(team.getHome());
		game.showPlayer(player);
		player.sendMessage("You have now respawned.");
	}
	
	private void addPlayer2Team(Player player, Team team) {
		team.add(player);
		player2team.put(player, team);
	}

	public Team getTeam(String teamName) {
		return teams.get(teamName.toLowerCase());
	}
	
	public Team getTeam(Player player) {
		return player2team.get(player);
	}

	public void changeTeam(Player player, Team team) {
		playerRespawnTime.remove(player);
		Team oldTeam = removePlayerFromTeam(player);
		addPlayer2Team(player, team);
		game.notifyPlayers(player.getDisplayName()+" changed team from "+oldTeam+" to "+team);
		player.teleport(team.getHome());
	}

	private Team removePlayerFromTeam(Player player) {
		Team oldTeam = player2team.remove(player);
		oldTeam.remove(player);
		game.getBattleHandler().playerQuitTeam(player);
		return oldTeam;
	}
	
	public Set<Player> getPlayers() {
		return player2team.keySet();
	}

	public Collection<Team> getTeams() {
		return teams.values();
	}

	public void setChestContents(Chest chest, Team team) {
		Inventory inventory = chest.getInventory();
		inventory.clear();
		
		GameConfiguration cfg = game.getConfiguration();
		Map<String, Object> allTeams = cfg.getMap("teamChest.all", false);
		addToInventroy(inventory, allTeams, team);

		Map<String, Object> teamItems = cfg.getMap("teamChest.team."+team.getName(), false);
		addToInventroy(inventory, teamItems, team);
	}

	private void addToInventroy(Inventory inventory, Map<String, Object> m, Team team) {
		if(m == null)
			return;
		
		for(Entry<String, Object> e : m.entrySet()) {
			String materialString = e.getKey();
			Material material = Material.matchMaterial(materialString);
			if(material == null) {
				System.out.println("TeamHandler.addToInventroy(), Material not matched: "+materialString);
				continue;
			}

			int amount = Integer.parseInt(String.valueOf(e.getValue()));
			System.out.println("TeamHandler.addToInventroy(), adding "+amount+" of "+material+" to "+team);
			inventory.addItem(new ItemStack(material, amount));
		}
	}

	public void tickPerSecond() {
		for(Entry<Player, Long> respawnTimer : playerRespawnTime.entrySet()) {
			long respawnAt = respawnTimer.getValue();
			long seconds = game.getSeconds();
			game.log(respawnTimer.getKey().getName(), " will respawn at: ", respawnAt, " time now: ", seconds);
			if(seconds >= respawnAt) {
				doRealRespawn(respawnTimer.getKey());
			}
		}
	}

	public boolean checkIfPlayerIsInRespawn(Player player) {
		if(playerRespawnTime.get(player) != null) {
			player.sendMessage("Sorry, you have not respawnd yet.. for realz!");
			return false;
		}
		return true;
	}

}
