package util.SA;

import model.PossibleSolution;
import model.Solution;

public class SimulatedAnnealing {

    public static Solution runSimulation(Solution initialSolution, int maxDuration, Permutations permutations) throws Exception {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long tempTime = System.currentTimeMillis();
        int minutes = 0;
        int countIterations = 0;

        double temperature = 1000000000;
        double alfa = 0.9999999;

        System.out.println("Starting Simulated Annealing...");

        Solution currentSolution = initialSolution;
        Solution bestSolution = initialSolution;

        while ((currentTime - startTime) < maxDuration && temperature > 1) {
            PossibleSolution possibleSolution = new PossibleSolution();
            int permu = permutations.getRandomNumberInRange(0,1);

            switch (permu) {
                case 0 -> possibleSolution = permutations.switch2Blocks(currentSolution);
                case 1 -> possibleSolution = permutations.moveBlock(currentSolution);
            }

            if(possibleSolution != null){

                double deltaE = possibleSolution.getNewCost() - possibleSolution.getOldCost();
                if (deltaE < 0 || acceptanceProbability(deltaE, temperature)) {
                    currentSolution = possibleSolution.getNewSolution();
                }
                if (currentSolution.getTotalCost() < bestSolution.getTotalCost()) {
                    bestSolution = currentSolution;
                }
            }

            countIterations++;
            temperature = alfa * temperature;
            currentTime = System.currentTimeMillis();

            if ((currentTime - tempTime) > 60000) {
                minutes++;
                System.out.printf("The program is currently running %d minute(s) [%s iterations; temperature %f; cost %s; duration %s; time wasted %s].%n", minutes, convert(countIterations), temperature, convert(bestSolution.getTotalPaymentDrivers()), convert(bestSolution.getTotalDuration()),convert(bestSolution.getTotalTimeWasted()));
                tempTime = currentTime;
            }
        }

        System.out.println("\nEndTemperature: " + convert((int) temperature));
        System.out.println("Iterations: " + convert(countIterations));
        System.out.println("Iterations/second: " + convert((countIterations/maxDuration)));
        System.out.println("FINISHED\n");
        return bestSolution;
    }

    private static boolean acceptanceProbability(double delta, double temperature) {
        return Math.exp(-delta / temperature) > Math.random();
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
