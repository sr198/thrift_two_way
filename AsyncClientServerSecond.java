import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TTransportException;
import tutorial.example.twoway.clientService;
import tutorial.example.twoway.serverService;

import java.io.IOException;


public class AsyncClientServerSecond {

    public static class LocalClient implements Runnable {
        @Override
        public void run() {
            
        }
    }

    private class AsyncLocalServerHandler implements serverService.AsyncIface {
        private boolean sendMessage = false;

        @Override
        public void repeatMessage(int interval, AsyncMethodCallback<String> resultHandler) throws TException {
            if( sendMessage ) {
                String message = "Some message";
                resultHandler.onComplete(message);
            }
        }

        @Override
        public void stopMessage(AsyncMethodCallback<Void> resultHandler) throws TException {
            sendMessage = false;
        }
    }

    public void startListener() {
        try {
            TProcessor proc = new serverService.AsyncProcessor<>(new AsyncLocalServerHandler());
            TNonblockingServerSocket trans_svr = new TNonblockingServerSocket(9090);
            TServer server =
                    new THsHaServer(new THsHaServer.Args(trans_svr)
                            .processor(proc)
                            .protocolFactory(new TBinaryProtocol.Factory())
                            .minWorkerThreads(4)
                            .maxWorkerThreads(4));
            System.out.println("[Server] listening of port 9090");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException, TException {
        Thread listeningThread = new Thread(new LocalServer());
        listeningThread.start();

        TAsyncClientManager clientManager = new TAsyncClientManager();
        TNonblockingSocket socket = new TNonblockingSocket("localhost", 9090);
        serverService.AsyncClient asyncClient = new serverService.AsyncClient(new TBinaryProtocol.Factory(),
                clientManager,
                socket);
        asyncClient.repeatMessage(5000, null);

        clientManager.stop();
        socket.close();
    }
}

