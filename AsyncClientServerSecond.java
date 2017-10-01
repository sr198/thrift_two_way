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

public class AsyncClientServerSecond {

    //Inner class for server process
    public static class LocalServer implements Runnable {
        @Override
        public void run() {
            startListener();
        }

        public class LocalServerHandlerAsync implements clientService.AsyncIface {
            @Override
            public void receiveMessage(String message, AsyncMethodCallback<Void> resultHandler) throws TException {
                System.out.println("Message received from server is "+ message);
                resultHandler.onComplete(null);
            }
        }

        public void startListener() {
            try {
                TProcessor proc = new clientService.AsyncProcessor<>(new LocalServerHandlerAsync());
                TNonblockingServerSocket trans_svr = new TNonblockingServerSocket(9096);
                TServer server =
                        new THsHaServer(new THsHaServer.Args(trans_svr)
                                .processor(proc)
                                .protocolFactory(new TBinaryProtocol.Factory())
                                .minWorkerThreads(4)
                                .maxWorkerThreads(4));
                System.out.println("[Client] listening on port 9096");
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }
    }
    public static class ClientCallback implements AsyncMethodCallback<String> {
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
        public void onComplete(String result) {
            System.out.print("Message received from server: "+ result );
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }

    public static class ClientCallback2 implements AsyncMethodCallback<Void> {
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
            System.out.print("Done" );
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, TException {
        Thread clientServer = new Thread( new LocalServer());
        clientServer.start();

        TAsyncClientManager clientManager = new TAsyncClientManager();
        TNonblockingSocket socket = new TNonblockingSocket("localhost", 9095);
        serverService.AsyncClient asyncClient = new serverService.AsyncClient(new TBinaryProtocol.Factory(),
                clientManager,
                socket);
        ClientCallback callback = new ClientCallback();
        ClientCallback2 callback2 = new ClientCallback2();

        callback.reset();
        asyncClient.repeatMessage(5000, callback);
        callback.wait(5000);

        callback.reset();
        asyncClient.stopMessage(callback2);
        callback.wait(1000);

        clientManager.stop();
        socket.close();
    }
}
