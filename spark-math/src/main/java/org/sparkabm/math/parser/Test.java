package org.sparkabm.math.parser;

/**
 * Author: Solovyev Alexey
 * Date: 01.11.2005
 * Time: 23:44:46
 */
public class Test
{
    public static void main(String[] args) throws Exception
    {
        UserFunction ch = UserFunction.addUserFunction("ch", "(exp(x) + exp(-x))/2");
        UserFunction sh = UserFunction.addUserFunction("sh", "(exp(x) - exp(-x))/2");
//        UserFunction.addUserFunction("chsh", "ch(x) + sh(y)", new String[] {"x", "y"});
        UserFunction ff = UserFunction.addUserFunction("sin2", "x - x^3/(3*2*1) + x^5/(5*4*3*2*1) - x^7/(7*6*5*4*3*2*1)");

//        UserFunction f = UserFunction.create("sin2(x) - sin(x)", new String[] {"x", "y", "z"});

        System.out.println(ff);

        double val = ff.evaluate(3.1415926 / 2);
        System.out.println(val);

        val = ch.evaluate(1.0);
        System.out.println(val);

        val = sh.evaluate(1.0);
        System.out.println(val);
    }
}
