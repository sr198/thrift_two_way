import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import tutorial.example.helloworld.helloWorldService;
public class HelloAsyncServer {

    public static class HelloWorldHandler implements helloWorldService.Iface{
        @Override
        public String sayHello(String name) throws TException {
            String str = "Hey "+name+"! How are you doing today?";
            return str;
        }
    }
    private static class AsyncHelloWorldHandler implements helloWorldService.AsyncIface {
        private HelloWorldHandler helloWorldHandler;
        public AsyncHelloWorldHandler()
        {
            helloWorldHandler = new HelloWorldHandler();
        }

        @Override
        public void sayHello(String name, AsyncMethodCallback<String> resultHandler) throws TException {
            resultHandler.onComplete(helloWorldHandler.sayHello(name));
        }
    }
    private void start() {
        try {
            TProcessor proc = new helloWorldService.AsyncProcessor<>(new AsyncHelloWorldHandler());
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

    public static void main(String[] args) {
        HelloAsyncServer srv = new HelloAsyncServer();
        srv.start();
    }
}
