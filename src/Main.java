import model.Block;
import model.Schedule;
import model.Station;
import model.Solution;
import util.LNS.DestroyRepair;
import util.LNS.LargeNeighbourhoodSearch;
import util.SA.Permutations;
import util.SA.SimulatedAnnealing;
import util.algorithms.Calculations;
import global.Parameters;
import util.algorithms.GreedyBaseAlgo;
import util.dataReader.DataReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

    private static ArrayList<Block> blocks;
    private static ArrayList<ArrayList<Integer>> consecutiveBlocks;
    private static ArrayList<ArrayList<Integer>> consecutiveBlocksWithBreak;
    private static ArrayList<Station> stations;
    private static ArrayList<Station> breakStations;
    private static ArrayList<Station> depots;
    private static Parameters parameters;
    private static int[][] travelmatrix;
    private static int[][] consmatrix;
    private static int[][] consbreakmatrix;

    public static void main(String[] args) throws Exception {

        //Initialize the parameters
        parameters = new Parameters();

        //Read in the data and make the blocks
        DataReader dataReader = new DataReader();
        blocks = dataReader.readBlocks();

        //create the travelmatrix
        travelmatrix = dataReader.readTravelMatrix();
        int[][] updatedTravelMatrix = floydWarshall(travelmatrix);

        //create the station objects
        stations = dataReader.readStations();
        breakStations = dataReader.breakStations(stations);
        depots = dataReader.depots(stations);

        //make the consecutive blocks
        consecutiveBlocks = consecutiveBlocks(blocks);

        //make the consecutive blocks with breaks
        consecutiveBlocksWithBreak = consecutiveBlocksWithBreak(blocks);

        consmatrix = consecutiveInMatrix();
        consbreakmatrix = consecutiveBreakInMatrix();

        //RUN THE ALGORITHM TO GET THE BASE SOLUTION
        Calculations calculations = new Calculations(blocks,stations,breakStations,depots,parameters,travelmatrix,consmatrix,consbreakmatrix);
        GreedyBaseAlgo algoTest = new GreedyBaseAlgo(calculations);
        Solution baseSolution = algoTest.runInitialSolution();
        finalSolutionCheck(baseSolution,calculations);


        //Solution baseSolution = algoTest.runTimeBasedInitialSolution();
        // finalSolutionCheck(baseSolution,calculations);

//        Solution baseSolution = algoTest.run1BlockPerScheduleInitialSolution();
//        finalSolutionCheck(baseSolution,calculations);

        //Solution baseSolution = algoTest.runRandomInitialSolution();
        //finalSolutionCheck(baseSolution,calculations);

        Permutations permutations = new Permutations(calculations);
        Solution endSolSA = SimulatedAnnealing.runSimulation(baseSolution,60000, permutations);
        finalSolutionCheck(endSolSA,calculations);

//        DestroyRepair builders = new DestroyRepair(calculations);
//        Solution endSolLNS = LargeNeighbourhoodSearch.runSimulationTMP(baseSolution,300000,builders);
//        finalSolutionCheck(endSolLNS,calculations);

    }

    //Final check of the schedules if the result is valid
    static void finalSolutionCheck(Solution solution,Calculations c){
        ArrayList<Schedule> schedules = solution.getSchedules();
        ArrayList<Schedule> dupli = new ArrayList<>();
        ArrayList<Integer> dupliii = new ArrayList<>();
        int duplicates = 0;
        int blockscovered = 0;
        int stationDrivers = 0;
        boolean valid = true;
        int invalids = 0;
        Set<Integer> bl = new HashSet<>();
        for (Schedule s : schedules){
            c.calculateSchedule(s);
            if(!finalCheckSchedule(s)){
                valid = false;
                invalids++;
            }
            for(Integer i : s.getBlocks()){
                blockscovered++;
                if(bl.contains(i)){
                    duplicates++;
                    dupliii.add(i);
                }else{
                    bl.add(i);
                }
            }
            if(s.isLocal()){stationDrivers++;}
        }

        for(Schedule s: schedules){
            for (Integer i : dupliii){
                if (s.getBlocks().contains(i) && !dupli.contains(s)){
                    dupli.add(s);
                }
            }
        }

        System.out.println("\n--------------- Solution ---------------");
        if(valid){
            System.out.println("The following solution is VALID.");
        }else{
            System.out.println("The following solution is INVALID.");
            System.out.println("There are " + convert(invalids) + " invalid schedules.");
        }
        System.out.println("Drivers needed : " + convert(solution.getSchedules().size()));
        System.out.println("Amount of blocks : " + convert(blocks.size()));
        System.out.println("Count of blocks covered : " + convert(blockscovered));
        System.out.println("Count of blocks covered twice : " + convert(duplicates));
        System.out.println("Count of schedules that can be covered by station drivers : " + convert(stationDrivers));
        System.out.println("Total duration : " + convert(solution.getTotalDuration()));
        System.out.println("Total Cost : " + convert(solution.getTotalPaymentDrivers()));
        System.out.println("Total Time Wasted : " + convert(solution.getTotalTimeWasted()));
//        System.out.println(dupli);
//        System.out.println(dupliii);
    }

    static boolean finalCheckSchedule(Schedule schedule){
        //Check if a schedule if VALID
        ArrayList<Integer> scheduleBlocks = schedule.getBlocks();
        if(scheduleBlocks.isEmpty()){
            System.out.println("Schedule is empty but not remove.");
            return false;
        }else if(scheduleBlocks.size() == 1){
            if (schedule.getDuration() > 300 && schedule.getBreakAfterBlock() == -1){
                System.out.println("The schedule consist of 1 block [" + scheduleBlocks.get(0) + "] and takes longer then accepted ("+schedule.getDuration()+").");
                return false;
            }
            return true;
        }else{
            for (int i = 0; i < scheduleBlocks.size()-2; i++) {
                int a = scheduleBlocks.get(i);
                int b = scheduleBlocks.get(i+1);
                if(consmatrix[a][b] != 1){
                    System.out.println("The following blocks aren't consecutive: " + a + " & " + b + ".");
                    return false;
                }
                if(schedule.getBreakAfterBlock() == a && (consbreakmatrix[a][b] != 1)){
                    System.out.println(schedule);
                    System.out.println("There is a break after block " + a + ", but it isn't allowed.");
                    System.out.println("consbreakmatrix[" + a + "][" + b + "] = " + consbreakmatrix[a][b]);
                    return false;
                }
                if(schedule.getDuration() > parameters.getMaximumShiftLengthWeekend()){
                    System.out.println("The shift is longer then allowed.");
                    return false;
                }
                if(schedule.getBreakAfterBlock() != -1 && schedule.getTimeWorkingWithoutBreak() > 300){
                    System.out.println(schedule);
                    System.out.println("A driver has been working longer than allowed before taking a break.");
                    return false;
                }

            }
            return true;
        }
    }

    //Check if blocks are consecutive and puts them as pairs in a list
    static ArrayList<ArrayList<Integer>> consecutiveBlocks(ArrayList<Block> blocks) {
        ArrayList<ArrayList<Integer>> allPairs = new ArrayList<>();
        for (Block a : blocks) {
            for (Block b : blocks) {
                if (a.getId() != b.getId()) {
                    if (checkFeasibility(a, b)) {
                        // A & B are consecutive
                        ArrayList<Integer> pair = new ArrayList<>();
                        pair.add(a.getId());
                        pair.add(b.getId());
                        allPairs.add(pair);
                    }
                }
            }
        }
        return allPairs;
    }

    static boolean checkFeasibility(Block a, Block b) {
        ArrayList<Integer> trainsA = a.getTrainNr();
        ArrayList<Integer> trainsB = b.getTrainNr();

        int delta = b.getDepartureTime() - a.getArrivalTime();

        if (a.getEndWeekday() == b.getStartWeekday()) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return delta >= 0 &&
                        delta < parameters.getMaximumShiftLengthWeekend();
            } else {
                return (delta - travelmatrix[a.getEndLoc()][b.getStartLoc()]) >= 0 &&
                        delta < parameters.getMaximumShiftLengthWeekend();
            }

        } else if ((a.getEndWeekday() - b.getStartWeekday()) == -1 || (a.getEndWeekday() - b.getStartWeekday()) == 6) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return (1440 - a.getArrivalTime() + b.getDepartureTime()) < parameters.getMaximumShiftLengthWeekend();
            } else {
                return (1440 - a.getArrivalTime() + b.getDepartureTime() - travelmatrix[a.getEndLoc()][b.getStartLoc()]) >= 0 &&
                        (1440 - a.getArrivalTime() + b.getDepartureTime()) < parameters.getMaximumShiftLengthWeekend();
            }
        } else {
            return false;
        }
    }

    static int findNearestBreakLocation(Station a, Station b) {
        int closest = 99999;
        int closestStation = 0;
        int distance;
        for (Station s : breakStations) {
            distance = travelmatrix[a.getID()][s.getID()] + travelmatrix[s.getID()][b.getID()];
            if (distance < closest) {
                closest = distance;
                closestStation = s.getID();
            }
        }
        return closestStation;
    }

    static ArrayList<ArrayList<Integer>> consecutiveBlocksWithBreak(ArrayList<Block> blocks) {
        ArrayList<ArrayList<Integer>> allPairs = new ArrayList<>();
        for (Block a : blocks) {
            for (Block b : blocks) {
                if (a.getId() != b.getId()) {
                    if (checkFeasibilityWithBreak(a, b)) {
                        // A & B are consecutive
                        ArrayList<Integer> pair = new ArrayList<>();
                        pair.add(a.getId());
                        pair.add(b.getId());
                        allPairs.add(pair);
                    }
                }
            }
        }
        return allPairs;
    }

    static boolean checkFeasibilityWithBreak(Block a, Block b) {
        ArrayList<Integer> trainsA = a.getTrainNr();
        ArrayList<Integer> trainsB = b.getTrainNr();

        if (a.getEndWeekday() == b.getStartWeekday()) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return (b.getDepartureTime() - a.getArrivalTime() - calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1))) >= 0 &&
                        (b.getDepartureTime() - a.getArrivalTime() /*- calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1))*/) <= parameters.getMaximumTimeBetweenBlocks();
            } else {
                int breakTime = calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1));
                return (b.getDepartureTime() - a.getArrivalTime() - breakTime) >= 0 &&
                        (b.getDepartureTime() - a.getArrivalTime() /*- breakTime*/) <= parameters.getMaximumTimeBetweenBlocks();
            }

        } else if ((a.getEndWeekday() - b.getStartWeekday()) == -1 || (a.getEndWeekday() - b.getStartWeekday()) == 6) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return (1440 - a.getArrivalTime() + b.getDepartureTime() /*+ calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1))*/) < parameters.getMaximumTimeBetweenBlocks();
            } else {
                int breakTime = calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1));
                return (1440 - a.getArrivalTime() + b.getDepartureTime() - breakTime) >= 0 &&
                        (1440 - a.getArrivalTime() + b.getDepartureTime() /*- breakTime*/) <= parameters.getMaximumTimeBetweenBlocks();
            }
        } else {
            return false;
        }
    }

    static int calculateBreakTime(Station a, Station b) {
        int stationA = a.getID();
        int stationB = b.getID();
        if (a.isBreakLocation() || b.isBreakLocation()) {
            if (stationA == stationB) {
                return 30;
            } else {
                return 30 + travelmatrix[stationA][stationB];
            }

        } else {
            int breaktime = 30;
            int breakStation = findNearestBreakLocation(a, b);
            int travel = travelmatrix[stationA][breakStation] + travelmatrix[breakStation][stationB];
            return (breaktime + travel);
        }

    }

    //Print the travelmatrix out in the form of a matrix
    static void printTravelMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    static int[][] consecutiveInMatrix() {
        int[][] matrix = new int[1312][1312];
        for (int i = 1; i < 1312; i++) {
            matrix[0][i] = i;
            matrix[i][0] = i;
            for (int j = 1; j < 1312; j++) {
                matrix[i][j] = 0;
            }
        }
        for (ArrayList<Integer> pair : consecutiveBlocks) {
            matrix[pair.get(0)][pair.get(1)] = 1;
        }

        return matrix;
    }

    static int[][] consecutiveBreakInMatrix() {
        int[][] matrix = new int[1312][1312];
        for (int i = 1; i < 1312; i++) {
            matrix[0][i] = i;
            matrix[i][0] = i;
            for (int j = 1; j < 1312; j++) {
                matrix[i][j] = 0;
            }
        }
        for (ArrayList<Integer> pair : consecutiveBlocksWithBreak) {
            matrix[pair.get(0)][pair.get(1)] = 1;
        }

        return matrix;
    }

    static int[][] floydWarshall(int graph[][]) {
        int size = 40;
        int matrix[][] = new int[size][size];
        int i, j, k;

        for (i = 0; i < size; i++)
            for (j = 0; j < size; j++)
                matrix[i][j] = graph[i][j];

        // Adding vertices individually
        for (k = 0; k < size; k++) {
            for (i = 0; i < size; i++) {
                for (j = 0; j < size; j++) {
                    if (matrix[i][k] + matrix[k][j] < matrix[i][j])
                        matrix[i][j] = matrix[i][k] + matrix[k][j];
                }
            }
        }
        return matrix;
    }

    public static String convert(int number) {
        String numberString = String.valueOf(number);
        StringBuilder builder = new StringBuilder(numberString);

        int index = builder.length() - 3;
        while (index > 0) {
            builder.insert(index, " ");
            index -= 3;
        }

        return builder.toString();
    }

}