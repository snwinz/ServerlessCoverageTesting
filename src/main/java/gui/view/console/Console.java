package gui.view.console;

import gui.controller.PersistenceUtilities;
import gui.view.console.controller.ConsoleController;
import logic.model.NodeModel;
import logic.mutation.MutationExecutor;
import org.apache.commons.cli.*;
import shared.model.NodeType;

import java.nio.file.Path;
import java.util.List;

public class Console {

    private final MutationExecutor executor;

    public Console(MutationExecutor executor, ConsoleController controller) {
        this.executor = executor;
        this.controller = controller;
    }


    private final ConsoleController controller;


    private final String TESTSUITE_OPTION = "t";
    private final String MUTATION_OPTION = "m";
    private final String OUTPUT_OPTION = "o";
    private final String GRAPH_OPTION = "g";
    private final String RESET_FUNCTION_OPTION = "rf";
    private final String REGION_OPTION = "re";
    private final String START_NUMBER_OPTION = "s";

    private final String END_NUMBER_OPTION = "e";

    public void handleInput(String[] args) {
        Options options = getOptions();


        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (areAllArgumentsAvailable(cmd)) {
                String testsuitePath = cmd.getOptionValue(TESTSUITE_OPTION);
                String mutationPath = cmd.getOptionValue(MUTATION_OPTION);
                String outputPath = cmd.getOptionValue(OUTPUT_OPTION);
                String graphPath = cmd.getOptionValue(GRAPH_OPTION);
                String resetFunction = cmd.getOptionValue(RESET_FUNCTION_OPTION);
                String region = cmd.getOptionValue(REGION_OPTION);
                Number startMutant = (Number) cmd.getParsedOptionValue(START_NUMBER_OPTION);
                Number endMutant = (Number) cmd.getParsedOptionValue(END_NUMBER_OPTION);
                MutationExecutor executor = new MutationExecutor();

                executor.setMutants(Path.of(mutationPath));
                executor.setTestSuits(Path.of(testsuitePath));
                List<String> allFunctions;
                var graph = PersistenceUtilities.loadLogicGraph(graphPath);
                if (graph.isPresent()) {
                    var logicGraph = graph.get();
                    var nodes = logicGraph.getNodes();
                    if (nodes == null || nodes.size() == 0) {
                        throw new IllegalStateException("no nodes available");
                    }
                    allFunctions = nodes.stream().filter(node -> NodeType.FUNCTION.equals(node.getType())).map(NodeModel::getNameOfNode).toList();
                } else {
                    throw new IllegalStateException("functions available");
                }
                executor.startMutations(allFunctions, startMutant.intValue()
                        , endMutant.intValue(), region, resetFunction, outputPath);
            }
        } catch (ParseException pe) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters needed for mutation run", options);
        }
    }

    private boolean areAllArgumentsAvailable(CommandLine cmd) {
        return cmd.hasOption(TESTSUITE_OPTION) && cmd.hasOption(MUTATION_OPTION) && cmd.hasOption(OUTPUT_OPTION)
                && cmd.hasOption(GRAPH_OPTION) && cmd.hasOption(RESET_FUNCTION_OPTION) && cmd.hasOption(REGION_OPTION)
                && cmd.hasOption(START_NUMBER_OPTION) && cmd.hasOption(END_NUMBER_OPTION);
    }

    private Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder(TESTSUITE_OPTION)
                .longOpt("testcaseFolder")
                .hasArg(true)
                .desc("path to testcases folder")
                .required(false)
                .build());
        options.addOption(Option.builder(MUTATION_OPTION)
                .longOpt("mutantFolder")
                .hasArg(true)
                .desc("path to mutants folder")
                .required(false)
                .build());
        options.addOption(Option.builder(OUTPUT_OPTION)
                .longOpt("output")
                .hasArg(true)
                .desc("output folder")
                .required(false)
                .build());
        options.addOption(Option.builder(GRAPH_OPTION)
                .longOpt("graphFile")
                .hasArg(true)
                .desc("path to graph file")
                .required(false)
                .build());
        options.addOption(Option.builder(RESET_FUNCTION_OPTION)
                .longOpt("resetFunction")
                .hasArg(true)
                .desc("name of reset function")
                .required(false)
                .build());
        options.addOption(Option.builder(REGION_OPTION)
                .longOpt("region")
                .hasArg(true)
                .desc("region of cloud")
                .required(true)
                .build());
        options.addOption(Option.builder(START_NUMBER_OPTION)
                .longOpt("startMutant")
                .hasArg(true)
                .desc("number of first mutant")
                .required(true)
                .type(Number.class)
                .build());
        options.addOption(Option.builder(END_NUMBER_OPTION)
                .longOpt("endMutant")
                .hasArg(true)
                .desc("number of last mutant")
                .required(true)
                .type(Number.class)
                .build());
        return options;
    }
}