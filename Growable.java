import processing.core.PImage;

import java.util.List;

public abstract class Growable extends ScheduleActions {

    protected int health;
    protected int healthLimit;

    protected Growable(String id, Point position, List<PImage> images, int health, int healthLimit, double animationperiod, double actionPeriod){
        super(id, position, images, animationperiod, actionPeriod);
        this.health = health;
        this.healthLimit = healthLimit;
    }

}
