package StaticTest;
import ilog.concert.IloColumn;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
public class Main{
	
	//算例截取参数
    //customerNum = 30，表示截取c101中100个客户点的前30个客户点作为测试数据
	static final int customerNum = 10;
	static final int nodeNum = customerNum + 2;//节点的数量=客户点的数量+2
	static final int vehicleNum = 25;
	static final int route = 3;
	static double gap = 1e-8;
	public static void main(String[] args) throws IOException ,IloException{
		//首先写入算例的路径
		String path = "D:\\eclipse\\eclipse_workspace\\JavaPractice\\data\\c201.txt";
		
        //根据算例路径，读取算例数据
		Instance instance = readData(path);
		
		//打印处算例数据信息
		printInstance(instance);
		
		//调用CPLEX构建模型
		//定义CPLEX对象
		IloCplex Cplex = new IloCplex();
		//定义决策变量
		IloNumVar[][][][] X = new IloNumVar[instance.nodeNum-1][instance.nodeNum-1][instance.vehicleNum][instance.route];
		IloNumVar[][][] Y = new IloNumVar[instance.nodeNum-1][instance.vehicleNum][instance.route];
		IloNumVar[][][] S = new IloNumVar[instance.nodeNum][instance.vehicleNum][instance.route];
		
		for(int k = 0 ; k < instance.vehicleNum ; k++) {
			for(int r = 0 ; r < instance.route ; r++) {
				for(int i = 0 ; i < instance.nodeNum-1 ; i++) {
					for(int j = 0 ; j < instance.nodeNum-1 ; j++) {
						if(i == j) {
							X[i][j][k][r] = null;
						}else {
							X[i][j][k][r] = Cplex.boolVar("X" + i + j + k + r);
							Y[i][k][r] = Cplex.boolVar("Y" + i + k + r);
						}
					}
				}
				for(int i= 0 ; i < instance.nodeNum ;i++) {
					S[i][k][r] = Cplex.intVar(0, Integer.MAX_VALUE, "S" + i + k + r);
				}
			}
		}
		
		//定义目标函数
		IloNumExpr obj = Cplex.numExpr();
		for(int i = 0 ; i < instance.nodeNum-1 ; i++){
			for(int j = 0 ; j < instance.nodeNum-1 ; j++) {
				if(i != j) {
					System.out.println("i = " + i + ", \t j = " + j);
					int x1 = instance.Nodes.get(i).Xcoor;
					int y1 = instance.Nodes.get(i).Ycoor;
					int x2 = instance.Nodes.get(j).Xcoor;
					int y2 = instance.Nodes.get(j).Ycoor;
					double arcDistance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
					arcDistance = double_truncate(arcDistance);
					System.out.println(i + "\t" + j + "\t" + arcDistance);
					
					for(int k = 0 ; k < instance.vehicleNum ; k++) {
						for(int r = 0 ; r < instance.route ;r++) {
							//构建目标函数
							obj = Cplex.sum(obj , Cplex.prod(arcDistance, X[i][j][k][r]));
						}
					}
				}
			}
		}
		Cplex.addMinimize(obj);
		
		//定义约束条件
		//首先引入两个线性表达式
		IloNumExpr expr = Cplex.numExpr();
		IloNumExpr expr1 = Cplex.numExpr();
		IloNumExpr expr2 = Cplex.numExpr();
		
		//约束1(已修改)每个客户都被服务
		for(int i = 1 ; i < instance.nodeNum-1 ; i++ ) {
			expr = Cplex.numExpr();
			for(int k = 0 ; k < instance.vehicleNum ; k++) {
				for(int r = 0 ; r < instance.route ; r++) {
					expr = Cplex.sum(expr, Y[i][k][r]);					
				}
			}
			Cplex.addEq(expr, 1.0);
		}
		
		//约束2(已修改)
		for(int i = 0 ; i < instance.nodeNum-1 ;i++) {
			expr = Cplex.numExpr();
			for(int k = 0 ; k <instance.vehicleNum ; k++) {
				 for(int r = 0 ; r < instance.route ; r++){
					 for(int j = 0 ; j < instance.nodeNum-1 ; j++){
						if(i != j) {
							expr = Cplex.sum(expr , X[i][j][k][r]);
						}
					}
					Cplex.addEq(expr, Y[i][k][r]);
				}
			}
		}
		for(int i = 0 ; i < instance.nodeNum-1 ;i++) {
			expr = Cplex.numExpr();
			for(int k = 0 ; k <instance.vehicleNum ; k++) {
				 for(int r = 0 ; r < instance.route ; r++){
					 for(int j = 0 ; j < instance.nodeNum-1 ; j++){
						if(i != j) {
							expr = Cplex.sum(expr , X[j][i][k][r]);
						}
					}
					Cplex.addEq(expr, Y[i][k][r]);
				}
			}
		}
		for(int i = 0 ; i < instance.nodeNum-1 ;i++) {
			expr1 = Cplex.numExpr();
			expr2 = Cplex.numExpr();
			for(int k = 0 ; k <instance.vehicleNum ; k++) {
				 for(int r = 0 ; r < instance.route ; r++){
					 for(int j = 0 ; j < instance.nodeNum-1 ; j++){
						if(i != j) {
							expr1 = Cplex.sum(expr1 , X[i][j][k][r]);
							expr2 = Cplex.sum(expr2 , X[j][i][k][r]);
						}
					}
					Cplex.addEq(expr1, expr2);
				}
			}
		}
		
	    //约束3(已修改)容量约束
		for(int k = 0 ; k < instance.vehicleNum ;k++) {
			expr = Cplex.numExpr();
			for(int i = 1 ; i < instance.nodeNum -1 ; i++) {
				for(int r = 0 ; r < instance.route ; r++) {
					expr = Cplex.sum(expr , Cplex.prod(instance.Nodes.get(i).demand, Y[i][k][r]));
				}
			}
			Cplex.addLe(expr, instance.vehicleCapacity);
		}
		
//		//约束7.4
//		for(int k = 0 ; k < instance.vehicleNum ; k++) {
//			for(int h = 1 ; h < instance.nodeNum - 1 ; h++) {
//				for(int r = 0 ; r < instance.route ; r++) {
//					expr1 = Cplex.numExpr();
//					expr2 = Cplex.numExpr();
//					for(int i = 0 ; i < instance.nodeNum ; i++) {
//						if(i != h) {
//							expr1 = Cplex.sum(expr1 , X[i][h][k][r]);
//						}
//					}
//					for(int j = 0 ; j < instance.nodeNum ; j++) {
//						if(h != j) {
//							expr2 = Cplex.sum(expr2 , X[h][j][k][r]);
//						}
//					}
//					Cplex.addEq(expr1, expr2);
//				}
//			}
//		}
//		
//		//约束7.5
//		for(int k = 0 ; k < instance.vehicleNum ; k++) {
//			expr = Cplex.numExpr();
//			for(int i = 0 ; i < instance.nodeNum - 1 ; i++) {
//				for(int r = 0 ; r < instance.route ; r++) {
//					expr = Cplex.sum(expr , X[i][instance.nodeNum - 1][k][r]);
//				}
//			}
//			Cplex.addEq(expr, 1.0);
//		}
		
		//约束4(已修改)
		int M = Integer.MAX_VALUE;
		for(int i = 0 ; i < instance.nodeNum-1 ; i++) {
			for(int j = 1 ; j < instance.nodeNum-1 ; j++) {
				for(int  k = 0 ; k < instance.vehicleNum ; k++) {
					for(int r = 0 ; r < instance.route ; r++) {
						if(i != j) {
							int x1 = instance.Nodes.get(i).Xcoor;
							int y1 = instance.Nodes.get(i).Ycoor;
							int x2 = instance.Nodes.get(j).Xcoor;
							int y2 = instance.Nodes.get(j).Ycoor;
							double arcDistance = Math.sqrt((x2 -x1) * (x2 - x1) + (y2 -y1) * (y2 -y1));
							arcDistance = double_truncate(arcDistance);
							
							expr = Cplex.numExpr();
							expr = Cplex.sum(expr , S[i][k][r]);
							expr = Cplex.sum(expr , instance.Nodes.get(i).serviceTime);
							expr = Cplex.sum(expr , arcDistance);
							expr = Cplex.diff(expr , S[j][k][r]);
							expr = Cplex.diff(expr , M);
							expr = Cplex.sum(expr , Cplex.prod(M, X[i][j][k][r]));
							Cplex.addLe(expr, 0);
						}
					}
				}		
			}
		}
		
		//约束5(已添加)
		for(int i = 1 ; i < instance.nodeNum -1 ; i++) {	
			for(int  k = 0 ; k < instance.vehicleNum ; k++) {					
				for(int r = 0 ; r < instance.route ; r++) {		
					int x1 = instance.Nodes.get(i).Xcoor;					
					int y1 = instance.Nodes.get(i).Ycoor;					
					int x2 = instance.Nodes.get(0).Xcoor;						
					int y2 = instance.Nodes.get(0).Ycoor;					
					double arcDistance = Math.sqrt((x2 -x1) * (x2 - x1) + (y2 -y1) * (y2 -y1));
					arcDistance = double_truncate(arcDistance);
							
					expr = Cplex.numExpr();
					expr = Cplex.sum(expr , S[i][k][r]);	
					expr = Cplex.sum(expr , instance.Nodes.get(i).serviceTime);					
					expr = Cplex.sum(expr , arcDistance);					
					expr = Cplex.diff(expr , S[instance.nodeNum-1][k][r]);					
					expr = Cplex.diff(expr , M);
					expr = Cplex.sum(expr , Cplex.prod(M, X[i][0][k][r]));
					Cplex.addLe(expr, 0);	
				}
			}		
		}
		
		//约束6
		for(int k = 0 ; k < instance.vehicleNum ; k++) {
			expr = Cplex.numExpr();
			for(int r = 0 ; r < instance.route ; r++) {
				if(r != instance.route-1) {
					expr = Cplex.sum(expr , S[instance.nodeNum-1][k][r]);
					expr = Cplex.diff(expr , S[0][k][r+1]);
					Cplex.addLe(expr, 0);
				}
			}
		}
				
		//约束7(已修改)
		for(int i = 1 ; i < instance.nodeNum-1 ; i++){
			for(int k = 0 ; k < instance.vehicleNum ; k++) {
				for(int r = 0 ; r < instance.route ;r++) {
					expr = Cplex.numExpr();
					expr1 = Cplex.numExpr();
					expr2 = Cplex.numExpr();
					expr = Cplex.sum(expr , S[i][k][r]);
					expr1 = Cplex.prod(Y[i][k][r], instance.Nodes.get(i).readyTime);
					expr2 = Cplex.prod(Y[i][k][r], instance.Nodes.get(i).dueTime);
					Cplex.addGe(expr, expr1);
					Cplex.addLe(expr, expr2);
				}
			}
		}

		//约束8
		for(int k = 0 ; k < instance.vehicleNum ; k++) {
			for(int r = 0 ; r < instance.route ; r++) {
				expr = Cplex.numExpr();
				expr = Cplex.sum(expr , S[instance.nodeNum-1][k][r]);
				expr = Cplex.diff(expr , 3390);
				Cplex.addLe(expr, 0);
			}
		}
		
		//约束9
		for(int i = 0 ; i < instance.nodeNum ; i++){
			for(int k = 0 ; k < instance.vehicleNum ; k++) {
				for(int r = 0 ; r < instance.route ;r++) {
					expr = Cplex.numExpr();
					expr = Cplex.sum(expr1 , S[i][k][r]);
					Cplex.addGe(expr, 0);
				}
			}
		}
		
		//求解模型
		Cplex.solve();
		
		//-----------------------输出结果--------------------------------------------
		System.out.println("\n\n--------------以下是求解结果---------------------\n");
		System.out.println("Objective\t\t:" + Cplex.getObjValue());
		int vehicleNum = 0;
		for(int k = 0 ; k < instance.vehicleNum ; k++) {
			for(int r = 0 ; r < instance.route ; r++) {
				wc:for(int i = 0 ; i < instance.nodeNum-1 ; i++) {
					nc:for(int j = 0 ; j < instance.nodeNum-1 ; j++) {
						if(i != j) {
							if(Cplex.getValue(X[i][j][k][r]) != 0 && i != 0 && j != instance.nodeNum -1) {
								vehicleNum += 1;
								break wc;
							}
						}
					}
				}
			}
		}
		System.out.println("vehicle number\t\t:" + vehicleNum);
//		int count = 1;
//		int nextNode = 0;
//		for(int k = 0 ; k < instance.vehicleNum ; k++) {
//			double load = 0;
//			double distance = 0;
//			for(int r = 0 ; r < instance.route ; r++) {
//				w: while(nextNode != instance.nodeNum - 1) {
//					int i = nextNode;
//					wc: for(;i < instance.nodeNum ; i++) {
//						nc: for(int j = 0 ; j < instance.nodeNum ; j++) {
//							if(i != j && Cplex.getValue(X[i][j][k][r]) != 0) {
//								if(i == 0 && j == instance.nodeNum -1) {
//									break w;
//								}else if(i != 0 || j != instance.nodeNum -1) {
//									System.out.println(nextNode + "-");
//									load += instance.Nodes.get(j).demand;
//									
//									int x1 = instance.Nodes.get(i).Xcoor;
//									int y1 = instance.Nodes.get(i).Ycoor;
//									int x2 = instance.Nodes.get(j).Xcoor;
//									int y2 = instance.Nodes.get(j).Ycoor;
//									double arcDistance = Math.sqrt((x2 -x1) * (x2 - x1) + (y2 -y1) * (y2 -y1));
//									arcDistance = double_truncate(arcDistance);
//									
//									distance += arcDistance;
//									nextNode= j;
//									i = nextNode - 1;
//									if(nextNode == instance.nodeNum -1) {
//										System.out.println(0 + "\t\tCap:" + load + "\t" + "distance: " + distance + "\n");
//										nextNode = 0;
//										break w;
//									}
//									break nc;
//								}
//							}
//						}
//					}
//				}
//			}
//			//System.out.println();
//		}
		//粗略输出的效果
		for(int k = 0 ; k < instance.vehicleNum ; k++) {
			System.out.println("----------第" + (k+1) + "车----------");
			for(int r = 0 ; r < instance.route ; r++) {
				System.out.println("----------第" + (r+1) + "程----------");
				for(int i = 0 ; i < instance.nodeNum-1 ; i++) {
					for(int j = 0 ; j < instance.nodeNum-1 ; j++) {
						if(i != j && Cplex.getValue(X[i][j][k][r]) != 0) {
							System.out.println("x[" + i + "," + j + "," + k + "," + r + "]=" + 1);
						}
					}
				}
			}
		}
	}
	
	//读取算例数据的方法
	public static Instance readData(String path) throws IOException{
		//创建存放所有节点的数据的列表
		Instance instance = new Instance();
		List<Node> Nodes = new ArrayList<Node>();
		
		//创建文件读入对象
		BufferedReader br = new BufferedReader(new FileReader(path));
		
		//进行读取操作
		String line = null;
		int count = 0;
		while((line = br.readLine()) != null) {
			count += 1;
			System.out.println(line + count);
			if(count == 5) {
				String[] str = line.split("\\s+");
				instance.customerNum = customerNum;
				instance.nodeNum = nodeNum;
				instance.route = route;
				instance.vehicleNum = Integer.parseInt(str[1]);
				instance.vehicleCapacity = Integer.parseInt(str[2]);
			}else if(count >= 10 && count <= 10 + customerNum) {
				//读取一部分数据
				//读取全部数据就只是count >= 10
				//读取部分数据就是count >= 10 && count <= 10 + customerNum
				String[] str = line.split("\\s+");
				Node node = new Node();
				node.ID = Integer.parseInt(str[1]);
				node.Xcoor = Integer.parseInt(str[2]);
				node.Ycoor = Integer.parseInt(str[3]);
				node.demand = Integer.parseInt(str[4]);
				node.readyTime = Integer.parseInt(str[5]);
				node.dueTime = Integer.parseInt(str[6]);
				node.serviceTime = Integer.parseInt(str[7]);
				Nodes.add(node);
			}
			
		}
		//按照模型，将起始点复制一份加入Nodes列表当中
		Node node1 = new Node();
		node1.ID = instance.customerNum + 1;
		node1.Xcoor = Nodes.get(0).Xcoor;
		node1.Ycoor = Nodes.get(0).Ycoor;
		node1.demand = Nodes.get(0).demand;
		node1.readyTime = Nodes.get(0).readyTime;
		node1.dueTime = Nodes.get(0).dueTime;
		node1.serviceTime = Nodes.get(0).serviceTime;
		Nodes.add(node1);
		
		instance.Nodes = Nodes;
		br.close();
		
		return instance;
	}
	
	public static void printInstance(Instance instance) {
		System.out.println("vehicleNum" + "\t\t" + instance.vehicleNum);
		System.out.println("vehicleCapacity" + "\t\t" + instance.vehicleCapacity);
		for(Node node : instance.Nodes) {
			System.out.println(node.ID + "\t");
			System.out.println(node.Xcoor + "\t");
			System.out.println(node.Ycoor + "\t");
			System.out.println(node.demand + "\t");
			System.out.println(node.readyTime + "\t");
			System.out.println(node.dueTime + "\t");
			System.out.println(node.serviceTime + "\t");
		}
		
	}
	
	//截断小数3.2643-->3.2
	public static double double_truncate(double v) {
		int iv = (int)v;
		if(iv + 1 - v <= gap)
			return iv + 1;
		double dv = (v - iv) * 10;
		int idv = (int)dv;
		double rv = iv + idv/10.0;
		return rv;
	}
}