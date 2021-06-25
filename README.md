# SPL Generator
Software Product Line Generator

SPL Generator is the tool used to generate the behavioral models used by the [ReAnaE](https://github.com/SPLMC/reana-spl/tree/reanaE) tool. 

## Building

Some required dependencies are in the `libs` folder:

- org.sat4j.core.jar: [Sat4j](https://www.sat4j.org/index.php) is a java library for solving boolean satisfaction and optimization problems. It can solve SAT, MAXSAT, Pseudo-Boolean,
Minimally Unsatisfiable Subset (MUS) problems.
- splar.jar: [Splar](https://code.google.com/archive/p/splar/) A library that offers SAT and BDD-based components to reason on and configure feature models.
SPLAR is the library that supports SPLOT (www.splot-research.org), a set of Software Product Lines Online Tools.

In addition to these dependencies, it's also necessary to have the [PARAM](https://github.com/SPLMC/param) tool available in the PATH.

## Running

The tool accepts a number of command-line arguments which represent the evolution scenarios presented in the article. Evolution primitives can also be run separately.

- `--analysis-strategy` (defaults to *FEATURE_FAMILY*): the analysis strategy to be used. Can be one of:
    FEATURE_FAMILY | FEATURE_PRODUCT | FAMILY | FAMILY_PRODUCT | PRODUCT.
- `--feature-model` (defaults to _fm.txt_): a text file with the feature model for the SPL to be analyzed represented in
    Conjunctive Normal Form (CNF) using Java logical operators. This representation can be obtained
    from a feature diagram using FeatureIDE's _Export to CNF_ functionality.
- `--uml-models` (defaults to _modeling.xml_): an XML file containing the UML behavioral models (Activity and Sequence Diagrams)
    to be analyzed. Currently the only accepted format is the one used by the MagicDraw tool.
- `--param-path` (defaults to _/opt/param-2-3-64_): the directory of the parametric model checker (PARAM or Prism) installation.
- `--configurations-file` (defaults to _configurations.txt_): path to a file with a comma-separated list of
    features per line, each corresponding to a configuration for which the reliability is wanted.
- `--configuration`: alternatively, it is possible to specify a single configuration inline. Overrides `--configurations-file`.
- `--all-configurations`: causes the tool to dump all possible configurations and corresponding reliabilities.
    Overrides `--configuration` and `--configurations-file`.
- `--concurrency-strategy` (defaults to _PARALLEL_): Run parallelizable computations concurrently (PARALLEL) or sequentially (SEQUENTIAL).
- `--pruning-strategy` (defaults to _FM_): The strategy that should be used for pruning invalid configurations
    during partial evaluations. Can be one of: FM (whole feature model); NONE (no pruning).
- `--stats`: Prints profiling statistics such as wall-clock time and used memory.
- `--suppress-report`: Suppress reliabilities report for all evaluated configurations. Useful when analyzing an SPL
    with a large configuration space.


After the run, if the applied strategy was the feature-family-based one, an Algebraic Decision Diagram (ADD)
representing the possible reliabilities for the SPL is dumped to a DOT file named _family-reliability.dot_.
