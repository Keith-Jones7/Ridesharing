package userEquilibrium;

public class SUE {
    static double precision = 1e-4;
    static int maxCount = 3000;
    static double theta = 0.1;
    static int M = 4;//出行方式类别 0:bus 1:solo_drive 2:rs_driver 3: rs_passenger
    static double vt = 40;

    double Nt;

    int W;      //出行者类别
    double[] driver_afford_rate;
    double[] passenger_afford_rate;
    double[] size_rate;

    double[][] cost;
    double[][] NA;
    double[][] NR;
    double[][] Prob;
    double[][] Prob_solve;
    double N0; // 道路车辆总数
    double N_matching_sum; // 总匹配成功数
    double sum_cost; // 总出行成本
    double[][][] delta_C; // 成本关于人数的雅克比矩阵
    MatchingProcess matching;

    public SUE(int W, double Nt, double[] size_rate,
               double[] driver_afford_rate, double[] passenger_afford_rate) {
        this.Nt = Nt;
        this.W = W;
        this.size_rate = size_rate;
        this.driver_afford_rate = driver_afford_rate;
        this.passenger_afford_rate = passenger_afford_rate;

        cost = new double[W][M];
        NA = new double[W][M];
        NR = new double[W][M];
        Prob = new double[W][M];
        Prob_solve = new double[W][M];
        delta_C = new double[W][4][4];
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < M; j++) {
                Prob_solve[i][j] = 0;
            }
        }
        for(int i = 0; i < W; i++) {
            for (int j = 0; j < M; j++) {
                NA[i][j] = size_rate[i] * Prob_solve[i][j] * Nt;
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
        while (Norm(Prob, Prob_solve) > precision && count < maxCount) {
            for(int i = 0; i < W; i++) {
                for(int j = 0; j < M; j++) {
                    Prob_solve[i][j] += ((Prob[i][j] - Prob_solve[i][j]) / count);
                    NA[i][j] = size_rate[i] * Prob_solve[i][j] * Nt;
                }
            }
            updateCost();
            updateProb();
            count++;
            System.out.println("期望比例");
            printMatrix(Prob_solve);
            System.out.println("实际比例");
            printMatrix(Prob);
            System.out.println("第"+ count + "次迭代");
        }
    }

    /**
     * 根据最新的成本公式更新期望比例
     */
    public void updateProb() {
        double[] cost_sum = new double[W];
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < M; j++) {
                cost_sum[i] += Math.exp(- SUE.theta * cost[i][j]);
            }
        }
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < M; j++) {
                Prob[i][j] = Math.exp(- SUE.theta * cost[i][j]) / cost_sum[i];
            }
        }
    }

    /**
     * 根据最新的实际比例更新期望成本
     */
    public void updateCost(){
        double[] driver_count = new double[W];
        double[] passenger_count = new double[W];
        sum_cost = 0;
        for(int i = 0; i < W; i++) {
            driver_count[i] = NA[i][2];
            passenger_count[i] = NA[i][3];
        }
        Cost cal_cost = new Cost(vt);
        if(matching == null) {
            matching = new MatchingProcess(W, W,
                    driver_afford_rate, passenger_afford_rate,
                    driver_count, passenger_count);
        }else {
            matching.setParam(driver_count, passenger_count);
        }
        matching.matching();
        for(int i = 0; i < W; i++) {
            cost[i] = cal_cost.calCost(driver_afford_rate, passenger_afford_rate,
                    matching.solution.matching_rate_driver[i], matching.solution.matching_rate_passenger[i],
                    matching.solution.matching_rate_sum_driver[i], matching.solution.matching_rate_sum_passenger[i],
                    this.NR, i);
        }
        for (int i = 0; i < cost.length; i++) {
            for (int j = 0; j < cost[0].length; j++) {
                sum_cost += (Nt * Prob_solve[i][j] * cost[i][j]);
            }
        }
        //  printMatrix(matching.solution.matching_rate_driver);

    }

    /**
     * 更新实际出行人数
     */
    public void updateNR() {
        for(int i = 0; i < W; i++) {
            NR[i][2] = matching.solution.matching_rate_sum_driver[i] * NA[i][2];
            NR[i][3] = matching.solution.matching_rate_sum_passenger[i] * NA[i][3];
            NR[i][0] = NA[i][0] + NA[i][3] - NR[i][3];
            NR[i][1] = NA[i][1] + NA[i][2] - NR[i][2];
        }

    }

    /**
     *
     * 求道路车辆总数
     */
    public void calNv() {
        this.N0 = 0;
        this.N_matching_sum = 0;
        for (int i = 0; i < W; i++) {
            N0 += (NR[i][1] + NR[i][2]);
            N_matching_sum += NR[i][2];
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
    static final double ALPHA = 30;         //行驶过程的VoT
    static final double LB = 36;            //公共交通里程
    static final double VB = 30;            //公共交通平均速度
    static final double FREQ = 10;          //公共交通发车频率
    static final double PB = 10;            //公共交通票价
    static final double GAMMA = 5;        //公交车拥挤成本系数
    static final double ROU = 0.35;          //公交车拥挤惩罚系数
    static final double B = 30;             //公交车最大容量

    static final double LS = 36;            //独驾里程
    static final double LD = 2;             //平均绕行距离
    static final double VS = 50;            //小汽车零流速度
    static final double LAMBDA = 0.6;       //每公里燃料费
    static final double CF = 0.1;           //每公里车辆折旧成本

    static final double ALPHA1 = 0.15;      //BPR系数1
    static final double ALPHA2 = 4;         //BPR系数2
    static final double Capacity = 100;     //道路车辆容量
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
     * @param driver_afford_rate    司机的承担比例
     * @param passenger_afford_rate 乘客的承担比例
     * @param matching_rate_driver  司机的预期匹配率
     * @param matching_rate_passenger   乘客的预期匹配率
     * @param matching_rate_sum_driver 司机的累计匹配率
     * @param matching_rate_sum_passenger 乘客的累计匹配率
     * @param index 出行者的类别
     * @return 返回同一类出行者选择该种出行方式的成本
     */
    public double[] calCost(double[] driver_afford_rate, double[] passenger_afford_rate,
                            double[] matching_rate_driver, double[] matching_rate_passenger,
                            double matching_rate_sum_driver, double matching_rate_sum_passenger,
                            double[][] NR, int index) {
        double nb = 0, nc = 0;
        for (double[] num : NR) {
            nb += num[0];
            nc += num[1];
            nc += num[2];
        }

        vt = VS / (1 + ALPHA1 * Math.pow(nc / Capacity, ALPHA2));
        double cb = ALPHA * (LB / VB + 1 / (2 * FREQ)) + PB + GAMMA * (LB / VB) * (1 + ROU * nb / B);
        double cs = ALPHA * (LS / vt)  + LAMBDA * LS + CF * LS;
        double cd = 0;
        for(int j = 0; j < passenger_afford_rate.length; j++) {
            cd += matching_rate_driver[j] *
                    ((driver_afford_rate[index] / (driver_afford_rate[index] + passenger_afford_rate[j])) * LAMBDA * (LS + LD) +
                            ALPHA * (LS + LD) / vt + DISCOMFORT_D);
        }
        double cp = 0;
        for(int i = 0; i < driver_afford_rate.length; i++) {
            cp += matching_rate_passenger[i] *
                    ((passenger_afford_rate[index] / (passenger_afford_rate[index] + driver_afford_rate[i])) * LAMBDA * (LS + LD) +
                            ALPHA * (LS + LD) / vt + DISCOMFORT_P);
        }
        return new double[]{cb, cs,
                cd + (1 - matching_rate_sum_driver) * cs + THETA_D,
                cp + (1 - matching_rate_sum_passenger) * cb + THETA_P};

    }
}
