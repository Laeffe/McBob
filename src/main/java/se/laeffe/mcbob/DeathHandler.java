package se.laeffe.mcbob;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

public class DeathHandler {

	private GameInterface game;

	public DeathHandler(GameInterface game) {
		this.game = game;
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			game.getBattleHandler().playerDied(player);
		}
	}
}
