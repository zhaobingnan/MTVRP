package heuristicAlgorithm;
import java.util.List;

public class AddEmptyRoute {
    // check if a new empty route is added
    public boolean addEmptyRoute (List<List<Integer>> destroySchedule){
        boolean t = false;
        for (int i=0;i<destroySchedule.size();i++){
            if (destroySchedule.get(i).size()==0){
                t = true;
                break;
            }else {
                continue;
            }
        }
        return t;
    }
}