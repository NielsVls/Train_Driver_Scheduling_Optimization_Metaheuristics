package util.dataReader;

import model.Block;
import model.Station;
import model.TravelTrain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class DataReader {

    HashMap<String, Integer> stations;

    //Read the blocks from the csv file
    public ArrayList<Block> readBlocks() throws Exception {
        int counter = 0;
        stations = connectStationswithId();
        ArrayList<Block> blocks = new ArrayList<>();
        Scanner sc = new Scanner(new File(".//Data//CSV//Blocks_Vest_2_ABL_FLB_NG.csv"));
        sc.useDelimiter(",");
        while (sc.hasNextLine() /*&& counter < 250*/) {
            String line = sc.nextLine();
            String[] param = line.split(",");
            Block block = new Block(
                    param[0], //NAME
                    convertTimeToMinutes(param[1]), //DEPARTURE TIME
                    convertTimeToMinutes(param[2]), //ARRIVAL TIME
                    searchCorrespondigId(param[3]), //START LOCATION
                    searchCorrespondigId(param[4]), //END LOCATION
                    Integer.parseInt(param[5]), //WEEKDAY START
                    Integer.parseInt(param[6]), //WEEKDAY END
                    param[7], //ROLLING STOCK
                    Integer.parseInt(param[9]), //FEASIBLE FOR WHICH LOCAL DRIVERS
                    seperateTrainNumbers(param[8]), //TRAIN NUMBER
                    Integer.parseInt(param[10]) //BLOCK ID/NUMBER
            );
            blocks.add(block);
            counter++;
        }
        sc.close();
        return blocks;
    }

    //Read the travel matrix from the csv file
    public int[][] readTravelMatrix() throws Exception {
        int[][] travelmatrix = new int[40][40];
        Scanner sc = new Scanner(new File(".//Data//CSV//Travel_Matrix.csv"));
        sc.useDelimiter(",");
        for (int i = 0; i < 40; i++) {
            String line = sc.nextLine();
            String[] param = line.split(",");
            for (int j = 0; j < 40; j++) {
                if (i == 0 || j == 0) {
                    travelmatrix[i][j] = 9999999;
                } else {
                    travelmatrix[i][j] = Integer.parseInt(param[j]);
                }
            }
        }
        sc.close();
        return travelmatrix;
    }

    //Read the stations with their corresponding capabilities (break or depot (regular/station))
    public ArrayList<Station> readStations() throws Exception {
        ArrayList<Station> allStations = new ArrayList<>();
        Scanner sc = new Scanner(new File(".//Data//CSV//Locations_depot_break_2_ABL_FLB_NG.csv"));
        sc.useDelimiter(",");
        sc.nextLine();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] param = line.split(",");
            //ArrayList<Integer> qualifications = getQualifications(param);
            Station station = new Station(
                    searchCorrespondigId(param[0]),
                    param[0],
                    getBoolean(param[1]),
                    getBoolean(param[2]),
                    getBoolean(param[3])
            );
            allStations.add(station);
        }
        sc.close();
        return allStations;
    }

    public ArrayList<TravelTrain> readTravelTrains() throws Exception {
        ArrayList<TravelTrain> allTrains = new ArrayList<>();
        Scanner sc = new Scanner(new File(".//Data//CSV//travel_by_train2.csv"));
        sc.useDelimiter(",");
        sc.nextLine();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] param = line.split(",");
            TravelTrain train = new TravelTrain(
                    searchCorrespondigId(param[2]),
                    searchCorrespondigId(param[3]),
                    Integer.parseInt(param[0]),
                    Integer.parseInt(param[1]),
                    Integer.parseInt(param[4]),
                    Integer.parseInt(param[5]),
                    Integer.parseInt(param[6])
            );
            allTrains.add(train);
        }
        sc.close();
        return allTrains;
    }

    public ArrayList<Station> breakStations(ArrayList<Station> stations) {
        ArrayList<Station> breakStations = new ArrayList<>();
        for (Station s : stations) {
            if (s.isBreakLocation()) {
                breakStations.add(s);
            }
        }
        return breakStations;
    }

    public void readRegulations(ArrayList<Station> depots) throws Exception{
        Scanner sc = new Scanner(new File(".//Data//CSV//Regulations.csv"));
        sc.useDelimiter(",");
        sc.nextLine();
        for  (Station depot : depots) {
            ArrayList<Integer> regul = new ArrayList<>();
            regul.add(-1);
            String line = sc.nextLine();
            String[] param = line.split(",");
            for(int i = 1;i <= 39;i++){
                regul.add(Integer.valueOf(param[i]));
            }
            depot.setRegulations(regul);
        }
        sc.close();

    }

    public ArrayList<Station> depots(ArrayList<Station> stations) {
        ArrayList<Station> depots = new ArrayList<>();
        for (Station s : stations) {
            if (s.isRegularDepot() || s.isStationDepot()) {
                depots.add(s);
            }
        }
        return depots;
    }

    //This method converts the digital time (17:25) to minutes
    private int convertTimeToMinutes(String time) {
        int minutes = 0;
        String[] str = time.split(":");
        if (str.length == 2) {
            minutes = Integer.parseInt(str[0]) * 60 + Integer.parseInt(str[1]);
        }
        return minutes % 1440;
    }

    //This method is putting the train numbers in a list. Mainly for the blocks who have 2 train numbers
    private ArrayList<Integer> seperateTrainNumbers(String numbers) {
        ArrayList<Integer> trains = new ArrayList<>();
        if (numbers.contains("_")) {
            String[] str = numbers.split("_");
            trains.add(Integer.parseInt(str[0]));
            trains.add(Integer.parseInt(str[1]));
        } else {
            trains.add(Integer.parseInt(numbers));
        }
        return trains;
    }

    //This method gives every station an ID based on their name.
    private HashMap<String, Integer> connectStationswithId() throws FileNotFoundException {
        Scanner sc1 = new Scanner(new File(".//Data//CSV//Stations_Id.csv"));
        sc1.useDelimiter(",");
        sc1.next();
        HashMap<String, Integer> stations = new HashMap<>();
        while (sc1.hasNext()) {
            stations.put(sc1.next(), Integer.parseInt(sc1.next()));
        }
        sc1.close();
        return stations;
    }

    //Give the station ID based on the name.
    private int searchCorrespondigId(String name) {
        int value = stations.get(name);
        return value;
    }

    //Translate the matrix with qualifications to a list with the stationIDs where Reg drivers are eligible to drive to
//    private ArrayList<Integer> getQualifications(String[] param) {
//        ArrayList<Integer> quali = new ArrayList<>();
//        if (getBoolean(param[4])) {
//            quali.add(searchCorrespondigId("AB"));
//        }
//        if (getBoolean(param[5])) {
//            quali.add(searchCorrespondigId("AR"));
//        }
//        if (getBoolean(param[6])) {
//            quali.add(searchCorrespondigId("ES"));
//        }
//        if (getBoolean(param[7])) {
//            quali.add(searchCorrespondigId("FA"));
//        }
//        if (getBoolean(param[8])) {
//            quali.add(searchCorrespondigId("KH"));
//        }
//        if (getBoolean(param[9])) {
//            quali.add(searchCorrespondigId("OD"));
//        }
//        if (getBoolean(param[10])) {
//            quali.add(searchCorrespondigId("STR"));
//        }
//        if (getBoolean(param[11])) {
//            quali.add(searchCorrespondigId("TE"));
//        }
//        if (quali.isEmpty()) {
//            quali.add(0);
//        }
//        return quali;
//    }

    public static boolean getBoolean(String value) {
        return !value.equals("0");
    }

}