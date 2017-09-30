import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import AsyncCalculator.AsyncCalculatorService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.TException;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.async.AsyncMethodCallback;

public class AsyncCalculatorClient {

    //Class template supporting async wait and timeout
    private static abstract class WaitableCallback<T>
            implements AsyncMethodCallback<T> {

        private CountDownLatch latch = new CountDownLatch(1);

        public void reset() { latch = new CountDownLatch(1); }
        public void complete() { latch.countDown(); }
        public boolean wait(int i) {
            boolean b = false;
            try { b = latch.await(i, TimeUnit.MILLISECONDS); }
            catch(Exception e) { System.out.println("[Client] await error"); }
            return b;
        }

        @Override
        public void onError(Exception ex) {
            if (ex instanceof TimeoutException) {
                System.out.println("[Client] Async call timed out");
            } else {
                System.out.println("[Client] Async call error");
            }
            complete();
        }
    }

    //Application entry point
    public static void main(String[] args)
            throws IOException, InterruptedException, TException {
        //Async client and I/O stack setup
        TNonblockingSocket trans_ep = new TNonblockingSocket("localhost", 9090);
        TAsyncClientManager client_man = new TAsyncClientManager();
        AsyncCalculatorService.AsyncClient client =
                new AsyncCalculatorService.AsyncClient(new TBinaryProtocol.Factory(),
                        client_man, trans_ep);

        WaitableCallback<Integer> wc =
                new WaitableCallback<Integer>() {

                    @Override
                    public void onComplete(Integer tr) {
                        try {
                            System.out.println("Results is "+tr);
                        } finally {
                            complete();
                        }
                    }
                };

        //Make async calls
        wc.reset();
        client.add(251,351, wc);
        wc.wait(5000);

        wc.reset();
        client.multiply(15,10, wc);
        wc.wait(5000);

        //Shutdown async client manager and close network socket
        client_man.stop();
        trans_ep.close();
    }
}