package se.laeffe.mcbob;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import se.laeffe.mcbob.observer.Observer;

public class AreaHandler {

	private AbstractGame	game;
	private int				radius			= 100;
	private Location		center;
	private int				distanceToBase	= 40;

	public AreaHandler(AbstractGame game) {
		this.game = game;
		GameConfiguration cfg = game.getConfiguration();

		radius = cfg.getInt("radius", radius);
		distanceToBase = cfg.getInt("distance", distanceToBase);
	}

	public void init() {
		center = game.getWorld().getSpawnLocation();
		initCenterMarker();
	}

	private void initCenterMarker() {
		int x = center.getBlockX();
		int y = center.getBlockY() - 1;
		int z = center.getBlockZ();
		center.getWorld().getBlockAt(x, y, z);
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		// System.out.println("AreaHandler.onPlayerMove(), x:"+to.getBlockX()+" y:"+to.getBlockY()+" z:"+to.getBlockZ());
		Player player = event.getPlayer();

		if(game.getBattleHandler().isTeamAreaRestrictionOn()) {
			Team team = game.getTeamHandler().getTeam(player);
			if(!isInsideTeamArea(team, to)) {
				event.setCancelled(true);
				if(!isInsideTeamArea(team, event.getFrom())) {
					Location home = team.getHome();
					player.sendMessage("You are in hostile territory, teleporting you home.");
					player.teleport(home);
				} else {
					player.sendMessage("You can not leave your team area now.");
					player.teleport(event.getFrom());
				}
			}
		}

		double distanceSquared = to.toVector().distanceSquared(getCenterVector());
		if(distanceSquared > radius * radius) {
			event.setCancelled(true);
			player.sendMessage("You are leaving the battle area.");
			player.teleport(event.getFrom());
		}

	}

	private boolean isInsideTeamArea(Team team, Location loc) {
		return (loc.getBlockX() - center.getBlockX()) * team.getLocationModifier().getX() > 0;
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
		int modA = modifier.getBlockY() < 0?-1:1;
		int modB = modifier.getBlockX() < 0?-1:1;
		int modC = modifier.getBlockZ() < 0?-1:1;

		//@formatter:off
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
						new int[]{91,0,-6,0,91},
				},
				new int[][]{
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
						new int[]{0,0,0,0,0},
				},
		};
		//@formatter:on

		final Byte[] specialBlocksData = new Byte[] { null, null, null, null, 0x2, (byte)(modB < 0?0x5:0x4) };

		// Vector dir = center.toVector().subtract(home.toVector()).normalize();

		World world = home.getWorld();
		int x = home.getBlockX() + (-2 * modB);
		int y = home.getBlockY() + (-1 * modA);
		int z = home.getBlockZ() + (-2 * modC);
		Cuboid nobuild = new Cuboid();
		Block block;
		// for(int
		// a=(modA>0?0:base.length-1);(modA>0?a<base.length:a>=0);a+=modA) {
		for(int a = 0; a < base.length; a++) {
			// for(int
			// b=(modB>0?0:base[a].length-1);(modB>0?b<base[a].length:b>=0);b+=modB)
			// {
			for(int b = 0; b < base[a].length; b++) {
				// for(int
				// c=(modC>0?0:base[a][b].length-1);(modC>0?c<base[a][b].length:c>=0);c+=modC)
				// {
				for(int c = 0; c < base[a][b].length; c++) {
					int type = base[a][b][c];
					int x2 = (x + b * modB);
					int y2 = (y + a * modA);
					int z2 = (z + c * modC);
					block = world.getBlockAt(x2, y2, z2);
					if(type < 0) {
						Byte specialData = specialBlocksData[(-type - 1)];
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
								game.getTeamHandler().setChestContents(chest, team);
								team.setChest(chest);
								break;
							case -4:
								type = Material.WORKBENCH.getId();
								break;
							case -5:
								type = Material.FURNACE.getId();
								break;
							case -6:
								type = Material.WALL_SIGN.getId();
								createScoreSignUpdater(team, block);
								break;
						}

						if(specialData != null) {
							block.setData(specialData);
						}
					}

					block.setTypeId(type);
					nobuild.recordOutter(x2, y2, z2);
				}
			}
		}

		game.getBuildHandler().addNoBuildCuboid(nobuild);
		return home;
	}

	private void createScoreSignUpdater(final Team homeTeam, final Block block) {
		final BattleHandler bh = game.getBattleHandler();
		bh.getScores().addObserver(new Observer<Scores>() {
			@Override
			public void update(Scores s) {
				Map<Team, Integer> scoresAsMap = bh.getScoresAsMap();
				StringBuilder sb = new StringBuilder();
				sb.append(scoresAsMap.get(homeTeam));
				for(Entry<Team, Integer> entry : scoresAsMap.entrySet()) {
					if(entry.getKey() != homeTeam) {
						sb.append("-").append(entry.getValue());
					}
				}

				BlockState state = block.getState();
				if(state instanceof Sign) {
					Sign sign = (Sign)state;
					sign.setLine(0, "Score");
					sign.setLine(2, sb.toString());
				} else {
					game.log("The sign block is not a sign >_<");
				}
			}
		});
	}

}
