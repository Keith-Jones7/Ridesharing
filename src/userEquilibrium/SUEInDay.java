package userEquilibrium;


public class SUEInDay extends SUE {

    int time;//当前时间
    double vt;//当前速度

    SUEInDay pre_time;
    SUEInDay pre_day;
    /**
     *
     * @param time 当前时刻
     * @param W 出行方式类别
     * @param Nt 当前出行总人数
     * @param vt 当前道路平均速度
     * @param size_rate 各类出行者的比例
     * @param driver_afford_rate 司机的承担比例
     * @param passenger_afford_rate 乘客的承担比例
     * @param pre_time 前一时刻的均衡解
     */
    public SUEInDay(int time, int W, double Nt, double vt, double[] size_rate,
                    double[] driver_afford_rate, double[] passenger_afford_rate,
                    SUEInDay pre_time, SUEInDay pre_day) {

        super(W, Nt, size_rate, driver_afford_rate, passenger_afford_rate);
        this.time = time;
        this.vt = vt;
        this.pre_time = pre_time;
        this.pre_day = pre_day;
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < M; j++) {
                if(pre_day == null) {
                    Prob_solve[i][j] = 1.0 / M;
                }else {
                    Prob_solve[i][j] = pre_day.Prob_solve[i][j];
                }
            }
        }
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < M; j++) {
                NA[i][j] = size_rate[i] * Prob_solve[i][j] * Nt;
            }
        }
        updateCost();
        updateProb();
        System.out.println("期望比例");
        printMatrix(Prob_solve);
        updateProbSolve();
        updateNR();
        System.out.println("实际比例");
        printMatrix(Prob);
        System.out.println("出行成本");
        printMatrix(cost);
    }

    public void updateProbSolve() {
        for(int i = 0; i < W; i++) {
            if (M >= 0) System.arraycopy(Prob[i], 0, Prob_solve[i], 0, M);
        }
    }
    /**
     * 根据最新的实际比例更新期望成本
     */
    public void updateCost(){
        double[] driver_count = new double[W];
        double[] passenger_count = new double[W];
        for(int i = 0; i < W; i++) {
            driver_count[i] = NA[i][2];
            passenger_count[i] = NA[i][3];
        }
        Cost cal_cost = new Cost(vt);
        if(pre_day == null) {
            for(int i = 0; i < W; i++) {
                cost[i] = cal_cost.calCost(driver_afford_rate, passenger_afford_rate,
                        new double[W], new double[W], 0, 0,
                        this.NR, i);
            }
        }else {
            for(int i = 0; i < W; i++) {
                cost[i] = cal_cost.calCost(driver_afford_rate, passenger_afford_rate,
                        pre_day.matching.solution.matching_rate_driver[i], pre_day.matching.solution.matching_rate_passenger[i],
                        pre_day.matching.solution.matching_rate_sum_driver[i], pre_day.matching.solution.matching_rate_sum_passenger[i],
                        this.NR, i);
            }
        }
        if(matching == null) {
            matching = new MatchingProcess(W, W,
                    driver_afford_rate, passenger_afford_rate,
                    driver_count, passenger_count);
        }else {
            matching.setParam(driver_count, passenger_count);
        }
        try {
            matching.matching(1);
        }catch (Exception e) {
            System.out.println(e);
        }

      //  printMatrix(matching.solution.matching_rate_driver);

    }

    /**
     * 更新实际出行人数
     */
    public void updateNR() {
        if(pre_time == null) {
            for(int i = 0; i < W; i++) {
                NR[i][0] = NA[i][0];
                NR[i][1] = NA[i][1];
                NR[i][2] = matching.solution.matching_rate_sum_driver[i] * NA[i][2];
                NR[i][3] = matching.solution.matching_rate_sum_passenger[i] * NA[i][3];
            }
        }else {
            for(int i = 0; i < W; i++) {
                NR[i][0] = NA[i][0] + (pre_time.NA[i][3] - pre_time.NR[i][3]);
                NR[i][1] = NA[i][1] + (pre_time.NA[i][2] - pre_time.NR[i][2]);
                NR[i][2] = matching.solution.matching_rate_sum_driver[i] * NA[i][2];
                NR[i][3] = matching.solution.matching_rate_sum_passenger[i] * NA[i][3];
            }
        }
    }

}



