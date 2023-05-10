package model;

import java.util.ArrayList;

public class Station {
    final int ID;
    final String name;
    final boolean regularDepot;
    final boolean stationDepot;
    final boolean breakLocation;
    ArrayList<Integer> regulations;

    public Station(int ID, String name ,boolean regularDepot, boolean stationDepot, boolean breakLocation) {
        this.ID = ID;
        this.name = name;
        this.regularDepot = regularDepot;
        this.stationDepot = stationDepot;
        this.breakLocation = breakLocation;
        this.regulations = new ArrayList<>();
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public boolean isRegularDepot() {
        return regularDepot;
    }

    public boolean isStationDepot() {
        return stationDepot;
    }

    public boolean isBreakLocation() {
        return breakLocation;
    }

    public ArrayList<Integer> getRegulations() {
        return regulations;
    }

    public void setRegulations(ArrayList<Integer> regulations){
        this.regulations = regulations;
    }

    @Override
    public String toString() {
        return "Station{" +
                "ID=" + ID +
                ", name= " + name +
                ", regularDepot=" + regularDepot +
                ", stationDepot=" + stationDepot +
                ", breakLocation=" + breakLocation +
                ", regulations=" + regulations +
                '}';
    }
}
