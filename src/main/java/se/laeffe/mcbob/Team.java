package se.laeffe.mcbob;

import java.util.LinkedHashSet;

import org.bukkit.entity.Player;

public class Team {
	LinkedHashSet<Player> players = new LinkedHashSet<Player>();
	String name = "";
	private int cord;
	private Area area;
	private Flag flag;
	private int score = 0;
	
	public Team(String name, Flag flag) {
		this.name = name;
		this.flag = flag;
		flag.setTeam(this);
	}

	public void setArea(Area area) {
		this.area = area;
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

	public Area getArea() {
		return area;
	}

	public boolean remove(Player player) {
		return players.remove(player);
	}

	public Flag getFlag() {
		return flag;
	}

	public void addScore(int i) {
		score  += i;
	}

	public int getScore() {
		return score;
	}
}
