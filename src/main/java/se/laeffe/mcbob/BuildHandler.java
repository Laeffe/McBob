package se.laeffe.mcbob;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BuildHandler {

	private AbstractGame game;
	private LinkedHashSet<Cuboid> nobuilds = new LinkedHashSet<Cuboid>();

	public BuildHandler(AbstractGame game) {
		this.game = game;
	}

	public void onBlockBreak(BlockBreakEvent event) {
		boolean cancel = false;
		if(!game.getBattleHandler().isBuildingAllowed()) {
			if(event.getBlock().getType() != Material.TNT)
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
			if(event.getBlock().getType() != Material.FIRE)
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

	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		final List<BlockState> blockStates = new LinkedList<BlockState>();
		List<Block> blockList = event.blockList();
		for(Block b : blockList) {
			if(isNoBuild(b.getLocation())) {
				BlockState state = b.getState();
				blockStates.add(state);
			}
		}
		
		int time = 10;
		for(final BlockState bs : blockStates) {
			game.getMcbob().scheduleSyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					Block block = bs.getBlock();
					block.setTypeId(bs.getTypeId());
					block.setData(bs.getRawData());
				}
			}, time);
			time+=5;
		}
	}
}
