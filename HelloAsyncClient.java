import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import tutorial.example.helloworld.helloWorldService;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HelloAsyncClient {

    public static class HelloWorldCallback implements AsyncMethodCallback<String> {
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
    public static void main(String[] args) throws IOException, TException {
        TAsyncClientManager clientManager = new TAsyncClientManager();
        TNonblockingSocket socket = new TNonblockingSocket("localhost", 9090);
        helloWorldService.AsyncClient asyncClient = new helloWorldService.AsyncClient(new TBinaryProtocol.Factory(),
                clientManager,
                socket);
        HelloWorldCallback callback = new HelloWorldCallback();
        callback.reset();
        asyncClient.sayHello("Shaili", callback);
        callback.wait(500);

        callback.reset();
        asyncClient.sayHello("John", callback);
        callback.wait(1000);

        clientManager.stop();
        socket.close();
    }
}
