package util.LNS;

import model.Block;
import model.Schedule;

public class InfoBestFit {
    Integer block;
    int scheduleID;
    int index;
    double cost;

    public InfoBestFit(Integer block, int scheduleID, int index, double cost) {
        this.block = block;
        this.scheduleID = scheduleID;
        this.index = index;
        this.cost = cost;
    }

    public Integer getBlock() {
        return block;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public int getIndex() {
        return index;
    }

    public double getCost() {
        return cost;
    }

    public void setBlock(Integer block) {
        this.block = block;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "InfoBestFit{" +
                "block=" + block +
                ", scheduleID=" + scheduleID +
                ", index=" + index +
                ", cost=" + cost +
                '}';
    }
}
