package se.laeffe.mcbob;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

public class AreaHandler extends PlayerListener {

	private Mcbob mcbob;
	private int radius = 100;
	private Location center;
	private int distanceToBase = 40;

	public AreaHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
		Configuration cfg = mcbob.getConfiguration();
		
		radius         = cfg.getInt("radius", radius);
		distanceToBase = cfg.getInt("distance", distanceToBase);
	}
	
	public void init() {
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
		Player player = event.getPlayer();
		
		if(mcbob.getBattleHandler().isTeamAreaRestrictionOn()) {
			Team team = mcbob.getTeamHandler().getTeam(player);
			if(!isInsideTeamArea(team, to)) {
					event.setCancelled(true);
					if(!isInsideTeamArea(team, event.getFrom())) {
						Location home = team.getHome();
						player.sendMessage("You are in hostile territory, teleporting you home."+event);
						player.teleport(home);
					} else {
						player.sendMessage("You can not leave your team area now."+event);
						player.teleport(event.getFrom());
					}
			}
		}
		
		if(to.toVector().distanceSquared(getCenterVector()) > radius*radius) {
			event.setCancelled(true);
			player.sendMessage("You are leaving the battle area.");
			System.out.println("Player movement, out of radius.");
			player.teleport(event.getFrom());
		}
		
	}

	private boolean isInsideTeamArea(Team team, Location loc) {
		return (loc.getBlockX()-center.getBlockX())*team.getLocationModifier().getX()>0;
	}

	private Vector getCenterVector() {
		return center.toVector();
	}

	public Location createTeamBase(Team team) {
		World world = center.getWorld();
		Vector direction = team.getLocationModifier();
		Location home = center.toVector().add(direction.clone().multiply(distanceToBase)).toLocation(world);
		home = buildBasicBase(home, team, direction);
		return home;
	}

	private Location buildBasicBase(Location home, Team team, Vector modifier) {
		int modA = modifier.getBlockY()<0?-1:1;
		int modB = modifier.getBlockX()<0?-1:1;
		int modC = modifier.getBlockZ()<0?-1:1;
		
		final int base[][][] = new int[][][] {
				new int[][]{
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
						new int[]{48,48,48,48,48},
				},
				new int[][]{
						new int[]{85,0, 0, 0,85},
						new int[]{0, 0, 0, 0,-5},
						new int[]{0, 0,-1, 0,-4},
						new int[]{0, 0, 0, 0,-3},
						new int[]{85,0,85, 0,85},
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
		int x = home.getBlockX()+(-2*modB);
		int y = home.getBlockY()+(-1*modA);
		int z = home.getBlockZ()+(-2*modC);
		Cuboid nobuild = new Cuboid();
		Block block;
//		for(int a=(modA>0?0:base.length-1);(modA>0?a<base.length:a>=0);a+=modA) {
		for(int a=0;a<base.length;a++) {
//			for(int b=(modB>0?0:base[a].length-1);(modB>0?b<base[a].length:b>=0);b+=modB) {
			for(int b=0;b<base[a].length;b++) {
//				for(int c=(modC>0?0:base[a][b].length-1);(modC>0?c<base[a][b].length:c>=0);c+=modC) {
				for(int c=0;c<base[a][b].length;c++) {
					int type = base[a][b][c];
					int x2 = (x+b*modB);
					int y2 = (y+a*modA);
					int z2 = (z+c*modC);
					block = world.getBlockAt(x2, y2, z2);
					if(type < 0) {
						switch(type) {
							case -1:
								home.setX(x2);
								home.setY(y2);
								home.setZ(z2);
								type = 0;
								break;
							case -2:
								Flag flag = team.getFlag();
								type = flag.getType().getId();
								flag.setBlock(block);
								break;
							case -3:
								type = Material.CHEST.getId();
								block.setTypeId(type);
								Chest chest = (Chest)block.getState();
								mcbob.getTeamHandler().setChestContents(chest, team);
								team.setChest(chest);
								break;
							case -4:
								type = Material.WORKBENCH.getId();
								break;
							case -5:
								type = Material.FURNACE.getId();
								break;
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
