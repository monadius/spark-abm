package org.spark.utils;

import  cern.jet.random.*;
import cern.jet.random.engine.*;

/**
 * Code based on C# Repast random auxiliary class implementation
 * @author maxim
 *
 */
public class Random {

  
      // / <summary> Creates a new random number generator with the specified
      // / seed. This will <b>invalidate</b> any previously created
      // / distributions.
      // / </summary>
      public static synchronized long getSeed()
      {
      	return rngSeed;
      	}

      public static synchronized void setSeed(long value)
      {
              getGenerator(value);
      }

      

      public static final int LINEAR_INTERPOLATION = Empirical.LINEAR_INTERPOLATION;
      public static final int NO_INTERPOLATIONN = Empirical.NO_INTERPOLATION;

      private static long rngSeed;
      private static RandomEngine generator;
      //private static synchronized RandomElement generator;

      public static Beta beta;
      public static  Binomial binomial;
      public static  BreitWigner breitWigner;
      public static  BreitWignerMeanSquare breitWignerMeanSquare;
      public static  ChiSquare chiSquare;
      public static  Empirical empirical;
      public static  EmpiricalWalker empiricalWalker;
      public static  Exponential exponential;
      public static  ExponentialPower exponentialPower;
      public static  Gamma gamma;
      public static  Hyperbolic hyperbolic;
      public static  HyperGeometric hyperGeometric;
      public static  Logarithmic logarithmic;
      public static  NegativeBinomial negativeBinomial;
      public static  Normal normal;
     // public static  Pareto pareto;
      public static  Poisson poisson;
      public static  PoissonSlow poissonSlow;
      public static  StudentT studentT;
      public static  Uniform uniform;
      public static  VonMises vonMises;
      public static  Zeta zeta;

      public static  double geometricPdf(int k, double p)
      {
          return Distributions.geometricPdf(k, p);
      }

      public static synchronized double nextBurr1(double r, int nr)
      {
          return Distributions.nextBurr1(r, nr, generator);
      }

      public static synchronized double nextBurr2(double r, double k, int nr)
      {
          return Distributions.nextBurr2(r, k, nr, generator);
      }

      public static synchronized double nextCauchy()
      {
          return Distributions.nextCauchy(generator);
      }

      public static synchronized double nextErlang(double variance, double mean)
      {
          return Distributions.nextErlang(variance, mean, generator);
      }

      public static synchronized int nextGeometric(double p)
      {
          return Distributions.nextGeometric(p, generator);
      }

      public static synchronized double nextLambda(double l3, double l4)
      {
          return Distributions.nextLambda(l3, l4, generator);
      }

      public static synchronized double nextLaplace()
      {
          return Distributions.nextLaplace(generator);
      }

      public static synchronized double nextLogistic()
      {
          return Distributions.nextLogistic(generator);
      }

      public static synchronized double nextPowLaw(double alpha, double cut)
      {
          return Distributions.nextPowLaw(alpha, cut, generator);
      }

      public static synchronized double nextTriangular()
      {
          return Distributions.nextTriangular(generator);
      }

      public static synchronized double nextWeibull(double alpha, double beta)
      {
          return Distributions.nextWeibull(alpha, beta, generator);
      }

      public static synchronized int nextZipfInt(double z)
      {
          return Distributions.nextZipfInt(z, generator);
      }

      public static synchronized void createZeta(double ro, double pk)
      {
          zeta = new Zeta(ro, pk, generator);
      }

      public static synchronized void createVonMises(double freedom)
      {
          vonMises = new VonMises(freedom, generator);
      }

      /*
      public static synchronized void createPareto(double loc, double shape)
      {
          pareto = new Pareto(loc, shape, generator);
      }*/
      
      public static synchronized void createUniform(double min, double max)
      {
          uniform = new Uniform(min, max, generator);
      }

      public static synchronized void createUniform()
      {
          uniform = new Uniform(generator);
      }

      public static synchronized void createStudentT(double freedom)
      {
          studentT = new StudentT(freedom, generator);
      }

      public static synchronized void createPoissonSlow(double mean)
      {
          poissonSlow = new PoissonSlow(mean, generator);
      }

      public static synchronized void createPoisson(double mean)
      {
          poisson = new Poisson(mean, generator);
      }

      public static synchronized void createNormal(double mean, double standardDeviation)
      {
          normal = new Normal(mean, standardDeviation, generator);
      }

      public static synchronized void createNegativeBinomial(int n, double p)
      {
          negativeBinomial = new NegativeBinomial(n, p, generator);
      }

      public static synchronized void createLogarithmic(double p)
      {
          logarithmic = new Logarithmic(p, generator);
      }

      public static synchronized void createHyperGeometric(int N, int s, int n)
      {
          hyperGeometric = new HyperGeometric(N, s, n, generator);
      }

      public static synchronized void createHyperbolic(double alpha, double beta)
      {
          hyperbolic = new Hyperbolic(alpha, beta, generator);
      }

      public static synchronized void createGamma(double alpha, double lambda)
      {
          gamma = new Gamma(alpha, lambda, generator);
      }

      public static synchronized void createExponentialPower(double tau)
      {
          exponentialPower = new ExponentialPower(tau, generator);
      }

      public static synchronized void createExponential(double lambda)
      {
          exponential = new Exponential(lambda, generator);
      }

      public static synchronized void createEmpiricalWalker(double[] pdf, int interpolationType)
      {
          empiricalWalker = new EmpiricalWalker(pdf, interpolationType, generator);
      }

      public static synchronized void createEmpirical(double[] pdf, int interpolationType)
      {
          empirical = new Empirical(pdf, interpolationType, generator);
      }

      public static synchronized void createChiSquare(double freedom)
      {
          chiSquare = new ChiSquare(freedom, generator);
      }

      public static synchronized void createBreitWignerMeanSquareState(double mean, double gamma, double cut)
      {
          breitWignerMeanSquare = new BreitWignerMeanSquare(mean, gamma, cut, generator);
      }

      public static synchronized void createBreitWigner(double mean, double gamma, double cut)
      {
          breitWigner = new BreitWigner(mean, gamma, cut, generator);
      }

      public static synchronized void createBinomial(int n, double p)
      {
          binomial = new Binomial(n, p, generator);
      }

      public static synchronized void createBeta(double alpha, double beta)
      {
      	Random.beta = new Beta(alpha, beta, generator);
      }

      /// <summary>
      /// Generates a new random number generator using the
      /// the current timestamp as a the seed.
      /// This will <b>invalidate</b> any previously created
      /// distributions.
      /// </summary>
      // public static synchronized RandomElement generateNewSeed()
      public static synchronized RandomEngine generateNewSeed()
      {
          //System.DateTime d = System.DateTime.Now;
          rngSeed = System.currentTimeMillis();
          generator = new cern.jet.random.engine.MersenneTwister((int) rngSeed);
          invalidateDists();
          return generator;
      }

      /// <summary>Gets the current random number generator.</summary>
      //public static synchronized RandomElement getGenerator()
      public static synchronized RandomEngine getGenerator()
      {
          return generator;
      }

      /// <summary>
      /// Creates a new random number generator with the specified
      /// seed and returns this new generator.
      /// This will <b>invalidate</b> any previously created
      /// distributions.
      /// </summary>
      /// 
      /// <param name="seed">the new generator seed</param>
      /// 
      /// <returns> the new generator</returns>
      //public static synchronized RandomElement getGenerator(long seed)
      public static synchronized RandomEngine getGenerator(long seed)
      {
          rngSeed = seed;
          generator = new cern.jet.random.engine.MersenneTwister((int)seed);
          invalidateDists();
          return generator;
      }

      private static synchronized void invalidateDists()
      {
          beta = null;
          binomial = null;
          breitWigner = null;
          breitWignerMeanSquare = null;
          chiSquare = null;
          empirical = null;
          empiricalWalker = null;
          exponential = null;
          exponentialPower = null;
          gamma = null;
          hyperbolic = null;
          hyperGeometric = null;
          logarithmic = null;
          negativeBinomial = null;
          normal = null;
          poisson = null;
          poissonSlow = null;
          studentT = null;
          uniform = null;
          vonMises = null;
          zeta = null;
      }
      
      static
      {
          {            	
              rngSeed = System.currentTimeMillis();
              generator = new cern.jet.random.engine.MersenneTwister((int) rngSeed);
          }
      }
      
      static synchronized public int getUniformIntFromTo ( int low, int high ) {
  		int randNum = Random.uniform.nextIntFromTo( low, high );
  		// System.out.println( "getUniformIntFromTo:  " + randNum );
  		return randNum;
  	}

  	static synchronized public double getNormalDouble ( double mean, double var ) {
  		double randNum =  Random.normal.nextDouble ( mean, var );
  		// System.out.println( "getNormalDouble:  " + randNum );
  		return randNum;
  	}
  	static synchronized public double getUniformDoubleFromTo( double low, double high ) {
  		double randNum = Random.uniform.nextDoubleFromTo( low, high );
  		// System.out.println( "getUniformDoubleFromTo:  " + randNum );
  		return randNum;
  	}

  	// loop until a number between 0 and 1 is generated,
  	// if mean and var are set correctly the loop will rarely happen
  	static synchronized public  double getNormalDoubleProb ( double mean, double var ) {
  		if ( mean < 0 || mean > 1 ) {
  			System.out.println ( "Invalid value set for normal distribution mean" );
  			return -1;
  		}
  		double d = Random.normal.nextDouble ( mean, var );
  		while ( d < 0 || d > 1 )
  			d = Random.normal.nextDouble ( mean, var );

  		// System.out.println( "getNormalDoubleProb:  " + d );

  		return d;
  	}
  	
  	public static synchronized  void resetRNGenerators ( ) {  		
  		// this is required because once you change the seed you invalidate
  		// any previously created distributions
  		setSeed( rngSeed );
  		createUniform();
  		createNormal( 0.0, 1.0 );
  	}
  

}
