package se.laeffe.mcbob;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Area {

	private Vector cord;
	private Location home;
	private Location center;

	public Area(Vector vector, Location center, Location home) {
		this.center = center;
		this.cord = vector;
		this.home = home;
	}

	public boolean isInside(Location to) {
		return (to.getBlockX()-center.getBlockX())*cord.getX()>0;
	}

	public Location getHome() {
		return home;
	}

}
