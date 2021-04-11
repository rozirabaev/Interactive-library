package bgu.spl.net.srv;

import bgu.spl.net.api.*;
import bgu.spl.net.impl.stomp.STOMP_MessageEncoderDecoder;
import bgu.spl.net.impl.stomp.STOMP_MessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<String> {

    private final STOMP_MessagingProtocol protocol;
    private final STOMP_MessageEncoderDecoder encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private boolean isConnected;
    private Connections_Impl connections;
    private int id;

    public BlockingConnectionHandler(Socket sock, STOMP_MessageEncoderDecoder reader, STOMP_MessagingProtocol protocol, Connections_Impl connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol =  protocol;
        this.isConnected=false;
        this.connections=connections;
        this.id = sock.getPort();
        this.protocol.start(id,  connections);
    }

    @Override
    public void run() {
        isConnected=true;
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                String nextMessage =  encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process( nextMessage);

                }
            }
            disconnect();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(String msg) {
        try {
            out = new BufferedOutputStream(sock.getOutputStream());
            out.write(encdec.encode( msg));
            out.flush();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect(){
        isConnected=false;
    }


}
