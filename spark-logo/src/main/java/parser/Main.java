package parser;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import main.SparkModel;


public class Main {
	public static File[] readFileList(File path, String fname) throws Exception {
		File file = new File(path, fname);
		
		javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(file);

		NodeList fileList = doc.getElementsByTagName("file");
		File[] files = new File[fileList.getLength()];
		
		for (int i = 0; i < fileList.getLength(); i++) {
			fname = fileList.item(i).getTextContent();
			files[i] = new File(path, fname);
		}
		
		return files;
	}
	
	
	public static void translate(File[] files) throws Exception {
		String name = "ToyInfectionModel";

		SparkModel.init(name);
		
		SparkLogoParser parser = new SparkLogoParser(files);
		SparkModel model = parser.read();
		model.parseMethods();
		model.translateToJava(new File("./output"));
	}
	
	
	public static void main(String args[]) throws Exception {
		String name = "ToyInfectionModel";
		SparkModel.init(name);
		
		SparkLogoParser parser = new SparkLogoParser(
				readFileList(new File("../Spark Language/SparkLOGO/"), "files.xml")
				);
		
		SparkModel model = parser.read();
		model.parseMethods();
		model.translateToJava(new File("./output"));
		model.createXMLFiles(new File("./output"));
	}
		
		
		
		
		
}
