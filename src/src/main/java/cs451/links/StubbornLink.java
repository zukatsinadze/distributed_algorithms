package cs451.links;

import cs451.Observer;

import java.net.DatagramSocket;

import cs451.Host;
import cs451.Message;
// import cs451.Constants;

public class StubbornLink implements Observer {
    private final FairLossLink fl;
    private final Observer observer;

    public StubbornLink(Observer observer, DatagramSocket socket) {
        this.fl = new FairLossLink(this, socket);
        this.observer = observer;
    }

    public void start() {
        fl.start();
    }

    public void send(Message message, Host host) {
        // TODO: maybe add retry logic here
        int retry = 0;
        for (; retry < 5; retry++) {
            fl.send(message, host);
        }
        // fl.send(message, host);
    }

    public static void stop() {
        FairLossLink.stop();
    }

    @Override
    public void deliver(Message message) {
        observer.deliver(message);
    }
}
