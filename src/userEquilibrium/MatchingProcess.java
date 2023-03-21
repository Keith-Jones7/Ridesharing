package userEquilibrium;

import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

class Solution {
    double[][] matching_result_driver;
    double[][] matching_result_passenger;
    double[][] matching_rate_driver;
    double[][] matching_rate_passenger;
    double[] matching_rate_sum_driver;
    double[] matching_rate_sum_passenger;
    double[] matching_sum;
}
public class MatchingProcess {
    double A = 1;
    double a = 1;
    double b = 1;
    double c = 1;

    int[][] matching_matrix;
    double[] driver_afford_rate;
    double[] passenger_afford_rate;
    int[] gender;
    double[] driver_count;
    double[] passenger_count;
    Solution solution;
    int driver_class_count;
    int passenger_class_count;
    IloCplex model;

    /**
     *
     * @param driver_class_count        司机种类
     * @param passenger_class_count     乘客种类
     * @param driver_afford_rate        司机意愿承担比例
     * @param passenger_afford_rate     乘客意愿承担比例
     * @param driver_count              司机各类数量
     * @param passenger_count           乘客各类数量
     */
    public MatchingProcess(int driver_class_count, int passenger_class_count,
                           double[] driver_afford_rate, double[] passenger_afford_rate,
                           double[] driver_count, double[] passenger_count) {
        matching_matrix = new int[driver_class_count][passenger_class_count];
        this.driver_class_count = driver_class_count;
        this.passenger_class_count = passenger_class_count;

        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                if((driver_afford_rate[i] + passenger_afford_rate[j] >= 1)) {
                    matching_matrix[i][j] = 1;
                }else {
                    matching_matrix[i][j] = 0;
                }
            }
        }
        this.driver_count = driver_count;
        this.passenger_count = passenger_count;
        this.driver_afford_rate = driver_afford_rate;
        this.passenger_afford_rate = passenger_afford_rate;
    }

    /**
     *
     * @param driver_class_count        司机种类
     * @param passenger_class_count     乘客种类
     * @param driver_afford_rate        司机意愿承担比例
     * @param passenger_afford_rate     乘客意愿承担比例
     * @param gender                    性别标识
     * @param driver_count              司机各类数量
     * @param passenger_count           乘客各类数量
     */
    public MatchingProcess(int driver_class_count, int passenger_class_count,
                           double[] driver_afford_rate, double[] passenger_afford_rate, int[] gender,
                           double[] driver_count, double[] passenger_count) {
        matching_matrix = new int[driver_class_count][passenger_class_count];
        this.driver_class_count = driver_class_count;
        this.passenger_class_count = passenger_class_count;

        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                if((driver_afford_rate[i] + passenger_afford_rate[j] >= 1) && (
                        Math.abs(gender[i] + gender[j]) * (gender[i] * gender[j]) >= 0
                        )) {
                    matching_matrix[i][j] = 1;
                }else {
                    matching_matrix[i][j] = 0;
                }
            }
        }
        this.driver_count = driver_count;
        this.passenger_count = passenger_count;
        this.driver_afford_rate = driver_afford_rate;
        this.passenger_afford_rate = passenger_afford_rate;
        this.gender = gender;
    }

    /**
     * 重置司机乘客参数
     * @param driver_count      司机各类数量
     * @param passenger_count   乘客各类数量
     */
    public void setParam(double[] driver_count, double[] passenger_count) {
        this.driver_count = driver_count;
        this.passenger_count = passenger_count;
    }

    /**
     * cplex匹配过程，求得实际匹配结果
     * @param _obj 目标函数选择，1为考虑公平性，2不考虑
     * @throws Exception cplex 直接抛出异常
     */
    public void matching(int _obj) throws Exception{
        //初始化相关内容
        model = new IloCplex();
        double optimality = 1e-9;
        model.setParam(IloCplex.Param.Simplex.Tolerances.Optimality, optimality);
        model.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, optimality);
        model.setOut(null);

        //决策变量
        IloNumVar[][] ND = new IloNumVar[driver_class_count][passenger_class_count];
        IloNumVar[][] NP = new IloNumVar[passenger_class_count][driver_class_count];
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                ND[i][j] = model.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "ND" + i + "," + j);
                NP[j][i] = model.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "NP" + j + "," + i);
            }
        }
        //目标函数
        IloNumExpr obj1 = model.numExpr();
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                obj1 = model.sum(obj1, model.prod(driver_afford_rate[i], ND[i][j]));
            }
        }
        for(int j = 0; j < passenger_class_count; j++) {
            for(int i = 0; i < driver_class_count; i++) {
                obj1 = model.sum(obj1, model.prod(passenger_afford_rate[j], NP[j][i]));
            }
        }
        IloNumExpr obj2 = model.numExpr();
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                obj2 = model.sum(obj2, NP[i][j]);
            }
        }
        IloNumExpr obj = model.numExpr();
        if(_obj == 1) {
            obj = obj1;
        }else if(_obj == 2) {
            obj = obj2;
        }
        model.addMaximize(obj);
        //约束条件

        //实际匹配人数小于意愿人数
        for(int i = 0; i < driver_class_count; i++) {
            IloNumExpr expr1 = model.numExpr();
            for(int j = 0; j < passenger_class_count; j++) {
                expr1 = model.sum(expr1, ND[i][j]);
            }
            model.addLe(expr1, driver_count[i]);
        }
        for(int j = 0; j < passenger_class_count; j++) {
            IloNumExpr expr2 = model.numExpr();
            for(int i = 0; i < driver_class_count; i++) {
                expr2 = model.sum(expr2, NP[j][i]);
            }
            model.addLe(expr2, passenger_count[j]);
        }

        //匹配对应人数相等
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                IloNumExpr expr1 = model.numExpr();
                expr1 = model.sum(expr1, ND[i][j]);
                IloNumExpr expr2 = model.numExpr();
                expr2 = model.sum(expr2, NP[j][i]);

                model.addEq(expr1, model.prod(expr2, matching_matrix[i][j]));
                model.addGe(expr1, 0);
                model.addEq(expr2, model.prod(expr1, matching_matrix[i][j]));
                model.addGe(expr2, 0);
            }
        }
        model.solve();
        //将结果输出
        solution = new Solution();
        solution.matching_result_driver = new double[driver_class_count][passenger_class_count];
        solution.matching_result_passenger = new double[passenger_class_count][driver_class_count];
        solution.matching_rate_driver = new double[driver_class_count][passenger_class_count];
        solution.matching_rate_passenger = new double[passenger_class_count][driver_class_count];
        solution.matching_rate_sum_driver = new double[driver_class_count];
        solution.matching_rate_sum_passenger = new double[passenger_class_count];
        solution.matching_sum = new double[2];
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                solution.matching_result_driver[i][j] = model.getValue(ND[i][j]);
                if(driver_count[i] == 0) {
                    solution.matching_rate_driver[i][j] = 0;
                }else {
                    solution.matching_rate_driver[i][j] = solution.matching_result_driver[i][j] / driver_count[i];
                }
                solution.matching_result_passenger[j][i] = model.getValue(NP[j][i]);
                if(passenger_count[j] == 0) {
                    solution.matching_rate_passenger[j][i] = 0;
                }else {
                    solution.matching_rate_passenger[j][i] = solution.matching_result_passenger[j][i] / passenger_count[j];
                }
            }
        }
        for(int i = 0; i < driver_class_count; i++) {
            double sum = 0;
            for(int j = 0; j < passenger_class_count; j++) {
                sum += solution.matching_result_driver[i][j];
            }
            if(driver_count[i] == 0) {
                solution.matching_rate_sum_driver[i] = 0;
            }else {
                solution.matching_rate_sum_driver[i] = sum / driver_count[i];
            }
            solution.matching_sum[0] += sum;
        }
        for(int j = 0; j < passenger_class_count; j++) {
            double sum = 0;
            for(int i = 0; i < driver_class_count; i++) {
                sum += solution.matching_result_passenger[j][i];
            }
            if(passenger_count[j] == 0) {
                solution.matching_rate_sum_passenger[j] = 0;
            }else {
                solution.matching_rate_sum_passenger[j] = sum / passenger_count[j];
            }
            solution.matching_sum[1] += sum;
        }
        model.clearModel();
    }

    /**
     * 匹配率的解析解（加速求解）
     */
    public void matching() {
        solution = new Solution();
        solution.matching_rate_driver = new double[driver_class_count][passenger_class_count];
        solution.matching_rate_passenger = new double[passenger_class_count][driver_class_count];
        solution.matching_rate_sum_driver = new double[driver_class_count];
        solution.matching_rate_sum_passenger = new double[passenger_class_count];
        solution.matching_result_driver = new double[driver_class_count][passenger_class_count];
        solution.matching_result_passenger = new double[passenger_class_count][driver_class_count];
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                solution.matching_rate_driver[i][j] = Prob_i_to_j(driver_count, passenger_count, i, j, 0);
                solution.matching_result_driver[i][j] = driver_count[i] * solution.matching_rate_driver[i][j];
                solution.matching_rate_sum_driver[i] += solution.matching_rate_driver[i][j];
            }

        }
        for(int j = 0; j < passenger_class_count; j++) {
            for(int i = 0; i < driver_class_count; i++) {
                solution.matching_rate_passenger[j][i] = Prob_i_to_j(driver_count, passenger_count, i, j, 1);
                solution.matching_result_passenger[j][i] = passenger_count[j] * solution.matching_rate_passenger[j][i];
                solution.matching_rate_sum_passenger[j] += solution.matching_rate_passenger[j][i];
            }
        }
    }

    /**
     * 计算匹配成功率
     * @param N_i_d 意愿司机人数
     * @param N_j_p 意愿乘客人数
     * @param i     司机类别
     * @param j     乘客类别
     * @param flag  flag == 0 求司机的匹配率，flag == 1 求乘客的匹配率
     * @return      匹配率
     */
    public double Prob_i_to_j(double[] N_i_d, double[] N_j_p, int i, int j, int flag) {
        double up;
        double down =  c;

        if(flag == 0) {
            up = A * matching_matrix[i][j] * N_j_p[j];
        }else {
            up = A * matching_matrix[i][j] * N_i_d[i];
        }

        for(int k = 0; k < driver_class_count; k++) {
            down += (a * matching_matrix[k][j] * N_i_d[k]);
        }
        for(int k = 0; k < passenger_class_count; k++) {
            down += (b * matching_matrix[i][k] * N_j_p[k]);
        }
        return up / down;
    }

    public double PartialProb(double[] N_i_d, double[] N_j_p, int i, int j, int flag) {
        double up = 0;
        double down = c;
        if (flag == 1 && i == j) {
            for (int k = 0; k < driver_class_count; k++) {
                up += (a * matching_matrix[k][j] * N_i_d[k]);
            }
            for (int k = 0; k < passenger_class_count; k++) {
                up += (A * b * matching_matrix[i][k] * N_j_p[k]);
            }
            up -= (A * b * matching_matrix[i][i] * N_j_p[i]);
            up += c;
        }else {
            up += (-A * a * matching_matrix[i][j] * N_j_p[j]);
        }

        for(int k = 0; k < driver_class_count; k++) {
            down += (a * matching_matrix[k][j] * N_i_d[k]);
        }
        for(int k = 0; k < passenger_class_count; k++) {
            down += (b * matching_matrix[i][k] * N_j_p[k]);
        }
        return up / (down * down);
    }
    /**
     * 输出意愿承担比例
     * @param afford_rate 承担比例
     */
    public void printAffordRate(double[] afford_rate) {
        System.out.println("\n意愿承担比例:");
        for(double rate : afford_rate) {
            System.out.print(rate + "  \t");
        }
    }

    /**
     * 输出意愿匹配人数
     * @param people_count 人数
     */
    public void printPeopleCount(double[] people_count) {
        System.out.println("\n意愿匹配人数:");
        for(double count : people_count) {
            System.out.print(count + "  \t");
        }
    }

    /**
     * 输出匹配结果
     * @param matching_result 匹配结果矩阵
     */
    public void printMatchingResult(double[][] matching_result) {
        System.out.println("\n实际匹配人数:");
        for(double[] results : matching_result) {
            double sum = 0;
            for(double result : results) {
                sum += result;
            }
            System.out.print(sum + "  \t");
        }
    }

    /**
     * 输出实际匹配概率
     * @param matching_rate 实际匹配率
     */
    public void printMatchingRate(double[] matching_rate) {
        System.out.println("\n实际匹配概率:");
        for(double rate : matching_rate) {
            System.out.print(String.format("%.2f", rate) + "  \t");
        }
    }

    /**
     * 输出匹配结果
     */
    public void printSolution() {
        System.out.println("匹配结果展示如下:");
        for(int i = 0; i < driver_class_count; i++) {
            for (int j = 0; j < passenger_class_count; j++) {
                System.out.print(solution.matching_result_driver[i][j] + "   \t");
            }
            System.out.println();
        }
        System.out.print("\n司机对应类别及其匹配率为:");
        printAffordRate(driver_afford_rate);
        printPeopleCount(driver_count);
        printMatchingResult(solution.matching_result_driver);
        printMatchingRate(solution.matching_rate_sum_driver);
        System.out.println();

        System.out.print("\n乘客对应类别及其匹配率为:");
        printAffordRate(passenger_afford_rate);
        printPeopleCount(passenger_count);
        printMatchingResult(solution.matching_result_passenger);
        printMatchingRate(solution.matching_rate_sum_passenger);
    }
    public static void main(String[] args) throws Exception{
        //设置输入相关参数
        int driver_class_count = 1;
        int passenger_class_count = 1;
        int _obj = 1;
        int _limit = 500;
        boolean _default =true;

        //生成算例
        SampleGenerator sample = new SampleGenerator(driver_class_count, passenger_class_count);
        sample.setLimit(_limit);
        sample.generate(_default);
        int k = 0;
        MatchingProcess test = new MatchingProcess(driver_class_count, passenger_class_count,
                sample.driver_afford_rate, sample.passenger_afford_rate,
                sample.driver_count, sample.passenger_count);
        test.matching();
        test.printSolution();
        System.out.println(test.solution.matching_sum[0]);
        System.out.println();

        SUEInDay.printMatrix(test.matching_matrix);
        /*
        for(int i = 0; i < 400; i++) {
            sample.driver_count[k] = i;
            //匹配结果求解
            MatchingProcess test = new MatchingProcess(
                    driver_class_count, passenger_class_count,
                    sample.driver_afford_rate, sample.passenger_afford_rate,
                    sample.driver_count, sample.passenger_count);
            test.matching(_obj);
            System.out.println(i + "\t" + String.format("%.2f", test.solution.matching_result_driver[k][0]) +
                    "\t" + String.format("%.2f", test.solution.matching_result_driver[k][1]) +
                    "\t" + String.format("%.2f", test.solution.matching_result_driver[k][2]) +
                    "\t" + String.format("%.2f", test.solution.matching_result_driver[k][0] +
                                                 test.solution.matching_result_driver[k][1] +
                                                 test.solution.matching_result_driver[k][2]) +
                    "\t" + String.format("%.4f", test.solution.matching_rate_driver[k]));
        }
         */
    }
}
