import model.*;
import util.HALNS.HybridALNS;
import util.HALNS.Repair;
import util.LNS.LargeNeighbourhoodSearch;
import util.LNS.Rebuild;
import util.SA.Permutations;
import util.SA.SimulatedAnnealing;
import util.algorithms.BlockComparator;
import util.algorithms.Calculations;
import global.Parameters;
import util.algorithms.GreedyBaseAlgo;
import util.dataReader.DataReader;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private static ArrayList<TravelTrain> travelTrains;

    public static void main(String[] args) throws Exception {

        //============================================= VARIABLES =============================================

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
        dataReader.readRegulations(depots);
        //create the travelTrains
        travelTrains = dataReader.readTravelTrains();
        //travelTrains = new ArrayList<>();

        //make the consecutive blocks
        consecutiveBlocks = consecutiveBlocks(blocks);

        //make the consecutive blocks with breaks
        consecutiveBlocksWithBreak = consecutiveBlocksWithBreak(blocks);

        consmatrix = consecutiveInMatrix();
        consbreakmatrix = consecutiveBreakInMatrix();


        Random random = new Random();
        //RUN THE ALGORITHM TO GET THE BASE SOLUTION
        Calculations calculations = new Calculations(blocks, stations, breakStations, depots, parameters, travelmatrix, consmatrix, consbreakmatrix, random, travelTrains);
        GreedyBaseAlgo algoTest = new GreedyBaseAlgo(calculations);
        Permutations permutations = new Permutations(calculations);
        Rebuild builders = new Rebuild(calculations);
        Repair repair = new Repair(calculations);

        Solution hastus = dataReader.readHASTUSsolution();

        Solution test = dataReader.readSolution(".//Data//Results//23-05-2023_18851_180min_1block.csv");
        for(Schedule s: test.getSchedules()){
            calculations.calculateSchedule(s);
        }
        test.calculateSolution();
        finalSolutionCheck(test,calculations);
        timeCalculation(test);

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = currentDate.format(formatter);
        String cost = "";

        //============================================= BASE SOLUTIONS =============================================
        Solution blockSchedulebaseSolution = algoTest.run1BlockPerScheduleInitialSolution();
        Solution timedInitial = algoTest.runTimeBasedInitialSolution();
        //Solution id = algoTest.runInitialSolution();
        //Solution station = algoTest.runStationDriverSolution();

        //finalSolutionCheck(id,calculations);
        //finalSolutionCheck(timedInitial,calculations);
        //finalSolutionCheck(blockSchedulebaseSolution,calculations);
        //finalSolutionCheck(station,calculations);

        System.out.println("\n =================================================================================== \n");
        //============================================= SIMULATED ANNEALING =============================================

        int minutes = 10;
        int milis = minutes * 60000;
//
//            Solution endSolSA = SimulatedAnnealing.runSimulation(timedInitial,milis, permutations);
//            finalSolutionCheck(endSolSA,calculations);
//            cost = String.valueOf(endSolSA.getTotalCost());
//            writeCsv(endSolSA.getSchedules(),".//Data//Results//" + cost + "_" + dateString + "_" + (minutes) + "min_SA.csv");

        //============================================= ADAPTIVE LNS =============================================

        Solution endSolLNS = null;
        for(int i = 0; i < 0 ; i++){
            endSolLNS = LargeNeighbourhoodSearch.runALNS(test,milis,builders);
            finalSolutionCheck(endSolLNS,calculations);
            cost = String.valueOf(endSolLNS.getTotalCost());
            writeCsv(endSolLNS.getSchedules(),".//Data//Results//" + cost + "_" + dateString + "_" + (minutes) + "min_18674OPL_LNS.csv");
            timeCalculation(endSolLNS);
        }


        //HALNS
//        HybridALNS halns = new HybridALNS();
//        Solution hybrid = halns.HALNS(blockSchedulebaseSolution,repair,calculations);
//        finalSolutionCheck(hybrid,calculations);
//        System.out.println("Writing CSV-file ...");
//        cost = String.valueOf(hybrid.getTotalCost());
//        writeCsv(hybrid.getSchedules(),".//Data//Results//" + dateString + "_" + cost + "_HALNS.csv");
        //============================================= TESTING AND ANALYSING =============================================
        //System.out.println(combinatorialBound2(calculations));
    }

    //Final check of the schedules if the result is valid
    static void finalSolutionCheck(Solution solution, Calculations c) {
        ArrayList<Schedule> schedules = solution.getSchedules();
        ArrayList<Schedule> dupli = new ArrayList<>();
        ArrayList<Integer> dupliii = new ArrayList<>();
        int duplicates = 0;
        int blockscovered = 0;
        int stationDrivers = 0;
        int empty = 0;
        int shortSchedules = 0;
        int shortage = 0;
        boolean valid = true;
        int invalids = 0;
        Set<Integer> bl = new HashSet<>();
        for (Schedule s : schedules) {
            if (s.getBlocks().isEmpty()) {
                empty++;
            }
            c.calculateSchedule(s);
            if (!finalCheckSchedule(s)) {
                valid = false;
                invalids++;
            }
            if (s.getDuration() < 360) {
                shortSchedules++;
                shortage += (360 - s.getDuration());
            }
            for (Integer i : s.getBlocks()) {
                blockscovered++;
                if (bl.contains(i)) {
                    duplicates++;
                    dupliii.add(i);
                } else {
                    bl.add(i);
                }
            }
            if (s.getDriverType() != 0) {
                stationDrivers++;
            }
        }

        for (Schedule s : schedules) {
            for (Integer i : dupliii) {
                if (s.getBlocks().contains(i) && !dupli.contains(s)) {
                    dupli.add(s);
                }
            }
        }

        System.out.println("\n--------------- Solution ---------------");
        if (valid) {
            System.out.println("The following solution is VALID.");
        } else {
            System.out.println("The following solution is INVALID.");
            System.out.println("There are " + convert(invalids) + " invalid schedules.");
        }
        System.out.println("Drivers needed : " + convert(solution.getSchedules().size() - empty));
        System.out.println("Amount of blocks : " + convert(blocks.size()));
        System.out.println("Count of blocks covered : " + convert(blockscovered));
        System.out.println("Count of blocks covered twice : " + convert(duplicates));
        System.out.println("Count of schedules that can be covered by station drivers : " + convert(stationDrivers));
        System.out.println("Total duration : " + convert(solution.getTotalDuration()));
        System.out.println("Total Cost : " + convert(solution.getTotalCost()));
        System.out.println("Total Time Wasted : " + convert(solution.getTotalTimeWasted()));
        System.out.println("Drivers that are working less than 6 hours (get paid more then worked for) : " + (shortSchedules - empty) + ", with " + shortage + " minutes.");
    }

    static boolean finalCheckSchedule(Schedule schedule) {
        //Check if a schedule if VALID
        ArrayList<Integer> scheduleBlocks = schedule.getBlocks();
        if (scheduleBlocks.isEmpty()) {
            System.out.println("Schedule "+ schedule.getId() + " is empty but not remove.");
            return true;
        } else if (scheduleBlocks.size() == 1) {
            if (schedule.getDuration() > 300 && schedule.getBreakAfterBlock() == -1) {
                System.out.println("The schedule "+ schedule.getId() + " consist of 1 block [" + scheduleBlocks.get(0) + "] and takes longer then accepted (" + schedule.getDuration() + ").");
                return false;
            }
            return true;
        } else {
            for (int i = 0; i < scheduleBlocks.size() - 2; i++) {
                int a = scheduleBlocks.get(i);
                int b = scheduleBlocks.get(i + 1);
                if (schedule.getBreakAfterBlock() != a && consmatrix[a][b] != 1) {
                    System.out.println("Schedule " + schedule.getId());
                    System.out.println("The following blocks aren't consecutive: " + a + " & " + b + ".");
                    return false;
                }
                if (schedule.getBreakAfterBlock() == a && (consbreakmatrix[a][b] != 1)) {
                    System.out.println("There is a break after block " + a + ", but it isn't allowed.");
                    System.out.println("consbreakmatrix[" + a + "][" + b + "] = " + consbreakmatrix[a][b]);
                    return false;
                }
                if (schedule.getDuration() > parameters.getMaximumShiftLengthWeekend()) {
                    System.out.println("Schedule " + schedule.getId());
                    System.out.println("The shift is longer then allowed.");
                    return false;
                }
                if (schedule.getBreakAfterBlock() != -1 && schedule.getTimeWorkingWithoutBreak() > 300) {
                    System.out.println("Schedule " + schedule.getId());
                    System.out.println("A driver has been working longer than allowed before taking a break.");
                    return false;
                }

            }
            return true;
        }
    }

    static void timeCalculation(Solution solution){
        int breaktime = 0;
        int traveltime = 0;
        int wastedTime = solution.getTotalTimeWasted();
        int blocktime = 0;
        int totalduration = solution.getTotalDuration();

        for(Schedule s : solution.getSchedules()){
            blocktime += parameters.getCheckOutTime() + parameters.getCheckInTime();
            breaktime += 30;
            for(Integer i : s.getBlocks()){
                Block b = blocks.get(i-1);
                int blockdur = b.getArrivalTime() - b.getDepartureTime();
                if(blockdur < 0){
                    blockdur += 1440;
                }
                blocktime += blockdur;
            }
        }

        traveltime = totalduration - blocktime - breaktime - wastedTime;

        System.out.println("TOT " + totalduration);
        System.out.println("TW " + wastedTime);
        System.out.println("TR " + traveltime);
        System.out.println("Bl " + blocktime);
        System.out.println("Bre " + breaktime);
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

//        if(a.getLocalDriver() != b.getLocalDriver()){
//            return false;
//        }

        if (a.getEndWeekday() == b.getStartWeekday()) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return delta >= 0 &&
                        delta < parameters.getMaximumDurationBeforeBreak();
            } else {
                int traveltime = calculateTravelTime(a, b); //travelmatrix[a.getEndLoc()][b.getStartLoc()]
                return (delta - traveltime) >= 0 &&
                        delta < parameters.getMaximumDurationBeforeBreak();
            }

        } else if ((a.getEndWeekday() - b.getStartWeekday()) == -1 || (a.getEndWeekday() - b.getStartWeekday()) == 6) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return (1440 - a.getArrivalTime() + b.getDepartureTime()) < parameters.getMaximumDurationBeforeBreak();
            } else {
                int traveltime = calculateTravelTime(a, b); //travelmatrix[a.getEndLoc()][b.getStartLoc()]
                return (1440 - a.getArrivalTime() + b.getDepartureTime() - traveltime) >= 0 &&
                        (1440 - a.getArrivalTime() + b.getDepartureTime()) < parameters.getMaximumDurationBeforeBreak();
            }
        } else {
            return false;
        }
    }

    static int calculateTravelTime(Block a, Block b) {
        int drive = travelmatrix[a.getEndLoc()][b.getStartLoc()];

        int shortest = 9999999;
        for (TravelTrain train : travelTrains) {
            if (a.getEndWeekday() == train.getStartDay() && b.getStartWeekday() == train.getEndDay()){
                if (train.getStartLoc() == a.getEndLoc() && train.getEndLoc() == b.getStartLoc() && (train.getStartTime() >= a.getArrivalTime() && train.getEndTime() <= b.getDepartureTime())) {
                    int traintime = train.getEndTime() - a.getArrivalTime();
                    if (traintime < shortest) {
                        shortest = traintime;
                    }
                }
            }
        }
        return Math.min(drive, shortest);
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

//        if(a.getLocalDriver() != b.getLocalDriver()){
//            return false;
//        }

        if (a.getEndWeekday() == b.getStartWeekday()) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return (b.getDepartureTime() - a.getArrivalTime() - calculateBreakTime(a,b)) >= 0 &&
                        (b.getDepartureTime() - a.getArrivalTime() /*- calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1))*/) <= parameters.getMaximumTimeBetweenBlocks();
            } else {
                int breakTime = calculateBreakTime(a,b);
                return (b.getDepartureTime() - a.getArrivalTime() - breakTime) >= 0 &&
                        (b.getDepartureTime() - a.getArrivalTime() /*- breakTime*/) <= parameters.getMaximumTimeBetweenBlocks();
            }

        } else if ((a.getEndWeekday() - b.getStartWeekday()) == -1 || (a.getEndWeekday() - b.getStartWeekday()) == 6) {
            if (a.getEndLoc() == b.getStartLoc() && trainsA.get(trainsA.size() - 1).equals(trainsB.get(0))) {
                return (1440 - a.getArrivalTime() + b.getDepartureTime() /*+ calculateBreakTime(stations.get(a.getEndLoc() - 1), stations.get(b.getStartLoc() - 1))*/) < parameters.getMaximumTimeBetweenBlocks();
            } else {
                int breakTime = calculateBreakTime(a,b);
                return (1440 - a.getArrivalTime() + b.getDepartureTime() - breakTime) >= 0 &&
                        (1440 - a.getArrivalTime() + b.getDepartureTime() /*- breakTime*/) <= parameters.getMaximumTimeBetweenBlocks();
            }
        } else {
            return false;
        }
    }

    static int calculateBreakTime(Block a, Block b) {
        int stationA = a.getEndLoc();
        int stationB = b.getStartLoc();
        Station stA = stations.get(stationA - 1);
        Station stB = stations.get(stationB - 1);
        if (stA.isBreakLocation() || stB.isBreakLocation()) {
            if (stationA == stationB) {
                return 30;
            } else {
                int traveltime = calculateTravelTime(a, b); //travelmatrix[stationA][stationB]
                return 30 + traveltime;
            }

        } else {
            int breakStation = findNearestBreakLocation(stA, stB);
            return calculateTravelTimeBreak(a, b, breakStation);
        }

    }

    static int calculateTravelTimeBreak(Block a, Block b, int breakstation) {
        int driveTo = travelmatrix[a.getEndLoc()][breakstation];
        int driveFrom = travelmatrix[breakstation][b.getStartLoc()];

        int to = 9999999;
        int from = 999999;

        for (TravelTrain train : travelTrains) {
            if (a.getEndWeekday() == train.getStartDay() && b.getStartWeekday() == train.getEndDay()) {
                if (train.getStartLoc() == a.getEndLoc() && train.getEndLoc() == breakstation && train.getStartTime() > a.getArrivalTime() && train.getEndTime() < b.getDepartureTime() -30) {
                    int traintime = train.getEndTime() - a.getArrivalTime();
                    if (traintime < to) {
                        to = traintime;
                    }
                }
                if (train.getStartLoc() == breakstation && train.getEndLoc() == b.getStartLoc() && train.getStartTime() > a.getArrivalTime() + 30 && (train.getEndTime() < b.getDepartureTime())) {
                    int traintime = b.getDepartureTime() - train.getStartTime();
                    if (traintime < from) {
                        from = traintime;
                    }
                }
            }
        }

        if (driveTo <= to && driveFrom <= from) {
            return driveTo + driveFrom + 30;
        } else if (to < driveTo && driveFrom <= from) {
            return 30 + to + driveFrom;
        } else if (to < driveTo) {
            return to + from + 30;
        } else {
            return 30 + driveTo + from;
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

    static double combinatorialBound() {
        ArrayList<Block> tempblocks = new ArrayList<>(blocks);
        tempblocks.sort(new BlockComparator());
        int start = tempblocks.get(0).getDepartureTime();
        int end = tempblocks.get(tempblocks.size() - 1).getArrivalTime();
        int duration = (1440 - start) + end + parameters.getCheckInTime() + parameters.getCheckOutTime();

        return duration * parameters.getCostPerMinute();
    }

    static double combinatorialBound2(Calculations c) {
        ArrayList<Block> tempblocks = new ArrayList<>(blocks);
        tempblocks.sort(new BlockComparator());

        ArrayList<Schedule> schedules = new ArrayList<>();
        Schedule first = new Schedule();
        first.getBlocks().add(tempblocks.get(0).getId());
        schedules.add(first);
        for (Block b : tempblocks) {
            if (b.equals(tempblocks.get(0))) {
                continue;
            }
            boolean added = false;
            for (Schedule s : schedules) {
                Block last = blocks.get(s.getBlocks().get(s.getBlocks().size() - 1) - 1);
                if (b.getDepartureTime() >= last.getArrivalTime()) {
                    s.getBlocks().add(b.getId());
                    added = true;
                    break;
                }
            }
            if (added) {
                continue;
            }
            Schedule ns = new Schedule();
            ns.getBlocks().add(b.getId());
            schedules.add(ns);
        }

        double duration = 0;
        for (Schedule s : schedules) {
            if (s.getBlocks().size() > 1) {
                Block one = blocks.get(s.getBlocks().get(0) - 1);
                Block end = blocks.get(s.getBlocks().get(s.getBlocks().size() - 1) - 1);

                if (one.getStartWeekday() != end.getEndWeekday()) {
                    duration += end.getArrivalTime() + (1440 - one.getDepartureTime());
                } else {
                    duration += (end.getArrivalTime() - one.getDepartureTime());
                }
            }
        }
        System.out.println("drivers " + schedules.size());
        System.out.println("dur " + duration);
        return duration * parameters.getCostPerMinute();

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

    public static void writeCsv(ArrayList<Schedule> schedules, String filename) {
        int lastlocation;
        int lasttime;
        int duty = 1;
        try {
            FileWriter writer = new FileWriter(filename);
            writer.append("Duty").append(",").append("Task / Block").append(",").append("Departure time").append(",")
                    .append("Arrival time").append(",").append("Departure Location").append(",")
                    .append("Arrival location").append(",").append("Weekday (block start)").append("Type").append("\n");
            for(Schedule s : schedules){
                String type = "Regular";
                if(s.getDriverType() != 0){ type = "Station";}

                //Checkin
                writer.append(String.valueOf(duty)).append(",").append("Check in").append(",").append(min2Str(s.getStartTime())).append(",")
                        .append(min2Str(s.getStartTime()+ parameters.getCheckInTime())).append(",")
                        .append(idToStationName(s.getClosestDepot())).append(",").append(idToStationName(s.getClosestDepot())).append(",")
                        .append(" ").append(",").append(type);
                writer.append("\n");
                lastlocation = s.getClosestDepot();
                lasttime = s.getStartTime()+ parameters.getCheckInTime();

                for(Integer i : s.getBlocks()){
                    Block block = blocks.get(i-1);
                    Integer aft = null;
                    Integer b = null;
                    Block bef = null;
                    ArrayList<Integer> trainsBef = null;
                    int indexI = s.getBlocks().indexOf(i);
                    if(indexI != s.getBlocks().size()-1){
                        aft = s.getBlocks().get(indexI + 1);
                    }
                    if(indexI != 0){
                        b = s.getBlocks().get(indexI - 1);
                        bef = blocks.get(b-1);
                        trainsBef = bef.getTrainNr();
                    }
                    ArrayList<Integer> trainsAft = block.getTrainNr();



                    if(lastlocation != block.getStartLoc()){
                        // NEED OF A TRAVEL
                        if(trainOrTaxi(i,aft)){
                            writer.append(String.valueOf(duty)).append(",").append("TravelTrain").append(",");
                        }else{
                            writer.append(String.valueOf(duty)).append(",").append("Travel").append(",");
                        }
                        writer.append(min2Str(lasttime)).append(",").append(min2Str(lasttime + travelmatrix[lastlocation][block.getStartLoc()])).append(",")
                                .append(idToStationName(lastlocation)).append(",").append(idToStationName(block.getStartLoc())).append(",")
                                .append(" ").append(",").append(type);
                        writer.append("\n");
                    } else if (bef != null && !trainsBef.get(trainsBef.size() - 1).equals(trainsAft.get(0))) {
                        //WALK FROM PLATFORM TO OTHER PLATFORM
                        writer.append(String.valueOf(duty)).append(",").append("Travel").append(",");
                        writer.append(min2Str(lasttime)).append(",").append(min2Str(lasttime + travelmatrix[lastlocation][block.getStartLoc()])).append(",")
                                .append(idToStationName(lastlocation)).append(",").append(idToStationName(block.getStartLoc())).append(",")
                                .append(" ").append(",").append(type);
                        writer.append("\n");
                    }

                    //Block
                    writer.append(String.valueOf(duty)).append(",").append("Block ").append(String.valueOf(block.getId())).append(",").append(min2Str(block.getDepartureTime())).append(",")
                            .append(min2Str(block.getArrivalTime())).append(",").append(idToStationName(block.getStartLoc()))
                            .append(",").append(idToStationName(block.getEndLoc())).append(",")
                            .append(String.valueOf(block.getStartWeekday())).append(",").append(type);
                    writer.append("\n");

                    lastlocation = block.getEndLoc();
                    lasttime = block.getArrivalTime();

                    if(s.getBreakAfterBlock() == block.getId()){
                        //Break
                        int index = s.getBlocks().indexOf(i);
                        int station;
                        if(index != s.getBlocks().size()-1){
                            //there is a block after the break
                            Block afterB = blocks.get(aft-1);
                            station = findNearestBreakLocation(stations.get(block.getEndLoc()-1),stations.get(afterB.getStartLoc()-1));

                        }else{
                            //Break after last block
                            station = findNearestBreakLocation(stations.get(block.getEndLoc()-1),stations.get(s.getClosestDepot()-1));
                        }
                        int startbreak = lasttime;
                        if(lastlocation != station){
                            writer.append(String.valueOf(duty)).append(",").append("Travel").append(",");
                            writer.append(min2Str(lasttime)).append(",").append(min2Str(lasttime + travelmatrix[lastlocation][station])).append(",")
                                    .append(idToStationName(lastlocation)).append(",").append(idToStationName(station)).append(",")
                                    .append(" ").append(",").append(type);
                            writer.append("\n");
                            startbreak = lasttime + travelmatrix[lastlocation][station];
                            if(startbreak >= 1440){startbreak -= 1440;}
                        }

                        writer.append(String.valueOf(duty)).append(",").append("Break").append(",").append(min2Str(startbreak)).append(",").append(min2Str(startbreak+30)).append(",")
                                .append(idToStationName(station)).append(",").append(idToStationName(station)).append(",").append(" ").append(",").append(type);
                        writer.append("\n");
                        lasttime = startbreak+30;
                        if(lasttime >= 1440){lasttime -= 1440;}
                        lastlocation = station;
                    }
                }

                //Travel to depot
                if(lastlocation != s.getClosestDepot()){
                    writer.append(String.valueOf(duty)).append(",").append("Travel").append(",");
                    writer.append(min2Str(lasttime)).append(",").append(min2Str(lasttime + travelmatrix[lastlocation][s.getClosestDepot()])).append(",")
                            .append(idToStationName(lastlocation)).append(",").append(idToStationName(s.getClosestDepot())).append(",")
                            .append(" ").append(",").append(type);
                    writer.append("\n");
                    lasttime += travelmatrix[lastlocation][s.getClosestDepot()];
                    if(lasttime > 1440){lasttime -=1440;}
                }

                //Check out
                writer.append(String.valueOf(duty)).append(",").append("Check out").append(",").append(min2Str(lasttime )).append(",")
                        .append(min2Str(lasttime + parameters.getCheckOutTime())).append(",")
                        .append(idToStationName(s.getClosestDepot())).append(",").append(idToStationName(s.getClosestDepot())).append(",")
                        .append(" ").append(",").append(type);
                writer.append("\n");
                duty++;
            }

            writer.flush();
            writer.close();
            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean trainOrTaxi(Integer a, Integer b){
        if(b == null){
            return false;
        }
        Block blockA = blocks.get(a-1);
        Block blockB = blocks.get(b-1);


        for(TravelTrain train : travelTrains){
            if(blockA.getEndLoc() == train.getStartLoc()
                && blockB.getStartLoc() == train.getEndLoc()
                && (blockA.getArrivalTime() < train.getStartTime()
                && blockB.getDepartureTime() > train.getEndTime()
                && train.getStartDay() == blockA.getEndWeekday()
                && train.getEndDay() == blockB.getStartWeekday())){
                    return true;

            }
        }
        return false;
    }

    public static String min2Str(int minutes){
        if(minutes >= 1440){
            minutes -= 1440;
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return String.format("%02d:%02d", hours, remainingMinutes);
    }

    public static String idToStationName (int id){
        Station station = stations.get(id-1);
        return station.getName();
    }

}