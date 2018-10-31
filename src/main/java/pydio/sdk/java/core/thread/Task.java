package pydio.sdk.java.core.thread;

public class Task {
    private Thread thread;
    private boolean done;

    Task(Thread t) {
        this.thread = t;
    }

    Task(){

    }

    void setTask(Thread t){
        thread = t;
    }

    void setDone(){
        this.done = true;
    }

    public void cancel() {
        done = true;

        if (thread == null){
            return;
        }

        try {
            this.thread.interrupt();
        } catch (Exception ignored){}
    }

    public boolean taskDone(){
        return done;
    }
}
