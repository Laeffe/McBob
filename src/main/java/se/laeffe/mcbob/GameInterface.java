package se.laeffe.mcbob;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.config.Configuration;

public abstract class GameInterface {

	public abstract void init();

	public abstract void onPlayerJoin(PlayerJoinEvent event);

	public abstract void onPlayerQuit(PlayerQuitEvent event);

	public abstract void onPlayerRespawn(PlayerRespawnEvent event);

	public abstract void onPlayerMove(PlayerMoveEvent event);

	public abstract void onBlockBreak(BlockBreakEvent event);

	public abstract void onBlockPlace(BlockPlaceEvent event);

	public abstract void onBlockDamage(BlockDamageEvent event);

	public abstract Configuration getConfiguration();

	public abstract TeamHandler getTeamHandler();

	public abstract AreaHandler getAreaHandler();

	public abstract BuildHandler getBuildHandler();

	public abstract BattleHandler getBattleHandler();

	public abstract void notifyPlayers(String string);

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

	public void log(Object...msg) {
		String calledFrom = "(unknown)";
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		if(stackTrace.length>1)
			calledFrom = stackTrace[1].getMethodName()+"()";
		
		StringBuilder sb = new StringBuilder(">> ").append(getName()).append(", ").append(calledFrom).append(" << ");
		for(Object m : msg)
			sb.append(String.valueOf(m));
		System.out.println(sb.toString());
	}

	public String getName() {
		return getClass().getSimpleName();
	}
}