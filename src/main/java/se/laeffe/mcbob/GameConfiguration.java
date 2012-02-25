package se.laeffe.mcbob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

public class GameConfiguration {

	private static final String GLOBAL = "global.";
	private static final String GAMES = "games.";
	private final String prefix;
	private final ConfigurationSection configurationSection;

	protected GameConfiguration(String prefix, ConfigurationSection configurationSection) {
		this.configurationSection = configurationSection;
		this.prefix = prefix;
	}

	public Object getProperty(String path) {
		Object property = getFromConfigurationSection(GAMES+prefix+"."+path);
		if(property != null)
			return property;
		return getFromConfigurationSection(GLOBAL+path);
	}

	private Object getFromConfigurationSection(String string) {
		Object property = configurationSection.get(string);
		if(property == null)
			System.out.println("GameConfiguration.superGetProperty(), didn't find: "+string);
		else
			System.out.println("GameConfiguration.superGetProperty(), found: "+string);
		return property;
	}

	public int getInt(String path, int defaultValue) {
		Object property = getProperty(path);
		if(property != null) {
			try {
			return Integer.parseInt(String.valueOf(property));
			} catch(NumberFormatException e) {}
		}
		return defaultValue;
	}
	
	public List<Map<String,Object>> getMapList(String path) {
		List<Map<String,Object>> mapList = configurationSection.getMapList(GAMES+path);
		if(mapList == null)
			mapList = configurationSection.getMapList(GLOBAL+path);
		return mapList;
	}
}
