import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.lang.StringBuilder;
import java.util.Date;


//This class represents the server application
public class ServerApp
{
    /**
    * GLobal variables used throughout the sevrer app class such as the servers response to the client request, 
    * the httpVersion, whether the file reading is complete or not, the time/date that the file was last modified
    * and accessed, a temporary File object, whether the connection is persistant or not, and whether the
    * embedded object has been fetched or not
    **/
    public static String response;
    public static float httpVersion;
    public static boolean complete;
    public static long lastModifiedMilli;
    public static Date lastModifiedDate;
    public static long lastAccessedMilli;
    public static Date lastAccessedDate;
    public static File f;
    public static boolean persistant;
    public static boolean objectFetch;

    public static void main(String[] args) throws Exception
    {
        //quick printouts
        System.out.println("Transmission Delay: " + args[0] + " Propagation Delay: " + args[1]);
        System.out.println("On standby....");

        // intializing variables from command lines
        float transmissionDelay = Float.parseFloat(args[0]);
        float propagationDelay = Float.parseFloat(args[1]);
        httpVersion = Float.parseFloat(args[2]);

        // declare the persistant variable true or false depending on the http version
        if(httpVersion == 1.0) {
            persistant = false;
        } else {
            persistant = true;
        }

        // before the program has run, the response and object fetch are incomplete 
        complete = false;
        objectFetch = false;

        //create a new transport layer for server (hence true) (wait for client)
        TransportLayer transportLayer = new TransportLayer(true, transmissionDelay, propagationDelay);

        // server sends an ack to client as well as opens connection
        transportLayer.synAck(2);

        while( true )
        {

            //receive message from client, and send the "received" message back.
            byte[] byteArray = transportLayer.receive();

            // the received file name from the client
            String fileName = new String ( byteArray );

            // if the file name starts **, then it is an embedded object file
            if(fileName.startsWith("**")){
                fileName = fileName.substring(2);
                objectFetch = true;
            }

            //if client disconnected
            if(byteArray==null)
                break;

            // if the fetch is incomplete
            // if the fetch is complete but the file has been modified
            // if the fetch is an object fetch
            if((!complete) || (complete && (lastModifiedMilli > lastAccessedMilli)) || (objectFetch)) {

                // create a file object from file name
                f = new File(fileName);
                StringBuilder contentBuilder = new StringBuilder();

                // read in the files data line by line
                try {
                    BufferedReader in = new BufferedReader(new FileReader(fileName));
                    String l;
                    // fetches the last modified time of the file
                    lastModifiedMilli = f.lastModified();
                    lastModifiedDate = new Date(lastModifiedMilli);
                    while((l = in.readLine()) != null){
                        contentBuilder.append(l);
                    }
                    in.close();
                    // file reading is complete
                    complete=true;
                } catch(Exception e){
                    // server response is 404 because file hasnt been found
                    response = notFound();
                    System.out.println("Error notification: There was a problem trying to obtain the web page. This could be due to an incorrect file name or the lack of the file in the project directory");
                }

                // the data is stored in a string 
                String content = contentBuilder.toString();
                objectFetch = false;

                // if the file reading was completed, then everything went fine 
                // server responses with 200, the content and the last modified date
                if(complete){
                     response = ok() + "\n" + content + "\n" + lastModifiedDate;
                     lastAccessedMilli = System.currentTimeMillis();
                     lastAccessedDate = new Date(lastAccessedMilli);
                }

            // this means that the file hasn't been modified since its inital fetch so send 304 back
            } else {
                response = notModified();
            }
             
            // sends response back to client and waits for next request
            byteArray = response.getBytes();
            transportLayer.send( byteArray );
            System.out.println("On standby..");

            // if non persistant, response to client with acknowlege
            if(!persistant){
                transportLayer.synAck(2);
            }

        }
    }

    /**
    * notFound() reponds with a 404 http code
    **/
    public static String notFound(){
        String toReturn = "HTTP/" + httpVersion + " 404 Not Found";
        return toReturn;
    }

    /**
    * ok() reponds with a 200 http code
    **/
    public static String ok(){
        String toReturn = "HTTP/" + httpVersion + " 200 OK";
        return toReturn;
    }

    /**
    * notModified() reponds with a 304 http code
    **/
    public static String notModified(){
        String toReturn = "HTTP/" + httpVersion + " 304 Not Modified";
        return toReturn;
    }

}


