package se.laeffe.mcbob;

import org.bukkit.Location;

public class Area {

	private int cord;
	private Location home;
	private Location center;

	public Area(int cord, Location center, Location home) {
		this.center = center;
		this.cord = cord;
		this.home = home;
	}

	public boolean isInside(Location to) {
		return (to.getBlockX()-center.getBlockX())*cord>0;
	}

	public Location getHome() {
		return home;
	}

}
