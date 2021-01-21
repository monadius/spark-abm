![SPARK logo](logo.jpg)

## Introduction

SPARK, or Simple Platform for Agent-based Representation of Knowledge, is an
agent based modeling (ABM) framework which includes the Java library for implementing
agent based models and the runtime engine for executing these models. SPARK was written
by Alexey Solovyev with additional help from Maxim Mikheev. The project leader is Qi Mi.

The official SPARK website: [http://www.mirmresearch.net/spark/default.asp](http://www.mirmresearch.net/spark/default.asp)

## License

SPARK largely uses the MIT License. For details see the [LICENSE](LICENSE) file.

SPARK contains several third-party libraries. Information about
these libraries and about their licenses can be found in the [spark/Licenses](spark/Licenses) folder.

## How to Run

JDK (Java Development Kit) 1.8 or higher is required to run SPARK. A free and open source
version of JDK can be downloaded at [https://adoptopenjdk.net/releases.html](https://adoptopenjdk.net/releases.html).

To make sure that JDK is properly installed, run the following command in the command line: `javac -version`.
This command should print the version of the installed JDK.

Download the latest SPARK [release](https://github.com/monadius/spark-abm/releases). Unzip the archive into
any folder on your computer. (Note for macOS users: do not unzip the SPARK distribution into a restricted
folder such as Documents, Downloads, or Desktop.) Double click `SPARK_Manager.jar` in a file manager to run
SPARK. If it does not work then run SPARK from the command line:

macOS/Linux:
```bash
cd 'name of SPARK directory'
bin/spark-manager
```

Windows:
```bash
cd 'name of SPARK directory'
bin\spark-manager
```

## Documentation

Documentation can be found at 
[http://www.mirmresearch.net/spark/documentation/documentation.asp](http://www.mirmresearch.net/spark/documentation/documentation.asp)

Note: the documentation is not updated yet and it may contain outdated information.

## Building from Source Code

Clone the repository and run

```bash
./gradlew sparkInstall
```

A compiled version of SPARK with all dependencies will be created in `spark/build/spark`.

Alternatively, the command `./gradlew sparkDist` will create a zip archive in `spark/build/sparkDist`.

## Publications

1. Solovyev, A., Mikheev, M., Zhou, L., Dutta-Moscato, J., Ziraldo, C., An, G., Vodovotz, Y., & Mi, Q. (2010). 
   **SPARK: A Framework for Multi-Scale Agent-Based Biomedical Modeling.** 
   International journal of agent technologies and systems, 2(3), 18–30.
   https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3806198/
   
1. Solovyev, A., Mi, Q., Tzen, Y. T., Brienza, D., & Vodovotz, Y. (2013). 
   **Hybrid equation/agent-based model of ischemia-induced hyperemia and pressure ulcer 
   formation predicts greater propensity to ulcerate in subjects with spinal cord injury.** 
   PLoS computational biology, 9(5), e1003070.
   https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1003070

1. Dutta-Moscato, J., Solovyev, A., Mi, Q., Nishikawa, T., Soto-Gutierrez, A., Fox, I. J., & Vodovotz, Y. (2014).
   **A Multiscale Agent-Based in silico Model of Liver Fibrosis Progression.**
   Frontiers in bioengineering and biotechnology, 2, 18.
   https://www.frontiersin.org/articles/10.3389/fbioe.2014.00018/full

1. Ziraldo, C., Solovyev, A., Allegretti, A., Krishnan, S., Henzel, M. K., Sowa, G. A., 
   Brienza, D., An, G., Mi, Q., & Vodovotz, Y. (2015). 
   **A Computational, Tissue-Realistic Model of Pressure Ulcer Formation in Individuals with Spinal Cord Injury.** 
   PLoS computational biology, 11(6), e1004309.
   https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004309

## Feedback and Troubleshooting

Please open issues at [https://github.com/monadius/spark-abm/issues](https://github.com/monadius/spark-abm/issues)