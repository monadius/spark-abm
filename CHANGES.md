# SPARK 1.4.0

### SPARK Manager
*[feature]* Save configuration files in the SPARK home directory (not in the current working directory).

### GUI
*[feature]* Save configuration files in the SPARK home directory (not in the current working directory).

*[feature]* OpenGL rendering is supported.

-------------------------

# SPARK 1.3.0

### General
*[feature]* SPARK packages are renamed: `org.sparkabm` for SPARK
and `org.sparklogo` for SPARK-PL. This change makes SPARK 1.3.0 incompatible
with previously compiled SPARK models. Such models should be recompiled
with the new version of SPARK.

### SPARK-PL
*[feature]* JDK 1.8 or higher is required to compile SPARK models. The old `javac.jar` is removed
from SPARK distributions.

*[feature]* SPARK Manager sets the path to required `jar` files automatically. This path
can be changed in options.

### GUI
*[bug fix]* Fixed a missing background color of color selection buttons in
the data layers dialog and in the agent style dialog. 

-------------------------

# SPARK 1.2.003

### SPARK-PL
*[feature]*
`use-primary-random-generator`, `use-secondary-random-generator`

*[bug fix]*
`set-seed` works correctly with `Long` numbers

*[misc]*
DTD for `SparkTypes.xml`

*[bug fix]*
Global variables are reset before each simulation.
Global variables which have the `@parameter` attribute are not reset.
Shared variables of agents are not reset (this issue will be addressed later).
	

### Math
*[misc]*
Two random generators

### GUI
*[bug fix]*
A random seed can be set in the Model Properties dialog using `Long` numbers