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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class AsyncClientServerFirst {

    //Server Part - inner class
    public static class LocalServer implements Runnable {
        @Override
        public void run() {
            startListener();
        }

        public class LocalServerHandler  implements serverService.Iface{

            private boolean serviceEnabled = true;
            @Override
            public String repeatMessage(int interval) throws TException {
                return "Hello from server";
            }

            @Override
            public void stopMessage() throws TException {
                System.out.println("Disabling the 5 second alarm");
                serviceEnabled = false;
            }

            public boolean isServiceEnabled(){
                return serviceEnabled;
            }

            public void setServiceEnabled( boolean service ){
                serviceEnabled = service;
            }
        }

        public class LocalServerHandlerAsync implements serverService.AsyncIface {
            private LocalServerHandler localServerHandler;

            LocalServerHandlerAsync(){
                localServerHandler = new LocalServerHandler();
            }

            @Override
            public void repeatMessage(int interval, AsyncMethodCallback<String> resultHandler) throws TException {
                localServerHandler.setServiceEnabled(true);
                //Send message as request to client

                TAsyncClientManager clientManager = null;
                try {
                    clientManager = new TAsyncClientManager();
                    TNonblockingSocket socket = new TNonblockingSocket("localhost", 9096);
                    clientService.AsyncClient asyncClient = new clientService.AsyncClient(new TBinaryProtocol.Factory(),
                            clientManager,
                            socket);
                    ClientCallback callback = new ClientCallback();
                    for( int i = 0; i < 1000; i++ ) {
                        callback.reset();
                        String message = "Repeat this message for me, will you?";
                        asyncClient.receiveMessage(message,callback);
                        callback.wait(1000);
                    }
                    clientManager.stop();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resultHandler.onComplete(localServerHandler.repeatMessage(5000));
            }

            @Override
            public void stopMessage(AsyncMethodCallback<Void> resultHandler) throws TException {
                localServerHandler.setServiceEnabled(false);
                resultHandler.onComplete(null);
            }
        }

        public void startListener() {
            try {
                TProcessor proc = new serverService.AsyncProcessor<>(new LocalServerHandlerAsync());
                TNonblockingServerSocket trans_svr = new TNonblockingServerSocket(9095);
                TServer server =
                        new THsHaServer(new THsHaServer.Args(trans_svr)
                                .processor(proc)
                                .protocolFactory(new TBinaryProtocol.Factory())
                                .minWorkerThreads(4)
                                .maxWorkerThreads(4));
                System.out.println("[Server] listening on port 9095");
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }

        public static class ClientCallback implements AsyncMethodCallback<Void> {
            private CountDownLatch latch = new CountDownLatch(1);

            //Synchronization Interface
            public void reset() { latch = new CountDownLatch(1); }
            public void complete() { latch.countDown(); }
            public boolean wait(int i) {
                boolean b = false;
                try { b = latch.await(i, TimeUnit.MILLISECONDS); }
                catch(Exception e) { System.out.println("[Client] await error"); }
                return b;
            }
            @Override
            public void onComplete(Void result) {
                System.out.print("Client received message from server" );
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws IOException, TException {
        Thread listeningThread = new Thread(new LocalServer());
        listeningThread.start();
    }
}

