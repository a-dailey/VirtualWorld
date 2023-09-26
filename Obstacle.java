import processing.core.PImage;

import java.util.List;

public class Obstacle extends ScheduleActions {

    public Obstacle(String id, Point position, List<PImage> images, double animationPeriod){
        super(id, position, images, animationPeriod, 0);
    }
    protected void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        Activity activity = new Activity(this, world, imageStore);
        Animation animation = new Animation(this, 0);
        scheduler.scheduleEvent(this, animation, this.animationPeriod);
    }

    protected void executeActivity(WorldModel worldModel, ImageStore imageStore, EventScheduler eventScheduler){}
}
