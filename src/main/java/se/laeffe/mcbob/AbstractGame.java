package se.laeffe.mcbob;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
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

public abstract class AbstractGame {
	protected final Mcbob	mcbob;

	public AbstractGame(Mcbob mcbob) {
		this.mcbob = mcbob;
	}

	public abstract void init();

	public abstract void onPlayerJoin(PlayerJoinEvent event);

	public abstract void onPlayerQuit(PlayerQuitEvent event);

	public abstract void onPlayerRespawn(PlayerRespawnEvent event);

	public abstract void onPlayerMove(PlayerMoveEvent event);

	public abstract void onBlockBreak(BlockBreakEvent event);

	public abstract void onBlockPlace(BlockPlaceEvent event);

	public abstract void onBlockDamage(BlockDamageEvent event);

	public abstract GameConfiguration getConfiguration();

	public abstract TeamHandler getTeamHandler();

	public abstract AreaHandler getAreaHandler();

	public abstract BuildHandler getBuildHandler();

	public abstract BattleHandler getBattleHandler();

	public void notifyPlayer(CommandSender sender, Object... messageObjects) {
		StringBuilder sb = formatMessage(messageObjects);
		sender.sendMessage(sb.toString());
	}

	private StringBuilder formatMessage(Object[] messageObjects) {
		StringBuilder sb = new StringBuilder();
		for(Object message : messageObjects) {
			sb.append(String.valueOf(message));
		}
		return sb;
	}

	public abstract void notifyPlayers(Object... messageObjects);

	public abstract World getWorld();

	public abstract Server getServer();

	public abstract Mcbob getMcbob();

	public abstract boolean cmdSetPeriod(CommandSender sender, String[] args);

	public abstract boolean cmdSetTime(CommandSender sender, String[] args);

	public abstract boolean cmdBattle(CommandSender sender, String[] args);

	public abstract boolean cmdTeamHome(CommandSender sender);

	public abstract boolean cmdChangeTeam(CommandSender sender, String[] args);

	public abstract boolean cmdRebuildBases();

	public abstract void onEntityDeath(EntityDeathEvent event);

	public abstract boolean isActive();

	public void log(Object... msg) {
		String calledFrom = "(unknown)";
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		if(stackTrace.length > 1)
			calledFrom = stackTrace[1].getMethodName() + "()";

		StringBuilder sb = new StringBuilder(">> ").append(getName()).append(", ").append(calledFrom).append(" << ");
		for(Object m : msg) {
			if(m instanceof BlockEvent)
				m = ((BlockEvent)m).getBlock();
			sb.append(String.valueOf(m));
		}
		System.out.println(sb.toString());
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	public boolean endGame(String string) {
		log("DERP!!! can not call endGame on the abstract class");
		return false;
	}

	public abstract void onPlayerChat(PlayerChatEvent event);

	public void onEntityExplodeEvent(EntityExplodeEvent event) {}

	public void hidePlayer(Player player) {
		for(Player other : getServer().getOnlinePlayers()) {
			other.hidePlayer(player);
		}
	}

	public void showPlayer(Player player) {
		for(Player other : getServer().getOnlinePlayers()) {
			other.showPlayer(player);
		}
	}

	public long getSeconds() {
		return 0;
	}

	public abstract void onPlayerInteractEvent(PlayerInteractEvent event);

	public abstract void onPlayerPickupItemEvent(PlayerPickupItemEvent event);

	public abstract void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event);

}