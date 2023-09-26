public class Animation extends Action {
    private int repeatCount;

    public Animation(Entity entity, int repeatCount){
        super(entity);
        this.repeatCount = repeatCount;
    }

    protected void executeAction(EventScheduler scheduler){
        entity.nextImage();

        if (this.repeatCount != 1) {
            Animation animation = new Animation(entity, Math.max(this.repeatCount - 1, 0));
            scheduler.scheduleEvent(this.entity, animation, ((ScheduleActions)entity).animationPeriod);
        }
    }

}
