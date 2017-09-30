import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import tutorial.example.helloworld.helloWorldService;
public class HelloSyncServer {
    public static class HelloWorldHandler implements helloWorldService.Iface {

        @Override
        public String sayHello(String name) throws TException {
            String message = "Hey " + name +"! How are you doing today?";
            return message;
        }
    }

    public static void main(String[] args) {
        try {
            TServerSocket serverSocket = new TServerSocket(9090);
            TProcessor processor = new helloWorldService.Processor<>(new HelloWorldHandler());
            TSimpleServer server = new TSimpleServer(new TSimpleServer.Args(serverSocket).processor(processor));
            System.out.println("Waiting for connection");
            server.serve();

        } catch (TTransportException e) {
            e.printStackTrace();
        }

    }
}
