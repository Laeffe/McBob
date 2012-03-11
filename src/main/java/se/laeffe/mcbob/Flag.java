package se.laeffe.mcbob;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Flag {
	private Material	type;
	private Block		block;
	private Team		team;
	private boolean		taken;
	private long		takenTime	= 0;

	public Flag(Material material) {
		this.type = material;
	}

	public Material getType() {
		return type;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Block getBlock() {
		return block;
	}

	public Team getTeam() {
		return team;
	}

	public boolean isTaken() {
		return taken;
	}

	public void setTaken(boolean taken) {
		this.taken = taken;
	}

	public long getTakenTime() {
		return takenTime;
	}

	public void setTakenTime(long takenTime) {
		this.takenTime = takenTime;
	}

}
