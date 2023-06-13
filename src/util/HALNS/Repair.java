package util.HALNS;

import model.Block;
import model.PossibleSolution;
import model.Schedule;
import model.Solution;
import util.LNS.GreedyLNSAlgo;
import util.LNS.InfoBestFit;
import util.LNS.InfoRegretFit;
import util.algorithms.BlockComparator;
import util.algorithms.Calculations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import static util.HALNS.ALNS.findSchedule;

public class Repair {
    static Calculations c;
    private static ArrayList<Block> blocks;
    private static ArrayList<Block> timeblocks;
    private static ArrayList<Integer> removedBlocks;
    private static ArrayList<Schedule> changedSchedules;

    public Repair(Calculations c) {
        Repair.c = c;
        blocks = c.blocks;
        removedBlocks = new ArrayList<>();
        changedSchedules = new ArrayList<>();
        timeblocks = new ArrayList<>(blocks);
        timeblocks.sort(new BlockComparator());
    }

    //=============================== DESTROY =========================================

    public PossibleSolution randomDestruct(Solution solution, int destructions, int repairmethod, ArrayList<Integer> candidates) {
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

        removedBlocks.clear();
        int removed = 0;

        //REMOVE
        if(candidates.isEmpty()){
            while (removed < destructions) {
                int sId1;
                Schedule s1 = null;
                while (s1 == null || s1.getBlocks().isEmpty()) {
                    sId1 = getRandomNumberInRange(0, newSolution.getSchedules().size() - 1);
                    s1 = newSolution.getSchedules().get(sId1);
                    if (s1 == null) {
                        System.out.println("Schedule was null");
                        return null;
                    }
                }
                Schedule tempS1 = new Schedule(s1);

                //Random Block from schedule removed
                int max1 = tempS1.getBlocks().size() - 1;
                int blockIndex1 = getRandomNumberInRange(0, max1);
                if (tempS1.getBlocks().size() == 1) {
                    blockIndex1 = 0;
                }
                Integer block1 = tempS1.getBlocks().get(blockIndex1);
                tempS1.getBlocks().remove(block1);

                c.calculateSchedule(tempS1);

                if (checkRemove(tempS1, blockIndex1)) {
                    //Keep track of the removed blocks
                    removedBlocks.add(block1);
                    newSolution.getSchedules().remove(s1);

                    if (!tempS1.getBlocks().isEmpty()) {
                        newSolution.getSchedules().add(tempS1);
                    }
                    removed++;
                }
            }
        }else{
            if(destructions > candidates.size()){
                destructions = candidates.size();
            }
            while (removed < destructions) {
                int index = getRandomNumberInRange(0,candidates.size()-1);
                Integer block1 = candidates.get(index);

                Schedule s = findSchedule(newSolution,block1);
                Schedule tempS1 = new Schedule(s);

                int blockIndex1 = tempS1.getBlocks().indexOf(block1);
                tempS1.getBlocks().remove(block1);

                c.calculateSchedule(tempS1);

                if (checkRemove(tempS1, blockIndex1)) {
                    //Keep track of the removed blocks
                    removedBlocks.add(block1);
                    newSolution.getSchedules().remove(s);
                    candidates.remove(block1);
                    if (!tempS1.getBlocks().isEmpty()) {
                        newSolution.getSchedules().add(tempS1);
                    }
                    removed++;
                }
            }
        }



        if (repairmethod == 0) {
            //BEST FIT REPAIR METHOD
            result = bestFitRepair(newSolution, result);
        } else {
            //REGRET FIT REPAIR METHOD
            result = bestRegretFitRepair(newSolution, result);
        }
        return result;
    }

    public PossibleSolution destructByTime(Solution solution, int destructions, int repairmethod, ArrayList<Integer> candidates) {
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

        removedBlocks.clear();



        if(candidates.isEmpty()) {
            int indexOrigin = getRandomNumberInRange(0, timeblocks.size() - 1);
            int offset = destructions / 2;

            for (int i = (indexOrigin - offset); i <= (indexOrigin + offset); i++) {
                if (i < 0) {
                    continue;
                }
                if (i >= timeblocks.size()) {
                    break;
                }

                Block b = timeblocks.get(i);
                Integer bINT = b.getId();

                //find schedule
                for (Schedule s : newSolution.getSchedules()) {
                    if (s.getBlocks().contains(bINT)) {
                        Schedule tempS1 = new Schedule(s);
                        int index = tempS1.getBlocks().indexOf(bINT);
                        tempS1.getBlocks().remove(bINT);
                        c.calculateSchedule(tempS1);
                        if (checkRemove(tempS1, index)) {
                            removedBlocks.add(bINT);
                            newSolution.getSchedules().remove(s);
                            if (!tempS1.getBlocks().isEmpty()) {
                                newSolution.getSchedules().add(tempS1);
                            }
                        }
                        break;
                    }
                }
            }
        }else{
            if(destructions > candidates.size()){
                destructions = candidates.size();
            }
            ArrayList<Block> candidatesBlocks = new ArrayList<>();
            for(Block b : timeblocks){
                if (candidates.contains(b.getId())){
                    candidatesBlocks.add(b);
                }
            }
            int indexOrigin = getRandomNumberInRange(0, candidatesBlocks.size() - 1);
            int offset = destructions / 2;
            for (int i = (indexOrigin - offset); i <= (indexOrigin + offset); i++) {
                if (i < 0) {
                    continue;
                }
                if (i >= candidatesBlocks.size()) {
                    break;
                }

                Block b = candidatesBlocks.get(i);
                Integer bINT = b.getId();

                //find schedule
                for (Schedule s : newSolution.getSchedules()) {
                    if (s.getBlocks().contains(bINT)) {
                        Schedule tempS1 = new Schedule(s);
                        int index = tempS1.getBlocks().indexOf(bINT);
                        tempS1.getBlocks().remove(bINT);
                        c.calculateSchedule(tempS1);
                        if (checkRemove(tempS1, index)) {
                            removedBlocks.add(bINT);
                            newSolution.getSchedules().remove(s);
                            if (!tempS1.getBlocks().isEmpty()) {
                                newSolution.getSchedules().add(tempS1);
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (repairmethod == 0) {
            //BEST FIT REPAIR METHOD
            result = bestFitRepair(newSolution, result);
        } else {
            //REGRET FIT REPAIR METHOD
            result = bestRegretFitRepair(newSolution, result);
        }
        return result;
    }

    public PossibleSolution destructByLocation(Solution solution, int repairmethod) {
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

        removedBlocks.clear();

        //Find Random Location
        int max = c.stations.size();
        int station = getRandomNumberInRange(1, max);


        for (Block b : blocks) {
            if (b.getStartLoc() == station || b.getEndLoc() == station) {
                Integer bINT = b.getId();
                //find schedule
                for (Schedule s : newSolution.getSchedules()) {
                    if (s.getBlocks().contains(bINT)) {
                        Schedule tempS1 = new Schedule(s);
                        int index = tempS1.getBlocks().indexOf(bINT);
                        tempS1.getBlocks().remove(bINT);
                        c.calculateSchedule(tempS1);
                        if (checkRemove(tempS1, index)) {
                            removedBlocks.add(bINT);
                            newSolution.getSchedules().remove(s);
                            if (!tempS1.getBlocks().isEmpty()) {
                                newSolution.getSchedules().add(tempS1);
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (repairmethod == 0) {
            //BEST FIT REPAIR METHOD
            result = bestFitRepair(newSolution, result);
        } else {
            //REGRET FIT REPAIR METHOD
            result = bestRegretFitRepair(newSolution, result);
        }
        return result;
    }

    public PossibleSolution destructSchedule(Solution solution, int repairmethod){
        Solution oldSolution = new Solution(solution);
        Solution newSolution = oldSolution.clone();

        oldSolution.calculateCost();
        int oldCost = oldSolution.getTotalCost();

        PossibleSolution result = new PossibleSolution();
        result.setOldSolution(oldSolution);
        result.setOldCost(oldCost);

        removedBlocks.clear();

        //Find Random Schedule
        int max = newSolution.getSchedules().size() -1;
        int schedule = getRandomNumberInRange(0, max);

        Schedule destroyed = newSolution.getSchedules().get(schedule);

        removedBlocks.addAll(destroyed.getBlocks());

        newSolution.getSchedules().remove(destroyed);

        if (repairmethod == 0) {
            //BEST FIT REPAIR METHOD
            result = bestFitRepair(newSolution, result);
        } else {
            //REGRET FIT REPAIR METHOD
            result = bestRegretFitRepair(newSolution, result);
        }
        return result;

    }

    private boolean checkRemove(Schedule s, int index) {
        if (s.getBlocks().isEmpty()) {
            return true;
        }
        if (index == 0 || index == s.getBlocks().size()) {
            return c.checkSchedule(s);
        }
        Integer before = s.getBlocks().get(index - 1);
        Integer after = s.getBlocks().get(index);
        if (c.consmatrix[before][after] == 1) {
            return c.checkSchedule(s);
        } else {
            return false;
        }
    }

    //=============================== REPAIR =========================================
    public PossibleSolution bestFitRepair(Solution newSolution, PossibleSolution result) {

        ArrayList<InfoBestFit> bestFits = new ArrayList<>();
        GreedyLNSAlgo greedyLNS = new GreedyLNSAlgo(c);

        //FIND BEST FIT FOR EVERY REMOVED BLOCK
        changedSchedules.clear();
        for (Integer block : removedBlocks) {
            InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block - 1), newSolution.getSchedules());
            if (bestFit == null) {
                return null;
            } else {
                bestFits.add(bestFit);
            }
        }

        //RE-ADD THE REMOVED BLOCKS STARTING FROM THE LOWEST COST
        while (!bestFits.isEmpty()) {
            bestFits.sort(new BestFitComparator());
            newSolution.insertBestFit(bestFits.get(0));
            c.calculateSchedule(newSolution.getScheduleByID(bestFits.get(0).getScheduleID()));
            changedSchedules.add(newSolution.getScheduleByID(bestFits.get(0).getScheduleID()));
            removedBlocks.remove(bestFits.get(0).getBlock());
            bestFits.remove(0);

            if (bestFits.isEmpty()) {
                break;
            }
            //IF MULTIPLE BLOCKS ARE PLANNED TO BE ADDED TO THE SAME SCHEDULE, WE RECHECK THEIR BEST FIT
            for (InfoBestFit b : bestFits) {
                if (changedSchedules.get(0) == newSolution.getScheduleByID(b.getScheduleID())) {
                    InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(b.getBlock() - 1), newSolution.getSchedules());
                    if (bestFit == null) {
                        return null;
                    } else {
                        //RESET THE CURRENT BESTFIT
                        b.setCost(bestFit.getCost());
                        b.setBlock(bestFit.getBlock());
                        b.setIndex(bestFit.getIndex());
                        b.setScheduleID(bestFit.getScheduleID());
                    }
                }
            }

            for (Integer block : removedBlocks) {
                InfoBestFit bestFit = greedyLNS.bestFitBlock(blocks.get(block - 1), changedSchedules);
                if (bestFit != null) {
                    for (InfoBestFit bestFitOld : bestFits) {
                        if (Objects.equals(bestFitOld.getBlock(), bestFit.getBlock()) && bestFitOld.getCost() > bestFit.getCost()) {
                            bestFits.remove(bestFitOld);
                            bestFits.add(bestFit);
                            break;
                        }
                    }
                }
            }
            changedSchedules.remove(0);
        }

        newSolution.calculateSolution();
        result.setNewSolution(newSolution);
        result.setNewCost(newSolution.getTotalCost());
        return result;
    }

    public PossibleSolution bestRegretFitRepair(Solution newSolution, PossibleSolution result) {

        ArrayList<InfoRegretFit> bestRegrets = new ArrayList<>();
        GreedyLNSAlgo greedyLNS = new GreedyLNSAlgo(c);

        //FIND BEST FIT FOR EVERY REMOVED BLOCK
        changedSchedules.clear();
        for (Integer block : removedBlocks) {
            InfoRegretFit bestFit = greedyLNS.bestRegretFitBlock(blocks.get(block - 1), newSolution.getSchedules());
            if (bestFit == null) {
                return null;
            } else {
                bestRegrets.add(bestFit);
            }
        }

        //RE-ADD THE REMOVED BLOCKS STARTING FROM THE LOWEST COST
        while (!bestRegrets.isEmpty()) {
            bestRegrets.sort(new RegretFitComparator());
            newSolution.insertRegretFit(bestRegrets.get(0));
            c.calculateSchedule(newSolution.getScheduleByID(bestRegrets.get(0).getScheduleID()));
            changedSchedules.add(newSolution.getScheduleByID(bestRegrets.get(0).getScheduleID()));
            removedBlocks.remove(bestRegrets.get(0).getBlock());
            bestRegrets.remove(0);

            if (bestRegrets.isEmpty()) {
                break;
            }
            //IF MULTIPLE BLOCKS ARE PLANNED TO BE ADDED TO THE SAME SCHEDULE, WE RECHECK THEIR BEST FIT
            for (InfoRegretFit b : bestRegrets) {
                if (changedSchedules.get(0) == newSolution.getScheduleByID(b.getScheduleID())) {
                    InfoRegretFit bestFit = greedyLNS.bestRegretFitBlock(blocks.get(b.getBlock() - 1), newSolution.getSchedules());
                    if (bestFit == null) {
                        return null;
                    } else {
                        //RESET THE CURRENT BESTFIT
                        b.setCost(bestFit.getCost());
                        b.setSecondCost(bestFit.getSecondCost());
                        b.setBlock(bestFit.getBlock());
                        b.setIndex(bestFit.getIndex());
                        b.setScheduleID(bestFit.getScheduleID());
                    }
                }
            }

            for (Integer block : removedBlocks) {
                InfoRegretFit bestFit = greedyLNS.bestRegretFitBlock(blocks.get(block - 1), changedSchedules);
                if (bestFit != null) {
                    for (InfoRegretFit bestFitOld : bestRegrets) {
                        if (Objects.equals(bestFitOld.getBlock(), bestFit.getBlock()) && bestFitOld.getRegret() > bestFit.getRegret()) {
                            bestRegrets.remove(bestFitOld);
                            bestRegrets.add(bestFit);
                            break;
                        }
                    }
                }
            }
            changedSchedules.remove(0);
        }

        newSolution.calculateSolution();
        result.setNewSolution(newSolution);
        result.setNewCost(newSolution.getTotalCost());
        return result;
    }


    //=============================== UTIL =========================================
    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) return 1;
        return c.random.nextInt((max - min) + 1) + min;
    }
}


//=============================== SORTING =========================================
class BestFitComparator implements Comparator<InfoBestFit> {
    @Override
    public int compare(InfoBestFit o1, InfoBestFit o2) {
        Double cost1 = o1.getCost();
        Double cost2 = o2.getCost();
        return cost1.compareTo(cost2);
    }
}

class RegretFitComparator implements Comparator<InfoRegretFit> {
    @Override
    public int compare(InfoRegretFit o1, InfoRegretFit o2) {
        Double cost1 = o1.getRegret();
        Double cost2 = o2.getRegret();
        return cost2.compareTo(cost1);
    }
}