import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.async.AsyncMethodCallback;


public class AsyncServer {
    public static class TradeHistoryHandler
            implements TradeReporting.TradeHistory.Iface {

        @Override
        public int add(int num1, int num2) throws TException {
            return num1 + num2;
        }
    }

    private static class AsyncTradeHistoryHandler
            implements TradeReporting.TradeHistory.AsyncIface {

        private TradeHistoryHandler electronic;

        public AsyncTradeHistoryHandler() {
            electronic = new TradeHistoryHandler();
        }

        @Override
        public void add(int num1, int num2, AsyncMethodCallback<Integer> resultHandler) throws TException {
            resultHandler.onComplete(electronic.add(num1, num2));
        }
    }

    public static void main(String[] args)
            throws TTransportException, IOException, InterruptedException {

        //FloorBroker floor = new FloorBroker();
        //(new Thread(floor)).start();

        TProcessor proc = new TradeReporting.TradeHistory.AsyncProcessor(
                new AsyncTradeHistoryHandler());
        TNonblockingServerSocket trans_svr = new TNonblockingServerSocket(9090);
        TServer server =
                new THsHaServer(new THsHaServer.Args(trans_svr)
                        .processor(proc)
                        .protocolFactory(new TBinaryProtocol.Factory())
                        .minWorkerThreads(4)
                        .maxWorkerThreads(4));
        System.out.println("[Server] listening of port 9090");
        server.serve();
    }
}
