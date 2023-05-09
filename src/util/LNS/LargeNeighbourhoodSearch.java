package util.LNS;

import model.PossibleSolution;
import model.Solution;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

import static util.LNS.Rebuild.c;
import static util.LNS.Rebuild.getRandomNumberInRange;

public class LargeNeighbourhoodSearch {

    private static boolean acceptanceProbability(double delta, double temperature) {
        if(Math.exp(-delta / temperature) > Math.random()){
            System.out.println("worse accepted");
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
        //System.out.println("REWARD : " + rewards);
        ArrayList<Double> probs = new ArrayList<>();
        double sum = 0;
        for(Double d : rewards){
            sum += d;
        }
        for (Double d : rewards){
            double p = d/sum;
            probs.add(p);
        }
        //System.out.println("PROBABILITY : " + probs);
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

    public static Solution runSimulationTMP(Solution initial, int maxDuration, Rebuild builders) {
        ArrayList<Integer> costgraph = new ArrayList<>();
        ArrayList<Long> timegraph = new ArrayList<>();
        costgraph.add(initial.getTotalCost());
        timegraph.add(0L);
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long tempTime = System.currentTimeMillis();
        int minutes = 0;
        int countIterations = 0;

        ArrayList<Double> rewardDestroy = new ArrayList<>();
        ArrayList<Double> rewardRepair = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            rewardDestroy.add(10.0);
        }
        for (int i = 0; i < 2; i++) {
            rewardRepair.add(10.0);
        }

        Solution current = initial;
        Solution best = current.clone();

        System.out.println("\nStarting Large Neighbourhood Search...\n");

        while((currentTime - startTime) < maxDuration){
            PossibleSolution possibleSolution = null;

            int destroymethod = randomPicker(calculateProbability(rewardDestroy));
            int repairmethod = randomPicker(calculateProbability(rewardRepair));
            int destructions;

            String destroy = "";
            String repair = "";

            repair = (repairmethod == 1) ? "REGRET FIT" : "BEST FIT";

            switch (destroymethod) {
                case 0 : destructions = getRandomNumberInRange(10,30);
                         possibleSolution = builders.randomDestruct(current, destructions, repairmethod);
                         destroy = "RANDOM 10-30";
                         break;
                case 1 : destructions = getRandomNumberInRange(35,65);
                         possibleSolution = builders.randomDestruct(current, destructions, repairmethod);
                         destroy = "RANDOM 35-65";
                         break;
                case 2 : destructions = getRandomNumberInRange(100,150);
                         possibleSolution = builders.randomDestruct(current, destructions, repairmethod);
                         destroy = "RANDOM 100-150";
                         break;
                case 3 : destructions = getRandomNumberInRange(10,30);
                         possibleSolution = builders.destructByTime(current, destructions, repairmethod);
                         destroy = "TIME 10-30";
                         break;
                case 4 : destructions = getRandomNumberInRange(25,50);
                         possibleSolution = builders.destructByTime(current, destructions, repairmethod);
                         destroy = "TIME 25 - 50";
                         break;
                case 5 : possibleSolution = builders.destructByLocation(current,repairmethod);
                         destroy = "LOCATION";
                         break;
                case 6 : possibleSolution = builders.destructSchedule(current,repairmethod);
                         destroy = "SCHEDULE";
                         break;
            }
            int points =0;

            if(possibleSolution != null) {
                double deltaE2 = possibleSolution.getNewCost() - possibleSolution.getOldCost();
                if (deltaE2 < 0) {
                    current = possibleSolution.getNewSolution();
                    current.calculateSolution();
                    points = 5;
                }
                if (current.getTotalCost() < best.getTotalCost()) {
                    best = current;
                    points = 10;
                }
                if(points == 5){
                    System.out.println("5");
                }
            }

            countIterations++;
            currentTime = System.currentTimeMillis();

            //System.out.println(destroy + " || " + repair + " ==> " + points + " points.");

            if(countIterations%10 == 0){
                for (int i =0 ; i < rewardDestroy.size(); i++){
                    double prev = rewardDestroy.get(i);
                    if(i == destroymethod){
                        rewardDestroy.set(i,update(prev,points));
                    }else{
                        //rewardDestroy.set(i,update(prev,0));
                    }
                }
                for (int i =0 ; i < rewardRepair.size(); i++){
                    double prev = rewardRepair.get(i);
                    if(i == repairmethod){
                        rewardRepair.set(i,update(prev,points));
                    }else{
                       // rewardRepair.set(i,update(prev,0));
                    }
                }
                //System.out.println("AFTER UPDATE : DESTROY : " + rewardDestroy);
                //System.out.println("AFTER UPDATE : REPAIR : " + rewardRepair);
            }else{
                //REWARD
                double prevD = rewardDestroy.get(destroymethod);
                double prevR = rewardRepair.get(repairmethod);
                rewardDestroy.set(destroymethod,(prevD + points));
                rewardRepair.set(repairmethod,(prevR + points));
                //System.out.println("POINTS ADDED :" + points);
            }

            if ((currentTime - tempTime) > 60000) {
                minutes++;
                System.out.printf("The program is currently running %d minute(s) [%s iterations;  cost %s; duration %s; time wasted %s].%n", minutes, convert(countIterations), convert(best.getTotalCost()), convert(best.getTotalDuration()),convert(best.getTotalTimeWasted()));
                tempTime = currentTime;
            }

            costgraph.add(best.getTotalCost());
            timegraph.add((currentTime-startTime));
        }

        System.out.println("Iterations: " + convert(countIterations));
        System.out.println("Iterations/second: " + convert((countIterations/(maxDuration/1000))));
        System.out.println("FINISHED");

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = currentDate.format(formatter);
        String cost = String.valueOf(best.getTotalCost());
        writeCsv(costgraph,timegraph,".//Data//Output//" + dateString + "_" + cost + ".csv");
        return best;
    }

    public static void writeCsv(ArrayList<Integer> data, ArrayList <Long> time, String filename) {

        try {
            FileWriter writer = new FileWriter(filename);

            for (int i = 0; i < data.size(); i++) {
                writer.append(data.get(i).toString()).append(",").append(time.get(i).toString());
                writer.append("\n");
            }

            writer.flush();
            writer.close();
            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
