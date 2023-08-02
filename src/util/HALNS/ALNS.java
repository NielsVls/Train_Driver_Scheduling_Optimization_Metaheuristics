package util.HALNS;

import model.PossibleSolution;
import model.Schedule;
import model.Solution;
import util.LNS.Rebuild;
import util.algorithms.Calculations;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static util.LNS.Rebuild.getRandomNumberInRange;

public class ALNS {
    private static boolean acceptanceProbability(double delta, double temperature) {
        if(Math.exp(-delta / temperature) > Math.random()){
            //System.out.println("worse accepted");
            return true;
        }else return false;
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

    public static ArrayList<Double> calculateProbability(ArrayList<Double> rewards){
        ArrayList<Double> probs = new ArrayList<>();
        double sum = 0;
        for(Double d : rewards){
            sum += d;
        }
        for (Double d : rewards){
            double p = d/sum;
            probs.add(p);
        }
        return probs;
    }

    public static int randomPicker (ArrayList<Double> weights){
        double randomValue = new Random().nextDouble();
        double cumulativeProbability = 0.0;
        int index = 0;
        for (double weight : weights) {
            cumulativeProbability += weight;
            if (randomValue <= cumulativeProbability) {
                break;
            }
            index++;
        }
        return index;
    }

    public static double update(double reward, double points){
        double lambda = 0.9;
        return lambda * reward + (1-lambda)*points;

    }

    public static Solution runALNS(Solution initial, Solution globalBest, int maxIterations, Repair repair, Calculations c) {

        int countIterations = 0;
        double startTemperature = 2;
        double temperature = startTemperature;
        double alfa = 0.001;

        ArrayList<Double> rewardDestroy = new ArrayList<>();
        ArrayList<Double> rewardRepair = new ArrayList<>();
        ArrayList<Double> rewardRemovalCandidates = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            rewardDestroy.add(20.0);
        }
        for (int i = 0; i < 2; i++) {
            rewardRepair.add(20.0);
        }
        for (int i = 0; i < 3; i++) {
            rewardRemovalCandidates.add(20.0);
        }

        Solution current = initial.clone();
        Solution best = current.clone();

        int iteNoImprovement = 0;
        while(iteNoImprovement < maxIterations){
            PossibleSolution possibleSolution = null;

            ArrayList<Integer> candidates = new ArrayList<>();
            ArrayList<Double> removalCProbs = calculateProbability(rewardRemovalCandidates);

            ArrayList<Double> destroyProbs = calculateProbability(rewardDestroy);
            ArrayList<Double> repairProbs = calculateProbability(rewardRepair);
            int destroymethod = randomPicker(destroyProbs);
            int repairmethod = randomPicker(repairProbs);
            int destructions;

            int removalCandidateMethod = 2;
            if(destroymethod < 6){
                removalCandidateMethod = randomPicker(removalCProbs);
                switch (removalCandidateMethod){
                    case 0 -> {
                        candidates = getRemovalCandidatesPreSuc(current,globalBest,c);
                    }
                    case 1 -> {
                        candidates = getRemovalCandidatesDepot(current,globalBest,c);
                    }
                    case default -> {
                        candidates = new ArrayList<>();
                    }
                }
            }else{
                candidates = new ArrayList<>();
            }


            switch (destroymethod) {
                case 0 -> {
                    destructions = getRandomNumberInRange(10, 30);
                    possibleSolution = repair.randomDestruct(current, destructions, repairmethod,candidates);
                }
                case 1 -> {
                    destructions = getRandomNumberInRange(35, 75);
                    possibleSolution = repair.randomDestruct(current, destructions, repairmethod,candidates);
                }
                case 2 -> {
                    destructions = getRandomNumberInRange(100, 150);
                    possibleSolution = repair.randomDestruct(current, destructions, repairmethod,candidates);
                }
                case 3 -> {
                    destructions = getRandomNumberInRange(10, 30);
                    possibleSolution = repair.destructByTime(current, destructions, repairmethod,candidates);
                }
                case 4 -> {
                    destructions = getRandomNumberInRange(35, 75);
                    possibleSolution = repair.destructByTime(current, destructions, repairmethod,candidates);
                }
                case 5 -> {
                    destructions = getRandomNumberInRange(100, 150);
                    possibleSolution = repair.destructByTime(current, destructions, repairmethod,candidates);
                }
                case 6 -> possibleSolution = repair.destructByLocation(current, repairmethod);
                case 7 -> possibleSolution = repair.destructSchedule(current, repairmethod);
            }

            int points =0;
            if(possibleSolution != null) {
                double deltaE2 = possibleSolution.getNewCost() - possibleSolution.getOldCost();
                points = 1;
                if (deltaE2 < 0 ) {
                    iteNoImprovement = 0;
                    current = possibleSolution.getNewSolution();
                    current.calculateSolution();
                    points = 5;
                    if (current.getTotalCost() < best.getTotalCost()) {
                        best = current;
                        points = 10;
                    }
                } else if (acceptanceProbability(deltaE2,temperature)) {
                    current = possibleSolution.getNewSolution();
                    current.calculateSolution();
                }
            }else{
                iteNoImprovement++;
            }

            countIterations++;
            temperature = startTemperature / Math.log(1 + countIterations * alfa);

            if(countIterations%5 == 0){
                for (int i =0 ; i < rewardDestroy.size(); i++){
                    double prev = rewardDestroy.get(i);
                    if(i == destroymethod){
                        rewardDestroy.set(i,update(prev,points));
                    }

                }
                for (int i =0 ; i < rewardRepair.size(); i++){
                    double prev = rewardRepair.get(i);
                    if(i == repairmethod){
                        rewardRepair.set(i,update(prev,points));
                    }

                }
                for (int i =0 ; i < rewardRemovalCandidates.size(); i++){
                    double prev = rewardRemovalCandidates.get(i);
                    if(i == removalCandidateMethod){
                        rewardRemovalCandidates.set(i,update(prev,points));
                    }

                }
            }else{
                //REWARD
                double prevD = rewardDestroy.get(destroymethod);
                double prevR = rewardRepair.get(repairmethod);
                double prevRC = rewardRemovalCandidates.get(removalCandidateMethod);
                rewardDestroy.set(destroymethod,(prevD + points));
                rewardRepair.set(repairmethod,(prevR + points));
                rewardRemovalCandidates.set(removalCandidateMethod,(prevRC + points));
            }

//            if ((currentTime - tempTime) > 60000) {
//                minutes++;
//                System.out.printf("The program is currently running %d minute(s) [%s iterations;  cost %s; duration %s; time wasted %s].%n", minutes, convert(countIterations), convert(best.getTotalCost()), convert(best.getTotalDuration()),convert(best.getTotalTimeWasted()));
//                tempTime = currentTime;
//            }
        }
        return best;
    }

    public static ArrayList<Integer> getRemovalCandidatesPreSuc(Solution curr, Solution best, Calculations c){
        Random random = new Random();
        if(best == null || curr.equals(best)){
            return new ArrayList<>();
        }
        ArrayList<Integer> candidates = new ArrayList<>();
        for(int i = 1 ; i <= c.blocks.size(); i++){
            Schedule currS = findSchedule(curr,i);
            Schedule bestS = findSchedule(best,i);

            int sizeC = currS.getBlocks().size();
            int sizeB = bestS.getBlocks().size();

            int indexC = currS.getBlocks().indexOf(i);
            int indexB = bestS.getBlocks().indexOf(i);

            if(sizeC == sizeB && sizeC == 1){
                continue;
            }else if(sizeC != sizeB && (sizeB == 1 || sizeC == 1)){
                candidates.add(i);
                continue;
            }
            if(indexC != indexB){
                candidates.add(i);
                continue;
            }else if(indexB == 0){
                if (!Objects.equals(currS.getBlocks().get(indexC + 1), bestS.getBlocks().get(indexB + 1))) {
                    candidates.add(i);
                    continue;
                }
            } else if ((indexB == (sizeB-1) || indexC == (sizeC-1))) {
                if(sizeC == sizeB){
                    if(!Objects.equals(currS.getBlocks().get(indexC - 1), bestS.getBlocks().get(indexB - 1))){
                        candidates.add(i);
                        continue;
                    }
                }else{
                    candidates.add(i);
                    continue;
                }
            }else{
                if(!Objects.equals(currS.getBlocks().get(indexC + 1), bestS.getBlocks().get(indexB + 1)) ||
                        !Objects.equals(currS.getBlocks().get(indexC - 1), bestS.getBlocks().get(indexB - 1))){
                    candidates.add(i);
                    continue;
                }
            }
            if(random.nextDouble() <= 0.3){
                candidates.add(i);
            }
        }
        return candidates;
    }

    public static ArrayList<Integer> getRemovalCandidatesDepot(Solution curr, Solution best, Calculations c){
        Random random = new Random();
        if(best == null || curr.equals(best)){
            return new ArrayList<>();
        }
        ArrayList<Integer> candidates = new ArrayList<>();
        for(int i = 1 ; i <= c.blocks.size(); i++){
            Schedule currS = findSchedule(curr,i);
            Schedule bestS = findSchedule(best,i);

            if(currS.getClosestDepot() != bestS.getClosestDepot()){
                candidates.add(i);
                continue;
            }
            if(random.nextDouble() <= 0.3){
                candidates.add(i);
            }
        }
        return candidates;
    }

    public static Schedule findSchedule(Solution sol, Integer block){
        for(Schedule s : sol.getSchedules()){
            if(s.getBlocks().contains(block)){
                return s;
            }
        }
        sol.calculateSolution();
        System.out.println("Error: Block not found in Schedules");
        System.out.println(block);
        System.out.println(sol);

        System.exit(0);
        return null;
    }

}

