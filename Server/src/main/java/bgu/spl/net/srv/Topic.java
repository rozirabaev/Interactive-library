package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Topic {
    private ConcurrentLinkedQueue<Integer> handlers;

    public Topic(){
        this.handlers = new ConcurrentLinkedQueue<>();
    }
    public ConcurrentLinkedQueue<Integer> gethandlerId() {
        if(!handlers.isEmpty())
        return handlers;
        else
            return null;
    }

   /*// public void sethandlers(ConcurrentLinkedQueue<ConnectionHandler> handlers) {
        this.handlers = handlers;
    }*/

    public void removeSubscriber (int h){
        handlers.remove(h);
    }

    public void addHandler (int h){
        handlers.add(h);
    }
}
