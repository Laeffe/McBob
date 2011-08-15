package se.laeffe.mcbob;

import org.bukkit.Location;

public class Cuboid {
	
	int xmax = Integer.MIN_VALUE, ymax = Integer.MIN_VALUE, zmax = Integer.MIN_VALUE;
	int xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE, zmin = Integer.MAX_VALUE;
	
	public void recordOutter(int x, int y, int z) {
		xmax = Math.max(x, xmax);
		ymax = Math.max(y, ymax);
		zmax = Math.max(z, zmax);

		xmin = Math.min(x, xmin);
		ymin = Math.min(y, ymin);
		zmin = Math.min(z, zmin);
	}

	public boolean isWithin(Location location) {
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		
		if(x >= xmin && x <= xmax && y >= ymin && y <= ymax && z >= zmin && z <= zmax) {
			return true;
		}
		
		return false;
	}

}
