package se.laeffe.mcbob;

import java.util.LinkedHashSet;

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildHandler {

	private GameInterface game;
	private LinkedHashSet<Cuboid> nobuilds = new LinkedHashSet<Cuboid>();

	public BuildHandler(GameInterface game) {
		this.game = game;
	}

	@Deprecated
	public void onBlockCanBuild(BlockCanBuildEvent event) {
		if(true) return;
		
		if(game.getBattleHandler().isBuildingAllowed()) {
			event.setBuildable(true);
		} else {
			event.setBuildable(false);
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		boolean cancel = false;
		if(!game.getBattleHandler().isBuildingAllowed()) {
			cancel = true;
		} else {
			if(isNoBuild(event.getBlock().getLocation())) {
				cancel = true;
			}
		}

		event.setCancelled(cancel);

		System.out.println("BuildHandler.onBlockBreak()");
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		boolean cancel = false;
		if(!game.getBattleHandler().isBuildingAllowed()) {
			cancel = true;
		} else {
			if(isNoBuild(event.getBlock().getLocation())) {
				cancel = true;
			}
		}

		event.setCancelled(cancel);
		
		System.out.println("BuildHandler.onBlockPlace()");
	}

	private boolean isNoBuild(Location location) {
		for(Cuboid e : nobuilds) {
			if(e.isWithin(location)) {
				System.out.println("Can not place a block there. "+location);
				return true;
			}
		}
		return false;
	}

	public void addNoBuildCuboid(Cuboid nobuild) {
		nobuilds.add(nobuild);
	}
}
