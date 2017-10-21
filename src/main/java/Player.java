import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Timothy Lampen on 2017-10-16.
 */
public class Player {

    private int money = 0;
    private int slot = 0;
    private Main.Route route;
    private int job = 0;
    private int rent = 0;

    public Player(){
        boolean ran = ThreadLocalRandom.current().nextBoolean();
        route = Main.Route.B;
    }

    public int getRent() {
        return rent;
    }

    public void setRent(int rent) {
        this.rent = rent;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public Main.Route getRoute() {
        return route;
    }

    public void setRoute(Main.Route route) {
        this.route = route;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}
