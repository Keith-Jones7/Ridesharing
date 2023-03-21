package userEquilibrium;

import java.util.Random;

public class InDayEvolution {
    static final double v0 = 50;                //道路车辆的零流速度
    static final double alpha = 0.15;           //BPR系数
    static final double Capacity = 300;         //道路车辆容量
    static int maxTime = 24;                    //最大时间
    SUEInDay[] sues;
    double[] Nt;
    double[] vt;

    int afford_w = 3;
    double[] _driver_afford_rate = {0.2, 0.4, 0.5};
    double[] _passenger_afford_rate = {0.6, 0.7, 0.9};
    double[] _afford_size_rate = {0.001, 0.399, 0.6};
    /**
     *
     * @param maxTime 最大时间
     */
    public InDayEvolution(int maxTime) {
        InDayEvolution.maxTime = maxTime;
        sues = new SUEInDay[maxTime];
        Nt = new double[maxTime];
        Random random = new Random(5);
        for(int i = 0; i < maxTime; i++) {
            Nt[i] = random.nextInt(300);
        }
        vt = new double[maxTime];
        vt[0] = v0;
    }

    /**
     *
     * @param Nv 当前道路车辆数
     * @return 当前速度
     */
    public static double calV(double Nv) {
        return v0 / (1 + alpha * Math.pow((Nv / Capacity), 4));
    }


    public void evolute(InDayEvolution pre_day) {
        if(pre_day == null) {
            this.sues[0] = new SUEInDay(0, afford_w, this.Nt[0], this.vt[0],
                    _afford_size_rate, _driver_afford_rate, _passenger_afford_rate, null, null);
            for(int i = 1; i < maxTime; i++) {
                System.out.println("第" + i + "时刻");
                this.vt[i] = calV(this.sues[i - 1].Nt);
                this.sues[i] = new SUEInDay(i, afford_w, this.Nt[1], this.vt[1],
                        _afford_size_rate, _driver_afford_rate, _passenger_afford_rate, this.sues[i - 1], null);
            }
        }else {
            this.sues[0] = new SUEInDay(0, afford_w, this.Nt[0], this.vt[0],
                    _afford_size_rate, _driver_afford_rate, _passenger_afford_rate, null, pre_day.sues[0]);
            for(int i = 1; i < maxTime; i++) {
                System.out.println("第" + i + "时刻");
                this.vt[i] = calV(this.sues[i - 1].Nt);
                this.sues[i] = new SUEInDay(i, afford_w, this.Nt[1], this.vt[1],
                        _afford_size_rate, _driver_afford_rate, _passenger_afford_rate, this.sues[i - 1], pre_day.sues[i]);
            }
        }
    }
    public static void main(String[] args) {
        InDayEvolution test = new InDayEvolution(24);

        int afford_w = 3;
        double[] _driver_afford_rate = {0.2, 0.4, 0.5};
        double[] _passenger_afford_rate = {0.6, 0.7, 0.9};
        double[] _afford_size_rate = {0.2, 0.2, 0.6};




    }

}
