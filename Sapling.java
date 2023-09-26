import processing.core.PImage;

import java.util.List;

public class Sapling extends Growable implements Transform {
    public static final String SAPLING_KEY = "sapling";

    protected static final int SAPLING_HEALTH_LIMIT = 5;

    private static final double SAPLING_ACTION_ANIMATION_PERIOD = 1.000;
    // have to be in sync since grows and gains health at same time
    private final double TREE_ANIMATION_MAX = 0.600;
    private final double TREE_ANIMATION_MIN = 0.050;

    private final double TREE_ACTION_MAX = 1.400;
    private final double TREE_ACTION_MIN = 1.000;
    private final int TREE_HEALTH_MAX = 3;
    private final int TREE_HEALTH_MIN = 1;


    public Sapling(String id, Point position, List<PImage> images, int health, int healthLimit){
        super(id, position, images, health, SAPLING_HEALTH_LIMIT, SAPLING_ACTION_ANIMATION_PERIOD, SAPLING_ACTION_ANIMATION_PERIOD);
        //TODO healthLimit
    }

//    protected void scheduleActions(EventScheduler scheduler, WorldModel world, ImageStore imageStore) {
//        Activity activity = new Activity(this, world, imageStore);
//        Animation animation = new Animation(this, 0);
//        scheduler.scheduleEvent(this, activity, this.actionPeriod);
//        scheduler.scheduleEvent(this, animation, this.animationPeriod);
//
//    }

    protected void executeActivity(WorldModel world, ImageStore imageStore, EventScheduler scheduler)
    {
        this.health++;
        if (!transform(world, scheduler, imageStore))
        {
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
        } else if (this.health >= this.healthLimit) {
            int newHealth = getIntFromRange(TREE_HEALTH_MAX, TREE_HEALTH_MIN);
            Tree tree = new Tree(Tree.TREE_KEY + "_" + this.id, this.position, ImageStore.getImageList(imageStore,
                    Tree.TREE_KEY),
                    newHealth,
                    0,
                    getNumFromRange(TREE_ANIMATION_MAX, TREE_ANIMATION_MIN),
                    getNumFromRange(TREE_ACTION_MAX, TREE_ACTION_MIN)
//                    TODO what should health/healthlimit be set to?

                    );

            world.removeEntity(scheduler, this);

            world.addEntity(tree);
            tree.scheduleActions(scheduler, world, imageStore);

            return true;
        }

        return false;
    }
}
