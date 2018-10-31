package pydio.sdk.java.core.thread;

public class Background {
    private Runnable runnable;

    private Background(Runnable r) {
        this.runnable = r;
    }

    public Task execute() {

        Task tracker = new Task();
        Thread t = new Thread(()->{
            runnable.run();
            tracker.setDone();
        });
        tracker.setTask(t);

        t.start();
        return tracker;
    }

    public static Task go(Runnable r) {
        return new Background(r).execute();
    }
}
