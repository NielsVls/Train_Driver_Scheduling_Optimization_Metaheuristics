package util.LNS;

import global.Parameters;
import model.Block;
import model.Schedule;
import model.Station;
import util.algorithms.Calculations;

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
            for(int i = 0; i <= s.getBlocks().size(); i++) {
                if(s.getBlocks().isEmpty()){
                    temp.getBlocks().add(b);
                    c.calculateSchedule(temp);
                    if(c.checkDuration(temp)){
                        int sCost = c.calculateCost(s,temp);
                        if(sCost < cost){
                            cost = sCost;
                            indexInSchedule = 0;
                            scheduleID = s.getId();
                        }
                    }
                    temp.getBlocks().remove(b);
                    break;
                } else if(i == 0){
                    Integer blockAfter = temp.getBlocks().get(0);
                    if(consmatrix[b][blockAfter] == 1){
                        temp.getBlocks().add(0,b);
                        c.calculateSchedule(temp);
                        if(c.checkSchedule(temp)){
                            int sCost = c.calculateCost(s,temp);
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
                            int sCost = c.calculateCost(s,temp);
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
                            int sCost = c.calculateCost(s,temp);
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

    private int calculateCost(Schedule oldS, Schedule newS) {

        //double feasibility =0; //Feasability //TODO CHECK IF NECESSARY

        double diffLD; //Local driver
        double diffWT; //Wasted Time
        double diffDur; //Duration
        double diffTT; //Travel Time
        double diffCPM; //
        double diffTC; //Total Cost

        if(oldS.getBlocks().isEmpty()){
            //TOTAL COST + LOCAL DRIVER
            if(newS.isLocal()){
                diffTC = parameters.getSalary()*parameters.getCostFraction();
                diffLD = 1;
            }else{
                diffTC = parameters.getSalary();
                diffLD = 0;
            }

            //WASTED TIME
            diffWT = c.calculateTimeWaste(newS);

            //TRAVEL TIME
            diffTT = 0;

            //TOTAL DURATION
            diffDur = newS.getDuration();

            return (int) (diffWT * 1 + diffDur * 1 + diffTC * -10 + diffTT * 1 + diffLD * -1000);
        }

        if (newS.getDuration() == 0) {
            return 0;
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

//        if(!oldS.isValid() && newS.isValid()){
//            diffvalid = 1;
//        } else if (oldS.isValid() && !newS.isValid()) {
//            diffvalid = -1;
//        }
         diffWT = oldWastedTime - newWastedTime;
         diffCPM = oldCostPerMinute - newCostPerMinute;

        //A positive result equals an improvement
        return (int) (diffWT * 1 + diffCPM * 0);
    }
}
