package se.laeffe.mcbob;

import static se.laeffe.mcbob.util.ParseHelper.getInt;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Game extends AbstractGame {
	private TeamHandler teamHandler;
	private AreaHandler areaHandler;
	private BuildHandler buildHandler;
	private BattleHandler battleHandler;
	private final String name;
	private final World world;
	private volatile boolean active = false;
	private GameConfiguration gameConfiguration = null;
	private AtomicLong seconds = new AtomicLong(0);

	public Game(Mcbob mcbob, String name, World world) {
		super(mcbob);
		this.name = name;
		this.world = world;
	}
	
	public void deactivate() {
		active = false;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public void init() {
		battleHandler = new BattleHandler(this);
		buildHandler = new BuildHandler(this);
		areaHandler = new AreaHandler(this);
		teamHandler = new TeamHandler(this);
		
		areaHandler.init();
		teamHandler.init();
		battleHandler.init();
	}
	
	public void startGame() {
		active = true;
		notifyPlayers(getBattleHandler().getEndCondition().getEndConditionSummary());
	}

	@Override
	public boolean endGame(String string) {
		deactivate();
		Team winners = getBattleHandler().getWinners();
		String scoreSummary = getBattleHandler().getScoreSummary();
		if(winners != null)
		{
			notifyPlayers("All your base are belong to team :"+winners.getName()+"!!! (yes, they won!)");
			notifyPlayers("The winning score was, "+scoreSummary);
		}
		else
		{
			notifyPlayers("All your sucking is a equilibrium.. (there was a tie)");
			notifyPlayers("The score was, "+scoreSummary);
		}

		notifyPlayers("(End condition '"+string+"')");
		log(string);
		return mcbob.removeGame(this);
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		log(event);
		teamHandler.onPlayerJoin(event);
		mcbob.playerJoined(event.getPlayer(), this);
	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		log(event);
		teamHandler.onPlayerQuit(event);
		mcbob.playerLeft(event.getPlayer(), this);
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		log(event);
		teamHandler.onPlayerRespawn(event);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
//		log(event);
//		teamHandler.onPlayerMove(event);
//		if(!event.isCancelled()) {
//			return;
//		}
		areaHandler.onPlayerMove(event);
	}
	
	@Override
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		log(event);
		if(!teamHandler.checkIfPlayerIsInRespawn(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		log(event);
		if(!teamHandler.checkIfPlayerIsInRespawn(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		buildHandler.onBlockBreak(event);
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		log(event);
		if(!teamHandler.checkIfPlayerIsInRespawn(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		buildHandler.onBlockPlace(event);
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		log(event);
		if(!teamHandler.checkIfPlayerIsInRespawn(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		battleHandler.onBlockDamage(event);
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		log(event);
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			getBattleHandler().playerDied(player);
		}
	}
	
	@Override
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if(!teamHandler.checkIfPlayerIsInRespawn(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@Override
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		log(event);
		buildHandler.onEntityExplodeEvent(event);
	}
	
	@Override
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		log(event);
		Entity damager = event.getDamager();
		if(damager instanceof Player && !teamHandler.checkIfPlayerIsInRespawn((Player) damager)) {
			event.setCancelled(true);
			return;
		}

		Entity entity = event.getEntity();
		if(entity instanceof Player && !teamHandler.checkIfPlayerIsInRespawn((Player) entity)) {
			event.setCancelled(true);
			return;
		}
	}

	@Override
	public GameConfiguration getConfiguration() {
		if(gameConfiguration == null)
			gameConfiguration = mcbob.getGameConfiguration(name);
		return gameConfiguration;
	}

	
	@Override
	public TeamHandler getTeamHandler() {
		return teamHandler;
	}

	@Override
	public AreaHandler getAreaHandler() {
		return areaHandler;
	}

	@Override
	public BuildHandler getBuildHandler() {
		return buildHandler;
	}

	@Override
	public BattleHandler getBattleHandler() {
		return battleHandler;
	}

	@Override
	public void notifyPlayers(String string) {
		Set<Player> players = teamHandler.getPlayers();
		if(players == null) {
			System.out.println("Game.notifyPlayers(), No players to notify.");
			return;
		}
		
		for(Player p : players) {
			p.sendMessage(string);
		}
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public Server getServer() {
		return mcbob.getServer();
	}

	@Override
	public Mcbob getMcbob() {
		return mcbob;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean cmdSetPeriod(CommandSender sender, String[] args) {
		if (args.length >= 1) {
			Integer buildPeriod = getInt(args, 0);
			Integer battlePeriod = getInt(args, 1);
			if(buildPeriod != null) {
				if(battlePeriod == null)
					battlePeriod = buildPeriod;
				
				getBattleHandler().setPeriod(buildPeriod, battlePeriod);
				return true;
			}
			sender.sendMessage("Period isn't a valid number.");
		}
		sender.sendMessage("Need at least one period number.");
		return false;
	}

	@Override
	public boolean cmdSetTime(CommandSender sender, String[] args) {
		if (args.length >= 1) {
			Integer buildTime  = getInt(args, 0);
			Integer battleTime = getInt(args, 1);
			if(buildTime != null) {
				if(battleTime == null)
					battleTime = buildTime;
				
				getBattleHandler().setTime(buildTime, battleTime);
				return true;
			}
			sender.sendMessage("Period isn't a valid number.");
		}
		sender.sendMessage("Need at least one period number.");
		return false;
	}

	@Override
	public boolean cmdBattle(CommandSender sender, String[] args) {
		boolean state;
		if (args.length >= 1) {
			state = Boolean.parseBoolean(args[0]);
		} else {
			state = !battleHandler.isBattle();
		}
		battleHandler.setBattleState(state);
		return true;
	}

	@Override
	public boolean cmdTeamHome(CommandSender sender) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Team team = teamHandler.getTeam(player);
			Location home = team.getHome();
			player.teleport(home);
			//If player teleports home while carrying the flag, return it.
			Flag flag = battleHandler.returnFlag(player);
			if(flag != null)
				notifyPlayers(flag.getTeam().getName()+"'s flag returned since "+player.getDisplayName()+" teleported home.");
			return true;
		}
		return false;
	}

	@Override
	public boolean cmdChangeTeam(CommandSender sender, String[] args) {
		String teamName = null;
		String playerName = null;
		if (args.length >= 1)
			teamName = args[0];
		if (args.length >= 2)
			playerName = args[1];

		Team team = null;
		if (teamName != null) {
			team = teamHandler.getTeam(teamName);
			if(team == null) {
				sender.sendMessage("Sorry but '"+teamName+"' does not exist.");
				return false;
			}
		} else {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				for(Team t : teamHandler.getTeams()) {
					if(!t.contains(player)) {
						team = t;
						break;
					}
				}
			} else {
				sender.sendMessage("A Player specifiy you must.");
				return false;
			}
		}

		Player player;
		if (playerName != null)
			player = getServer().getPlayer(playerName);
		else if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			return false;
		}

		if (player != null) {
			teamHandler.changeTeam(player, team);
		}
		return true;
	}

	@Override
	public boolean cmdRebuildBases() {
		Collection<Team> teams = teamHandler.getTeams();
		for(Team team : teams) {
			areaHandler.createTeamBase(team);
		}
		return false;
	}

	public void tickPerSecond() {
		seconds.incrementAndGet();
		System.out.println("Game.tickPerSecond(), "+seconds.get());
		battleHandler.tickPerSecond();
		teamHandler.tickPerSecond();
	}

	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		String chatWithAllIndicator = getMcbob().getConfig().getString("chatisolation.chatWithAllIndicator", "!");
		event.getRecipients().clear();
		Collection<Player> players;
		if(event.getMessage().startsWith(chatWithAllIndicator))
		{
			players = getTeamHandler().getPlayers();
		}
		else
		{
			players = getTeamHandler().getTeam(event.getPlayer()).getPlayers();
		}
		event.getRecipients().addAll(players);
	}
	
	@Override
	public long getSeconds() {
		return seconds.get();
	}
}
