import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Fairy extends ScheduleActions implements Move {

    public Fairy(String id, Point position, List<PImage> images, double animationPeriod, double actionPeriod){
        super(id, position, images, animationPeriod, actionPeriod);
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

        if (horiz == 0 || world.isOccupied(newPos)) {
            int vert = Integer.signum(destPos.y - this.position.y);
            newPos = new Point(this.position.x, this.position.y + vert);

            if (vert == 0 || world.isOccupied(newPos)) {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public boolean moveTo(WorldModel world, Entity target, EventScheduler scheduler) {
        if (this.position.adjacent(target.position)) {
            world.removeEntity(scheduler, target);
            return true;
        } else {
            Point nextPos = nextPosition(world, target.position);

            if (!this.position.equals(nextPos)) {
                world.moveEntity(scheduler, this, nextPos);
            }
            return false;
        }
    }

    protected void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {
        Optional<Entity> fairyTarget = findNearest(world, this.position, new ArrayList<>(List.of("Stump")));

        if (fairyTarget.isPresent()) {
            Point tgtPos = fairyTarget.get().position;

            if (moveTo(world, fairyTarget.get(), scheduler)) {

                Sapling sapling = new Sapling(Sapling.SAPLING_KEY + "_" +
                        fairyTarget.get().id, tgtPos, ImageStore.getImageList(imageStore, Sapling.SAPLING_KEY), 0, Sapling.SAPLING_HEALTH_LIMIT);
                world.addEntity(sapling);
                sapling.scheduleActions(scheduler, world, imageStore);
            }
        }
        Activity activity = new Activity(this, world, imageStore);
        scheduler.scheduleEvent(this, activity, this.actionPeriod);
    }

}
