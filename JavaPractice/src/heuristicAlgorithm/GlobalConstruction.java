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
    // ����δ���ʿͻ��е�ÿ���ͻ�
    public List<List<Integer>> globalGreedyHeuristic() throws FileNotFoundException {
        // define the set of unserved customers
    	// ����δ����Ŀͻ�����
        List<Integer> unServed = new ArrayList<Integer>();
        for (int i = 1;i < instance.nrCustomers;i++) {
            unServed.add(i);
        }
        List<List<Integer>> schedule = new ArrayList<>();
        schedule.add(new ArrayList());

        AddEmptyRoute aer = new AddEmptyRoute();
        while(unServed.size()>0){

        // add a new empty route if there is no empty route int the schedule
        // ���schedule��û�п�·�ߣ������һ���µĿ�·��

            if (schedule.size() <= instance.nrVehicles){
                if (!aer.addEmptyRoute(schedule)){ //�жϽ����Ƿ��п�·��������������·��
                    schedule.add(new ArrayList());
                }
            }
    //���漸�д������ж����еĽ����Ƿ񳬹�������������ƣ��Լ��Ƿ�Ӧ�ò����µĿճ���
    GlobalGreedyInsertWithWaiting fgg = new GlobalGreedyInsertWithWaiting(instance);
    List<Integer> greedyInsertList = new ArrayList();
    greedyInsertList.addAll(fgg.findGlobalGreedyInsert(schedule,unServed));
     //����һ�д����ǵ��ú���findGlobalGreedyInsert��������һ�����²���Ŀͻ��������λ��

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
