package se.laeffe.mcbob;

import java.util.LinkedHashSet;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Team {
	LinkedHashSet<Player> players = new LinkedHashSet<Player>();
	String name = "";
	private Flag flag;
	private Chest chest;
	private Location home;
	private final Vector locationModifier;
	
	public Team(String name, Flag flag, Vector locationModifier) {
		this.name = name;
		this.flag = flag;
		this.locationModifier = locationModifier;
		flag.setTeam(this);
	}

	public int size() {
		return players.size();
	}

	public void add(Player player) {
		players.add(player);
	}

	public String getName() {
		return name;
	}

	public boolean remove(Player player) {
		return players.remove(player);
	}

	public Flag getFlag() {
		return flag;
	}

	public Chest getChest() {
		return chest;
	}

	public void setChest(Chest chest) {
		this.chest = chest;
	}

	public boolean contains(Player player) {
		return players.contains(player);
	}

	public void setHome(Location teamHome) {
		this.home = teamHome;
	}
	
	public Location getHome() {
		return home;
	}
	
	public Vector getLocationModifier() {
		return locationModifier;
	}
	
	@Override
	public String toString() {
		return "Team["+name+"]";
	}
	
	public LinkedHashSet<Player> getPlayers() {
		return players;
	}
}
