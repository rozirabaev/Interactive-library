package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.stomp.STOMP_MessageEncoderDecoder;
import bgu.spl.net.impl.stomp.STOMP_MessagingProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

//import java.net.ServerSocket;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<STOMP_MessagingProtocol> protocolFactory;
    private final Supplier<STOMP_MessageEncoderDecoder> encdecFactory;
    private ServerSocket sock;
    private ConcurrentHashMap<String,Topic> topics;
    private Connections_Impl connections;


    public BaseServer(
            int port,
            Supplier<STOMP_MessagingProtocol > protocolFactory,
            Supplier<STOMP_MessageEncoderDecoder> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
		this.topics=new ConcurrentHashMap<>();
		this.connections=  new Connections_Impl();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                ConnectionHandler<String> handler;
                if(connections.getHandler(clientSock.getPort())!=null)
                    handler=connections.getHandler(clientSock.getPort());

                else {
                    handler = (ConnectionHandler<String>) new BlockingConnectionHandler(
                            clientSock,
                            encdecFactory.get(),
                            protocolFactory.get(),
                            connections);
                    connections.getConnectionsList().put(clientSock.getPort(),handler);
                }

                execute((BlockingConnectionHandler) handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract  void execute(BlockingConnectionHandler<T> handler);

}
