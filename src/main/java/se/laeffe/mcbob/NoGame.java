package se.laeffe.mcbob;

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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class NoGame extends AbstractGame {

	public NoGame(Mcbob mcbob) {
		super(mcbob);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if(!allowBuild()) {
			Player player = event.getPlayer();
			notifyPlayer(player, "You are not allowed to build outside of a ongoing Game");
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!allowBuild()) {
			Player player = event.getPlayer();
			notifyPlayer(player, "You are not allowed to build outside of a ongoing Game");
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		if(!allowBuild()) {
			Player player = event.getPlayer();
			notifyPlayer(player, "You are not allowed to build outside of a ongoing Game");
			event.setCancelled(true);
		}
	}

	@Override
	public GameConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TeamHandler getTeamHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AreaHandler getAreaHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BuildHandler getBuildHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BattleHandler getBattleHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public World getWorld() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Server getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mcbob getMcbob() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean cmdSetPeriod(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cmdSetTime(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cmdBattle(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cmdTeamHome(CommandSender sender) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cmdChangeTeam(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cmdRebuildBases() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		for(Player p : mcbob.getPlayer2game().keySet()) {
			event.getRecipients().remove(p);
		}
	}

	private boolean allowBuild() {
		return mcbob.getConfig().getBoolean("nogame.allowbuild");
	}

	@Override
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if(!allowBuild()) {
			Player player = event.getPlayer();
			notifyPlayer(player, "You are not allowed to build outside of a ongoing Game");
			event.setCancelled(true);
		}
	}

	@Override
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		if(!allowBuild()) {
			Player player = event.getPlayer();
			notifyPlayer(player, "You are not allowed to build outside of a ongoing Game");
			event.setCancelled(true);
		}
	}

	@Override
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if(!allowBuild()) {
			Entity damager = event.getDamager();
			if(damager instanceof Player) {
				Player player = (Player)damager;
				notifyPlayer(player, "You are not allowed to build outside of a ongoing Game");
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void notifyPlayers(Object... messageObjects) {
		// TODO Auto-generated method stub

	}

}
