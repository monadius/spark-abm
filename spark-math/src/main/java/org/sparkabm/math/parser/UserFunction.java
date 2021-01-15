package org.sparkabm.math.parser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Solovyev Alexey Date: 01.11.2005 Time: 23:04:57
 */

/**
 * A user defined function
 */
public class UserFunction {
    // The corresponding parser tree
    private final ParserTree tree;
    // Commands
    private final int[] commands;
    // Constants
    private final double[] constants;
    // Values of variables
    private final double[] vars;
    // Stack
    private final double[] stack;
    private int sp;

    // All possible commands
    static final int ADD = 1;
    static final int SUB = 2;
    static final int MUL = 3;
    static final int DIV = 4;
    static final int NEG = 5;
    static final int POWER = 6;
    static final int CLOAD = 10;
    static final int LOAD = 11;
    static final int INVOKE = 20;

    // Built-in constants
    private static final int SIN = 21;
    private static final int COS = 22;
    private static final int EXP = 23;
    private static final int TAN = 24;
    private static final int LOG = 25;
    private static final int ATAN = 26;
    private static final int SQRT = 27;
    private static final int ACOS = 28;
    private static final int ASIN = 29;

    // Collection of built-in functions
    private final static HashMap<String, Integer> builtinFunctions;
    // Collection of user-defined functions
    private final static HashMap<String, Integer> userFunctions;
    // List of user-defined functions
    private final static ArrayList<UserFunction> userFunctionsList;
    // A tree builder
    private final static ParserTreeBuilder treeBuilder;

    /**
     * Static initializer
     */
    static {
        treeBuilder = new ParserTreeBuilder();
        userFunctionsList = new ArrayList<UserFunction>();
        userFunctions = new HashMap<String, Integer>();
        builtinFunctions = new HashMap<String, Integer>();

        // Initialize built-in functions
        builtinFunctions.put("sin", new Integer(SIN));
        builtinFunctions.put("cos", new Integer(COS));
        builtinFunctions.put("tan", new Integer(TAN));
        builtinFunctions.put("exp", new Integer(EXP));
        builtinFunctions.put("log", new Integer(LOG));
        builtinFunctions.put("ln", new Integer(LOG));
        builtinFunctions.put("atan", new Integer(ATAN));
        builtinFunctions.put("atn", new Integer(ATAN));
        builtinFunctions.put("arctan", new Integer(ATAN));
        builtinFunctions.put("asin", new Integer(ASIN));
        builtinFunctions.put("arcsin", new Integer(ASIN));
        builtinFunctions.put("acos", new Integer(ACOS));
        builtinFunctions.put("arccos", new Integer(ACOS));
        builtinFunctions.put("sqrt", new Integer(SQRT));
    }

    /**
     * Private constructor
     */
    private UserFunction(ParserTree tree) throws Exception {
        this.tree = tree;
        ArrayList<Integer> program = tree.compile();

        this.commands = new int[program.size()];
        this.constants = new double[tree.constants.size()];
        this.vars = new double[tree.vars.size()];
        this.stack = new double[tree.maxStack];

        // Fill in commands
        for (int i = 0; i < program.size(); i++)
            commands[i] = program.get(i);

        // Fill in constants
        for (int i = 0; i < constants.length; i++)
            constants[i] = tree.constants.get(i);

        this.sp = 0;
    }

    /**
     * Number of arguments == number of variables
     */
    public int getArgsNumber() {
        return vars.length;
    }

    /**
     * Returns the index of the given variable.
     * Returns -1 if the variable is not defined.
     */
    public int getVarIndex(String varName) {
        return tree.getVar(varName);
    }

    /**
     * Returns names of all variables (arguments)
     */
    public String[] getVarNames() {
        String[] names = new String[vars.length];
        return tree.vars.keySet().toArray(names);
    }

    /**
     * Returns the index of the built-in function or -1 if the function is not a
     * built-in function
     */
    static int isBuiltin(String name) {
        if (builtinFunctions.containsKey(name))
            return builtinFunctions.get(name);

        return -1;
    }

    /**
     * Returns the index of the user function or -1 if the function is not a
     * user function
     *
     * @param name
     * @return
     */
    static int isUserFunction(String name) {
        if (userFunctions.containsKey(name))
            return userFunctions.get(name);

        return -1;
    }

    /**
     * Returns the n-th user function
     */
    static UserFunction getUserFunction(int n) {
        return userFunctionsList.get(n);
    }

    /**
     * Adds the given function into the collection of user functions
     */
    public static void addUserFunction(String name, UserFunction f)
            throws Exception {
        if (userFunctions.containsKey(name))
            throw new Exception("The function " + name + " is already defined");

        userFunctions.put(name, new Integer(userFunctionsList.size()));
        userFunctionsList.add(f);
    }

    /**
     * Parses the given expression and adds the parsed function into the
     * collection of user functions
     */
    public static UserFunction addUserFunction(String name, String str)
            throws Exception {
        UserFunction f = create(str);
        addUserFunction(name, f);

        return f;
    }

    /**
     * Parses the given expression and returns the parsed function
     */
    public static UserFunction create(String str) throws Exception {
        ParserTree tree = treeBuilder.create(str);
        return new UserFunction(tree);
    }

    /**
     * push
     */
    private void push(double v) {
        stack[sp++] = v;
    }

    /**
     * pop
     */
    private double pop() {
        return stack[--sp];
    }

    /**
     * loadConst
     */
    private double loadConst(int n) {
        return constants[n];
    }

    /**
     * load (variable)
     */
    private double load(int n) {
        return vars[n];
    }

    /**
     * invoke (user function)
     */
    private double invoke(int n) throws Exception {
        UserFunction f = getUserFunction(n);
        int nArgs = f.getArgsNumber();

        for (int i = nArgs - 1; i >= 0; i--)
            f.vars[i] = stack[--sp];

        return f.evaluate0();
    }


    /**
     * Sets values of variables. The number of values should be exactly the
     * number of variables
     */
    public void setVars(double[] values) throws Exception {
        int n = vars.length;

        if (values == null) {
            if (n > 0)
                throw new Exception("Incorrect number of values");
        } else if (values.length != n)
            throw new Exception("Incorrect number of values");

        // Copy values
        for (int i = 0; i < n; i++)
            vars[i] = values[i];
    }

    /**
     * Sets values of variables. The number of values should be exactly the
     * number of variables
     */
    public void setVars(ArrayList<Double> values) throws Exception {
        int n = vars.length;

        if (values == null) {
            if (n > 0)
                throw new Exception("Incorrect number of values");
        } else if (values.size() != n)
            throw new Exception("Incorrect number of values");

        // Copy values
        for (int i = 0; i < n; i++)
            vars[i] = values.get(i);
    }

    /**
     * Sets values of variables and evaluates the function
     */
    public double evaluate(double... values) throws Exception {
        setVars(values);
        return evaluate0();
    }

    /**
     * Sets values of variables and evaluates the function
     */
    public double evaluate(ArrayList<Double> values) throws Exception {
        setVars(values);
        return evaluate0();
    }

    /**
     * Evaluates the function at the current values of variables
     */
    private double evaluate0() throws Exception {
        if (sp != 0)
            throw new Exception("Recursion is not implemented");

        int n = commands.length;
        for (int i = 0; i < n; i++) {
            switch (commands[i]) {
                // +
                case ADD:
                    push(pop() + pop());
                    break;
                // -
                case SUB:
                    push(pop() - pop());
                    break;
                // *
                case MUL:
                    push(pop() * pop());
                    break;
                // /
                case DIV:
                    push(pop() / pop());
                    break;
                // -, unary
                case NEG:
                    push(-pop());
                    break;
                // ^
                case POWER:
                    push(Math.pow(pop(), pop()));
                    break;
                // constant
                case CLOAD:
                    push(loadConst(commands[++i]));
                    break;
                // variable
                case LOAD:
                    push(load(commands[++i]));
                    break;
                // function
                case INVOKE:
                    push(invoke(commands[++i]));
                    break;
                // sin
                case SIN:
                    push(Math.sin(pop()));
                    break;
                // cos
                case COS:
                    push(Math.cos(pop()));
                    break;
                // tan
                case TAN:
                    push(Math.tan(pop()));
                    break;
                // atan
                case ATAN:
                    push(Math.atan(pop()));
                    break;
                // acos
                case ACOS:
                    push(Math.acos(pop()));
                    break;
                // asin
                case ASIN:
                    push(Math.asin(pop()));
                    break;
                // exp
                case EXP:
                    push(Math.exp(pop()));
                    break;
                // log
                case LOG:
                    push(Math.log(pop()));
                    break;
                // sqrt
                case SQRT:
                    push(Math.sqrt(pop()));
                    break;
                default:
                    throw new Exception("Bad command: " + commands[i]);
            }
        }

        if (sp != 1)
            throw new Exception("Stack is not empty!");

        return pop();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof UserFunction))
            return false;

        UserFunction f2 = (UserFunction) obj;
        // TODO: more efficient tree comparison is better
        return this.toString().equals(f2.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return tree.toString();
    }
}