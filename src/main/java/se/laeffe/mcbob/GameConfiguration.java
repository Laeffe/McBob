package se.laeffe.mcbob;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

public class GameConfiguration {

	private static final String			GLOBAL	= "global.";
	private static final String			GAMES	= "games.";
	private final String				prefix;
	private final ConfigurationSection	configurationSection;

	protected GameConfiguration(String prefix, ConfigurationSection configurationSection) {
		this.configurationSection = configurationSection;
		this.prefix = prefix;
	}

	public Object getProperty(String path) {
		Object property = getFromConfigurationSection(GAMES + prefix + "." + path);
		if(property != null)
			return property;
		return getFromConfigurationSection(GLOBAL + path);
	}

	private Object getFromConfigurationSection(String string) {
		return configurationSection.get(string);
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

	public long getLong(String path, long defaultValue) {
		Object property = getProperty(path);
		if(property != null) {
			try {
				return Long.parseLong(String.valueOf(property));
			} catch(NumberFormatException e) {}
		}
		return defaultValue;
	}

	public Map<String, Object> getMap(String string, boolean deep) {
		ConfigurationSection cs = configurationSection.getConfigurationSection(GAMES + string);
		if(cs == null)
			cs = configurationSection.getConfigurationSection(GLOBAL + string);

		if(cs == null)
			return null;

		return cs.getValues(deep);
	}
}
