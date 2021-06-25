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

- `--sce <scenario> <activity name> <initial behavioral model file> <initial feature model file>` to specify some complex (non-primitive) evolution scenario. Scenario can be: `add_msg`, `add_frag`, `ch_pc_fortify`, `ch_pc_weaken`.
    - `add_msg` to execute Evolution Scenario 2 (at each evolution step, adds 10 messages to an existing behavioral fragment). 20 evolution steps.
    - `add_frag` to execute Evolution Scenario 3 (at each evolution step, add a fragment with a guard condition equal to True. This added fragment contains 10 messages. 20 evolution steps.
    - `ch_pc_fortify` to execute Evolution Scenario 4 - Strengthening (at each evolution step, strengthen the presence condition of a fragment, i.e. adds a new randomly chosen feature to it's propositional formula using a conjunction). 20 evolution steps or until there are no new features to be added, whichever comes first.
    - `ch_pc_weaken`  to execute Evolution Scenario 4 - Weakening (at each evolution step, weakens the presence condition of a fragment, i.e. adds a new randomly chosen feature to it's propositional formula using a disjunction). 20 evolution steps or until there are no new features to be added, whichever comes first.

- `--ch pc <activity name> <fragment name> <new presence condition>` to change the presence condition of a given fragment within a given activity.
- `--add <primitive type>` to execute some adition primitive. Primitive type can be: `frag`, `msg`.
    - `msg <activity name> <fragment name> <source lifeline> <dst lifeline> <message type> <message name> <probability>`
    - `frag <activity name> <fragment name> <sequence diagram name> <presence condition> <num. of messages>`

After the run, the .xml files of the resulting evolutions will be saved. If *add_msg* scenario is chosen, a folder named *evo_10_msg* containing 20 XML files will be created, each file representing an evolution step. In case of *add_frag*, a folder named *evo_add_frag* containg 20 XML files will be created.

In case of *ch_pc_fortify* or *ch_pc_weaken*, a folder named *evo_ch_pc* will be created. If the strengthening evolution is chosen, it will be created a subfolder named *fortify*, while if weakening scenario is chosen, the subfolder will be named *weaken*. In both *strengthening* and *weakening* scenarios, the number of XML files is limited by 20 or by the number of the features present in the feature model, whichever comes first.
