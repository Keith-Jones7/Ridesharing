package userEquilibrium;

import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

class Solution {
    double[][] matchingResultDriver;
    double[][] matchingResultPassenger;
    double[][] matchingRateDriver;
    double[][] matchingRatePassenger;
    double[] matchingRateSumDriver;
    double[] matchingRateSumPassenger;
    double[] matchingSum;
}
public class MatchingProcess {
    double A = 1;
    double a = 1;
    double b = 1;
    double c = 1;

    double[][] matchingMatrix;
    double[] driverAffordRate;
    double[] passengerAffordRate;
    int[] gender;
    double[] driverCount;
    double[] passengerCount;
    Solution solution;
    int driverClassCount;
    int passengerClassCount;
    IloCplex model;

    /**
     *
     * @param driverClassCount        司机种类
     * @param passengerClassCount     乘客种类
     * @param driverAffordRate        司机意愿承担比例
     * @param passengerAffordRate     乘客意愿承担比例
     * @param driverCount              司机各类数量
     * @param passengerCount           乘客各类数量
     */
    public MatchingProcess(int driverClassCount, int passengerClassCount,
                           double[] driverAffordRate, double[] passengerAffordRate,
                           double[] driverCount, double[] passengerCount) {
        matchingMatrix = new double[driverClassCount][passengerClassCount];
        this.driverClassCount = driverClassCount;
        this.passengerClassCount = passengerClassCount;

        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                matchingMatrix[i][j] = driverAffordRate[i] + passengerAffordRate[j];
//                if((driverAffordRate[i] + passengerAffordRate[j] >= 1)) {
//                    matchingMatrix[i][j] = 1;
//                }else {
//                    matchingMatrix[i][j] = 0;
//                }
            }
        }
        this.driverCount = driverCount;
        this.passengerCount = passengerCount;
        this.driverAffordRate = driverAffordRate;
        this.passengerAffordRate = passengerAffordRate;
    }

    /**
     *
     * @param driverClassCount        司机种类
     * @param passengerClassCount     乘客种类
     * @param driverAffordRate        司机意愿承担比例
     * @param passengerAffordRate     乘客意愿承担比例
     * @param gender                    性别标识
     * @param driverCount              司机各类数量
     * @param passengerCount           乘客各类数量
     */
    public MatchingProcess(int driverClassCount, int passengerClassCount,
                           double[] driverAffordRate, double[] passengerAffordRate, int[] gender,
                           double[] driverCount, double[] passengerCount) {
        matchingMatrix = new double[driverClassCount][passengerClassCount];
        this.driverClassCount = driverClassCount;
        this.passengerClassCount = passengerClassCount;

        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                if((driverAffordRate[i] + passengerAffordRate[j] >= 1) && (
                        Math.abs(gender[i] + gender[j]) * (gender[i] * gender[j]) >= 0
                        )) {
                    matchingMatrix[i][j] = 1;
                }else {
                    matchingMatrix[i][j] = 0;
                }
            }
        }
        this.driverCount = driverCount;
        this.passengerCount = passengerCount;
        this.driverAffordRate = driverAffordRate;
        this.passengerAffordRate = passengerAffordRate;
        this.gender = gender;
    }

    /**
     * 重置司机乘客参数
     * @param driverCount      司机各类数量
     * @param passengerCount   乘客各类数量
     */
    public void setParam(double[] driverCount, double[] passengerCount) {
        this.driverCount = driverCount;
        this.passengerCount = passengerCount;
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
        IloNumVar[][] ND = new IloNumVar[driverClassCount][passengerClassCount];
        IloNumVar[][] NP = new IloNumVar[passengerClassCount][driverClassCount];
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                ND[i][j] = model.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "ND" + i + "," + j);
                NP[j][i] = model.numVar(0, Double.MAX_VALUE, IloNumVarType.Float, "NP" + j + "," + i);
            }
        }
        //目标函数
        IloNumExpr obj1 = model.numExpr();
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                obj1 = model.sum(obj1, model.prod(driverAffordRate[i], ND[i][j]));
            }
        }
        for(int j = 0; j < passengerClassCount; j++) {
            for(int i = 0; i < driverClassCount; i++) {
                obj1 = model.sum(obj1, model.prod(passengerAffordRate[j], NP[j][i]));
            }
        }
        IloNumExpr obj2 = model.numExpr();
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
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
        for(int i = 0; i < driverClassCount; i++) {
            IloNumExpr expr1 = model.numExpr();
            for(int j = 0; j < passengerClassCount; j++) {
                expr1 = model.sum(expr1, ND[i][j]);
            }
            model.addLe(expr1, driverCount[i]);
        }
        for(int j = 0; j < passengerClassCount; j++) {
            IloNumExpr expr2 = model.numExpr();
            for(int i = 0; i < driverClassCount; i++) {
                expr2 = model.sum(expr2, NP[j][i]);
            }
            model.addLe(expr2, passengerCount[j]);
        }

        //匹配对应人数相等
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                IloNumExpr expr1 = model.numExpr();
                expr1 = model.sum(expr1, ND[i][j]);
                IloNumExpr expr2 = model.numExpr();
                expr2 = model.sum(expr2, NP[j][i]);

                model.addEq(expr1, model.prod(expr2, matchingMatrix[i][j]));
                model.addGe(expr1, 0);
                model.addEq(expr2, model.prod(expr1, matchingMatrix[i][j]));
                model.addGe(expr2, 0);
            }
        }
        model.solve();
        //将结果输出
        solution = new Solution();
        solution.matchingResultDriver = new double[driverClassCount][passengerClassCount];
        solution.matchingResultPassenger = new double[passengerClassCount][driverClassCount];
        solution.matchingRateDriver = new double[driverClassCount][passengerClassCount];
        solution.matchingRatePassenger = new double[passengerClassCount][driverClassCount];
        solution.matchingRateSumDriver = new double[driverClassCount];
        solution.matchingRateSumPassenger = new double[passengerClassCount];
        solution.matchingSum = new double[2];
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                solution.matchingResultDriver[i][j] = model.getValue(ND[i][j]);
                if(driverCount[i] == 0) {
                    solution.matchingRateDriver[i][j] = 0;
                }else {
                    solution.matchingRateDriver[i][j] = solution.matchingResultDriver[i][j] / driverCount[i];
                }
                solution.matchingResultPassenger[j][i] = model.getValue(NP[j][i]);
                if(passengerCount[j] == 0) {
                    solution.matchingRatePassenger[j][i] = 0;
                }else {
                    solution.matchingRatePassenger[j][i] = solution.matchingResultPassenger[j][i] / passengerCount[j];
                }
            }
        }
        for(int i = 0; i < driverClassCount; i++) {
            double sum = 0;
            for(int j = 0; j < passengerClassCount; j++) {
                sum += solution.matchingResultDriver[i][j];
            }
            if(driverCount[i] == 0) {
                solution.matchingRateSumDriver[i] = 0;
            }else {
                solution.matchingRateSumDriver[i] = sum / driverCount[i];
            }
            solution.matchingSum[0] += sum;
        }
        for(int j = 0; j < passengerClassCount; j++) {
            double sum = 0;
            for(int i = 0; i < driverClassCount; i++) {
                sum += solution.matchingResultPassenger[j][i];
            }
            if(passengerCount[j] == 0) {
                solution.matchingRateSumPassenger[j] = 0;
            }else {
                solution.matchingRateSumPassenger[j] = sum / passengerCount[j];
            }
            solution.matchingSum[1] += sum;
        }
        model.clearModel();
    }

    /**
     * 匹配率的解析解（加速求解）
     */
    public void matching() {
        solution = new Solution();
        solution.matchingRateDriver = new double[driverClassCount][passengerClassCount];
        solution.matchingRatePassenger = new double[passengerClassCount][driverClassCount];
        solution.matchingRateSumDriver = new double[driverClassCount];
        solution.matchingRateSumPassenger = new double[passengerClassCount];
        solution.matchingResultDriver = new double[driverClassCount][passengerClassCount];
        solution.matchingResultPassenger = new double[passengerClassCount][driverClassCount];
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                solution.matchingRateDriver[i][j] = Prob_i_to_j(driverCount, passengerCount, i, j, 0);
                solution.matchingResultDriver[i][j] = driverCount[i] * solution.matchingRateDriver[i][j];
                solution.matchingRateSumDriver[i] += solution.matchingRateDriver[i][j];
            }

        }
        for(int j = 0; j < passengerClassCount; j++) {
            for(int i = 0; i < driverClassCount; i++) {
                solution.matchingRatePassenger[j][i] = Prob_i_to_j(driverCount, passengerCount, i, j, 1);
                solution.matchingResultPassenger[j][i] = passengerCount[j] * solution.matchingRatePassenger[j][i];
                solution.matchingRateSumPassenger[j] += solution.matchingRatePassenger[j][i];
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
            up = A * matchingMatrix[i][j] * N_j_p[j];
        }else {
            up = A * matchingMatrix[i][j] * N_i_d[i];
        }

        for(int k = 0; k < driverClassCount; k++) {
            down += (a * matchingMatrix[k][j] * N_i_d[k]);
        }
        for(int k = 0; k < passengerClassCount; k++) {
            down += (b * matchingMatrix[i][k] * N_j_p[k]);
        }
        return up / down;
    }

    public double PartialProb(double[] N_i_d, double[] N_j_p, int i, int j, int flag) {
        double up = 0;
        double down = c;
        if (flag == 1 && i == j) {
            for (int k = 0; k < driverClassCount; k++) {
                up += (a * matchingMatrix[k][j] * N_i_d[k]);
            }
            for (int k = 0; k < passengerClassCount; k++) {
                up += (A * b * matchingMatrix[i][k] * N_j_p[k]);
            }
            up -= (A * b * matchingMatrix[i][i] * N_j_p[i]);
            up += c;
        }else {
            up += (-A * a * matchingMatrix[i][j] * N_j_p[j]);
        }

        for(int k = 0; k < driverClassCount; k++) {
            down += (a * matchingMatrix[k][j] * N_i_d[k]);
        }
        for(int k = 0; k < passengerClassCount; k++) {
            down += (b * matchingMatrix[i][k] * N_j_p[k]);
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
        for(int i = 0; i < driverClassCount; i++) {
            for (int j = 0; j < passengerClassCount; j++) {
                System.out.print(solution.matchingResultDriver[i][j] + "   \t");
            }
            System.out.println();
        }
        System.out.print("\n司机对应类别及其匹配率为:");
        printAffordRate(driverAffordRate);
        printPeopleCount(driverCount);
        printMatchingResult(solution.matchingResultDriver);
        printMatchingRate(solution.matchingRateSumDriver);
        System.out.println();

        System.out.print("\n乘客对应类别及其匹配率为:");
        printAffordRate(passengerAffordRate);
        printPeopleCount(passengerCount);
        printMatchingResult(solution.matchingResultPassenger);
        printMatchingRate(solution.matchingRateSumPassenger);
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
        System.out.println(test.solution.matchingSum[0]);
        System.out.println();

        SUEInDay.printMatrix(test.matchingMatrix);
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
