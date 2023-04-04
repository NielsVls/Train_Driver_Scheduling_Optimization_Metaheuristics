package model;

import java.util.ArrayList;

public class Station {
    final int ID;
    final String name;
    final boolean regularDepot;
    final boolean stationDepot;
    final boolean breakLocation;
    final ArrayList<Integer> regDriverQualifications;
    ArrayList<Driver> drivers;

    public Station(int ID, String name ,boolean regularDepot, boolean stationDepot, boolean breakLocation, ArrayList<Integer> regDriverQualifications) {
        this.ID = ID;
        this.name = name;
        this.regularDepot = regularDepot;
        this.stationDepot = stationDepot;
        this.breakLocation = breakLocation;
        this.regDriverQualifications = extendedQuali(regDriverQualifications);
        drivers = new ArrayList<>();
    }

    public ArrayList<Integer> extendedQuali(ArrayList<Integer> list){
        ArrayList<Integer> extendedQuali = new ArrayList<>();

        //AB
        for (int i = 0; i < 4; i++) {
            if(list.contains(1)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //AR
        for (int i = 0; i < 4; i++) {
            if(list.contains(5)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //CPH
        for (int i = 0; i < 2; i++) {
            extendedQuali.add(0);
        }

        //ES
        for (int i = 0; i < 3; i++) {
            if(list.contains(11)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //FA
        for (int i = 0; i < 4; i++) {
            if(list.contains(14)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //HGL & KD
        for (int i = 0; i < 4; i++) {
            extendedQuali.add(0);
        }

        //KH
        for (int i = 0; i < 2; i++) {
            if(list.contains(22)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //KK
        for (int i = 0; i < 3; i++) {
            extendedQuali.add(0);
        }

        //OD
        for (int i = 0; i < 3; i++) {
            if(list.contains(27)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //PA & SDB
        for (int i = 0; i < 4; i++) {
            extendedQuali.add(0);
        }

        //STR
        for (int i = 0; i < 2; i++) {
            if(list.contains(34)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //TE
        for (int i = 0; i < 2; i++) {
            if(list.contains(36)){
                extendedQuali.add(1);
            }else{
                extendedQuali.add(0);
            }
        }

        //VJ
        for (int i = 0; i < 2; i++) {
            extendedQuali.add(0);
        }

        return extendedQuali;
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

    public ArrayList<Integer> getRegDriverQualifications() {
        return regDriverQualifications;
    }

    public ArrayList<Driver> getDrivers() {
        return drivers;
    }

    @Override
    public String toString() {
        return "Station{" +
                "ID=" + ID +
                ", name= " + name +
                ", regularDepot=" + regularDepot +
                ", stationDepot=" + stationDepot +
                ", breakLocation=" + breakLocation +
                ", regDriverQualifications=" + regDriverQualifications +
                '}';
    }
}
