package org.spark.test.basictests;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.spark.core.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RunAllTests {

	public static SparkModel setup;
	private static URLClassLoader classLoader;
	private static File xmlDocFile;

	private static void setupClassPath(Node node) {
		classLoader = null;

		NamedNodeMap attributes = node.getAttributes();
		Node tmp;

		String path = (tmp = attributes.getNamedItem("path")) != null ? tmp
				.getNodeValue() : null;

		try {
			if (path != null) {
				File dir = xmlDocFile.getAbsoluteFile().getParentFile();
				String path2 = dir.getAbsolutePath().concat(path);

				URI uri = new File(path2).toURI();
				classLoader = new URLClassLoader(new URL[] { uri.toURL() });
			}
		} catch (Exception e) {
			e.printStackTrace();
			classLoader = null;
		}

	}

	public static void main(String[] args) throws Exception {
		ObserverFactory.create(null, "org.spark.core.Observer1", 0);
//		org.spark.core.Observer.init("org.spark.core.ObserverParallel");
//		Observer.getInstance().setExecutionMode(false);

		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		Document doc;
//		xmlDocFile = new File("./tests/MovingAgentsA.xml");
//		xmlDocFile = new File("./tests/MovingAgentsB.xml");
//		xmlDocFile = new File("./tests/MovingAgentsC.xml");
//		xmlDocFile = new File("./tests/CreateDieA.xml");
//		xmlDocFile = new File("./tests/CreateDieB.xml");
//		xmlDocFile = new File("./tests/EvaporationAndDiffusion.xml");
		xmlDocFile = new File("./tests/ToyInfectionModel.xml");

		doc = db.parse(xmlDocFile);
		setupClassPath(doc.getElementsByTagName("classpath").item(0));

		NodeList nodes;

		nodes = doc.getElementsByTagName("setup");
		if (nodes.getLength() != 1)
			throw new Exception("The setup class must be uniquely specified");

		String setupName = nodes.item(0).getTextContent();
		setup = (SparkModel) classLoader.loadClass(setupName).newInstance();
		
		for (int k = 10; k <= 10; k++) {
			Observer.getInstance().reset();
			setup.setup();

			Observer.getInstance().finalizeSetup();

			long startTime = System.currentTimeMillis();

			for (long t = 0; t < 1000; t++) {
				setup.begin(t);
				org.spark.core.Observer.getInstance().processAllAgents(t);
				org.spark.core.Observer.getInstance().processAllDataLayers(t);
				setup.end(t);
				if (t % 100 == 0)
					System.out.println(t);
			}

			System.err.println("k = " + k);
			
			long endTime = System.currentTimeMillis();
			System.err.println(endTime - startTime);

			Observer.getInstance().printStatistics();
		}
	}

}
