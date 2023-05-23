package model;

import global.Parameters;

import java.util.ArrayList;

public class Schedule implements Cloneable{
    Integer id;
    int driverType;
    int duration;
    int startStation;
    int startDay;
    int startTime;
    ArrayList<Integer> blocks;
    int closestDepot;
    int timeWorkingBeforeBreak;
    int timeWorkingWithoutBreak;
    int breakAfterBlock;
    ArrayList<Integer> breakPossibleAfterBlocks;
    int timeWasted;
    double totalCost;

    public Schedule(){
        breakAfterBlock = -1;
        blocks = new ArrayList<>();
        breakPossibleAfterBlocks = new ArrayList<>();
        totalCost = 99999999;
    }

    public Schedule(Schedule s){
        this.id = s.id;
        this.driverType = s.driverType;
        this.duration = s.duration;
        this.startStation = s.startStation;
        this.startTime = s.startTime;
        this.startDay = s.startDay;
        this.blocks = new ArrayList<>(s.blocks);
        this.closestDepot = s.closestDepot;
        this.timeWorkingBeforeBreak = s.timeWorkingBeforeBreak;
        this.timeWorkingWithoutBreak = s.timeWorkingWithoutBreak;
        this.breakAfterBlock = s.breakAfterBlock;
        this.breakPossibleAfterBlocks = new ArrayList<>(s.breakPossibleAfterBlocks);
        this.timeWasted = s.timeWasted;
        this.totalCost = s.totalCost;
    }

    @Override
    public Schedule clone() {
        try {
            // Call Object's clone method to create a shallow copy
            Schedule cloned = (Schedule) super.clone();

            // Create deep copies of mutable fields
            cloned.blocks = new ArrayList<>(blocks);
            cloned.breakPossibleAfterBlocks = new ArrayList<>(breakPossibleAfterBlocks);

            return cloned;
        } catch (CloneNotSupportedException e) {
            // Should not happen since we are Cloneable
            throw new InternalError(e);
        }
    }

    public void calculateCost(){
        Parameters parameters = new Parameters();

        if(duration > 0 && duration < 360){
            totalCost = 360 * parameters.getCostPerMinute();
        }else{
            totalCost = duration * parameters.getCostPerMinute();
        }

        if(driverType != 0){
            totalCost = totalCost * parameters.getCostFraction();
        }
    }

    public int getClosestDepot() {
        return closestDepot;
    }

    public void setClosestDepot(int closestDepot) {
        this.closestDepot = closestDepot;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getDriverType() {
        return driverType;
    }

    public int getDuration() {
        return duration;
    }

    public int getStartStation() {
        return startStation;
    }

    public ArrayList<Integer> getBlocks() {
        return blocks;
    }

    public void setDriverType(int driverType) {
        this.driverType = driverType;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStartStation(int startStation) {
        this.startStation = startStation;
    }

    public int getTimeWorkingWithoutBreak() {
        return timeWorkingWithoutBreak;
    }

    public void setTimeWorkingWithoutBreak(int timeWorkingWithoutBreak) {
        this.timeWorkingWithoutBreak = timeWorkingWithoutBreak;
    }

    public ArrayList<Integer> getBreakPossibleAfterBlocks() {
        return breakPossibleAfterBlocks;
    }

    public int getBreakAfterBlock() {
        return breakAfterBlock;
    }

    public void setBreakAfterBlock(int breakAfterBlock) {
        this.breakAfterBlock = breakAfterBlock;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getTimeWorkingBeforeBreak() {
        return timeWorkingBeforeBreak;
    }

    public void setTimeWorkingBeforeBreak(int timeWorkingBeforeBreak) {
        this.timeWorkingBeforeBreak = timeWorkingBeforeBreak;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimeWasted() {
        return timeWasted;
    }

    public void setTimeWasted(int timeWasted) {
        this.timeWasted = timeWasted;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {

        if (breakAfterBlock != -1){
            return "\n" + "Schedule " + id +" {\n" +
                    "duration =" + duration +
                    "\nstartTime=" + startTime +
                    "\nTime before Break=" + timeWorkingBeforeBreak +
                    "\nThere will be a break after block =" + breakAfterBlock +
                    "\nTime since Last Break=" + timeWorkingWithoutBreak +
                    "\nBREAKS?=" + breakPossibleAfterBlocks +
                    "\nblocks=" + blocks +
                    "\nType of driver= " + getDriverType() +
                    " }\n";
        }else{
            return "\n" + "Schedule " + id +" {\n" +
                    "duration =" + duration +
                    "\nstartTime=" + startTime +
                    "\nblocks=" + blocks +
                    "\nType of driver= " + getDriverType() +
                    " }\n";
        }

    }
}
