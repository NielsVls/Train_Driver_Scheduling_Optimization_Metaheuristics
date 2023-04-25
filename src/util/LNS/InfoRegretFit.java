package util.LNS;

public class InfoRegretFit {
    Integer block;
    int scheduleID;
    int index;
    double cost;
    double secondCost;

    public InfoRegretFit(Integer block, int scheduleID, int index, double cost, double secondCost) {
        this.block = block;
        this.scheduleID = scheduleID;
        this.index = index;
        this.cost = cost;
        this.secondCost = secondCost;
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

    public double getSecondCost() {
        return secondCost;
    }

    public void setSecondCost(double secondCost) {
        this.secondCost = secondCost;
    }

    public double getRegret(){
        return secondCost - cost;
    }

    @Override
    public String toString() {
        return "InfoRegretFit{" +
                "block=" + block +
                ", scheduleID=" + scheduleID +
                ", index=" + index +
                ", cost=" + cost +
                ", seccost=" + secondCost +
                '}';
    }
}
