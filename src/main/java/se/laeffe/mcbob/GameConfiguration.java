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
		Object property = super.getProperty(prefix+"."+path);
		if(property == null) {
			System.out.println("GameConfiguration.getProperty(), property with prefix not found: "+prefix+"."+path);
			property = super.getProperty(path);
			if(property == null)
				System.out.println("GameConfiguration.getProperty(), property not found: "+path );
		} else {
			System.out.println("GameConfiguration.getProperty(), found: "+prefix+"."+path);
		}
		return property;
	}
}
