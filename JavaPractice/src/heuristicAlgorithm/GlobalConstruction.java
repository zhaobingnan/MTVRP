package heuristicAlgorithm;
import model.Instance;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GlobalConstruction {
    Instance instance;
    public GlobalConstruction(Instance instance) {
        this.instance = instance;
    }
    // for each customer among unvisited customers
    // 对于未访问客户中的每个客户
    public List<List<Integer>> globalGreedyHeuristic() throws FileNotFoundException {
        // define the set of unserved customers
    	// 定义未服务的客户集合
        List<Integer> unServed = new ArrayList<Integer>();
        for (int i = 1;i < instance.nrCustomers;i++) {
            unServed.add(i);
        }
        List<List<Integer>> schedule = new ArrayList<>();
        schedule.add(new ArrayList());

        AddEmptyRoute aer = new AddEmptyRoute();
        while(unServed.size()>0){

        // add a new empty route if there is no empty route int the schedule
        // 如果schedule中没有空路线，则添加一个新的空路线

            if (schedule.size() <= instance.nrVehicles){
                if (!aer.addEmptyRoute(schedule)){ //判断解中是否有空路径，若无则插入空路径
                    schedule.add(new ArrayList());
                }
            }
    //上面几行代码是判断已有的解中是否超过了最大车辆数限制，以及是否应该产生新的空车辆
    GlobalGreedyInsertWithWaiting fgg = new GlobalGreedyInsertWithWaiting(instance);
    List<Integer> greedyInsertList = new ArrayList();
    greedyInsertList.addAll(fgg.findGlobalGreedyInsert(schedule,unServed));
     //上面一行代码是调用函数findGlobalGreedyInsert来产生下一个如下插入的客户及插入的位置

            if (greedyInsertList.size()==0){
                System.out.println("there is no feasible greedy insertion");
                schedule = null;
            }else {
                int cusID =  greedyInsertList.get(0);
                int routeSt =  greedyInsertList.get(1);
                int routeStIndex =  greedyInsertList.get(2);
                schedule.get(routeSt).add(routeStIndex, cusID);
                unServed.remove(greedyInsertList.get(0));
            }
        }
     return schedule;
    }  
   }
