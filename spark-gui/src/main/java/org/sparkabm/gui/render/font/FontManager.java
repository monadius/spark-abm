package org.sparkabm.gui.render.font;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparkabm.utils.FileUtils;

/**
 * Loads and manages all fonts
 */
public class FontManager {
	// Log
	private static final Logger log = LogManager.getLogger();
	
	// Font directories
	private final ArrayList<File> dirs;
	
	// Default font name
	private String defaultFontName;
	
	// A list of all fonts
	private final ArrayList<BitmapFont> fonts;
	
	// A table of all fonts
	private final Hashtable<String, BitmapFont> fontTable;
	
	/**
	 * Constructor
	 */
	public FontManager() {
		dirs = new ArrayList<File>();
		fonts = new ArrayList<BitmapFont>();
		fontTable = new Hashtable<String, BitmapFont>();
		defaultFontName = null;
	}
	
	/**
	 * Removes all loaded fonts
	 */
	public void clear() {
		dirs.clear();
		fonts.clear();
		fontTable.clear();
		defaultFontName = null;
	}
	
	/**
	 * Loads all fonts from the given directory
	 */
	public void load(File dir) {
		if (dirs.contains(dir)) {
			log.warn("Directory is already loaded: " + dir);
			return;
		}

		ArrayList<File> files = FileUtils.findAllFiles(dir, "fnt", false);
		
		if (files == null || files.size() == 0) {
			log.warn("No font files in the directory: " + dir);
			dirs.add(dir);
			return;
		}
	
		// Load all font files
		for (File file : files) {
			try {
				BitmapFont font = new BitmapFont(file);
				String name = file.getName();
				if (fontTable.containsKey(name)) {
					log.warn("Two fonts with the same name: " + name);
				}
				
				fonts.add(font);
				fontTable.put(name, font);
			}
			catch (Exception e) {
				log.error(e);
			}
		}
		
		dirs.add(dir);
	}
	
	/**
	 * Returns the font with the given name
	 */
	public BitmapFont getFont(String name) {
		if (name == null)
			return null;
		return fontTable.get(name);
	}
	
	/**
	 * Returns names of all loaded fonts
	 */
	public String[] getFontNames() {
		String[] names = new String[fontTable.size()];
		return fontTable.keySet().toArray(names);
	}
	
	/**
	 * Returns the name of the default font
	 */
	public String getDefaultFontName() {
		return defaultFontName;
	}
	
	/**
	 * Sets the name of the default font
	 */
	public void setDefaultFontName(String name) {
		this.defaultFontName = name;
	}
	
	/**
	 * Returns all font directories
	 * @return
	 */
	public File[] getFontDirectories() {
		File[] files = new File[dirs.size()];
		return dirs.toArray(files);
	}
}
