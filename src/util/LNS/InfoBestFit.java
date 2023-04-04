package util.LNS;

import model.Block;
import model.Schedule;

public class InfoBestFit {
    Integer block;
    int scheduleID;
    int index;
    int cost;

    public InfoBestFit(Integer block, int scheduleID, int index, int cost) {
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

    public int getCost() {
        return cost;
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
