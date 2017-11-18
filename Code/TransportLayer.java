/**
* The Transport Layer ensures the reliable arrival and departure of packets by employing TCP and UDP
* TL receives packets of data from the sockets of the end system, processes them and passes them to the Network Layer
**/
public class TransportLayer
{

    private NetworkLayer networkLayer;
    public boolean server;


    //server is true if the application is a server (should listen) or false if it is a client (should try and connect)
    /**
    * Constructor which takes boolean of whether it is a client or a sever, the transmission and propagation delay 
    * Initializes the server boolean and intiates a network layer class for this data to be sent down and processes
    **/
    public TransportLayer(boolean server, float transmissionDelay, float propagationDelay)
    {
        this.server = server;
        networkLayer = new NetworkLayer(server, transmissionDelay, propagationDelay);
    }

    /**
    * Send method which takes in the bytes of the messages and sends it down to the
    * network layer to be processed
    **/
    public void send(byte[] payload)
    {
        networkLayer.send( payload);
    }

    /**
    * Receive method which receives the bytes coming back from the network layers and 
    * send it back up to the application layer (client or sever)
    **/
    public byte[] receive()
    {
        byte[] payload = networkLayer.receive();    
        return payload;
    }

    /**
    * synAck method is the 3 way handshake implementation of TCP protocol
    * The first step (case 1) is the client initating a syn, the second step (Case 2) is the
    * server sending a ack back to client and the third step (Case 3) is the client sending
    * a request message for webpage back to server 
    **/
    public void synAck(int step){

        switch(step){

            // synchronize
            case 1:

                System.out.println("Client sending a TCP synchronize packet to Server...");
                this.waitForResponse();
                break;

            // acknowledge
            case 2:
                this.waitForResponse();
                System.out.println("Server creating TCP connection...");
                
                break;

            // starts sending its http requests for the desired webpage
            case 3:
                this.waitForResponse();
                System.out.println("Client sending request...");

                break;

            default:
                break;

        }
 
    }

    /**
    * waitForResponse() method simply has the program "wait" for 2 seconds to stimulate the
    * 3 way handshaking protocol efficiently to the user
    **/
    public void waitForResponse(){
            try {
                Thread.sleep(2000);
            } catch (Exception e){
               System.out.println("Sleep thread not working"); 
            }

    }

}
