package gui.view.console;

import gui.controller.PersistenceUtilities;
import gui.view.console.controller.ConsoleController;
import logic.model.NodeModel;
import org.apache.commons.cli.*;
import shared.model.NodeType;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Console {


    public Console(ConsoleController controller) {
        this.controller = controller;
    }


    private final ConsoleController controller;


    private final String MODE = "mode";

    private final String TESTSUITE_OPTION = "t";
    private final String MUTATION_OPTION = "m";
    private final String OLD_MUTATION_RESULT_OPTION = "om";
    private final String OUTPUT_OPTION = "o";
    private final String GRAPH_OPTION = "g";
    private final String RESET_FUNCTION_OPTION = "rf";
    private final String AUTH_VALUES_OPTION = "av";
    private final String COVERAGE_METRIC_OPTION = "cm";
    private final String REGION_OPTION = "re";
    private final String START_NUMBER_OPTION = "s";
    private final String END_NUMBER_OPTION = "e";


    public void handleInput(String[] args) {
        Options options = getOptions();


        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (areAllArgumentsAvailableForMutation(cmd)) {
                String testsuitePath = cmd.getOptionValue(TESTSUITE_OPTION);
                String mutationPath = cmd.getOptionValue(MUTATION_OPTION);
                String oldMutationResultPath = cmd.getOptionValue(OLD_MUTATION_RESULT_OPTION);
                String outputPath = cmd.getOptionValue(OUTPUT_OPTION);
                String graphPath = cmd.getOptionValue(GRAPH_OPTION);
                String resetFunction = cmd.getOptionValue(RESET_FUNCTION_OPTION);
                String regions = cmd.getOptionValue(REGION_OPTION);
                Number startMutant = (Number) cmd.getParsedOptionValue(START_NUMBER_OPTION);
                Number endMutant = (Number) cmd.getParsedOptionValue(END_NUMBER_OPTION);

                controller.setMutants(Path.of(mutationPath));
                if (oldMutationResultPath != null) {
                    controller.setOldMutationResults(Path.of(oldMutationResultPath));
                }
                controller.setTestSuits(Path.of(testsuitePath));

                List<String> allFunctions;
                var logicGraph = PersistenceUtilities.loadLogicGraph(graphPath);
                var nodes = logicGraph.getNodes();
                if (nodes == null || nodes.size() == 0) {
                    throw new IllegalStateException("no nodes available");
                }
                allFunctions = nodes.stream().filter(node -> NodeType.FUNCTION.equals(node.getType())).map(NodeModel::getNameOfNode).toList();
                controller.startMutations(allFunctions, startMutant.intValue()
                        , endMutant.intValue(), regions, resetFunction, outputPath);
            } else if (areAllArgumentsAvailableForCalibration(cmd)) {
                String testsuitePath = cmd.getOptionValue(TESTSUITE_OPTION);
                String region = cmd.getOptionValue(REGION_OPTION);
                String resetFunction = cmd.getOptionValue(RESET_FUNCTION_OPTION);

                controller.calibrateFolder(Path.of(testsuitePath), region, resetFunction);
            } else if (areAllArgumentsAvailableForReCalibration(cmd)) {
                String testsuitePath = cmd.getOptionValue(TESTSUITE_OPTION);
                String region = cmd.getOptionValue(REGION_OPTION);
                String resetFunction = cmd.getOptionValue(RESET_FUNCTION_OPTION);
                controller.reCalibrateFolder(Path.of(testsuitePath), region, resetFunction);
            } else if (areAllArgumentsAvailableForExecution(cmd)) {
                String testsuitePath = cmd.getOptionValue(TESTSUITE_OPTION);
                String region = cmd.getOptionValue(REGION_OPTION);
                String resetFunction = cmd.getOptionValue(RESET_FUNCTION_OPTION);
                controller.executeTestcases(testsuitePath, region, resetFunction);
            } else if (areAllArgumentsAvailableForDynamicTestcaseGeneration(cmd)) {
                String outputPath = cmd.getOptionValue(OUTPUT_OPTION);
                String resetFunction = cmd.getOptionValue(RESET_FUNCTION_OPTION);
                String regions = cmd.getOptionValue(REGION_OPTION);
                String graphPath = cmd.getOptionValue(GRAPH_OPTION);
                Set<String> authKeys = new HashSet<>();
                int startNumberIncluding = -1;
                int endNubmerExcluding = -1;
                String metric = null;
                if (cmd.hasOption(AUTH_VALUES_OPTION)) {
                    String authValuesUnparsed = cmd.getOptionValue(AUTH_VALUES_OPTION);
                    var authKeysArray = authValuesUnparsed.split(",");
                    authKeys = Arrays.stream(authKeysArray).collect(Collectors.toSet());
                }
                if (cmd.hasOption(START_NUMBER_OPTION)) {
                    startNumberIncluding = ((Number) cmd.getParsedOptionValue(START_NUMBER_OPTION)).intValue();
                }
                if (cmd.hasOption(END_NUMBER_OPTION)) {
                    endNubmerExcluding = ((Number) cmd.getParsedOptionValue(END_NUMBER_OPTION)).intValue();
                }
                if (cmd.hasOption(COVERAGE_METRIC_OPTION)) {
                    metric = cmd.getOptionValue(COVERAGE_METRIC_OPTION);
                }
                controller.createDynamicTestcases(graphPath, resetFunction, authKeys, regions, startNumberIncluding, endNubmerExcluding, outputPath, metric);

            }
        } catch (ParseException pe) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters needed for mutation run", options);
        }
    }

    private boolean areAllArgumentsAvailableForDynamicTestcaseGeneration(CommandLine cmd) {
        return cmd.hasOption(MODE) && cmd.hasOption(REGION_OPTION) && cmd.hasOption(RESET_FUNCTION_OPTION)
                && cmd.hasOption(OUTPUT_OPTION) && cmd.hasOption(GRAPH_OPTION)
                && "create".equals(cmd.getOptionValue(MODE));
    }

    private boolean areAllArgumentsAvailableForReCalibration(CommandLine cmd) {
        return cmd.hasOption(MODE) && cmd.hasOption(TESTSUITE_OPTION) && cmd.hasOption(REGION_OPTION)
                && cmd.hasOption(RESET_FUNCTION_OPTION)
                && "recalibrate".equals(cmd.getOptionValue(MODE));
    }

    private boolean areAllArgumentsAvailableForMutation(CommandLine cmd) {
        return cmd.hasOption(MODE) && cmd.hasOption(TESTSUITE_OPTION) && cmd.hasOption(MUTATION_OPTION)
                && cmd.hasOption(OUTPUT_OPTION)
                && cmd.hasOption(GRAPH_OPTION) && cmd.hasOption(RESET_FUNCTION_OPTION) && cmd.hasOption(REGION_OPTION)
                && cmd.hasOption(START_NUMBER_OPTION) && cmd.hasOption(END_NUMBER_OPTION)
                && "mutate".equals(cmd.getOptionValue(MODE));
    }

    private boolean areAllArgumentsAvailableForCalibration(CommandLine cmd) {
        return cmd.hasOption(MODE) && cmd.hasOption(TESTSUITE_OPTION) && cmd.hasOption(REGION_OPTION)
                && cmd.hasOption(RESET_FUNCTION_OPTION)
                && "calibrate".equals(cmd.getOptionValue(MODE));
    }

    private boolean areAllArgumentsAvailableForExecution(CommandLine cmd) {
        return cmd.hasOption(MODE) && cmd.hasOption(TESTSUITE_OPTION) && cmd.hasOption(REGION_OPTION)
                && cmd.hasOption(RESET_FUNCTION_OPTION)
                && "execute".equals(cmd.getOptionValue(MODE));
    }

    private Options getOptions() {
        Options options = new Options();


        options.addOption(Option.builder(MODE)
                .longOpt("mode")
                .hasArg(true)
                .desc("calibrate testcases or mutate testcases or create testcases")
                .required(false)
                .build());

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
        options.addOption(Option.builder(OLD_MUTATION_RESULT_OPTION)
                .longOpt("oldMutationResult")
                .hasArg(true)
                .desc("path to folder of old mutation results")
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
                .longOpt("startNumber")
                .hasArg(true)
                .desc("number of first mutant")
                .required(false)
                .type(Number.class)
                .build());
        options.addOption(Option.builder(END_NUMBER_OPTION)
                .longOpt("endNumber")
                .hasArg(true)
                .desc("number of last mutant")
                .required(false)
                .type(Number.class)
                .build());
        options.addOption(Option.builder(AUTH_VALUES_OPTION)
                .longOpt("authorizationValues")
                .hasArg(true)
                .desc("keys for authorization")
                .required(false)
                .build());
        options.addOption(Option.builder(COVERAGE_METRIC_OPTION)
                .longOpt("coverageMetric")
                .hasArg(true)
                .desc("coverage criterion to be used")
                .required(false)
                .build());
        return options;
    }
}
