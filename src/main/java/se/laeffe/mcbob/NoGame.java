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

public class NoGame extends GameInterface {

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		// TODO Auto-generated method stub

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
	public void notifyPlayers(String string) {
		// TODO Auto-generated method stub

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

}
