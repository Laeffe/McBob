package se.laeffe.mcbob;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class DeathHandler extends EntityListener {

	private Mcbob mcbob;

	public DeathHandler(Mcbob mcbob) {
		this.mcbob = mcbob;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			mcbob.getBattleHandler().playerDied(player);
		}
	}
}
