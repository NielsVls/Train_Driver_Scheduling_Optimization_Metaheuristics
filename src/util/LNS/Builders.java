package util.LNS;

import global.Parameters;
import model.*;
import util.algorithms.Calculations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static util.LNS.LargeNeighbourhoodSearch.getRandomNumberInRange;

public class Builders {
    Calculations c;
    private ArrayList<Block> blocks;
    private ArrayList<Station> stations;
    private ArrayList<Station> breakStations;
    private int[][] consmatrix;
    private int[][] consbreakmatrix;
    private int[][] travelmatrix;
    private Parameters parameters;

    public Builders(Calculations c){
        this.c = c;
        this.blocks = c.blocks;
        this.stations = c.stations;
        this.breakStations = c.breakStations;
        this.consmatrix = c.consmatrix;
        this.consbreakmatrix = c.consbreakmatrix;
        this.travelmatrix = c.travelmatrix;
        this.parameters = c.parameters;
    }


    public PossibleSolution build(Solution solution, int destructions) throws Exception {
        solution.calculateSolution();
        //Making the new solution and list of schedules we can edit
        //Solution newSolution = new Solution(solution);
        Solution newSolution = new Solution();
        newSolution.setSchedules(solution.getSchedules());
        newSolution = destruct(newSolution,destructions);

//        solution.calculateBlocks();
//        System.out.println("OLD "+ solution.getBlocksExecuted());
//        newSolution.calculateBlocks();
//        System.out.println("NEW "+ newSolution.getBlocksExecuted());

        ArrayList<Schedule> schedules = newSolution.getSchedules();
        if(schedules == null){
            return null;
        }else{
            newSolution.calculateCost();
            newSolution.calculateBlocks();
            return new PossibleSolution(newSolution, solution, solution.getTotalTimeWasted(), newSolution.getTotalTimeWasted());
        }
    }

    public Solution destruct(Solution solution, int destructions) throws Exception {
        ArrayList<Schedule> schedules = solution.getSchedules();
        ArrayList<Integer> changedSchedules = new ArrayList<>();
        ArrayList<Integer> removedBlocks = new ArrayList<>();

        int removed = 0;

        while(removed < destructions){
            int sId1 = 0;
            Schedule s1 = schedules.get(sId1);
            while (s1 == null) {
                sId1 = getRandomNumberInRange(0, schedules.size() - 1);
                s1 = schedules.get(sId1);
                if (s1 == null) return null;
            }

            c.calculateSchedule(s1);
            Schedule tempS1 = new Schedule(s1);

            //keep track of the adjusted schedules
            changedSchedules.add(tempS1.getId());

            //Random Block from schedule removed
            int max1 = tempS1.getBlocks().size() - 1;
            int blockIndex1 = getRandomNumberInRange(0, max1);
            if (tempS1.getBlocks().size() == 1) {
                blockIndex1 = 0;
            }
            Integer block1 = tempS1.getBlocks().get(blockIndex1);
            tempS1.getBlocks().remove(blockIndex1);
            //Keep track of the removed blocks
            removedBlocks.add(block1);
            schedules.remove(s1);
            if(!tempS1.getBlocks().isEmpty()){
                schedules.add(tempS1);
            }
            removed++;
        }
        solution.setSchedules(schedules);
        solution.calculateBlocks();
        construct2(solution,changedSchedules,removedBlocks);

        return solution;
    }

//    public ArrayList<Schedule> construct(ArrayList<Schedule> schedules, ArrayList<Integer> changedSchedules, ArrayList<Integer> removedBlocks) throws Exception {
//
//        //Adjusting the list where the blocks can be added
//        ArrayList<Schedule> neighbourhood = new ArrayList<>(schedules);
//        for(Schedule s : schedules){
//            if(changedSchedules.contains(s.getId())){
//                neighbourhood.remove(s);
//            }
//        }
//        GreedyLNS greedyLNS = new GreedyLNS(c);
//        while(!removedBlocks.isEmpty()){
//            ArrayList<InfoBestFit> bestfits = new ArrayList<>();
//            for(Integer block : removedBlocks){
//                bestfits.add(greedyLNS.bestFitBlock(blocks.get(block-1),neighbourhood));
//            }
//            InfoBestFit bestFit = searchBestFit(bestfits);
//            if(bestFit == null){
//                //System.out.println("Did not find a valid spot for a block!");
//                return new ArrayList<>();
//                }else{
//                insert(bestFit,neighbourhood);
//                removedBlocks.remove(bestFit.block);
//            }
//        }
//        for(Schedule s : schedules){
//            if(changedSchedules.contains(s.getId())){
//                neighbourhood.add(s);
//            }
//        }
//        return neighbourhood;
//    }

    public InfoBestFit searchBestFit(ArrayList<InfoBestFit> bestFits) {
        InfoBestFit bestFit = null;
        int cost = 999999;
        for(InfoBestFit fit : bestFits){
            if(fit != null && fit.getCost() < cost){
                bestFit = fit;
            }
        }
        return bestFit;
    }

    public void insert (InfoBestFit fit, Solution solution){
        for (Schedule s : solution.getSchedules()){
            if(s.getId() == fit.getScheduleID()){
                s.getBlocks().add(fit.getIndex(),fit.getBlock());
                c.calculateSchedule(s);
                return;
            }
        }
    }

    // OPTION 2 NEW METHOD/ 1SUCKS

    public void construct2(Solution solution, ArrayList<Integer> changedSchedules, ArrayList<Integer> removedBlocks) throws Exception {

        ArrayList<Schedule> schedules = solution.getSchedules();

        //Adjusting the list where the blocks can be added
        //ArrayList<Schedule> neighbourhood = new ArrayList<>(schedules);
        ArrayList<InfoBestFit> bestFits = new ArrayList<>();
//        for(Schedule s : schedules){
//            if(changedSchedules.contains(s.getId())){
//                neighbourhood.remove(s);
//            }
//        }
        GreedyLNS greedyLNS = new GreedyLNS(c);
        Collections.shuffle(removedBlocks);
        for(Integer block : removedBlocks){
            InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block-1),solution);
            if(bestFit == null){
                solution.setSchedules(null);
                break;
            }else{
                bestFits.add(bestFit);
                insert(bestFit,solution);
            }
        }
//        for(InfoBestFit bf: bestFits){
//            System.out.println(bf);
//        }
//        System.out.println("----------------------------");
//        for(Schedule s : schedules){
//            if(changedSchedules.contains(s.getId())){
//                System.out.println(s);
//            }
//        }
//        System.out.println("----------------------------");
    }

}
