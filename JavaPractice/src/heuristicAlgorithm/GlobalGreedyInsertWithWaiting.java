package heuristicAlgorithm;
import model.Instance;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GlobalGreedyInsertWithWaiting {
    Instance instance; //新建一个案例数据对象
    public GlobalGreedyInsertWithWaiting(Instance instance) {
        this.instance = instance;
    }
    // calculate total load of a route
    public int calculateTotalLoad(List routeList) {
        int totalLoad = 0;
        if (routeList.size() == 0) {
            totalLoad = 0;
        } else {
            for (int i = 0; i < routeList.size(); i++) {
                totalLoad += instance.demands[(int)routeList.get(i)];
            }
        }
        return totalLoad;
    }
    public boolean feasibleInsertForTimewindows(List<Integer> list,int cusID,int index) {
        boolean t = true;
        // calculate the total time after the location is added
        List<Integer> afterList = new ArrayList();

        if (list.size() == 0) {
            afterList = new ArrayList();
        } else {
            for (int i = 0; i < list.size(); i++) {
                afterList.add(list.get(i));
            }
        }

            afterList.add(index, cusID);
            afterList.add(0, 0);
            afterList.add(afterList.size(), 0);

            double[] arrivalTime = new double[afterList.size()];
            double[] waitingTime = new double[afterList.size()];
            arrivalTime[0] = 0;
            waitingTime[0] = 0;

            for (int i = 0; i < afterList.size() - 1; i++) {
                arrivalTime[i + 1] = arrivalTime[i] + instance.serviceTime[i] + waitingTime[i] + instance.travelTimes[afterList.get(i)][afterList.get(i + 1)];
                waitingTime[i + 1] = Math.max(0, instance.timeWindows[afterList.get(i + 1)][0] - arrivalTime[i + 1]);
            }
            // arrive the location before the end time
            for (int i = 1; i < afterList.size(); i++) {
                if (arrivalTime[i] <= instance.timeWindows[afterList.get(i)][1]) {
                    continue;
                } else {
                    t = false;
                    break;
                }
            }
            return t;
        }

    //返回一个集合，带三个元素，第一个是要插入的客户的ID，路径以及该路径中插入位置
    public List findGlobalGreedyInsert(List<List<Integer>> schedule, List<Integer> unServed) throws FileNotFoundException {

        // store the best candidate insertion
        List<Integer> bestCandidateList = new ArrayList();
        // store all the feasible candidate insertion
        List<List<Integer>> candidateList = new ArrayList();
        //calculate the route time before the customer is inserted
        double [] beforeRouteTime = new double [schedule.size()];

        GetCostEachRoute gcer = new GetCostEachRoute(instance);
        for (int j = 0;j < schedule.size();j++) {
            beforeRouteTime[j] = gcer.calculateTravelTimeEachRoute(schedule.get(j));
        }

        //loop through each remaining customers
        for (int c=0;c<unServed.size();c++){
            for (int j=0;j<schedule.size();j++){
                //判断是否超过路径最大容量
                if (instance.demands[unServed.get(c)]+calculateTotalLoad(schedule.get(j)) <= instance.capacity){
                    if (schedule.get(j).size()==0){
                        if(feasibleInsertForTimewindows(schedule.get(j), unServed.get(c), 0)){
                            List modelList = new ArrayList();
                            for (int s=0;s<schedule.get(j).size();s++){
                                modelList.add(schedule.get(j).get(s));
                            }
                            modelList.add(0,unServed.get(c));

                            double aTT = gcer.calculateTravelTimeEachRoute(modelList);

                            List medList = new ArrayList();
                            // locationID of selected customer
                            medList.add(unServed.get(c));
                            // route number
                            medList.add(j);
                            //index of route number
                            medList.add(0);
                            int costChange = (int)(aTT - 0);
                            medList.add(costChange);
                            candidateList.add(medList);

                        }else {
                            continue;
                        }
                    }
                    if (schedule.get(j).size()>0){
                        for (int k = 0; k <= schedule.get(j).size(); k++) {
                            //判断是否满足时间窗内及路径周期约束
                            if (feasibleInsertForTimewindows(schedule.get(j), unServed.get(c), k)) {

                                List modelList = new ArrayList();
                                for (int s=0;s<schedule.get(j).size();s++){
                                    modelList.add(schedule.get(j).get(s));
                                }
                                modelList.add(k,unServed.get(c));

                                double aTT1 = gcer.calculateTravelTimeEachRoute(modelList);
                                List medList = new ArrayList();
                                // locationID of selected customer
                                medList.add(unServed.get(c));
                                // route number
                                medList.add(j);
                                //index of route number
                                medList.add(k);
                                int costChange = (int)(aTT1 - beforeRouteTime[j]);
                                medList.add(costChange);
                                candidateList.add(medList);

                            }else {
                                continue;
                            }
                        }
                    }
                }else {
                continue;
                }
            }
        }

        // sort collection by the cost change
        Collections.sort(candidateList, new Comparator<List>() {

            @Override
            public int compare(List o1, List o2) {
                return (int) o1.get(3) - (int) o2.get(3);
            }
        });

        if (candidateList.size()==0){
            System.out.println("there is no feasible candidate insertion");
        }else if (candidateList.size()>0){
            bestCandidateList.add(candidateList.get(0).get(0));
            bestCandidateList.add(candidateList.get(0).get(1));
            bestCandidateList.add(candidateList.get(0).get(2));
            bestCandidateList.add(candidateList.get(0).get(3));
        }
    return bestCandidateList;
    }
    }
