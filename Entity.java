import java.util.*;
import processing.core.PImage;

/**
 * An entity that exists in the world. See EntityKind for the
 * different kinds of entities that exist.
 */
public abstract class Entity {
    //private EntityKind kind;
    protected String id;
    protected Point position;
    protected List<PImage> images;
    private int imageIndex;
    Random rand = new Random();

    protected Entity(String id, Point position, List<PImage> images){
        this.id = id;
        this.position = position;
        this.images = images;
        this.imageIndex = 0;
    }

    protected String getId(){
        return this.id;
    }
    protected Point getPosition(){
        return this.position;
    }
    protected void setPosition(Point point){
        this.position = point;
    }
    protected List<PImage> getImages(){
        return this.images;
    }
    protected int getImageIndex(){
        return this.imageIndex;
    }

//    public int getHealth(){
//        return this.health;
//    }

    /**
     * Helper method for testing. Preserve this functionality while refactoring.
     */
    protected String log(){
        return this.id.isEmpty() ? null :
                String.format("%s %d %d %d", this.id, this.position.x, this.position.y, this.imageIndex);
    }



    protected void nextImage() {
        this.imageIndex = this.imageIndex + 1;
    }


    protected int getIntFromRange(int max, int min) {
        return min + rand.nextInt(max-min);
    }
    protected double getNumFromRange(double max, double min) {
        return min + rand.nextDouble() * (max - min);
    }




}
