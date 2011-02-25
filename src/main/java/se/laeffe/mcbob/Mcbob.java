package se.laeffe.mcbob;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Mcbob extends JavaPlugin {

	private TeamHandler teamHandler;
	private AreaHandler areaHandler;
	private BuildHandler buildHandler;
	private BattleHandler battleHandler;
	private DeathHandler deathHandler;

	public Mcbob(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDisable() {
		System.out.println("Mcbob.onDisable()");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnable() {
		System.out.println("Mcbob.onEnable()");
		// TODO Auto-generated method stub
		battleHandler = new BattleHandler(this);
		buildHandler = new BuildHandler(this);
		areaHandler = new AreaHandler(this);
		teamHandler = new TeamHandler(this);
		deathHandler = new DeathHandler(this);
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Type.PLAYER_JOIN, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_COMMAND, teamHandler, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE, areaHandler, Priority.Normal, this);
//		pm.registerEvent(Type.BLOCK_CANBUILD, buildHandler, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, buildHandler, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACED, buildHandler, Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_RIGHTCLICKED, battleHandler, Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_DEATH, deathHandler, Priority.Normal, this);
		
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, battleHandler.getTask(), 1, 100);
	}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();

        if (commandName.equals("chteam")) {
        	cmdChangeTeam(sender, args);
        } else if (commandName.equals("teamhome")) {
        	cmdTeamHome(sender);
        } else if (commandName.equals("battle")) {
        	cmdBattle(sender, args);
        }

        return false;
    }

	private void cmdBattle(CommandSender sender, String[] args) {
		boolean state;
		if(args.length >= 1) {
			state = Boolean.parseBoolean(args[0]);
		} else {
			state = !battleHandler.isBattle();
		}
		battleHandler.setBattleState(state);
	}

	private void cmdTeamHome(CommandSender sender) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			Area teamArea = teamHandler.getTeamArea(player);
			Location home = teamArea.getHome();
			player.teleportTo(home);
		}
		
	}

	private void cmdChangeTeam(CommandSender sender, String[] args) {
		String teamName = null;
		String playerName = null;
		if(args.length >= 1)
			teamName = args[0];
		if(args.length >= 2)
			playerName = args[1];
		
		Team team;
		if(teamName != null) {
			team = teamHandler.getTeam(teamName);
		} else {
			return;
		}
		
		Player player;
		if(playerName != null)
			player = getServer().getPlayer(playerName);
		else if(sender instanceof Player) {
			player = (Player) sender;
		} else {
			return;
		}
		
		if(player != null) {
			teamHandler.changeTeam(player, team);
		}
	}
	
	public void notifyPlayers(String msg) {
		String message = "McBob: "+msg;
		System.out.println("notification: "+message);
		getServer().broadcastMessage(message );
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
