package userEquilibrium;

class matchSolution {
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
    double c = 100;

    double[][] matchingMatrix;
    double[] driverAffordRate;
    double[] passengerAffordRate;
    double[] driverCount;
    double[] passengerCount;
    matchSolution matchSolution;
    int driverClassCount;
    int passengerClassCount;

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
//                matchingMatrix[i][j] = (Math.pow(driverAffordRate[i], 0.5) * Math.pow(passengerAffordRate[i], 0.5));
//                if((driverAffordRate[i] + passengerAffordRate[j] >= 1)) {
//                    matchingMatrix[i][j] = 1;
//                }else {
//                    matchingMatrix[i][j] = 0;
//                }
                matchingMatrix[i][j] = 1;
            }
        }
        this.driverCount = driverCount;
        this.passengerCount = passengerCount;
        this.driverAffordRate = driverAffordRate;
        this.passengerAffordRate = passengerAffordRate;
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
     * 匹配率的解析解（加速求解）
     */
    public void matching() {
        matchSolution = new matchSolution();
        matchSolution.matchingRateDriver = new double[driverClassCount][passengerClassCount];
        matchSolution.matchingRatePassenger = new double[passengerClassCount][driverClassCount];
        matchSolution.matchingRateSumDriver = new double[driverClassCount];
        matchSolution.matchingRateSumPassenger = new double[passengerClassCount];
        matchSolution.matchingResultDriver = new double[driverClassCount][passengerClassCount];
        matchSolution.matchingResultPassenger = new double[passengerClassCount][driverClassCount];
        for(int i = 0; i < driverClassCount; i++) {
            for(int j = 0; j < passengerClassCount; j++) {
                matchSolution.matchingRateDriver[i][j] = Prob_i_to_j(driverCount, passengerCount, i, j, 0);
                matchSolution.matchingResultDriver[i][j] = driverCount[i] * matchSolution.matchingRateDriver[i][j];
                matchSolution.matchingRateSumDriver[i] += matchSolution.matchingRateDriver[i][j];
            }

        }
        for(int j = 0; j < passengerClassCount; j++) {
            for(int i = 0; i < driverClassCount; i++) {
                matchSolution.matchingRatePassenger[j][i] = Prob_i_to_j(driverCount, passengerCount, i, j, 1);
                matchSolution.matchingResultPassenger[j][i] = passengerCount[j] * matchSolution.matchingRatePassenger[j][i];
                matchSolution.matchingRateSumPassenger[j] += matchSolution.matchingRatePassenger[j][i];
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

//        if(flag == 0) {
//            up = A * matchingMatrix[i][j] * N_j_p[j];
//        }else {
//            up = A * matchingMatrix[i][j] * N_i_d[i];
//        }
        if(flag == 0) {
            up = A * (Math.pow(driverAffordRate[i], 0.5) + Math.pow(passengerAffordRate[i], 0.5)) * N_j_p[j];
        }else {
            up = A * (Math.pow(driverAffordRate[i], 0.5) + Math.pow(passengerAffordRate[i], 0.5)) * N_i_d[i];
        }
        for(int k = 0; k < driverClassCount; k++) {
            down += (a * matchingMatrix[k][j] * N_i_d[k]);
        }
        for(int k = 0; k < passengerClassCount; k++) {
            down += (b * matchingMatrix[i][k] * N_j_p[k]);
        }
        return up / down;
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
                System.out.print(matchSolution.matchingResultDriver[i][j] + "   \t");
            }
            System.out.println();
        }
        System.out.print("\n司机对应类别及其匹配率为:");
        printAffordRate(driverAffordRate);
        printPeopleCount(driverCount);
        printMatchingResult(matchSolution.matchingResultDriver);
        printMatchingRate(matchSolution.matchingRateSumDriver);
        System.out.println();

        System.out.print("\n乘客对应类别及其匹配率为:");
        printAffordRate(passengerAffordRate);
        printPeopleCount(passengerCount);
        printMatchingResult(matchSolution.matchingResultPassenger);
        printMatchingRate(matchSolution.matchingRateSumPassenger);
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
                sample.driverAffordRate, sample.passengerAffordRate,
                sample.driverCount, sample.passengerCount);
        test.matching();
        test.printSolution();
        System.out.println(test.matchSolution.matchingSum[0]);
        System.out.println();

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
