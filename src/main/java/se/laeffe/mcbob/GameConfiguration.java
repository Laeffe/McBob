package se.laeffe.mcbob;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.util.config.ConfigurationNode;

public class GameConfiguration extends ConfigurationNode {

	private final String prefix;

	protected GameConfiguration(String prefix, ConfigurationNode originNode) {
		super(new HashMap<String, Object>());
		Map<String, Object> all = originNode.getAll();
		for(Entry<String, Object> e : all.entrySet()) {
			setProperty(e.getKey(), e.getValue());
		}
		this.prefix = prefix;
	}

	@Override
	public Object getProperty(String path) {
		Object property = superGetProperty("worlds."+prefix+"."+path);
		if(property != null)
			return property;
		return superGetProperty("global."+path);
	}

	private Object superGetProperty(String string) {
		Object property = super.getProperty(string);
		if(property == null)
			System.out.println("GameConfiguration.superGetProperty(), didn't find: "+string);
		else
			System.out.println("GameConfiguration.superGetProperty(), found: "+string);
		return property;
	}
}
