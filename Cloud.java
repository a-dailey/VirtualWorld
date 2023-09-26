import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cloud extends ScheduleActions implements Move{

    public Cloud(String id, Point position, List<PImage> images, double animationPeriod, double actionPeriod){
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
        Optional<Entity> cloudTarget = findNearest(world, this.position, new ArrayList<>(List.of("DudeNotFull", "DudeFull")));

        if (cloudTarget.isPresent()) {
            Entity regDude = cloudTarget.get();
            Point tgtPos = cloudTarget.get().position;

            if (moveTo(world, cloudTarget.get(), scheduler)) {
                DudeNotFull dude1 = new DudeNotFull(cloudTarget.get().id, tgtPos, ImageStore.getImageList(imageStore, "slowdude"),
                        1.2, 1.4, 2);
                DudeFull dude2 = new DudeFull(cloudTarget.get().id, tgtPos, ImageStore.getImageList(imageStore, "slowdude"),
                        1.2, 1.4, 3);
                world.removeEntity(scheduler, this);
                world.removeEntity(scheduler, regDude);
                if (regDude.getClass().getName() == "DudeFull"){
                    world.addEntity(dude2);
                    dude2.scheduleActions(scheduler, world, imageStore);
                }
                else {
                    world.addEntity(dude1);
                    dude1.scheduleActions(scheduler, world, imageStore);
                }

//TODO add effect on dude

            }
        }
        Activity activity = new Activity(this, world, imageStore);
        scheduler.scheduleEvent(this, activity, this.actionPeriod);
    }

}
