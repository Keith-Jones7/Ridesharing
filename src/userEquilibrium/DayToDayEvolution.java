package userEquilibrium;

public class DayToDayEvolution {
    static int maxDay = 100;
    InDayEvolution[] inDayEvolutions;

    public DayToDayEvolution(int maxDay) {
        DayToDayEvolution.maxDay = maxDay;
        inDayEvolutions = new InDayEvolution[maxDay];
        for(int i = 0; i < maxDay; i++) {
            inDayEvolutions[i] = new InDayEvolution(InDayEvolution.maxTime);
        }
    }
    public static void main(String[] args) {
        DayToDayEvolution test = new DayToDayEvolution(DayToDayEvolution.maxDay);
        System.out.println("第" + 0 +"天");
        test.inDayEvolutions[0].evolute(null);
        for(int i = 1; i < maxDay; i++) {
            System.out.println("第" + i +"天");
            test.inDayEvolutions[i].evolute(test.inDayEvolutions[i - 1]);
        }
    }

}
