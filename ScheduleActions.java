import processing.core.PImage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class ScheduleActions extends Entity{
    protected double animationPeriod;
    protected double actionPeriod;

    protected ScheduleActions(String id, Point position, List<PImage> images, double animationPeriod, double actionPeriod){
        super(id, position, images);
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }

//    public double getAnimationPeriod()
//            //TODO trash
//    {
//        return this.animationPeriod;
//    }

    protected void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
        Activity activity = new Activity(this, world, imageStore);
        Animation animation = new Animation(this, 0);
        scheduler.scheduleEvent(this, activity, this.actionPeriod);
        scheduler.scheduleEvent(this, animation, this.animationPeriod);

    }

    protected Optional<Entity> findNearest(WorldModel world, Point pos, List<String> names) {
        List<Entity> ofType = new LinkedList<>();
        for (String name : names) {
            for (Entity entity : world.entities) {

                if (String.valueOf(entity.getClass().getName()).equals(name)) {
                    ofType.add(entity);
                }
            }
        }

        return nearestEntity(ofType, pos);
    }

    private Optional<Entity> nearestEntity(List<Entity> entities, Point pos) {
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = nearest.position.distanceSquared(pos);

            for (Entity other : entities) {
                int otherDistance = other.position.distanceSquared(pos);

                if (otherDistance < nearestDistance) {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return Optional.of(nearest);
        }
    }



    protected abstract void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler);

}
