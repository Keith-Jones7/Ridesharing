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
    double nc; // 道路车辆总数
    double nMatchingSum; // 总匹配成功数
    double sumCost; // 总出行成本
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
        for (int i = 0; i < W; i++) {
            for (int j = 0; j < Param.M; j++) {
                probSolve[i][j] = 0;
            }
        }
        for (int i = 0; i < W; i++) {
            for (int j = 0; j < Param.M; j++) {
                na[i][j] = size_rate[i] * probSolve[i][j] * Nt;
            }
        }

    }

    /**
     * 求解两个矩阵之间的精度之差，采用二范数
     *
     * @param matrix1 矩阵1
     * @param matrix2 矩阵2
     * @return 矩阵差值的2范数
     */
    public static double Norm(double[][] matrix1, double[][] matrix2) {
        double sum1 = 0, sum2 = 0;
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                sum1 += Math.pow(matrix1[i][j] - matrix2[i][j], 2);
                sum2 += matrix2[i][j];
            }
        }
        return Math.sqrt(sum1);
    }

    /**
     * 打印一个矩阵
     *
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
        for (int[] integers : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(integers[j] + "  \t");
            }
            System.out.println();
        }
    }

    public static void printArray(double[] array) {
        for (double arr : array) {
            System.out.print(String.format("%.2f", arr) + "  \t");
        }
        System.out.println();
    }

    public void solveSUE() {
        updateCost();
        updateProb();
        MSA();
        updateNR();
//        System.out.println("分配比例");
//        printMatrix(Prob);
//        System.out.println("出行成本");
//        printMatrix(cost);
//        System.out.println("司机的匹配率");
//        printArray(matching.solution.matching_rate_sum_driver);
//        System.out.println("乘客的匹配率");
//        printArray(matching.solution.matching_rate_sum_passenger);
        calNv();
    }

    /**
     * 使用MSA算法求解均衡解
     */
    public void MSA() {
        int count = 1;
        while (Norm(prob, probSolve) > Param.precision && count < Param.maxCount) {
            for (int i = 0; i < W; i++) {
                for (int j = 0; j < Param.M; j++) {
                    probSolve[i][j] += ((prob[i][j] - probSolve[i][j]) / count);
                    na[i][j] = sizeRate[i] * probSolve[i][j] * Nt;
                }
            }
            updateCost();
            updateProb();
            count++;
            System.out.println("期望比例");
            printMatrix(probSolve);
            System.out.println("实际比例");
            printMatrix(prob);
            if (Param.isFree) {
                updateRate(count);
                System.out.println("分摊比例");
                printArray(driverAffordRate);
                printArray(passengerAffordRate);
            }
            System.out.println("第" + count + "次迭代");
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
        for (int i = 0; i < W; i++) {
            driverCount[i] = na[i][2];
            passengerCount[i] = na[i][3];
        }
        Cost calCost = new Cost(Param.vt);
        for (int i = 0; i < W; i++) {


            double passengerRate = passengerAffordRate[i];
            double tempPassengerRate = 0;
//            double coff1 = (1 / (1 - (Param.LAMBDA * Param.LS - Param.ALPHA[i] * Param.LD / Param.vt - Param.DISCOMFORT_D) /
//                    (Param.LAMBDA * (Param.LS + Param.LD))) - 1);
//            double coff2 = (1 / (1 - (Param.ALPHA[i] * (Param.LB / Param.VB + 0.5 / Param.FREQ - (Param.LS + Param.LD) / Param.vt)
//             + Param.PB + Param.GAMMA * (Param.LB / Param.VB) - Param.DISCOMFORT_P) / (Param.LAMBDA * (Param.LS + Param.LD))) - 1);

            double tempPassengerMinCost = Double.MAX_VALUE;
            while (tempPassengerRate < 1) {
                tempPassengerRate += 0.01;
                passengerAffordRate[i] = tempPassengerRate;
                MatchingProcess match = new MatchingProcess(W, W,
                        driverAffordRate, passengerAffordRate,
                        driverCount, passengerCount);
                match.matching();

                double cost = calCost.calCost(driverAffordRate, passengerAffordRate,
                        match.matchSolution.matchingRateDriver[i], match.matchSolution.matchingRatePassenger[i],
                        match.matchSolution.matchingRateSumDriver[i], match.matchSolution.matchingRateSumPassenger[i],
                        this.nr, i)[3];
                if (cost < tempPassengerMinCost) {
                    tempPassengerMinCost = cost;
                    optPassengerAffordRate[i] = tempPassengerRate;
                }
            }
            passengerAffordRate[i] = passengerRate;

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
                double cost = calCost.calCost(driverAffordRate, passengerAffordRate,
                        match.matchSolution.matchingRateDriver[i], match.matchSolution.matchingRatePassenger[i],
                        match.matchSolution.matchingRateSumDriver[i], match.matchSolution.matchingRateSumPassenger[i],
                        this.nr, i)[2];
                if (cost < tempDriverMinCost) {
                    tempDriverMinCost = cost;
                    optDriverAffordRate[i] = tempDriverRate;
                }
            }
            driverAffordRate[i] = driverRate;
        }
        for (int i = 0; i < W; i++) {
            driverAffordRate[i] += (optDriverAffordRate[i] - driverAffordRate[i]) / count;
            passengerAffordRate[i] += (optPassengerAffordRate[i] - passengerAffordRate[i]) / count;
        }
    }

    /**
     * 根据最新的成本公式更新期望比例
     */
    public void updateProb() {
        double[] cost_sum = new double[W];
        for (int i = 0; i < W; i++) {
            for (int j = 0; j < Param.M; j++) {
                cost_sum[i] += Math.exp(-Param.theta * cost[i][j]);
            }
        }
        for (int i = 0; i < W; i++) {
            for (int j = 0; j < Param.M; j++) {
                prob[i][j] = Math.exp(-Param.theta * cost[i][j]) / cost_sum[i];
            }
        }
    }

    /**
     * 根据最新的实际比例更新期望成本
     */
    public void updateCost() {
        double[] driverCount = new double[W];
        double[] passengerCount = new double[W];
        sumCost = 0;
        for (int i = 0; i < W; i++) {
            driverCount[i] = na[i][2];
            passengerCount[i] = na[i][3];
        }
        Cost calCost = new Cost(Param.vt);
        matching = new MatchingProcess(W, W,
                driverAffordRate, passengerAffordRate,
                driverCount, passengerCount);
        matching.matching();
        for (int i = 0; i < W; i++) {
            cost[i] = calCost.calCost(driverAffordRate, passengerAffordRate,
                    matching.matchSolution.matchingRateDriver[i], matching.matchSolution.matchingRatePassenger[i],
                    matching.matchSolution.matchingRateSumDriver[i], matching.matchSolution.matchingRateSumPassenger[i],
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
        for (int i = 0; i < W; i++) {
            nr[i][2] = matching.matchSolution.matchingRateSumDriver[i] * na[i][2];
            nr[i][3] = matching.matchSolution.matchingRateSumPassenger[i] * na[i][3];
            nr[i][0] = na[i][0] + na[i][3] - nr[i][3];
            nr[i][1] = na[i][1] + na[i][2] - nr[i][2];
        }
    }

    /**
     * 求道路车辆总数
     */
    public void calNv() {
        this.nc = 0;
        this.nMatchingSum = 0;
        for (int i = 0; i < W; i++) {
            nc += (nr[i][1] + nr[i][2]);
            nMatchingSum += nr[i][2];
        }
    }
}

class Cost {
    double vt;                              //路网车辆平均行驶速度（可变）

    public Cost(double vt) {
        this.vt = vt;
    }

    /**
     * 计算成本
     *
     * @param driverAffordRate         司机的承担比例
     * @param passengerAffordRate      乘客的承担比例
     * @param matchingRateDriver       司机的预期匹配率
     * @param matchingRatePassenger    乘客的预期匹配率
     * @param matchingRateSumDriver    司机的累计匹配率
     * @param matchingRateSumPassenger 乘客的累计匹配率
     * @param index                    出行者的类别
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

        vt = Param.VS / (1 + Param.ALPHA1 * Math.pow(nc / Param.Capacity, Param.ALPHA2));
        double cb = Param.ALPHA[index] * (Param.LB / Param.VB + 1 / (2 * Param.FREQ)) + Param.PB + Param.GAMMA * (Param.LB / Param.VB) * (1 + Param.ROU * nb / Param.B);
        double cs = Param.ALPHA[index] * (Param.LS / vt) + Param.LAMBDA * Param.LS + Param.CF * Param.LS;
        double cd = 0;
        for (int j = 0; j < passengerAffordRate.length; j++) {
            cd += matchingRateDriver[j] *
                    ((driverAffordRate[index] / (driverAffordRate[index] + passengerAffordRate[j])) * Param.LAMBDA * (Param.LS + Param.LD) +
                            Param.ALPHA[index] * (Param.LS + Param.LD) / vt + Param.DISCOMFORT_D);
        }
        double cp = 0;
        for (int i = 0; i < driverAffordRate.length; i++) {
            cp += matchingRatePassenger[i] *
                    ((passengerAffordRate[index] / (passengerAffordRate[index] + driverAffordRate[i])) * Param.LAMBDA * (Param.LS + Param.LD) +
                            Param.ALPHA[index] * (Param.LS + Param.LD) / vt + Param.DISCOMFORT_P);
        }
//        double cdd = cd + (1 - matchingRateSumDriver) * cs;
//        double cpp = cp + (1 - matchingRateSumPassenger) * cb;
        return new double[]{cb, cs,
                cd + (1 - matchingRateSumDriver) * cs + Param.THETA_D,
                cp + (1 - matchingRateSumPassenger) * cb + Param.THETA_P};

    }
}
