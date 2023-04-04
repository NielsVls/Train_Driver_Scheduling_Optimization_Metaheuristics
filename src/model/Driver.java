package model;

import java.util.ArrayList;

public class Driver {
    final int ID;
    ArrayList<Integer> blocks;
    final int type; //0 --> regular ; 1-6 --> Station ;
    int depot;

    public Driver(int ID, int type) {
        this.ID = ID;
        this.type = type;
        blocks = new ArrayList<>();
    }

    public void assignDriverToDepot(Station depot){
        this.depot = depot.getID();
    }

    public int getDepot() {
        return depot;
    }

    public void setDepot(int depot) {
        this.depot = depot;
    }

    public int getID() {
        return ID;
    }

    public ArrayList<Integer> getBlocks() {
        return blocks;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "ID=" + ID +
                ", type=" + type +
                ", depot=" + depot +
                '}';
    }
}
