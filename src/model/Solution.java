package model;

import global.Parameters;

import java.util.ArrayList;

import static util.LNS.DestroyRepair.getRandomNumberInRange;

public class Solution implements Cloneable{
    private ArrayList<Schedule> schedules;
    private int totalCost;
    private int totalCostPerMinute;
    private int totalDuration;
    private int blocksExecuted;
    private int totalDrivers;
    private int totalRegularDrivers;
    private int totalStationDrivers;
    private int totalTimeWasted;
    private double averageDuration;
    private int driversWorkingLessThen6hours;

    public Solution() {
        this.schedules = new ArrayList<>();
        this.totalCost = 999999999;
    }

    public Solution(Solution other) {
        this.schedules = new ArrayList<>(other.schedules);
        this.totalCost = other.totalCost;
        this.totalCostPerMinute = other.totalCostPerMinute;
        this.totalDuration = other.totalDuration;
        this.blocksExecuted = other.blocksExecuted;
        this.totalDrivers = other.totalDrivers;
        this.totalRegularDrivers = other.totalRegularDrivers;
        this.totalStationDrivers = other.totalStationDrivers;
        this.totalTimeWasted = other.totalTimeWasted;
        this.averageDuration = other.averageDuration;
        this.driversWorkingLessThen6hours = other.driversWorkingLessThen6hours;
    }
    public void insertBlock(){

    }

    public int getTotalCost() {
        return totalCost;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public int getTotalTimeWasted() {
        return totalTimeWasted;
    }

    public void calculateSolution(){
        calculateCost();
        calculateBlocks();
        calculateDrivers();
        calculateDurations();
        calculateTimeWasted();
    }

    public void calculateBlocks(){
        blocksExecuted = 0;
        if(!schedules.isEmpty()){
            for (Schedule s : schedules){
                blocksExecuted += s.getBlocks().size();
            }
        }
    }

    public void calculateDrivers(){
        totalDrivers = 0;
        totalRegularDrivers = 0;
        totalStationDrivers = 0;

        for (Schedule s: schedules){
            if(s.getType() == 0){
                totalRegularDrivers++;
            }else{
                totalStationDrivers++;
            }
            totalDrivers++;
        }
    }

    public void calculateCost(){
        Parameters parameters = new Parameters();
        if(!schedules.isEmpty()){
            totalCost = 0;
            totalCostPerMinute = 0;
            totalDuration = 0;
            for (Schedule s : schedules){
                totalDuration += s.getDuration();
                if(s.getType()==0){
                    totalCost += parameters.getSalary();
                }else{
                    totalCost += (parameters.getSalary() * parameters.getCostFraction());
                }
            }
        }
    }

    public void calculateDurations(){
        averageDuration = 0.0;
        driversWorkingLessThen6hours = 0;
        if(!schedules.isEmpty()){
            int totalduration = 0;
            for (Schedule s : schedules){
                totalduration += s.getDuration();
                if(s.getDuration() < 300){
                    driversWorkingLessThen6hours++;
                }
            }
            averageDuration = totalduration/ schedules.size();
        }

    }

    public void calculateTimeWasted(){
        totalTimeWasted = 0;
        if(!schedules.isEmpty()){
            for (Schedule s : schedules){
                totalTimeWasted += s.getTimeWasted();
            }
        }
    }

    public int getBlocksExecuted() {
        return blocksExecuted;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = new ArrayList<>(schedules);
    }

    @Override
    public String toString() {
        return "Solution{" +
                "totalCost=" + totalCost +
                ", blocksExecuted=" + blocksExecuted +
                ", totalDrivers=" + totalDrivers +
                ", totalRegularDrivers=" + totalRegularDrivers +
                ", totalStationDrivers=" + totalStationDrivers +
                ", averageDuration=" + averageDuration +
                ", driversWorkingLessThen6hours=" + driversWorkingLessThen6hours +
                '}';
    }

    @Override
    public Solution clone() {
        try {
            // Call Object's clone method to create a shallow copy
            Solution cloned = (Solution) super.clone();

            // Create deep copy of mutable field schedules
            cloned.schedules = new ArrayList<>();
            for (Schedule schedule : schedules) {
                cloned.schedules.add(schedule.clone());
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            // Should not happen since we are Cloneable
            throw new InternalError(e);
        }
    }
}
