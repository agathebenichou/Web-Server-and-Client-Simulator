import java.util.concurrent.TimeUnit;

/**
*  The Network layer provides data routing paths where data is transferred in the form of packets.
* These packets face several delays such as the transmission delay and propagation delay.
* The Network Layers receives packets from the Transport Layer, processes them and sends them to the Link Layer.
**/
public class NetworkLayer
{

    private LinkLayer linkLayer;

    /**
    * Global transmission delay which is amount of time needed to push all the bits of a packet
    * onto the link
    **/
    public float transmissionDelay;

    /**
    * Global propagation delay which is amount of time needed to propagate all the bits of a packet
    * across the link
    **/
    public float propagationDelay;

    /**
    * Constructor which takes boolean of whether it is a client or a sever, the transmission and propagation delay
    * Initializes parameters with global variables and intantiates the link layer 
    **/
    public NetworkLayer(boolean server, float transmissionDelay, float propagationDelay)
    {
        this.transmissionDelay = transmissionDelay;
        this.propagationDelay  = propagationDelay;
        linkLayer = new LinkLayer(server);
    }

    /**
    * Send method which sends bytes of the messages to link layer which can forward it to corresponding network nodes
    * Before sending the bytes, the transmission delay method must load bits onto link and propagation delay method
    * must propgate the bytes across link
    **/
    public void send(byte[] payload)
    {
        this.transDelay(payload);

        this.propDelay();

        System.out.println("Propagating the message across the link...");

        linkLayer.send( payload );
    }

    /**
    * Receive method which returns the received bytes and employs the propagation delay
    * method
    **/
    public byte[] receive()
    {

        byte[] payload = linkLayer.receive();

        this.propDelay();

        System.out.println("Receiving the message from the link...\n");

        return payload;
    }

    /**
    * Transmission Delay method which, for eveyr byte in the byte array, waits the amount of
    * time in seconds indicated by the transmission delay
    **/
    public void transDelay(byte[] payload){

        for(int i = 1; i < payload.length+1; i++){
            System.out.println("Transmitting byte " + i + " out of " + payload.length);

            try {
                Thread.sleep((int)(this.transmissionDelay)*1000);
            } catch (Exception e){
               System.out.println("Sleep thread not working"); 
            }
            
        }
    }

    /**
    * Propagation Delay method which delays sending the bytes by the amount of time
    * in seconds
    **/
    public void propDelay(){

        try {
            Thread.sleep((int)(this.propagationDelay)*1000);
        } catch (Exception e){
            System.out.println("Sleep thread not working");
        }
        
    }
}
