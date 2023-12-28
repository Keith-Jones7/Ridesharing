package userEquilibrium;

import userEquilibrium.common.Param;

public class SUE {

    double Nt;

    int W;      //出行者类别
    double[] driverAffordRate;
    double[] passengerAffordRate;
    double[] sizeRate;

    double[][] cost;
    double[][] na;
    double[][] nr;
    double[][] prob;
    double[][] probSolve;
    double N0; // 道路车辆总数
    double nMatchingSum; // 总匹配成功数
    double sumCost; // 总出行成本
    double[][][] deltaC; // 成本关于人数的雅克比矩阵
    MatchingProcess matching;

    public SUE(int W, double Nt, double[] size_rate,
               double[] driverAffordRate, double[] passengerAffordRate) {
        this.Nt = Nt;
        this.W = W;
        this.sizeRate = size_rate;
        this.driverAffordRate = driverAffordRate;
        this.passengerAffordRate = passengerAffordRate;

        cost = new double[W][Param.M];
        na = new double[W][Param.M];
        nr = new double[W][Param.M];
        prob = new double[W][Param.M];
        probSolve = new double[W][Param.M];
        deltaC = new double[W][4][4];
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < Param.M; j++) {
                probSolve[i][j] = 0;
            }
        }
        for(int i = 0; i < W; i++) {
            for (int j = 0; j < Param.M; j++) {
                na[i][j] = size_rate[i] * probSolve[i][j] * Nt;
            }
        }

    }

    public void solveSUE() {
        updateCost();
        updateProb();
        MSA();
        updateNR();
        //System.out.println("分配比例");
        //printMatrix(Prob);
        //System.out.println("出行成本");
        //printMatrix(cost);
        //System.out.println("司机的匹配率");
        //printArray(matching.solution.matching_rate_sum_driver);
        //System.out.println("乘客的匹配率");
        //printArray(matching.solution.matching_rate_sum_passenger);
        calNv();
    }

    /**
     * 使用MSA算法求解均衡解
     */
    public void MSA() {
        int count = 1;
        while (Norm(prob, probSolve) > Param.precision && count < Param.maxCount) {
            for(int i = 0; i < W; i++) {
                for(int j = 0; j < Param.M; j++) {
                    probSolve[i][j] += ((prob[i][j] - probSolve[i][j]) / count);
                    na[i][j] = sizeRate[i] * probSolve[i][j] * Nt;
                }
            }
            updateCost();
            updateProb();
//            updateRate(count);
            count++;
//            System.out.println("期望比例");
//            printMatrix(probSolve);
//            System.out.println("实际比例");
//            printMatrix(prob);
//            System.out.println("分摊比例");
//            printArray(driverAffordRate);
//            printArray(passengerAffordRate);
            System.out.println("第"+ count + "次迭代");
        }
    }

    /**
     * 更新共乘成本意愿分摊比例
     */
    public void updateRate(int count) {
        double[] optDriverAffordRate = new double[W];
        double[] optPassengerAffordRate = new double[W];
        double[] driverCount = new double[W];
        double[] passengerCount = new double[W];
        for(int i = 0; i < W; i++) {
            driverCount[i] = na[i][2];
            passengerCount[i] = na[i][3];
        }
        for (int i = 0; i < W; i++) {
            double driverRate = driverAffordRate[i];
            double tempDriverRate = 0;
            double tempDriverMinCost = Double.MAX_VALUE;
            while (tempDriverRate < 1) {
                tempDriverRate += 0.01;
                driverAffordRate[i] = tempDriverRate;
                MatchingProcess match = new MatchingProcess(W, W,
                        driverAffordRate, passengerAffordRate,
                        driverCount, passengerCount);
                match.matching();
                Cost calCost = new Cost(Param.vt);
                double cost = calCost.calCost(driverAffordRate, passengerAffordRate,
                        match.solution.matchingRateDriver[i], match.solution.matchingRatePassenger[i],
                        match.solution.matchingRateSumDriver[i], match.solution.matchingRateSumPassenger[i],
                        this.nr, i)[2];
                if (cost < tempDriverMinCost) {
                    tempDriverMinCost = cost;
                    optDriverAffordRate[i] = tempDriverRate;
                }
            }
            driverAffordRate[i] = driverRate;

            double passengerRate = passengerAffordRate[i];
            double tempPassengerRate = 0;
            double tempPassengerMinCost = Double.MAX_VALUE;
            while (tempPassengerRate < 1) {
                tempPassengerRate += 0.01;
                passengerAffordRate[i] = tempPassengerRate;
                MatchingProcess match = new MatchingProcess(W, W,
                        driverAffordRate, passengerAffordRate,
                        driverCount, passengerCount);
                match.matching();
                Cost calCost = new Cost(Param.vt);
                double cost = calCost.calCost(driverAffordRate, passengerAffordRate,
                        match.solution.matchingRateDriver[i], match.solution.matchingRatePassenger[i],
                        match.solution.matchingRateSumDriver[i], match.solution.matchingRateSumPassenger[i],
                        this.nr, i)[3];
                if (cost < tempPassengerMinCost) {
                    tempPassengerMinCost = cost;
                    optPassengerAffordRate[i] = tempPassengerRate;
                }
            }
            passengerAffordRate[i] = passengerRate;
        }
        for (int i = 0; i < W; i++) {
            driverAffordRate[i] += (optDriverAffordRate[i] - driverAffordRate[i]) / count;
            passengerAffordRate[i] += (optPassengerAffordRate[i] - passengerAffordRate[i]) / count;
        }
        printArray(optDriverAffordRate);
        printArray(optPassengerAffordRate);
    }
    /**
     * 根据最新的成本公式更新期望比例
     */
    public void updateProb() {
        double[] cost_sum = new double[W];
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < Param.M; j++) {
                cost_sum[i] += Math.exp(- Param.theta * cost[i][j]);
            }
        }
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < Param.M; j++) {
                prob[i][j] = Math.exp(- Param.theta * cost[i][j]) / cost_sum[i];
            }
        }
    }

    /**
     * 根据最新的实际比例更新期望成本
     */
    public void updateCost(){
        double[] driverCount = new double[W];
        double[] passengerCount = new double[W];
        sumCost = 0;
        for(int i = 0; i < W; i++) {
            driverCount[i] = na[i][2];
            passengerCount[i] = na[i][3];
        }
        Cost calCost = new Cost(Param.vt);
        if(matching == null) {
            matching = new MatchingProcess(W, W,
                    driverAffordRate, passengerAffordRate,
                    driverCount, passengerCount);
        }else {
            matching.setParam(driverCount, passengerCount);
        }
        matching.matching();
        for(int i = 0; i < W; i++) {
            cost[i] = calCost.calCost(driverAffordRate, passengerAffordRate,
                    matching.solution.matchingRateDriver[i], matching.solution.matchingRatePassenger[i],
                    matching.solution.matchingRateSumDriver[i], matching.solution.matchingRateSumPassenger[i],
                    this.nr, i);
        }
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[0].length; j++) {
                sumCost += (Nt * probSolve[i][j] * cost[i][j]);
            }
        }
        //  printMatrix(matching.solution.matching_rate_driver);

    }

    /**
     * 更新实际出行人数
     */
    public void updateNR() {
        for(int i = 0; i < W; i++) {
            nr[i][2] = matching.solution.matchingRateSumDriver[i] * na[i][2];
            nr[i][3] = matching.solution.matchingRateSumPassenger[i] * na[i][3];
            nr[i][0] = na[i][0] + na[i][3] - nr[i][3];
            nr[i][1] = na[i][1] + na[i][2] - nr[i][2];
        }
    }

    /**
     *
     * 求道路车辆总数
     */
    public void calNv() {
        this.N0 = 0;
        this.nMatchingSum = 0;
        for (int i = 0; i < W; i++) {
            N0 += (nr[i][1] + nr[i][2]);
            nMatchingSum += nr[i][2];
        }
    }

    /**
     * 求解两个矩阵之间的精度之差，采用二范数
     * @param matrix1 矩阵1
     * @param matrix2 矩阵2
     * @return 矩阵差值的2范数
     */
    public static double Norm(double[][] matrix1, double[][] matrix2) {
        double sum1 = 0, sum2 = 0;
        for(int i = 0; i < matrix1.length; i++) {
            for(int j = 0; j < matrix1[0].length; j++) {
                sum1 += Math.pow(matrix1[i][j] - matrix2[i][j], 2);
                sum2 += matrix2[i][j];
            }
        }
        return Math.sqrt(sum1);
    }

    /**
     * 打印一个矩阵
     * @param matrix 需要打印的矩阵
     */
    public static void printMatrix(double[][] matrix) {
        for (double[] doubles : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(String.format("%.4f", doubles[j]) + "  \t");
            }
            System.out.println();
        }
    }
    public static void printMatrix(int[][] matrix) {
        for (int[] ints : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(ints[j] + "  \t");
            }
            System.out.println();
        }
    }
    public static void printArray(double[] array) {
        for(double arr : array) {
            System.out.println(String.format("%.2f", arr) + "  \t");
        }
    }
}
class Cost {
    static final double[] ALPHA = {30, 30, 20, 30, 40, 50, 60, 70, 80, 90};         //行驶过程的VoT
    static final double LB = 30;            //公共交通里程
    static final double VB = 30;            //公共交通平均速度
    static final double FREQ = 10;          //公共交通发车频率
    static final double PB = 2;            //公共交通票价
    static final double GAMMA = 2;        //公交车拥挤成本系数
    static final double ROU = 0.35;          //公交车拥挤惩罚系数
    static final double B = 30;             //公交车最大容量

    static final double LS = 36;            //独驾里程
    static final double LD = 2;             //平均绕行距离
    static final double VS = 50;            //小汽车零流速度
    static final double LAMBDA = 0.7;       //每公里燃料费
    static final double CF = 1;           //每公里车辆折旧成本

    static final double ALPHA1 = 0.15;      //BPR系数1
    static final double ALPHA2 = 4;         //BPR系数2
    static final double Capacity = 600;     //道路车辆容量
    static final double DISCOMFORT_D = 2;   //司机的舒适度成本
    static final double DISCOMFORT_P = 3;   //乘客的舒适度成本
    static final double THETA_D = 2;        //司机参与匹配的固定成本
    static final double THETA_P = 2;        //乘客参与匹配的固定成本
    double vt;                              //路网车辆平均行驶速度（可变）
    double sum_cost;
    public Cost(double vt) {
        this.vt = vt;
    }

    /**
     * 计算成本
     *
     * @param driverAffordRate    司机的承担比例
     * @param passengerAffordRate 乘客的承担比例
     * @param matchingRateDriver  司机的预期匹配率
     * @param matchingRatePassenger   乘客的预期匹配率
     * @param matchingRateSumDriver 司机的累计匹配率
     * @param matchingRateSumPassenger 乘客的累计匹配率
     * @param index 出行者的类别
     * @return 返回同一类出行者选择该种出行方式的成本
     */
    public double[] calCost(double[] driverAffordRate, double[] passengerAffordRate,
                            double[] matchingRateDriver, double[] matchingRatePassenger,
                            double matchingRateSumDriver, double matchingRateSumPassenger,
                            double[][] NR, int index) {
        double nb = 0, nc = 0;
        for (double[] num : NR) {
            nb += num[0];
            nc += num[1];
            nc += num[2];
        }

        vt = VS / (1 + ALPHA1 * Math.pow(nc / Capacity, ALPHA2));
        double cb = ALPHA[index] * (LB / VB + 1 / (2 * FREQ)) + PB + GAMMA * (LB / VB) * (1 + ROU * nb / B);
        double cs = ALPHA[index] * (LS / vt)  + LAMBDA * LS + CF * LS;
        double cd = 0;
        for(int j = 0; j < passengerAffordRate.length; j++) {
            cd += matchingRateDriver[j] *
                    ((driverAffordRate[index] / (driverAffordRate[index] + passengerAffordRate[j])) * LAMBDA * (LS + LD) +
                            ALPHA[index] * (LS + LD) / vt + DISCOMFORT_D);
        }
        double cp = 0;
        for(int i = 0; i < driverAffordRate.length; i++) {
            cp += matchingRatePassenger[i] *
                    ((passengerAffordRate[index] / (passengerAffordRate[index] + driverAffordRate[i])) * LAMBDA * (LS + LD) +
                            ALPHA[index] * (LS + LD) / vt + DISCOMFORT_P);
        }
        double cdd = cd + (1 - matchingRateSumDriver) * cs + THETA_D;
        double cpp = cp + (1 - matchingRateSumPassenger) * cb + THETA_P;
        return new double[]{cb, cs,
                cd + (1 - matchingRateSumDriver) * cs + THETA_D,
                cp + (1 - matchingRateSumPassenger) * cb + THETA_P};

    }
}
