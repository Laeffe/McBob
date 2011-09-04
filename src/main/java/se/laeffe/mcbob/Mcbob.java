package se.laeffe.mcbob;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Mcbob extends JavaPlugin {
	private TeamHandler teamHandler;
	private AreaHandler areaHandler;
	private BuildHandler buildHandler;
	private BattleHandler battleHandler;
	private DeathHandler deathHandler;

	@Override
	public void onDisable() {
		System.out.println("Mcbob.onDisable()");
		// TODO Auto-generated method stub
	}

	@Override
	public void onEnable() {
		System.out.println("Mcbob.onEnable()");

		battleHandler = new BattleHandler(this);
		buildHandler = new BuildHandler(this);
		areaHandler = new AreaHandler(this);
		teamHandler = new TeamHandler(this);
		deathHandler = new DeathHandler(this);
		
		areaHandler.init();
		teamHandler.init();

		registerCommands();
		registerEvents();

		getServer().getScheduler().scheduleSyncRepeatingTask(this, battleHandler.get20TickTask(), 1, 20);
	}

	private void registerCommands() {
		getCommand("chteam").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdChangeTeam(sender, args);
			}
		});

		getCommand("teamhome").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdTeamHome(sender);
			}
		});
		
		getCommand("battle").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdBattle(sender, args);
			}
		});

		getCommand("setperiod").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdSetPeriod(sender, args);
			}
		});
		
		getCommand("settime").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdSetTime(sender, args);
			}
		});
		
		getCommand("rebuildbases").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdRebuildBases();
			}
		});
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_JOIN, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, teamHandler, Priority.Normal, this);
//		pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE, areaHandler, Priority.Normal, this);
		// pm.registerEvent(Type.BLOCK_CANBUILD, buildHandler, Priority.Normal,
		// this);
		pm.registerEvent(Type.BLOCK_BREAK, buildHandler, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE, buildHandler, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_DAMAGE, battleHandler, Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_DEATH, deathHandler, Priority.Normal, this);
	}

	private boolean cmdSetPeriod(CommandSender sender, String[] args) {
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

	private boolean cmdSetTime(CommandSender sender, String[] args) {
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

	private Integer getInt(String[] args, int i) {
		if(args.length > i) {
			try {
				return Integer.parseInt(args[i]);
			} catch (NumberFormatException e) {}
		}
		return null;
	}

	private boolean cmdBattle(CommandSender sender, String[] args) {
		boolean state;
		if (args.length >= 1) {
			state = Boolean.parseBoolean(args[0]);
		} else {
			state = !battleHandler.isBattle();
		}
		battleHandler.setBattleState(state);
		return true;
	}

	private boolean cmdTeamHome(CommandSender sender) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Team team = teamHandler.getTeam(player);
			Location home = team.getHome();
			player.teleport(home);
			return true;
		}
		return false;
	}

	private boolean cmdChangeTeam(CommandSender sender, String[] args) {
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

	private boolean cmdRebuildBases() {
		Collection<Team> teams = teamHandler.getTeams();
		for(Team team : teams) {
			areaHandler.createTeamBase(team);
		}
		return false;
	}

	public void notifyPlayers(String msg) {
		String message = "McBob: " + msg;
		System.out.println("notification: " + message);
		getServer().broadcastMessage(message);
	}

	public TeamHandler getTeamHandler() {
		return teamHandler;
	}

	public AreaHandler getAreaHandler() {
		return areaHandler;
	}

	public BuildHandler getBuildHandler() {
		return buildHandler;
	}

	public BattleHandler getBattleHandler() {
		return battleHandler;
	}

	public World getWorld() {
		return getServer().getWorlds().get(0);
	}

	public boolean validateWorld(Location location) {
		return getWorld() == location.getWorld();
	}
}
