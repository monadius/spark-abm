package org.spark.runtime.external;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.spark.runtime.external.gui.menu.ISparkMenuListener;
import org.spark.runtime.external.gui.menu.SparkMenu;
import org.spark.runtime.external.gui.menu.SparkMenuFactory;
import org.spark.runtime.external.gui.menu.SparkMenuItem;
import org.spark.runtime.external.render.Render;
import org.spark.runtime.external.render.font.FontManager;
import org.spark.utils.XmlDocUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

/**
 * Works with a SPARK config file
 * @author Monad
 *
 */
public class Configuration {
	private static final Logger logger = Logger.getLogger();

	
	/******* Recent projects *******/
	
	/* For recently open projects */
	private final SparkMenu fileMenu;
	
	/* List of all recent projects */
	private final ArrayList<File> recentProjects;
	
	/* Font directory */
	private File fontDirectory;
	
	/* Default font name */
	private String defaultFontName;
	
	/* The number of recent projects */
	private int maxRecentProjects;
	private static final int DEFAULT_MAX_RECENT_PROJECTS = 10;
	
	
	/******* Graphics options ******/
	
	/* Default render type */
	private int renderType;
	
	
	/**
	 * Creates a config file reader
	 * @param fileMenu
	 */
	public Configuration(SparkMenu fileMenu) {
		this.fileMenu = fileMenu;
		this.recentProjects = new ArrayList<File>();
	}
	
	
	/**
	 * Reads a configuration file
	 */
	public void readConfigFile() {
		Document doc;

		maxRecentProjects = DEFAULT_MAX_RECENT_PROJECTS;
		renderType = Render.JAVA_2D_RENDER;
		
		doc = XmlDocUtils.loadXmlFile("spark-config.xml");
		if (doc == null) {
			return;
		}
		
		Node root = doc.getFirstChild();
		if (root == null)
			return;

		Node graphics = XmlDocUtils.getChildByTagName(root, "graphics");
		readGraphicsOptions(graphics);

		Node recentProjects = XmlDocUtils.getChildByTagName(root, "recent-models");
		readRecentProjects(recentProjects);
		
		Node fontNode = XmlDocUtils.getChildByTagName(root, "font");
		readFontParameters(fontNode);
	}

	
	/**
	 * Loads font parameters
	 */
	private void readFontParameters(Node fontNode) {
		String dirName = XmlDocUtils.getValue(fontNode, "directory", null);
		if (dirName != null)
			fontDirectory = new File(dirName);
		
		defaultFontName = XmlDocUtils.getValue(fontNode, "default-name", null);
	}
	

	/**
	 * Loads graphics options
	 * @param graphics
	 */
	private void readGraphicsOptions(Node graphics) {
		int type = XmlDocUtils.getIntegerValue(graphics, "render", Render.JAVA_2D_RENDER);
		setRenderType(type);
	}
	
	
	/**
	 * Loads recent projects
	 * @param recentProjects
	 */
	private void readRecentProjects(Node recentProjects) {
		maxRecentProjects = XmlDocUtils.getIntegerValue(recentProjects, "max", DEFAULT_MAX_RECENT_PROJECTS);
		
		ArrayList<Node> models = XmlDocUtils.getChildrenByTagName(recentProjects, "file");
		for (Node model : models) {
			String path = model.getTextContent();
			if (path == null || path.equals(""))
				continue;
			
			addRecentProject(new File(path));
		}
	}
	
	

	/**
	 * Saves a configuration file
	 */
	public void saveConfigFile() throws Exception {
		PrintStream out = new PrintStream(new File("spark-config.xml"));

		out.println("<config>");

		// Graphics
		out.println("\t<graphics render = \"" + renderType + "\"/>");
		
		// Font
		out.print("\t<font");
		if (fontDirectory != null) {
			out.print(" directory = \"" + fontDirectory.getPath() + "\"");
		}
		
		if (defaultFontName != null) {
			out.print(" default-name = \"" + defaultFontName + "\"");
		}
		
		out.println("/>");
		
		// Recent projects
		out.print("\t<recent-models max = \"");
		out.print(maxRecentProjects);
		out.print("\">");
		for (int i = recentProjects.size() - 1; i >= 0; i--) {
			out.print("\t\t<file>");
			out.print(recentProjects.get(i).getPath());
			out.print("</file>");
			out.println();
		}
		out.println("\t</recent-models>");

		out.println("</config>");
		out.close();
	}
	
	
	
	/**
	 * Returns default render type
	 * @return
	 */
	public int getRenderType() {
		return renderType;
	}
	
	
	/**
	 * Sets default render type
	 * @param type
	 */
	public void setRenderType(int type) {
		if (type != Render.JAVA_2D_RENDER && type != Render.JOGL_RENDER)
			type = Render.JAVA_2D_RENDER;
		
		renderType = type;
	}
	
	
	/**
	 * Loads parameters of the given font manager
	 */
	public void loadFontManager(FontManager fontManager) {
		fontManager.clear();
		fontManager.setDefaultFontName(defaultFontName);
		if (fontDirectory != null)
			fontManager.load(fontDirectory);
	}
	
	
	/**
	 * Saves parameters of the given font manager
	 */
	public void saveFontManager(FontManager fontManager) {
		defaultFontName = fontManager.getDefaultFontName();
		// TODO: save all directories
		File[] dirs = fontManager.getFontDirectories();
		if (dirs.length > 0)
			fontDirectory = dirs[0];
	}
	
	
	/**
	 * Returns the max number of recent projects
	 * @return
	 */
	public int getMaxRecentProjects() {
		return maxRecentProjects;
	}
	
	
	/**
	 * Sets the max number of recent projects
	 * @param max
	 */
	public void setMaxRecentProjects(int max) {
		if (max < 1)
			max = 1;
		else if (max > 100)
			max = 100;
		
		maxRecentProjects = max;
		if (maxRecentProjects < recentProjects.size()) {
			for (int i = recentProjects.size() - 1; i >= maxRecentProjects; i--)
				recentProjects.remove(i);
			
			updateRecentMenu();
		}
	}

	
	/**
	 * Adds a project to the recent projects list
	 * 
	 * @param file
	 */
	public void addRecentProject(File file) {
		if (!file.exists())
			return;

		// Check duplicates
		for (int i = 0; i < recentProjects.size(); i++) {
			File f = recentProjects.get(i);
			if (f.equals(file)) {
				if (i == 0)
					return;

				// Move this project to the top
				recentProjects.remove(i);
				recentProjects.add(0, f);
				updateRecentMenu();

				return;
			}
		}

		// Add new project to the top
		recentProjects.add(0, file);

		// Remove old 'recent' projects
		if (recentProjects.size() > maxRecentProjects) {
			int n = recentProjects.size() - maxRecentProjects;
			for (int i = 0; i < n; i++) {
				// Index should be the same because elements are automatically shifted
				recentProjects.remove(maxRecentProjects);
			}
		}

		updateRecentMenu();
	}
	

	/**
	 * Updates menu of recent projects
	 */
	private void updateRecentMenu() {
		if (fileMenu == null)
			return;
		
		// Remove all recent projects from the menu
		fileMenu.removeGroup(100);

		// Insert all current components
		for (int i = 0; i < recentProjects.size(); i++) {
			final File project = recentProjects.get(i);
			String name = project.getName();
			
			SparkMenuItem item = SparkMenuFactory.getFactory().createItem(name, 100);
			item.setActionListener(new ISparkMenuListener() {
				
				public void onClick(SparkMenuItem item) {
					Coordinator c = Coordinator.getInstance();
					if (c == null)
						return;
					
					try {
						c.loadModel(project);
						c.startLoadedModel(Long.MAX_VALUE, true);
					}
					catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				}
			});
			
			fileMenu.addItem(item);
		}
	}	
}
