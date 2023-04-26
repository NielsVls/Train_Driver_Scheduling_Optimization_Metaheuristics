package util.LNS;

import model.PossibleSolution;
import model.Solution;

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

    public static Solution runSimulationTMP(Solution initial, int maxDuration, Rebuild builders, int min, int max) {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long tempTime = System.currentTimeMillis();
        int minutes = 0;
        int countIterations = 0;

        Solution current = initial;
        Solution best = current.clone();


        System.out.println("\nStarting Large Neighbourhood Search...\n");

        while((currentTime - startTime) < maxDuration){
            PossibleSolution possibleSolution;

            int destructions = getRandomNumberInRange(min,max);
            int destroymethod = getRandomNumberInRange(1,2);
            int repairmethod = getRandomNumberInRange(1,2);


            possibleSolution = switch (destroymethod) {
                case 1 -> builders.randomDestruct(current, destructions, repairmethod);
                case 2 -> builders.destructByTime(current, destructions, repairmethod);
                default -> builders.randomDestruct(current, destructions, repairmethod);
            };

            if(possibleSolution != null) {
                double deltaE2 = possibleSolution.getNewCost() - possibleSolution.getOldCost();
                if (deltaE2 < 0) {
                    current = possibleSolution.getNewSolution();
                    current.calculateSolution();
                }
                if (current.getTotalCost() < best.getTotalCost()) {
                    best = current;
                }
            }

            countIterations++;
            currentTime = System.currentTimeMillis();

            if ((currentTime - tempTime) > 60000) {
                minutes++;
                System.out.printf("The program is currently running %d minute(s) [%s iterations;  cost %s; duration %s; time wasted %s].%n", minutes, convert(countIterations), convert(best.getTotalCost()), convert(best.getTotalDuration()),convert(best.getTotalTimeWasted()));
                tempTime = currentTime;
            }
        }

        System.out.println("Iterations: " + convert(countIterations));
        System.out.println("Iterations/second: " + convert((countIterations/(maxDuration/1000))));
        System.out.println("FINISHED");
        return best;
    }

}
