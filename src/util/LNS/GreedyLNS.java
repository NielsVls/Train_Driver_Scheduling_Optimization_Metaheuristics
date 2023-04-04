package util.LNS;

import global.Parameters;
import model.Block;
import model.Schedule;
import model.Solution;
import model.Station;
import util.algorithms.Calculations;

import java.sql.SQLOutput;
import java.util.ArrayList;


public class GreedyLNS {

    Calculations c;
    ArrayList<Block> blocks;
    ArrayList<Station> stations;
    ArrayList<Station> breakStations;
    ArrayList<Station> depots;
    Parameters parameters;
    int[][] travelmatrix;
    int[][] consmatrix;
    int[][] consbreakmatrix;

    public GreedyLNS(Calculations c) {
        this.c =c;
        this.blocks = c.blocks;
        this.stations = c.stations;
        this.breakStations = c.breakStations;
        this.depots = c.depots;
        this.parameters = c.parameters;
        this.travelmatrix = c.travelmatrix;
        this.consmatrix = c.consmatrix;
        this.consbreakmatrix = c.consbreakmatrix;
    }

    //True means that Block b is added to Schedule s
    public InfoBestFit bestFitBlock(Block block, ArrayList<Schedule> schedules){
        Integer b = block.getId();
        int cost = 9999999;
        int indexInSchedule = -1;
        int scheduleID = -1;
        for (Schedule s: schedules) {
            Schedule temp = new Schedule(s);
            for(int i = 0; i <= s.getBlocks().size(); i++){
                if(i == 0){
                    Integer blockAfter = temp.getBlocks().get(0);
                    if(consmatrix[b][blockAfter] == 1){
                        temp.getBlocks().add(0,b);
                        c.calculateSchedule(temp);
                        if(c.checkSchedule(temp)){
                            int sCost = calculateCost2(s,temp);
                            if(sCost < cost){
                                cost = sCost;
                                indexInSchedule = 0;
                                scheduleID = s.getId();
                            }
                        }
                        temp.getBlocks().remove(b);
                        break;
                    }
                } else if (i == temp.getBlocks().size()) {
                    Integer blockBefore = temp.getBlocks().get(temp.getBlocks().size()-1);
                    if(consmatrix[blockBefore][b] == 1){
                        temp.getBlocks().add(b);
                        c.calculateSchedule(temp);
                        if(c.checkSchedule(temp)){
                            int sCost = calculateCost2(s,temp);
                            if(sCost < cost){
                                cost = sCost;
                                indexInSchedule = i;
                                scheduleID = s.getId();
                            }
                        }
                        temp.getBlocks().remove(b);
                        break;
                    }
                } else {
                    Integer blockBefore = temp.getBlocks().get(i-1);
                    Integer blockAfter = temp.getBlocks().get(i);
                    if(consmatrix[blockBefore][b] == 1 && consmatrix[b][blockAfter] == 1){
                        temp.getBlocks().add(i,b);
                        c.calculateSchedule(temp);
                        if(c.checkSchedule(temp)){
                            int sCost = calculateCost2(s,temp);
                            if(sCost < cost){
                                cost = sCost;
                                indexInSchedule = i;
                                scheduleID = s.getId();
                            }
                        }
                        temp.getBlocks().remove(b);
                        break;
                    }
                }
            }
        }
        if(scheduleID != -1){
            return new InfoBestFit(b,scheduleID,indexInSchedule,cost);
        }else{
            return null;
        }
    }

    private int calculateCost2(Schedule oldS, Schedule newS) {
        if (newS.getDuration() == 0) {
            System.out.println("NEW DURATION = 0");
            return oldS.getDuration();
        }

        if(oldS.getDuration() == 0){
            System.out.println(oldS);
            System.out.println("\n-------------------\n");
            System.out.println(newS);
        }

        double oldCostPerMinute = (double) parameters.getSalary() / oldS.getDuration();
        double newCostPerMinute = (double) parameters.getSalary() / newS.getDuration();

        double oldWastedTime = c.calculateTimeWaste(oldS);
        double newWastedTime = c.calculateTimeWaste(newS);

        if (!oldS.isLocal() && newS.isLocal()) {
            newCostPerMinute = (parameters.getCostFraction() * parameters.getSalary()) / newS.getDuration();
        }

        int diffvalid = 0;
        if(!oldS.isValid() && newS.isValid()){
            diffvalid = 1;
        } else if (oldS.isValid() && !newS.isValid()) {
            diffvalid = -1;
        }
        double diffWT = oldWastedTime - newWastedTime;
        double diffCPM = oldCostPerMinute - newCostPerMinute;

        //A positive result equals an improvement
        return (int) (diffWT * 1 + diffCPM * 0 + diffvalid * 1000);
    }
}
