package util.SA;

import model.PossibleSolution;
import model.Solution;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static util.LNS.LargeNeighbourhoodSearch.writeCsv;

public class SimulatedAnnealing {

    public static Solution runSimulation(Solution initialSolution, int maxDuration, Permutations permutations) throws Exception {
        ArrayList<Double> temperatureGraph = new ArrayList<>();
        ArrayList<Integer> costgraphBEST = new ArrayList<>();
        ArrayList<Integer> costgraphCURR = new ArrayList<>();
        ArrayList<Long> timegraph = new ArrayList<>();

        costgraphBEST.add(initialSolution.getTotalCost());
        costgraphCURR.add(initialSolution.getTotalCost());
        timegraph.add(0L);

        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long tempTime = System.currentTimeMillis();
        int minutes = 0;
        int countIterations = 0;
        double startTemperature = 50;
        double temperature = startTemperature;
        double alfa = 0.001;

        temperatureGraph.add(startTemperature);

        System.out.println("Starting Simulated Annealing...");

        Solution currentSolution = initialSolution;
        Solution bestSolution = currentSolution.clone();

        while ((currentTime - startTime) < maxDuration && temperature > 1) {
            PossibleSolution possibleSolution = new PossibleSolution();
            int permu = permutations.getRandomNumberInRange(0,1);

            switch (permu) {
                case 0 -> possibleSolution = permutations.switch2Blocks(currentSolution);
                case 1 -> possibleSolution = permutations.moveBlock(currentSolution);
            }

            if(possibleSolution != null){
                double deltaE = possibleSolution.getNewCost() - possibleSolution.getOldCost();
                if (deltaE < 0) {
                    currentSolution = possibleSolution.getNewSolution();
                    currentSolution.calculateSolution();
                    if (currentSolution.getTotalCost() < bestSolution.getTotalCost()) {
                        bestSolution = currentSolution;
                    }
                } else if (acceptanceProbability(deltaE, temperature)) {
                    currentSolution = possibleSolution.getNewSolution();
                    currentSolution.calculateSolution();
                }

            }

            countIterations++;
            temperature = startTemperature / Math.log(1 + countIterations * alfa);
            currentTime = System.currentTimeMillis();

            if ((currentTime - tempTime) > 60000) {
                minutes++;
                System.out.printf("The program is currently running %d minute(s) [%s iterations; temperature %f; cost %s; duration %s; time wasted %s].%n", minutes, convert(countIterations), temperature, convert(bestSolution.getTotalCost()), convert(bestSolution.getTotalDuration()),convert(bestSolution.getTotalTimeWasted()));
                tempTime = currentTime;
            }

            if(countIterations%1000 == 0){
                costgraphBEST.add(bestSolution.getTotalCost());
                costgraphCURR.add(currentSolution.getTotalCost());
                timegraph.add((currentTime-startTime));
                temperatureGraph.add(temperature);
            }

        }

        System.out.println("\nEndTemperature: " + convert((int) temperature));
        System.out.println("Iterations: " + convert(countIterations));
        System.out.println("Iterations/second: " + convert((countIterations/(maxDuration/1000))));
        System.out.println("FINISHED\n");

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateString = currentDate.format(formatter);
        String cost = String.valueOf(bestSolution.getTotalCost());
        writeCsv(costgraphBEST,costgraphCURR,timegraph,temperatureGraph,".//Data//Output//" + dateString + "_" + cost + "_" + (maxDuration/60000) + "min_Graph_SA.csv");

        return bestSolution;
    }

    private static boolean acceptanceProbability(double delta, double temperature) {
        if(Math.exp(-delta / temperature) > Math.random()){
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

    public static void writeCsv(ArrayList<Integer> dataBest, ArrayList<Integer> dataCurr, ArrayList <Long> time, ArrayList<Double> temp , String filename) {

        try {
            FileWriter writer = new FileWriter(filename);

            for (int i = 0; i < dataBest.size(); i++) {
                StringBuilder strD = new StringBuilder();
                StringBuilder strR = new StringBuilder();
                writer.append(dataBest.get(i).toString()).append(",")
                        .append(dataCurr.get(i).toString()).append(",")
                        .append(temp.get(i).toString()).append(",")
                        .append(",")
                        .append(strD)
                        .append(",")
                        .append(strR)
                        .append(",")
                        .append(time.get(i).toString());
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
