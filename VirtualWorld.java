import java.io.File;
import java.io.FileNotFoundException;
import java.security.PrivateKey;
import java.util.*;

import processing.core.*;

public final class VirtualWorld extends PApplet {
    private static String[] ARGS;

    private static final int VIEW_WIDTH = 640;
    private static final int VIEW_HEIGHT = 480;
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 32;

    private static final int VIEW_COLS = VIEW_WIDTH / TILE_WIDTH;
    private static final int VIEW_ROWS = VIEW_HEIGHT / TILE_HEIGHT;

    private static final String IMAGE_LIST_FILE_NAME = "imagelist";
    private static final String DEFAULT_IMAGE_NAME = "background_default";
    private static final int DEFAULT_IMAGE_COLOR = 0x808080;

    private static final String FAST_FLAG = "-fast";
    private static final String FASTER_FLAG = "-faster";
    private static final String FASTEST_FLAG = "-fastest";
    private static final double FAST_SCALE = 0.5;
    private static final double FASTER_SCALE = 0.25;
    private static final double FASTEST_SCALE = 0.10;

    private String loadFile = "world.sav";
    private long startTimeMillis = 0;
    private double timeScale = 1.0;

    private final int ICE_TREE_HEALTH_LIMIT = 5;
    private final int ICE_TREE_ANIMATION_PERIOD = 1;

    private final double CLOUD_ANIMATION_PERIOD = .3;
    private final double CLOUD_ACTION_PERIOD = 1;

    private final String CLOUD_KEY = "cloud";

    private ImageStore imageStore;
    private WorldModel world;
    private WorldView view;
    private EventScheduler scheduler;

    public void settings() {
        size(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /*
       Processing entry point for "sketch" setup.
    */
    public void setup() {
        parseCommandLine(ARGS);
        loadImages(IMAGE_LIST_FILE_NAME);
        loadWorld(loadFile, this.imageStore);

        this.view = new WorldView(VIEW_ROWS, VIEW_COLS, this, world, TILE_WIDTH, TILE_HEIGHT);
        this.scheduler = new EventScheduler();
        this.startTimeMillis = System.currentTimeMillis();
        this.scheduleActions(world, scheduler, imageStore);
    }

    public void draw() {
        double appTime = (System.currentTimeMillis() - startTimeMillis) * 0.001;
        double frameTime = (appTime - scheduler.getCurrentTime())/timeScale;
        this.update(frameTime);
        view.drawViewport();
    }

    private void update(double frameTime){
        scheduler.updateOnTime(frameTime);
    }

    private List<Point> getAffectedPoints(Point pressed){
        List<Point> affectedPoints = new ArrayList<>();
        List<Integer> pointChanges = List.of(1, -1);
        for (int i : pointChanges) {
            Point newX = new Point(pressed.x + i, pressed.y);
            Point newy = new Point(pressed.x, pressed.y + i);
            if (newX.x >= 0) {
                affectedPoints.add(newX);
            }
            if (newy.y >= 0) {
                affectedPoints.add(newy);
            }
        }
        affectedPoints.add(pressed);
        return affectedPoints;
    }
    private void addIceTree(Entity occupant, Point p){
        world.removeEntity(scheduler, occupant);
        Tree iceTree = new Tree("", p, ImageStore.getImageList(imageStore, "icetree"),
                ICE_TREE_HEALTH_LIMIT, ICE_TREE_HEALTH_LIMIT, ICE_TREE_ANIMATION_PERIOD, ICE_TREE_ANIMATION_PERIOD);
        world.addEntity(iceTree);
        iceTree.scheduleActions(scheduler, world, imageStore);
    }
    private void addIceFairy(Entity occupant, Point p){
        world.removeEntity(scheduler, occupant);
        Fairy iceFairy = new Fairy("", p, ImageStore.getImageList(imageStore, "icefairy"), 0.1, 1);
        world.addEntity(iceFairy);
        iceFairy.scheduleActions(scheduler, world, imageStore);
    }

    // Just for debugging and for P5
    // Be sure to refactor this method as appropriate
    public void mousePressed() {
        Point pressed = mouseToPoint();
        System.out.println("CLICK! " + pressed.x + ", " + pressed.y);
        Optional<Entity> entityOptional = world.getOccupant(pressed);
        Cloud cloud = new Cloud("", pressed, ImageStore.getImageList(imageStore, CLOUD_KEY), CLOUD_ANIMATION_PERIOD, CLOUD_ACTION_PERIOD);
        Background ice = new Background("", ImageStore.getImageList(imageStore, "ice"));
        List<Point> affectedPoints = getAffectedPoints(pressed);

        for (Point p : affectedPoints) {
            //set background to ice
            world.setBackgroundCell(p, ice);
            Entity occupant = world.getOccupancyCell(p);
            //add iced version of entity if present
            if (occupant != null) {
                if (occupant.getClass().getName() == "Tree") {
                    addIceTree(occupant, p);
                }
                if (occupant.getClass().getName() == "Fairy") {
                    addIceFairy(occupant, p);
                }
            }
        }
            try {
                world.tryAddEntity(cloud);
                cloud.scheduleActions(scheduler, world, imageStore);
            } catch (IllegalArgumentException e) {
                System.out.println("position occupied");
            }
            if (entityOptional.isPresent()) {
                Entity entity = entityOptional.get();
                int health = 0;
                if (entity instanceof Growable) {
                    health = ((Growable) entity).health;

                }
                System.out.println(entity.getId() + ": " + entity.getClass().getName() + " : " + health);
            }

        }


    private void scheduleActions(WorldModel world, EventScheduler scheduler, ImageStore imageStore) {
        for (Entity entity : world.entities) {
            if (entity instanceof ScheduleActions) {
                ((ScheduleActions) entity).scheduleActions(scheduler, world, imageStore);
            }
        }
    }
//        TODO make sure this is right

    private Point mouseToPoint() {
        return view.viewport.viewportToWorld(mouseX / TILE_WIDTH, mouseY / TILE_HEIGHT);
    }

    public void keyPressed() {
        if (key == CODED) {
            int dx = 0;
            int dy = 0;

            switch (keyCode) {
                case UP -> dy -= 1;
                case DOWN -> dy += 1;
                case LEFT -> dx -= 1;
                case RIGHT -> dx += 1;
            }
            view.shiftView(dx, dy);
        }
    }

    private static Background createDefaultBackground(ImageStore imageStore) {
        return new Background(DEFAULT_IMAGE_NAME, ImageStore.getImageList(imageStore, DEFAULT_IMAGE_NAME));
    }

    private static PImage createImageColored(int width, int height, int color) {
        PImage img = new PImage(width, height, RGB);
        img.loadPixels();
        Arrays.fill(img.pixels, color);
        img.updatePixels();
        return img;
    }

    private void loadImages(String filename) {
        this.imageStore = new ImageStore(createImageColored(TILE_WIDTH, TILE_HEIGHT, DEFAULT_IMAGE_COLOR));
        try {
            Scanner in = new Scanner(new File(filename));
            ImageStore.loadImages(in, imageStore,this);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    private void loadWorld(String file, ImageStore imageStore) {
        this.world = new WorldModel();
        try {
            Scanner in = new Scanner(new File(file));
            world.load(in, imageStore, createDefaultBackground(imageStore));
        } catch (FileNotFoundException e) {
            Scanner in = new Scanner(file);
            world.load(in, imageStore, createDefaultBackground(imageStore));
        }
    }

    private void parseCommandLine(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case FAST_FLAG -> timeScale = Math.min(FAST_SCALE, timeScale);
                case FASTER_FLAG -> timeScale = Math.min(FASTER_SCALE, timeScale);
                case FASTEST_FLAG -> timeScale = Math.min(FASTEST_SCALE, timeScale);
                default -> loadFile = arg;
            }
        }
    }

    public static void main(String[] args) {
        VirtualWorld.ARGS = args;
        PApplet.main(VirtualWorld.class);
    }

    public static List<String> headlessMain(String[] args, double lifetime){
        VirtualWorld.ARGS = args;

        VirtualWorld virtualWorld = new VirtualWorld();
        virtualWorld.setup();
        virtualWorld.update(lifetime);

        return virtualWorld.world.log();
    }
}
