package cs451.links;

import cs451.Observer;
import cs451.Host;
import cs451.Message;
// import cs451.Constants;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;

    public StubbornLink(int port, Observer observer) {
        this.fl = new FairLossLink(port, this);
        this.observer = observer;
    }

    public void start() {
        fl.start();
    }

    public void send(Message message, Host host) {
        // TODO: maybe add retry logic here
        // int retry = 0;
        // for (; retry < 3; retry++) {
        // fl.send(message, host);
        // }
        fl.send(message, host);
    }

    public void stop() {
        fl.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
