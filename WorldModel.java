import processing.core.PImage;

import java.util.*;

/**
 * Represents the 2D World in which this simulation is running.
 * Keeps track of the size of the world, the background image for each
 * location in the world, and the entities that populate the world.
 */
public final class WorldModel extends getImage{
    public int numRows;
    public int numCols;
    public Background[][] background;
    public Entity[][] occupancy;
    public Set<Entity> entities;


    private static final int PROPERTY_ID = 1;
    private static final int PROPERTY_COL = 2;
    private static final int PROPERTY_ROW = 3;
    private static final int ENTITY_NUM_PROPERTIES = 4;

    private static final int STUMP_NUM_PROPERTIES = 0;
    private static final int SAPLING_HEALTH = 0;
    private static final int SAPLING_NUM_PROPERTIES = 1;

    private static final int OBSTACLE_ANIMATION_PERIOD = 0;
    private static final int OBSTACLE_NUM_PROPERTIES = 1;

    private static final int DUDE_ACTION_PERIOD = 0;
    private static final int DUDE_ANIMATION_PERIOD = 1;
    private static final int DUDE_LIMIT = 2;
    private static final int DUDE_NUM_PROPERTIES = 3;

    private static final int HOUSE_NUM_PROPERTIES = 0;

    private static final int FAIRY_ANIMATION_PERIOD = 0;
    private static final int FAIRY_ACTION_PERIOD = 1;
    private static final int FAIRY_NUM_PROPERTIES = 2;


    private static final int TREE_ANIMATION_PERIOD = 0;
    private static final int TREE_ACTION_PERIOD = 1;
    private static final int TREE_HEALTH = 2;
    private static final int TREE_NUM_PROPERTIES = 3;

    private static final int PROPERTY_KEY = 0;

    private static final String OBSTACLE_KEY = "obstacle";

    private static final String DUDE_KEY = "dude";


    private static final String HOUSE_KEY = "house";


    private static final String FAIRY_KEY = "fairy";

    private static final String CLOUD_KEY = "cloud";

    private static final int CLOUD_NUM_PROPERTIES = 2;
    private static final int CLOUD_ANIMATION_PERIOD = 0;
    private static final int CLOUD_ACTION_PERIOD = 1;


    public WorldModel() {

    }

    /**
     * Helper method for testing. Don't move or modify this method.
     */
    public List<String> log(){
        List<String> list = new ArrayList<>();
        for (Entity entity : entities) {
            String log = entity.log();
            if(log != null) list.add(log);
        }
        return list;
    }

    public Optional<Entity> getOccupant(Point pos) {
        if (isOccupied(pos)) {
            return Optional.of(getOccupancyCell(pos));
        } else {
            return Optional.empty();
        }
    }

    public Entity getOccupancyCell(Point pos) {
        return this.occupancy[pos.y][pos.x];
    }

    private void setOccupancyCell(Point pos, Entity entity) {
        this.occupancy[pos.y][pos.x] = entity;
    }

    public void load(Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        parseSaveFile(saveFile, imageStore, defaultBackground);
        if(this.background == null){
            this.background = new Background[this.numRows][this.numCols];
            for (Background[] row : this.background)
                Arrays.fill(row, defaultBackground);
        }
        if(this.occupancy == null){
            this.occupancy = new Entity[this.numRows][this.numCols];
            this.entities = new HashSet<>();
        }
    }

    private Background getBackgroundCell(Point pos) {
        return this.background[pos.y][pos.x];
    }

    public void setBackgroundCell(Point pos, Background background) {
        this.background[pos.y][pos.x] = background;
    }

    private boolean withinBounds(Point pos) {
        return pos.y >= 0 && pos.y < this.numRows && pos.x >= 0 && pos.x < this.numCols;
    }

    public boolean isOccupied(Point pos) {
        return withinBounds(pos) && this.getOccupancyCell(pos) != null;
    }

    public void tryAddEntity(Entity entity) {
        if (this.isOccupied(entity.getPosition())) {
            // arguably the wrong type of exception, but we are not
            // defining our own exceptions yet
            throw new IllegalArgumentException("position occupied");
        }
        addEntity(entity);
    }

    public void addEntity(Entity entity) {
        if (this.withinBounds(entity.getPosition())) {
            this.setOccupancyCell(entity.getPosition(), entity);
            this.entities.add(entity);
        }
    }

    public void moveEntity(EventScheduler scheduler, Entity entity, Point pos) {
        Point oldPos = entity.getPosition();
        if (this.withinBounds(pos) && !pos.equals(oldPos)) {
            this.setOccupancyCell(oldPos, null);
            Optional<Entity> occupant = this.getOccupant(pos);
            occupant.ifPresent(target -> removeEntity(scheduler, target));
            this.setOccupancyCell(pos, entity);
            entity.setPosition(pos);
        }
    }

    public void removeEntity(EventScheduler scheduler, Entity entity) {
        scheduler.unscheduleAllEvents(entity);
        removeEntityAt(entity.getPosition());
    }

    private void removeEntityAt(Point pos) {
        if (this.withinBounds(pos) && this.getOccupancyCell(pos) != null) {
            Entity entity = this.getOccupancyCell(pos);

            /* This moves the entity just outside of the grid for
             * debugging purposes. */
            entity.setPosition(new Point(-1, -1));
            this.entities.remove(entity);
            this.setOccupancyCell(pos, null);
        }
    }
    public Optional<PImage> getBackgroundImage(Point pos) {
        if (this.withinBounds(pos)) {
            return Optional.of(getCurrentImage(this.getBackgroundCell(pos)));
        } else {
            return Optional.empty();
        }
    }

    private void parseSapling(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == SAPLING_NUM_PROPERTIES) {
            int health = Integer.parseInt(properties[SAPLING_HEALTH]);
            Sapling sapling = new Sapling(id, pt, ImageStore.getImageList(imageStore, Sapling.SAPLING_KEY), health, Sapling.SAPLING_HEALTH_LIMIT);
            this.tryAddEntity(sapling);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                    Sapling.SAPLING_KEY, SAPLING_NUM_PROPERTIES));
        }
    }
// TODO should healthlimit be 0
    private void parseDude(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == DUDE_NUM_PROPERTIES) {
            Dude dudeNotFull = new DudeNotFull(id, pt, ImageStore.getImageList(imageStore, DUDE_KEY),
                    Double.parseDouble(properties[DUDE_ANIMATION_PERIOD]),
                    Double.parseDouble(properties[DUDE_ACTION_PERIOD]),
                    Integer.parseInt(properties[DUDE_LIMIT]));
            this.tryAddEntity(dudeNotFull);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing", DUDE_KEY,
                    DUDE_NUM_PROPERTIES));
        }
    }

    private void parseFairy(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == FAIRY_NUM_PROPERTIES) {
            Fairy fairy = new Fairy(id, pt, ImageStore.getImageList(imageStore, FAIRY_KEY),
                    Double.parseDouble(properties[FAIRY_ANIMATION_PERIOD]),
                    Double.parseDouble(properties[FAIRY_ACTION_PERIOD]));
            this.tryAddEntity(fairy);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                    FAIRY_KEY, FAIRY_NUM_PROPERTIES));
        }
    }

    private void parseTree(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == TREE_NUM_PROPERTIES) {
            Tree tree = new Tree(id, pt, ImageStore.getImageList(imageStore, Tree.TREE_KEY),
                    Integer.parseInt(properties[TREE_HEALTH]),
                    0,
                    Double.parseDouble(properties[TREE_ANIMATION_PERIOD]),
                    Double.parseDouble(properties[TREE_ACTION_PERIOD])
                    );
            this.tryAddEntity(tree);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                    Tree.TREE_KEY, TREE_NUM_PROPERTIES));
        }

    }

    private void parseObstacle(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == OBSTACLE_NUM_PROPERTIES) {
            Obstacle obstacle = new Obstacle(id, pt,
                    ImageStore.getImageList(imageStore, OBSTACLE_KEY),
                    Double.parseDouble(properties[OBSTACLE_ANIMATION_PERIOD]));
            this.tryAddEntity(obstacle);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                    OBSTACLE_KEY, OBSTACLE_NUM_PROPERTIES));
        }
    }

    private void parseHouse(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == HOUSE_NUM_PROPERTIES) {
            House house = new House(id, pt, ImageStore.getImageList(imageStore, HOUSE_KEY));
            this.tryAddEntity(house);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                    HOUSE_KEY, HOUSE_NUM_PROPERTIES));
        }
    }
    private void parseStump(String[] properties, Point pt, String id, ImageStore imageStore) {
        if (properties.length == STUMP_NUM_PROPERTIES) {
            Stump stump = new Stump(id, pt, ImageStore.getImageList(imageStore, Stump.STUMP_KEY));
            tryAddEntity(stump);
        }else{
            throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                    Stump.STUMP_KEY, STUMP_NUM_PROPERTIES));
        }
    }

    public void parseCloud(String[] properties, Point pt, String id, ImageStore imageStore){
        if (properties.length == CLOUD_NUM_PROPERTIES) {
                Cloud cloud = new Cloud(id, pt, ImageStore.getImageList(imageStore, CLOUD_KEY),
                        Double.parseDouble(properties[CLOUD_ANIMATION_PERIOD]),
                        Double.parseDouble(properties[CLOUD_ACTION_PERIOD]));
                this.tryAddEntity(cloud);
            }else{
                throw new IllegalArgumentException(String.format("%s requires %d properties when parsing",
                        CLOUD_KEY, CLOUD_NUM_PROPERTIES));
            }
        }


    private void parseSaveFile(Scanner saveFile, ImageStore imageStore, Background defaultBackground){
        String lastHeader = "";
        int headerLine = 0;
        int lineCounter = 0;
        while(saveFile.hasNextLine()){
            lineCounter++;
            String line = saveFile.nextLine().strip();
            if(line.endsWith(":")){
                headerLine = lineCounter;
                lastHeader = line;
                switch (line){
                    case "Backgrounds:" -> this.background = new Background[this.numRows][this.numCols];
                    case "Entities:" -> {
                        this.occupancy = new Entity[this.numRows][this.numCols];
                        this.entities = new HashSet<>();
                    }
                }
            }else{
                switch (lastHeader){
                    case "Rows:" -> this.numRows = Integer.parseInt(line);
                    case "Cols:" -> this.numCols = Integer.parseInt(line);
                    case "Backgrounds:" -> parseBackgroundRow(line, lineCounter-headerLine-1, imageStore);
                    case "Entities:" -> parseEntity(line, imageStore);
                }
            }
        }
    }
    private void parseBackgroundRow(String line, int row, ImageStore imageStore) {
        String[] cells = line.split(" ");
        if(row < this.numRows){
            int rows = Math.min(cells.length, this.numCols);
            for (int col = 0; col < rows; col++){
                this.background[row][col] = new Background(cells[col], ImageStore.getImageList(imageStore, cells[col]));
            }
        }
    }

    private void parseEntity(String line, ImageStore imageStore) {
        String[] properties = line.split(" ", ENTITY_NUM_PROPERTIES + 1);
        if (properties.length >= ENTITY_NUM_PROPERTIES) {
            String key = properties[PROPERTY_KEY];
            String id = properties[PROPERTY_ID];
            System.out.println(id);
            Point pt = new Point(Integer.parseInt(properties[PROPERTY_COL]),
                    Integer.parseInt(properties[PROPERTY_ROW]));

            properties = properties.length == ENTITY_NUM_PROPERTIES ?
                    new String[0] : properties[ENTITY_NUM_PROPERTIES].split(" ");

            switch (key) {
                case OBSTACLE_KEY -> parseObstacle(properties, pt, id, imageStore);
                case DUDE_KEY -> parseDude(properties, pt, id, imageStore);
                case FAIRY_KEY -> parseFairy(properties, pt, id, imageStore);
                case HOUSE_KEY -> parseHouse(properties, pt, id, imageStore);
                case Tree.TREE_KEY -> parseTree(properties, pt, id, imageStore);
                case Sapling.SAPLING_KEY -> parseSapling(properties, pt, id, imageStore);
                case Stump.STUMP_KEY -> parseStump(properties, pt, id, imageStore);
                //Entity entity = Stump.create(id, pt, ImageStore.getImageList(imageStore, Functions.STUMP_KEY));
                default -> throw new IllegalArgumentException("Entity key is unknown");
            }
            //tryAddEntity(entity);
        }else{
            throw new IllegalArgumentException("Entity must be formatted as [key] [id] [x] [y] ...");
        }
    }


}
