package se.laeffe.mcbob;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Mcbob extends JavaPlugin {
	private ConcurrentHashMap<String, Game> games         = new ConcurrentHashMap<String, Game>();
	private ConcurrentHashMap<World, Game>  gamesInWorlds = new ConcurrentHashMap<World,  Game>();
	private ConcurrentHashMap<Player, Game> player2game   = new ConcurrentHashMap<Player, Game>();

	private GameInterface noGame = new NoGame();
	
	@Override
	public void onDisable() {
		System.out.println("Mcbob.onDisable()");
		// TODO Auto-generated method stub
	}

	@Override
	public void onEnable() {
		System.out.println("Mcbob.onEnable()");

		registerCommands();
		registerEvents();

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				updateGameTicks();
			}
		}, 1, 20);
	}
	
	protected void updateGameTicks() {
		for(Game game : games.values()){
			game.tickPerSecond();
		}
	}

	public GameInterface getGame(Player player) {
		if(player == null)
			return noGame;
		
		GameInterface game = player2game.get(player);
		if(game == null || !game.isActive())
			return noGame;
		return game;
	}
	
	private GameInterface getGame(World world) {
		if(world == null)
			return noGame;
		
		GameInterface game = gamesInWorlds.get(world);
		if(game == null || !game.isActive())
			return noGame;
		return game;
	}
	
	public GameInterface getGame(PlayerEvent playerEvent) {
		return getGame(playerEvent.getPlayer());
	}
	
	public GameInterface getGame(BlockEvent blockEvent) {
		World world = blockEvent.getBlock().getLocation().getWorld();
		return getGame(world);
	}

	public GameInterface getGame(EntityEvent entityEvent) {
		Entity entity = entityEvent.getEntity();
		if (entity instanceof Player) {
			return getGame((Player) entity);
		}
		World world = entityEvent.getEntity().getLocation().getWorld();
		return getGame(world);
	}
	
	public GameInterface getGame(CommandSender sender, String[] args) {
		if(sender instanceof Player)
			return getGame((Player)sender);
		//FIXME Add support to specify game name or something like that.
		return noGame;
	}

	private void registerCommands() {
		getCommand("chteam").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return getGame(sender, args).cmdChangeTeam(sender, args);
			}
		});

		getCommand("teamhome").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return getGame(sender, args).cmdTeamHome(sender);
			}
		});
		
		getCommand("battle").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return getGame(sender, args).cmdBattle(sender, args);
			}
		});

		getCommand("setperiod").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return getGame(sender, args).cmdSetPeriod(sender, args);
			}
		});
		
		getCommand("settime").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return getGame(sender, args).cmdSetTime(sender, args);
			}
		});
		
		getCommand("rebuildbases").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return getGame(sender, args).cmdRebuildBases();
			}
		});
		
		getCommand("startgame").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdStartGame(sender, args);
			}
		});

		getCommand("stopgame").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				return cmdStopGame(sender, args);
			}
		});
		
		getCommand("createworld").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if(args.length < 1)
					return false;
				
				String name = args[0];
				
				Environment environment = Environment.NORMAL;
				if(args.length > 1) {
					try {
						environment = Environment.valueOf(args[1]);
					} catch(IllegalArgumentException e) {
						StringBuilder sb = new StringBuilder();
						for(Environment env : Environment.values()) {
							sb.append(env.toString()).append(",");
						}
						sender.sendMessage("Not a valid environment, choose from: "+sb.toString());
						System.out.println("Mcbob.registerCommands().new CommandExecutor() {...}.onCommand(), "+e);
						return false;
					}
				}
				
				if(getServer().createWorld(name, environment) == null) {
					sender.sendMessage("Could not create world.");
					return false;
				}
				return true;
			}
		});
		
		getCommand("removeworld").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		getCommand("tpworld").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if(args.length < 1)
					return false;
				
				World world = getServer().getWorld(args[0]);
				if(world == null) {
					sender.sendMessage("World does not exist." +getServer().getWorlds());
					return false;
				}
				
				Location spawnLocation = world.getSpawnLocation();
				if(sender instanceof Player) {
					sender.sendMessage("Sending you of to the new world");
					((Player)sender).teleport(spawnLocation);
					return true;
				} else {
					sender.sendMessage("You'r not a player");
					return false;
				}
			}
		});
		
		
	}
	
	private boolean cmdStartGame(CommandSender sender, String[] args) {
		String worldName = null;
		if(args.length>=1)
			worldName = args[0];
		
		World world = null;
		if(worldName != null) {
			world = getServer().getWorld(worldName);
		} else {
			if (sender instanceof Player) {
				world = ((Player) sender).getWorld();
			}
		}
		
		if(world != null) {
			if(!startGame(world, worldName))
				sender.sendMessage("Game already in progress.");
			else
				return true;
		}
		
		return false;
	}

	private boolean cmdStopGame(CommandSender sender, String[] args) {
		String worldName = null;
		if(args.length>=1)
			worldName = args[0];
		
		Game game = null;
		if(worldName != null) {
			game = games.get(worldName);
		} else {
			if (sender instanceof Player) {
				World world = ((Player) sender).getWorld();
				game = gamesInWorlds.get(world);
			}
		}
		
		if(game != null) {
			return stopGame(game);
		} else {
			sender.sendMessage("No active game in that/this world.");
		}
		
		return false;
	}
	
	private boolean stopGame(Game game) {
		game.deactivate();
		gamesInWorlds.remove(game.getWorld());
		games.remove(game.getName());
		for(Iterator<Entry<Player, Game>> iterator = player2game.entrySet().iterator(); iterator.hasNext();) {
			if(iterator.next().getValue() == game)
				iterator.remove();
		}
		return true;
	}

	private boolean startGame(World world, String worldName) {
		if(worldName == null)
			worldName = world.getName();
		Game game = new Game(this, worldName, world);
		if(gamesInWorlds.putIfAbsent(world, game) != null) {
			return false;
		}
		games.put(game.getName(), game);
		
		game.init();
		
		List<Player> players = world.getPlayers();
		for(Player player : players) {
			game.onPlayerJoin(new PlayerJoinEvent(player, "Autojoining players in this world"));
		}
		
		return true;
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		PlayerListener playerListener = new PlayerListener() {
			@Override
			public void onPlayerJoin(PlayerJoinEvent event) {
				getGame(event.getPlayer().getWorld()).onPlayerJoin(event);
			}
			
			@Override
			public void onPlayerQuit(PlayerQuitEvent event) {
				getGame(event).onPlayerQuit(event);
			}
			
			@Override
			public void onPlayerRespawn(PlayerRespawnEvent event) {
				getGame(event).onPlayerRespawn(event);
			}
			
			@Override
			public void onPlayerMove(PlayerMoveEvent event) {
				getGame(event).onPlayerMove(event);
			}
		};
		
		BlockListener blockListener = new BlockListener() {
			@Override
			public void onBlockBreak(BlockBreakEvent event) {
				getGame(event.getPlayer()).onBlockBreak(event);
			}
			
			@Override
			public void onBlockPlace(BlockPlaceEvent event) {
				getGame(event.getPlayer()).onBlockPlace(event);
			}
			
			@Override
			public void onBlockDamage(BlockDamageEvent event) {
				getGame(event.getPlayer()).onBlockDamage(event);
			}
		};
		
		EntityListener entityListener = new EntityListener() {
			@Override
			public void onEntityDeath(EntityDeathEvent event) {
				getGame(event).onEntityDeath(event);
			}
		};
		
		pm.registerEvent(Type.PLAYER_JOIN,    playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT,    playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE,    playerListener, Priority.Normal, this);

		pm.registerEvent(Type.BLOCK_BREAK,    blockListener,  Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_PLACE,    blockListener,  Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_DAMAGE,   blockListener,  Priority.Normal, this);
		
		pm.registerEvent(Type.ENTITY_DEATH,   entityListener, Priority.Normal, this);
	}

	public void notifyPlayers(String msg) {
		String message = "McBob: " + msg;
		System.out.println("notification: " + message);
		getServer().broadcastMessage(message);
	}

	public void playerJoined(Player player, Game game) {
		player2game.put(player, game);
	}

	public void playerLeft(Player player, Game game) {
		player2game.remove(player);
	}
}
