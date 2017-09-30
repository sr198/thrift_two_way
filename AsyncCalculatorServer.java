import java.io.IOException;

import AsyncCalculator.AsyncCalculatorService;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.async.AsyncMethodCallback;


public class AsyncCalculatorServer {

    public static class CalculatorHandler
            implements AsyncCalculatorService.Iface {

        @Override
        public int add(int num1, int num2) throws TException {
            return num1 + num2;
        }

        @Override
        public int multiply(int num1, int num2) throws TException {
            return num1 * num2;
        }
    }

    private static class AsyncCalculatorHandler
            implements AsyncCalculatorService.AsyncIface {

        private CalculatorHandler calc;

        public AsyncCalculatorHandler() {
            calc = new CalculatorHandler();
        }

        @Override
        public void add(int num1, int num2, AsyncMethodCallback<Integer> resultHandler) throws TException {
            resultHandler.onComplete(calc.add(num1, num2));
        }

        @Override
        public void multiply(int num1, int num2, AsyncMethodCallback<Integer> resultHandler) throws TException {
            resultHandler.onComplete(calc.multiply(num1, num2));
        }
    }

    public static void main(String[] args)
            throws TTransportException, IOException, InterruptedException {

        TProcessor proc = new AsyncCalculatorService.AsyncProcessor(
                new AsyncCalculatorHandler());
        TNonblockingServerSocket trans_svr = new TNonblockingServerSocket(9090);
        TServer server =
                new THsHaServer(new THsHaServer.Args(trans_svr)
                        .processor(proc)
                        .protocolFactory(new TBinaryProtocol.Factory())
                        .minWorkerThreads(4)
                        .maxWorkerThreads(4));
        System.out.println("[Server] listening at port 9090");
        server.serve();
    }
}
