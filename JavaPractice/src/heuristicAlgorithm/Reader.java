package heuristicAlgorithm;

public class Reader {
    public static Instance readInstance(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        String line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();

        String [] parts = line.split("\\s+");
        int nrVehicles = Integer.parseInt(parts[0]);
        int capacity = Integer.parseInt(parts[1]);

        line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();
        line = scanner.nextLine().trim();
        //line = scanner.nextLine().trim();
        int nrCustomers = 101;//根据客户数量进行修改
        int duration;
        int [] customerID = new int [nrCustomers];
        int [][] coordinates = new int [nrCustomers][2];
        int [] demands = new int [nrCustomers];
        int [][] timeWindows = new int [nrCustomers][2];
        int [] serviceTime = new int [nrCustomers];

        int justy = 0;
        for (int i = 0;i < nrCustomers;i++){
            line = scanner.nextLine().trim();
            parts = line.split("\\s+");
            
            if (justy==0){
                int cusID = Integer.parseInt(parts[0])-1;
                int x_coor = (int)Double.parseDouble(parts[1]);
                int y_coor = (int)Double.parseDouble(parts[2]);
                int demand = (int)Double.parseDouble(parts[3]);
                int start_Time =(int) Double.parseDouble(parts[4]);
                int end_Time =(int) Double.parseDouble(parts[5]);
                int service_Time =(int) Double.parseDouble(parts[6]);

                customerID [i] = cusID;
                coordinates[i][0] = x_coor;
                coordinates[i][1] = y_coor;
                demands[i] = demand;
                timeWindows[i][0] = start_Time;
                timeWindows[i][1] = end_Time;
                serviceTime [i] = service_Time;
            }else {
                int cusID = Integer.parseInt(parts[0]);
                int x_coor = Integer.parseInt(parts[1]);
                int y_coor = Integer.parseInt(parts[2]);
                int demand = Integer.parseInt(parts[3]);
                int start_Time = Integer.parseInt(parts[4]);
                int end_Time = Integer.parseInt(parts[5]);
                int service_Time = Integer.parseInt(parts[6]);

                customerID [i] = cusID;
                coordinates[i][0] = x_coor;
                coordinates[i][1] = y_coor;
                demands[i] = demand;
                timeWindows[i][0] = start_Time;
                timeWindows[i][1] = end_Time;
                serviceTime [i] = service_Time;
            }
        }
        duration = timeWindows[0][1];

        double [][] distanceMatrix = new double [nrCustomers][nrCustomers];
        double [][] travelTimes = new double [nrCustomers][nrCustomers];
        
        for (int i=0;i<nrCustomers;i++){
            for (int j=0;j<nrCustomers;j++){
                if (i!=j) {
                   if (i<j) {
                       double x = Math.pow((coordinates[i][0] - coordinates[j][0]), 2);
                       double y = Math.pow((coordinates[i][1] - coordinates[j][1]), 2);
                       distanceMatrix[i][j] = Math.sqrt(x + y);

                       distanceMatrix[i][j] = (int) (distanceMatrix[i][j] * 100);
                       distanceMatrix[i][j] =  distanceMatrix[i][j] / 100;

                       travelTimes[i][j] = distanceMatrix[i][j];
                   }else {
                       distanceMatrix[i][j] = distanceMatrix[j][i];
                       travelTimes[i][j] = travelTimes[j][i];
                   }
                }else{
                    distanceMatrix[i][j] = 0;
                    travelTimes[i][j] = 0;
                }
            }
        }
        return new Instance(file.getName(),nrCustomers,nrVehicles,duration,capacity,customerID,demands,timeWindows,distanceMatrix,travelTimes,coordinates,serviceTime);
    }
}