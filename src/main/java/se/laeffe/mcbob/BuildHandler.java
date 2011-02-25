package se.laeffe.mcbob;

import java.util.LinkedHashSet;

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildHandler extends BlockListener {

	private Mcbob mcbob;
	private LinkedHashSet<Cuboid> nobuilds = new LinkedHashSet<Cuboid>();

	public BuildHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
	}

	@Override
	public void onBlockCanBuild(BlockCanBuildEvent event) {
		if(true) return;
		if(!mcbob.validateWorld(event.getBlock().getLocation()))
			return;
		
		if(mcbob.getBattleHandler().isBuildingAllowed()) {
			event.setBuildable(true);
		} else {
			event.setBuildable(false);
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if(!mcbob.validateWorld(event.getBlock().getLocation()))
			return;
		
		boolean cancel = false;
		if(!mcbob.getBattleHandler().isBuildingAllowed()) {
			cancel = true;
		} else {
			if(isNoBuild(event.getBlock().getLocation())) {
				cancel = true;
			}
		}

		event.setCancelled(cancel);

		System.out.println("BuildHandler.onBlockBreak()");
	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!mcbob.validateWorld(event.getBlock().getLocation()))
			return;
		
		boolean cancel = false;
		if(!mcbob.getBattleHandler().isBuildingAllowed()) {
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
