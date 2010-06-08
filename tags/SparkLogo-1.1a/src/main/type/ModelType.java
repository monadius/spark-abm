package main.type;


import java.io.File;
import java.util.ArrayList;

import parser.SparkLogoParser;
import parser.Symbol;
import parser.SymbolList;
import parser.sym;
import parser.xml.ModelFileWriter;
import main.Id;
import main.SparkModel;
import main.Variable;
import main.annotation.InterfaceAnnotation;
import main.annotation.ModelAnnotation;
import main.annotation.ObserverAnnotation;
import main.annotation.TickAnnotation;

public class ModelType extends Type {
	/* Source code of space declaration */
	protected SymbolList spaceDeclarationSource;
	
	/* Annotations associated with the model type */
	protected final ArrayList<ModelAnnotation> annotations;

	/**
	 * Creates a model type.
	 * There can be several models in the same project.
	 * @param id
	 * @param parent
	 */
	protected ModelType(Id id, Type parent) {
		// TODO: better implementation, check wrong parent types
		super(id, new UnresolvedType(new Id("$model")));
		annotations = new ArrayList<ModelAnnotation>();
	}
	
	
	/**
	 * Adds an annotation to the model type
	 * @param annotation
	 * @throws Exception
	 */
	public void addAnnotation(InterfaceAnnotation annotation) throws Exception {
		if (!(annotation instanceof ModelAnnotation))
			throw new Exception("Annotation " + annotation.getId() + " cannot be associated with " + this);
		
		ModelAnnotation a = (ModelAnnotation) annotation;
		annotations.add(a);
		a.associateModelType(this);
	}
	
	
	/**
	 * Returns annotations associated with the model type
	 * @return
	 */
	public ArrayList<ModelAnnotation> getAnnotations() {
		return annotations;
	}
	
	
	
	/**
	 * Reads the source code of space declaration
	 */
	public void beginSpaceDeclaration(String spaceName) throws Exception {
		if (spaceDeclarationSource != null)
			throw new Exception("Space is already defined");
		
		spaceDeclarationSource = new SymbolList();
		// TODO: verify space name
		// Now it is impossible since parent type (ABMModel) is not resolved here

		spaceDeclarationSource.add(
				new Symbol(spaceName, sym.IDENTIFIER, "$" + spaceName, -1, -1)
				);
	}
	
	
	/**
	 * Adds a symbol to the space declaration source code
	 * @param s
	 */
	public void addSpaceDeclarationSymbol(Symbol s) {
		spaceDeclarationSource.add(s);
	}
	
	
	/**
	 * Overrides existing method in order to prevent creation of the real
	 * constructor for the model type
	 */
	@Override
	protected void createConstructor() throws Exception {
	}

	/**
	 * This method also creates default initialization for grids (globally defined)
	 */
	@Override
	protected void createFieldInitializationMethod() throws Exception {
		for (Variable field : fieldList) {
			// We are interested only in global fields
			if (!field.global)
				continue;
			
			Type gridType = SparkModel.getInstance().getType(new Id("grid"));
			Type grid3dType = SparkModel.getInstance().getType(new Id("grid3d"));
			Type parallelGridType = SparkModel.getInstance().getType(new Id("parallel-grid"));
			
			// We are interested only in grids
			if (field.type != gridType &&
					field.type != grid3dType &&
					field.type != parallelGridType)
				continue;
			
			// We are interested only in uninitialized fields
			if (field.initializationSource != null && field.initializationSource.size() > 0)
				continue;

			String translation;
			
			if (field.type == gridType)
				translation = "create-grid \"@@id\" @x-size @y-size";
			else if (field.type == grid3dType)
				translation = "create-grid3d \"@@id\" @x-size @y-size @z-size";
			else
				translation = "create-parallel-grid \"@@id\" @x-size @y-size";
			
			translation = translation.replaceAll("@@id", field.id.name);
			
			// TODO: grid annotation specifies x-size and y-size
			
			String xSizeTranslation = "space-xsize";
			String ySizeTranslation = "space-ysize";
			String zSizeTranslation = "space-zsize";
			
			translation = translation.replaceAll("@x-size", xSizeTranslation);
			translation = translation.replaceAll("@y-size", ySizeTranslation);
			translation = translation.replaceAll("@z-size", zSizeTranslation);

			field.initializationSource = SparkLogoParser.stringToSymbols(translation);
			
		}
		
		super.createFieldInitializationMethod();
	}
	
	/**
	 * Parses all methods in the model-specific way,
	 * i.e., setup, end, begin methods are created and
	 * setup-specific routines (e.g., space creation) are added
	 */
	@Override
	public void parseMethods() throws Exception {
		if (spaceDeclarationSource == null)
			throw new Exception("Space is not declared");
		
		// void setup() method
		Method setup = getMethod(new Id("setup"), false);
		if (setup == null) {
			setup = new Method(new Id("setup"));
			addMethod(setup);
		}
		
		// Insert space initialization code
		for (int i = 0; i < spaceDeclarationSource.size(); i++) {
			setup.sourceCode.insert(spaceDeclarationSource.get(i), i);
		}

		
		// boolean end(long tick) method
		createEndMethod();
		
		// boolean begin(long tick) method
		createBeginMethod();
		
		
		
		// Call general parse routine
		super.parseMethods();
	}


	/**
	 * Creates the end method
	 * @throws Exception
	 */
	private void createEndMethod() throws Exception {
		Method end = getMethod(new Id("end-step"), false);
		if (end == null) {
			end = new Method(new Id("end"));
			addMethod(end);
		}
		
		end.id = new Id("end");
		if (end.arguments.size() >= 2)
			throw new Exception("Wrong number of argument for the end-step method");
		
		// TODO: verify first argument type
		Id tickId = new Id("tick");
		if (end.arguments.size() == 1) {
			tickId = end.arguments.get(0).id;
		}
		
		// Argument should be of type 'long'
		end.arguments.clear();
		end.addArgument( new Variable(tickId, SparkModel.getInstance().getType(new Id("$long"))) );

		
		// Check return type: should be boolean or void
		if (end.getReturnType() == null || 
				end.getReturnType() == SparkModel.getInstance().getType(new Id("boolean"))) {
		
			// If the type is void then set it to boolean
			// TODO: better implementation is required: 
			// deal with all exit commands, etc.
			if (end.getReturnType() == null) {
				int n = end.sourceCode.size();
				end.sourceCode.insert(new Symbol("return", sym.IDENTIFIER, "return", -1, -1), n);
				end.sourceCode.insert(new Symbol("false", sym.IDENTIFIER, "false", -1, -1), n + 1);
			}

			end.setReturnType(SparkModel.getInstance().getType(new Id("boolean")));
		}
		else {
			throw new Exception("end-step method should be of type boolean (or void)");
		}

	}

	
	
	/**
	 * Creates the begin method
	 * @throws Exception
	 */
	private void createBeginMethod() throws Exception {
		Method begin = getMethod(new Id("begin-step"), false);
		if (begin == null) {
			begin = new Method(new Id("begin"));
			addMethod(begin);
		}
		
		begin.id = new Id("begin");
		if (begin.arguments.size() >= 2)
			throw new Exception("Wrong number of argument for the begin-step method");
		
		// TODO: verify first argument type
		Id tickId = new Id("tick");
		if (begin.arguments.size() == 1) {
			tickId = begin.arguments.get(0).id;
		}
		
		// Argument should be of type 'long'
		begin.arguments.clear();
		begin.addArgument( new Variable(tickId, SparkModel.getInstance().getType(new Id("$long"))) );
		
		// TODO: check return type: should be boolean or void
		begin.setReturnType(SparkModel.getInstance().getType(new Id("boolean")));
		
		// TODO: only if the type is void
		// TODO: better implementation is required
		int n = begin.sourceCode.size();
		begin.sourceCode.insert(new Symbol("return", sym.IDENTIFIER, "return", -1, -1), n);
		begin.sourceCode.insert(new Symbol("false", sym.IDENTIFIER, "false", -1, -1), n + 1);
	}
	
	
	/**
	 * Creates the xml model description file
	 * @param file
	 * @param agents
	 */
	public void createXMLModelFile(File file, ArrayList<AgentType> agents) throws Exception {
		String packageName = SparkModel.getInstance().getName().toJavaName();
		
		// Process data layers without attributes
outer:
		for (Variable field : fieldList) {
			if (!field.global)
				continue;
			
			Type dataLayerType = SparkModel.getInstance().getType(new Id("$DataLayer"));
			
			if (!field.type.instanceOf(dataLayerType))
				continue;

			for (InterfaceAnnotation annotation : field.annotations) {
				if (annotation.getId().equals("datalayer"))
					continue outer;
			}
		
			SymbolList list = SparkLogoParser.stringToSymbols("datalayer color = \"0;0;1\" min = 0 max = 1");
			InterfaceAnnotation dataLayerAnnotation = InterfaceAnnotation.beginParsing(list.next()); 
			field.addAnnotation(dataLayerAnnotation);
			
			while (true) {
				Symbol s = list.next();
				if (s.id == sym.END || s.id == sym.EOF)
					break;
				
				Symbol id = s;
				// Skip '='
				list.next();
				Symbol val = list.next();
				
				dataLayerAnnotation.processElement(id, val);
			}
			
			dataLayerAnnotation.setAutoGenerated(true);
		}


		// TODO: is there a more elegant solution?
		ArrayList<InterfaceAnnotation> variables = new ArrayList<InterfaceAnnotation>();
		ArrayList<InterfaceAnnotation> parameters = new ArrayList<InterfaceAnnotation>();
		ArrayList<InterfaceAnnotation> dataset = new ArrayList<InterfaceAnnotation>();
		ArrayList<InterfaceAnnotation> datalayers = new ArrayList<InterfaceAnnotation>();
		ArrayList<InterfaceAnnotation> charts = new ArrayList<InterfaceAnnotation>();
		ArrayList<InterfaceAnnotation> methods = new ArrayList<InterfaceAnnotation>();
		
		for (Variable field : fieldList) {
			for (InterfaceAnnotation annotation : field.annotations) {
				String str = annotation.toString();
				if (str.startsWith("<variable"))
					variables.add(annotation);
				else if (str.startsWith("<parameter"))
					parameters.add(annotation);
				else if (str.startsWith("<item"))
					dataset.add(annotation);
				else if (str.startsWith("<datalayer"))
					datalayers.add(annotation);
				else if (str.startsWith("<chart"))
					charts.add(annotation);
			}
		}
		
		for (Method method : methodList) {
			for (InterfaceAnnotation annotation : method.annotations) {
				String str = annotation.toString();
				
				if (str.startsWith("<method"))
					methods.add(annotation);
			}
		}
		
		// Length of a tick
		String tickTime = null;
		ObserverAnnotation observer = null;
		
		// TODO: more general approach is required
		for (ModelAnnotation ann : annotations) {
			if (ann instanceof TickAnnotation) {
				tickTime = ((TickAnnotation) ann).getTime();
				continue;
			}
			
			if (ann instanceof ObserverAnnotation) {
				observer = (ObserverAnnotation) ann;
			}
		}
		
		ModelFileWriter modelWriter = new ModelFileWriter(file, tickTime);
		
		modelWriter.addSetupInformation(".", packageName + "." + id.toJavaName(), observer);
		// TODO: how to accept other files?
		modelWriter.addAboutInformation("readme.txt");
		modelWriter.addAgents(packageName, agents);
		modelWriter.addMainFrame();
		modelWriter.addAnnotations(variables, "variables", true, null);
		modelWriter.addDataLayers(datalayers);
		modelWriter.addAnnotations(parameters, "parameterframe", false, "Parameters");
		modelWriter.addCharts(charts);
		modelWriter.addMethods(methods);
//		modelWriter.addAnnotations(methods, "methods", true, null);
		modelWriter.addAnnotations(dataset, "dataset", false, "Data");
		
		modelWriter.save();
	}

	
}
