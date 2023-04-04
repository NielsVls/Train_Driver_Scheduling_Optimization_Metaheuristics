package util.LNS;

import model.PossibleSolution;
import model.Schedule;
import model.Solution;

import java.util.Random;

public class LargeNeighbourhoodSearch {

    private static boolean acceptanceProbability(double delta, double temperature) {
        if(Math.exp(-delta / temperature) > Math.random()){
            System.out.println("worse accepted");
            return true;
        }else return false;
    }

    public static void count(Solution s){
        int count = 0;
        for(Schedule sc : s.getSchedules()){
            count += sc.getBlocks().size();
        }
        System.out.println("COUNT : " + count);
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

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        Random r = new Random();
        int number = r.nextInt((max - min) + 1) + min;
        return number;
    }

    public static Solution runSimulationTMP(Solution initial, int maxDuration, DestroyRepair builders) {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long tempTime = System.currentTimeMillis();
        int minutes = 0;
        int countIterations = 0;

        Solution current = initial;
        Solution best = initial.clone();


        System.out.println("\nStarting Large Neighbourhood Search...\n");

        while((currentTime - startTime) < maxDuration){
            PossibleSolution possibleSolution;

            int destructions = getRandomNumberInRange(20,100);

            possibleSolution = builders.destruct2(current,destructions);

            if(possibleSolution != null) {
                //System.out.println("VALID NEW SOLU");
                double deltaE2 = possibleSolution.getNewCost() - possibleSolution.getOldCost();
                if (deltaE2 < 0) {
                    System.out.println("OLD " + possibleSolution.getOldCost());
                    System.out.println("NEW " + possibleSolution.getNewCost());
                    current = possibleSolution.getNewSolution();
                }
                if (current.getTotalCost() < best.getTotalCost()) {
                    best = possibleSolution.getNewSolution();
                }
            }

            countIterations++;
            currentTime = System.currentTimeMillis();

            if ((currentTime - tempTime) > 60000) {
                minutes++;
                System.out.printf("The program is currently running %d minute(s) [%s iterations;  cost %s; duration %s; time wasted %s].%n", minutes, convert(countIterations), convert(best.getTotalPaymentDrivers()), convert(best.getTotalDuration()),convert(best.getTotalTimeWasted()));
                tempTime = currentTime;
            }
        }

        System.out.println("Iterations: " + convert(countIterations));
        System.out.println("Iterations/second: " + convert((countIterations/maxDuration)));
        System.out.println("FINISHED");
        return best;
    }

}
