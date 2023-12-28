package userEquilibrium;


import userEquilibrium.common.Param;

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
            for(int j = 0; j < Param.M; j++) {
                if(pre_day == null) {
                    probSolve[i][j] = 1.0 / Param.M;
                }else {
                    probSolve[i][j] = pre_day.probSolve[i][j];
                }
            }
        }
        for(int i = 0; i < W; i++) {
            for(int j = 0; j < Param.M; j++) {
                na[i][j] = size_rate[i] * probSolve[i][j] * Nt;
            }
        }
        updateCost();
        updateProb();
        System.out.println("期望比例");
        printMatrix(probSolve);
        updateProbSolve();
        updateNR();
        System.out.println("实际比例");
        printMatrix(prob);
        System.out.println("出行成本");
        printMatrix(cost);
    }

    public void updateProbSolve() {
        for(int i = 0; i < W; i++) {
            if (Param.M >= 0) System.arraycopy(prob[i], 0, probSolve[i], 0, Param.M);
        }
    }
    /**
     * 根据最新的实际比例更新期望成本
     */
    public void updateCost(){
        double[] driver_count = new double[W];
        double[] passenger_count = new double[W];
        for(int i = 0; i < W; i++) {
            driver_count[i] = na[i][2];
            passenger_count[i] = na[i][3];
        }
        Cost cal_cost = new Cost(vt);
        if(pre_day == null) {
            for(int i = 0; i < W; i++) {
                cost[i] = cal_cost.calCost(driverAffordRate, passengerAffordRate,
                        new double[W], new double[W], 0, 0,
                        this.nr, i);
            }
        }else {
            for(int i = 0; i < W; i++) {
                cost[i] = cal_cost.calCost(driverAffordRate, passengerAffordRate,
                        pre_day.matching.solution.matchingRateDriver[i], pre_day.matching.solution.matchingRatePassenger[i],
                        pre_day.matching.solution.matchingRateSumDriver[i], pre_day.matching.solution.matchingRateSumPassenger[i],
                        this.nr, i);
            }
        }
        if(matching == null) {
            matching = new MatchingProcess(W, W,
                    driverAffordRate, passengerAffordRate,
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
                nr[i][0] = na[i][0];
                nr[i][1] = na[i][1];
                nr[i][2] = matching.solution.matchingRateSumDriver[i] * na[i][2];
                nr[i][3] = matching.solution.matchingRateSumPassenger[i] * na[i][3];
            }
        }else {
            for(int i = 0; i < W; i++) {
                nr[i][0] = na[i][0] + (pre_time.na[i][3] - pre_time.nr[i][3]);
                nr[i][1] = na[i][1] + (pre_time.na[i][2] - pre_time.nr[i][2]);
                nr[i][2] = matching.solution.matchingRateSumDriver[i] * na[i][2];
                nr[i][3] = matching.solution.matchingRateSumPassenger[i] * na[i][3];
            }
        }
    }

}



