import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import tutorial.example.helloworld.*;
public class HelloSyncClient {
    public static void main(String[] args){
        TSocket socket = new TSocket("localhost", 9090);
        TBinaryProtocol protocol = new TBinaryProtocol(socket);
        helloWorldService.Client client = new helloWorldService.Client(protocol);

        try{
            socket.open();
            String str = client.sayHello("Srijan");
            System.out.println("Message received from server " + str);
        }catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
