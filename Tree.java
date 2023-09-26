import processing.core.PImage;

import java.util.List;

public class Tree extends Growable implements Transform {
    public static final String TREE_KEY = "tree";

    protected Tree(String id, Point position, List<PImage> images, int health, int healthLimit, double animationPeriod, double actionPeriod){
        super(id, position, images, health, healthLimit, animationPeriod, actionPeriod);
        this.health = health;
        //TODO healthLimit
    }

//    protected void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
//        Activity activity = new Activity(this, world, imageStore);
//        Animation animation = new Animation(this, 0);
//        scheduler.scheduleEvent(this, activity, this.actionPeriod);
//        scheduler.scheduleEvent(this, animation, this.animationPeriod);
//
//    }

    protected void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler) {

        if (!transform(world, scheduler, imageStore)) {
            Activity activity = new Activity(this, world, imageStore);
            scheduler.scheduleEvent(this, activity, this.actionPeriod);
        }
    }

    public boolean transform(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        if (this.health <= 0) {
            Stump stump = new Stump(Stump.STUMP_KEY + "_" + this.id, this.position,
                    ImageStore.getImageList(imageStore, Stump.STUMP_KEY));

            world.removeEntity(scheduler, this);

            world.addEntity(stump);

            return true;
        }

        return false;
    }
}
