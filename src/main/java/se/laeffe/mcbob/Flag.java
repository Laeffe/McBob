package se.laeffe.mcbob;

import org.bukkit.block.Block;

public class Flag {
	private int type;
	private Block block;
	private Team team;
	private boolean taken;

	public Flag(int type) {
		this.type = type;
	}

	public int getType() {
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

}
