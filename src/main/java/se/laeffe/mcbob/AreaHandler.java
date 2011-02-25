package se.laeffe.mcbob;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class AreaHandler extends PlayerListener {

	private Mcbob mcbob;
	private Player player;
	private int radius = 100;
	private Location center;

	public AreaHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
		center = mcbob.getWorld().getSpawnLocation();
		initCenterMarker();
	}

	private void initCenterMarker() {
		int x = center.getBlockX();
		int y = center.getBlockY()-1;
		int z = center.getBlockZ();
		center.getWorld().getBlockAt(x, y, z);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
//		System.out.println("AreaHandler.onPlayerMove(), x:"+to.getBlockX()+" y:"+to.getBlockY()+" z:"+to.getBlockZ());
		player = event.getPlayer();
		
		if(mcbob.getBattleHandler().isTeamAreaRestrictionOn()) {
			Area area = mcbob.getTeamHandler().getTeamArea(player);
			if(!area.isInside(event.getTo())) {
				event.setCancelled(true);
				if(!area.isInside(event.getFrom())) {
					Location home = area.getHome();
					player.teleportTo(home);
					player.sendMessage("You are in hostile territory, teleporting you home.");
					return;
				} else {
					player.sendMessage("You can not leave your team area now.");
				}
			}
		}
		
		if(to.toVector().distance(getCenterVector()) > radius) {
			event.setCancelled(true);
			player.sendMessage("You are leaving the battle area.");
			System.out.println("Player movement, out of radius.");
		}
		
		if(event.isCancelled()) {
			player.teleportTo(event.getFrom());
		}
	}

	private Vector getCenterVector() {
		return center.toVector();
	}

	public Area createArea(int cord, Flag flag) {
		World world = center.getWorld();
		Vector direction = new Vector(radius/2 * cord, 0, 0);
		Location home = center.toVector().add(direction).toLocation(world);
//		home.setY(world.getHighestBlockYAt(home));
		home = buildBasicBase(home, flag);
		return new Area(cord, center, home);
	}

	private Location buildBasicBase(Location home, Flag flag) {
		final int base[][][] = new int[][][] {
				new int[][]{
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
				},
				new int[][]{
						new int[]{85,0,0,0,85},
						new int[]{0,0,0,0,0},
						new int[]{0,0,-1,0,0},
						new int[]{0,0,0,0,0},
						new int[]{85,0,85,0,85},
				},
				new int[][]{
						new int[]{85,0,0,0,85},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{85,0,-2,0,85},
				},
				new int[][]{
						new int[]{85,0,0,0,85},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{85,0,0,0,85},
				},
				new int[][]{
						new int[]{91,0,0,0,91},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{91,0,0,0,91},
				},
				new int[][]{
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
				},
		};
		
//		Vector dir = center.toVector().subtract(home.toVector()).normalize();		
		
		World world = home.getWorld();
		int x = home.getBlockX()-2;
		int y = home.getBlockY()-1;
		int z = home.getBlockZ()-2;
		Cuboid nobuild = new Cuboid();
		Block block;
		for(int a=0;a<base.length;a++) {
			for(int b=0;b<base[a].length;b++) {
				for(int c=0;c<base[a][b].length;c++) {
					int type = base[a][b][c];
					int x2 = (x+b);
					int y2 = (y+a);
					int z2 = (z+c);
					block = world.getBlockAt(x2, y2, z2);
					if(type < 0) {
						if(type == -1) {
							home.setX(x2);
							home.setY(y2);
							home.setZ(z2);
							type = 0;
						} else if(type == -2) {
							type = flag.getType();
							flag.setBlock(block);
						}
					}
					
					block.setTypeId(type);
					nobuild.recordOutter(x2, y2, z2);
				}
			}
		}
		
		mcbob.getBuildHandler().addNoBuildCuboid(nobuild);
		return home;
	}
	
}
