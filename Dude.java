import processing.core.PImage;

import java.util.List;
import java.util.Map;

public abstract class Dude extends ScheduleActions {

    private boolean full;

    protected int resourceLimit;

    protected Dude(String id, Point position, List<PImage> images, double animationPeriod, double actionPeriod, int resourceLimit){
        super(id, position, images, animationPeriod, actionPeriod);
        this.full = false;
        this.resourceLimit = resourceLimit;
        //TODO should resourceLimit be 0?
    }

//    protected void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
//        Activity activity = new Activity(this, world, imageStore);
//        Animation animation = new Animation(this, 0);
//        scheduler.scheduleEvent(this, activity, this.actionPeriod);
//        scheduler.scheduleEvent(this, animation, this.animationPeriod);
//
//    }

    protected Point nextPosition(WorldModel world, Point destPos) {
        int horiz = Integer.signum(destPos.x - this.position.x);
        Point newPos = new Point(this.position.x + horiz, this.position.y);

        if (horiz == 0 || world.isOccupied(newPos) && world.getOccupancyCell(newPos).getClass().getName() != "Stump") {
            int vert = Integer.signum(destPos.y - this.position.y);
            newPos = new Point(this.position.x, this.position.y + vert);

            if (vert == 0 || world.isOccupied(newPos) && world.getOccupancyCell(newPos).getClass().getName() != "Stump") {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public abstract void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler);


}
