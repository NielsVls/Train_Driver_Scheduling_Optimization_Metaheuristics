package util.HALNS;

import model.Schedule;
import model.Solution;
import util.algorithms.Calculations;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static util.HALNS.ALNS.findSchedule;
import static util.HALNS.ALNS.runALNS;
import static util.LNS.LargeNeighbourhoodSearch.convert;

public class HybridALNS {
    ArrayList<Solution> initialPopulation = new ArrayList<>();
    ArrayList<Solution> population = new ArrayList<>();
    static int populationSize = 3;
    int gensWithoutImprove = 5;
    int gensMax = 500;
    static Calculations c;

    public Solution HALNS(Solution initial, Repair repair, Calculations calc) {

        c = calc;

        long startTime = System.currentTimeMillis();

        System.out.println("Starting the HALNS\n");
        System.out.println("Generation 0:");
        //INITIAL POPULATION (1-4)
        for (int i = 0; i < populationSize; i++) {
            Solution solution = runALNS(initial, null, 100, repair, c);
            initialPopulation.add(solution);
            finalSolutionCheck(solution,c);
            System.out.println("ALNS " + (i+1) + " added.");
        }

        population = initialPopulation;
        int generation = 1;
        int genNoImprove = 0;
        Solution globalbest = initial.clone();

        while (genNoImprove < gensWithoutImprove && generation < gensMax) {

            if(generation %5 == 0){
                System.out.println("Adding Diversity ...");
                for (int j = 0;j < 3 ; j++){
                    Solution solution = runALNS(initial, null, 250, repair, c);
                    population.add(solution);
                }
                System.out.println("Diversity added.");
            }

            System.out.println("Generation " + generation);
            long genStart = System.currentTimeMillis();

            System.out.println("Picking Best Solution ...");
            //Best Solution 6
            Solution bestTemp = population.get(pickBestSolution(population));
            if (bestTemp.getTotalCost() < globalbest.getTotalCost()) {
                globalbest = bestTemp;
                genNoImprove = 0;
            }else{
                genNoImprove++;
            }

            System.out.println("Crossover ...");
            // 7-11
            for (int i = 0; i < populationSize; i++) {
                Solution curr = population.get(i).clone();
                curr.calculateSolution();
                if (!curr.equals(globalbest)) {
                    Solution temp = runALNS(curr, globalbest, 100, repair, c);
                    temp.calculateSolution();
                    if (temp != null) {
                        population.add(temp);
                    }
                }
            }

            System.out.println("Selecting Survivors ...");
            population = selectSurvivors(population);
            long genEnd = System.currentTimeMillis();

            long genTime = genEnd-genStart;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(genTime);

            System.out.println("End of Generation " + generation + " : Best " + globalbest.getTotalCost() + " ; Population size : " + population.size());
            System.out.println("Generation " + generation + " took " + minutes + " minutes.\n");

            generation++;
        }

        long totalTime = System.currentTimeMillis() - startTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        System.out.println("Finished in " + hours + " hours " + remainingMinutes + " minutes.");

        Solution lastBest = population.get(pickBestSolution(population));
        if(lastBest.getTotalCost() < globalbest.getTotalCost()){
            System.out.println("Final Cost : " + lastBest.getTotalCost());
            return lastBest;
        }else{
            System.out.println("Final Cost : " + globalbest.getTotalCost());
            return globalbest;
        }
    }

    public int pickBestSolution(ArrayList<Solution> solutions) {
        int lowest = -1;
        int cost = 99999999;

        for (int i = 0; i < solutions.size(); i++) {
            Solution sol = solutions.get(i);
            if (sol.getTotalCost() < cost) {
                lowest = i;
                cost = sol.getTotalCost();
            }
        }

        return lowest;
    }

    public static ArrayList<Solution> selectSurvivors(ArrayList<Solution> solutions) {

        ArrayList<Integer> hamming = new ArrayList<>();
        ArrayList<Integer> totalcost = new ArrayList<>();

        //Rank of totalCost
        ArrayList<Solution> sorted = new ArrayList<>(solutions);
        sorted.sort(new SolutionComparator());
        for (Solution s : solutions) {
            totalcost.add(sorted.indexOf(s) + 1);
        }

        ArrayList<Integer> hammingScore = new ArrayList<>();
        for (Solution s : solutions) {
            int hammingSum = 0;
            for (Solution s2 : solutions) {
                if (!s.equals(s2)) {
                    hammingSum += getHammingDistance(s,s2);
                }
            }
            hammingScore.add(hammingSum);
        }

        //Rank of HammingDistance
        ArrayList<Solution> sortedH = new ArrayList<>(solutions);
        Comparator<Solution> costComparator = Comparator.comparingInt(Solution::getTotalCost);
        Collections.sort(sortedH,costComparator);
        for (Solution s : solutions) {
            hamming.add(sortedH.indexOf(s) + 1);
        }

        ArrayList<Integer> totalRank = new ArrayList<>();
        for(int i = 0; i < solutions.size();i++){
            totalRank.add(hamming.get(i) + totalcost.get(i));
        }

        ArrayList<Integer> totalSorted = new ArrayList<>(totalRank);
        Collections.sort(totalSorted);

        ArrayList<Solution> finalRank = new ArrayList<>();
        for(Integer i : totalSorted){
            finalRank.add(solutions.get(totalRank.indexOf(i)));
        }

        //Remove the extras
        int maxIndex = 4 * populationSize -1;
        if(finalRank.size() > maxIndex){
            finalRank.subList(maxIndex + 1, finalRank.size()).clear();
        }

        return finalRank;
    }


    public static int getHammingDistance(Solution curr, Solution best) {
        if (best == null || curr.equals(best)) {
            return 0;
        }
        int hammingDistance = 0;
        for (int i = 1; i <= c.blocks.size(); i++) {
            Schedule currS = findSchedule(curr, i);
            Schedule bestS = findSchedule(best, i);

            int sizeC = currS.getBlocks().size();
            int sizeB = bestS.getBlocks().size();

            int indexC = currS.getBlocks().indexOf(i);
            int indexB = bestS.getBlocks().indexOf(i);


            if(sizeC == sizeB && sizeC == 1){
                continue;
            }else if(sizeC != sizeB && (sizeB == 1 || sizeC == 1)){
                hammingDistance++;
                continue;
            }

            if (indexC != indexB) {
                hammingDistance++;
            } else if (indexB == 0) {
                if (!Objects.equals(currS.getBlocks().get(indexC + 1), bestS.getBlocks().get(indexB + 1))) {
                    hammingDistance++;
                }
            } else if (indexB == (sizeB - 1) || indexC == (sizeC - 1)) {
                if(sizeC == sizeB){
                    if (!Objects.equals(currS.getBlocks().get(indexC - 1), bestS.getBlocks().get(indexB - 1))) {
                        hammingDistance++;
                    }
                }else{
                    hammingDistance++;
                }
            } else {
                if (!Objects.equals(currS.getBlocks().get(indexC + 1), bestS.getBlocks().get(indexB + 1)) ||
                        !Objects.equals(currS.getBlocks().get(indexC - 1), bestS.getBlocks().get(indexB - 1))) {
                    hammingDistance++;
                }
            }
        }
        return hammingDistance;
    }


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
        System.out.println("Amount of blocks : " + convert(c.blocks.size()));
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
            System.out.println("Schedule is empty but not remove.");
            return true;
        } else if (scheduleBlocks.size() == 1) {
            if (schedule.getDuration() > 300 && schedule.getBreakAfterBlock() == -1) {
                System.out.println("The schedule consist of 1 block [" + scheduleBlocks.get(0) + "] and takes longer then accepted (" + schedule.getDuration() + ").");
                return false;
            }
            return true;
        } else {
            for (int i = 0; i < scheduleBlocks.size() - 2; i++) {
                int a = scheduleBlocks.get(i);
                int b = scheduleBlocks.get(i + 1);
                if (c.consmatrix[a][b] != 1) {
                    System.out.println(schedule);
                    System.out.println("The following blocks aren't consecutive: " + a + " & " + b + ".");
                    return false;
                }
                if (schedule.getBreakAfterBlock() == a && (c.consbreakmatrix[a][b] != 1)) {
                    System.out.println(schedule);
                    System.out.println("There is a break after block " + a + ", but it isn't allowed.");
                    System.out.println("consbreakmatrix[" + a + "][" + b + "] = " + c.consbreakmatrix[a][b]);
                    return false;
                }
                if (schedule.getDuration() > c.parameters.getMaximumShiftLengthWeekend()) {
                    System.out.println(schedule);
                    System.out.println("The shift is longer then allowed.");
                    return false;
                }
                if (schedule.getBreakAfterBlock() != -1 && schedule.getTimeWorkingWithoutBreak() > 300) {
                    System.out.println(schedule);
                    System.out.println("A driver has been working longer than allowed before taking a break.");
                    return false;
                }

            }
            return true;
        }
    }



}

class SolutionComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution s1, Solution s2) {
        return Integer.compare(s1.getTotalCost(), s2.getTotalCost());
    }
}