package se.laeffe.mcbob;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Mcbob extends JavaPlugin {
	private ConcurrentHashMap<String, Game> games         = new ConcurrentHashMap<String, Game>();
	private ConcurrentHashMap<World, Game>  gamesInWorlds = new ConcurrentHashMap<World,  Game>();
	private ConcurrentHashMap<Player, Game> player2game   = new ConcurrentHashMap<Player, Game>();

	private AbstractGame noGame = new NoGame(this);
	
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

	public AbstractGame getGame(Player player) {
		if(player == null)
			return noGame;
		
		AbstractGame game = player2game.get(player);
		if(game == null || !game.isActive())
			return noGame;
		return game;
	}
	
	private AbstractGame getGame(World world) {
		if(world == null)
			return noGame;
		
		AbstractGame game = gamesInWorlds.get(world);
		if(game == null || !game.isActive())
			return noGame;
		return game;
	}
	
	public AbstractGame getGame(PlayerEvent playerEvent) {
		return getGame(playerEvent.getPlayer());
	}
	
	public AbstractGame getGame(BlockEvent blockEvent) {
		World world = blockEvent.getBlock().getLocation().getWorld();
		return getGame(world);
	}

	public AbstractGame getGame(EntityEvent entityEvent) {
		Entity entity = entityEvent.getEntity();
		if (entity instanceof Player) {
			return getGame((Player) entity);
		}
		World world = entityEvent.getEntity().getLocation().getWorld();
		return getGame(world);
	}
	
	public AbstractGame getGame(CommandSender sender, String[] args) {
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
			public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
				if(args.length < 1)
					return false;
				
				final String worldName = args[0];
				
//				Environment environment = Environment.NORMAL;
//				if(args.length > 1) {
//					try {
//						environment = Environment.valueOf(args[1]);
//					} catch(IllegalArgumentException e) {
//						StringBuilder sb = new StringBuilder();
//						for(Environment env : Environment.values()) {
//							sb.append(env.toString()).append(",");
//						}
//						sender.sendMessage("Not a valid environment, choose from: "+sb.toString());
//						System.out.println("Mcbob.registerCommands().new CommandExecutor() {...}.onCommand(), "+e);
//						return false;
//					}
//				}
//				final Environment env = environment;
				
				getServer().getScheduler().scheduleSyncDelayedTask(Mcbob.this, new Runnable() {
					public void run() {
						if(getServer().createWorld(WorldCreator.name(worldName)) == null) {
							sender.sendMessage("Could not create world.");
						} else {
							sender.sendMessage("World were created.");
						}
					}
				});
				
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
				World world = null;
				if(args.length > 0)
					world = getServer().getWorld(args[0]);
				
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
		getCommand("reloadconfig").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				reloadConfig();
				sender.sendMessage("Config reloaded from disk");
				return true;
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
		return game.endGame("stopped by admin");
	}

	public boolean removeGame(Game game) {
		gamesInWorlds.remove(game.getWorld());
		games.remove(game.getName());
		for(Iterator<Entry<Player, Game>> iterator = player2game.entrySet().iterator(); iterator.hasNext();) {
			if(iterator.next().getValue() == game)
				iterator.remove();
		}
		System.out.println("Game ended and removed: "+game);
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
		
		game.startGame();
		
		return true;
	}

	private void registerEvents() {
		@SuppressWarnings("unused")
		Listener listener = new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event) {
				getGame(event.getPlayer().getWorld()).onPlayerJoin(event);
			}
			
			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent event) {
				getGame(event).onPlayerQuit(event);
			}
			
			@EventHandler
			public void onPlayerRespawn(PlayerRespawnEvent event) {
				getGame(event).onPlayerRespawn(event);
			}
			
			@EventHandler
			public void onPlayerMove(PlayerMoveEvent event) {
				getGame(event).onPlayerMove(event);
			}

			@EventHandler
			public void onBlockBreak(BlockBreakEvent event) {
				getGame(event.getPlayer()).onBlockBreak(event);
			}
			
			@EventHandler
			public void onBlockPlace(BlockPlaceEvent event) {
				getGame(event.getPlayer()).onBlockPlace(event);
			}
			
			@EventHandler
			public void onBlockDamage(BlockDamageEvent event) {
				getGame(event.getPlayer()).onBlockDamage(event);
			}

			@EventHandler
			public void onEntityDeath(EntityDeathEvent event) {
				getGame(event).onEntityDeath(event);
			}
			
			@EventHandler
			public void onPlayerChat(PlayerChatEvent event) {
				if(getConfig().getBoolean("chatisolation.enable")) {
					getGame(event).onPlayerChat(event);
				}
			}
			
			@EventHandler
			public void onEntityExplodeEvent(EntityExplodeEvent event) {
				getGame(event).onEntityExplodeEvent(event);
			}
			
			@EventHandler
			public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
				getGame(event).onPlayerPickupItemEvent(event);
			}
			
			@EventHandler
			public void onPlayerInteractEvent(PlayerInteractEvent event) {
				getGame(event).onPlayerInteractEvent(event);
			}
			
			@EventHandler
			public void onEntityDamageByEntityEvent (EntityDamageByEntityEvent event) {
				getGame(event).onEntityDamageByEntityEvent(event);
			}
				
				
		};
		getServer().getPluginManager().registerEvents(listener, this);
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

	public GameConfiguration getGameConfiguration(String name) {
		return new GameConfiguration(name, getConfig());
	}
	
	public ConcurrentHashMap<Player, Game> getPlayer2game() {
		return player2game;
	}

	public void scheduleSyncDelayedTask(Runnable task, int delay) {
		getServer().getScheduler().scheduleSyncDelayedTask(this, task, delay);
	}
}
