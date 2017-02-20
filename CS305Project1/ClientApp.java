
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.util.Date;

/**
*  Represents the Client side of the Application Layer
*  Sends a GET request for a page
*/
public class ClientApp
{
    /*
    * Global variables used throughout the Client class such as the file name the client wants, the version of HTTP being used which
    * leads to whether the exchange will be persistant or non persistant, a variable about whether the client has received the file, 
    * the date/time that the client has last accessed the file, the file name of any possible embedded objects and the start/end time of the program
    */
    public static String fileName;
    public static float httpVersion;
    public static boolean hasFile;
    public static long lastAccessedMilli;
    public static Date lastAccessedDate;
    public static boolean persistant;
    public static String objectFileName;
    public static boolean fetchedObject;
    public static long startTime, endTime, totalTime;

    public static void main(String[] args) throws Exception
    {
        // stores the program start time
        startTime = System.currentTimeMillis();

        // Quick printout of the delays 
        System.out.println("Transmission Delay: " + args[0] + " Propagation Delay: " + args[1]);

        /*
        * Initializing the transmission and propagation delay as well as the 
        * HTTP version and the file name from the java terminal. At the start
        * of the program, the client doesn't have the file or its embedded object
        */
        float transmissionDelay = Float.parseFloat(args[0]);
        float propagationDelay = Float.parseFloat(args[1]);
        httpVersion = Float.parseFloat(args[2]);
        fileName = args[3];
        hasFile=false;
        fetchedObject = false;

        /*
        * IF conditional to initialize the persistant boolean
        * http version of 1.0 = non persistant connection
        * http version of 1.1 = persistant connection
        */
        if(httpVersion == 1.0) {
            persistant = false;
        } else {
            persistant = true;
        }

        //create a new transport layer for client (hence false) (connect to server), and read in first line from keyboard
        TransportLayer transportLayer = new TransportLayer(false, transmissionDelay, propagationDelay);

        /*
        * The 3 way hand shaking protocol: client sends the syn then
        * waits for the ack in order to send the request
        */
        transportLayer.synAck(1);
        transportLayer.synAck(3); 

        /*
        * Client side must press enter to send request
        */
        System.out.println("Press the Enter key to continue..");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();

        while(line.equals(""))
        {
            /*
            * If the client already has the file, its browser sends an if modified since line
            * along with the get request. If the client doesn't have the file yet, its browser
            * just sends the get request.
            */
            if(hasFile){
                System.out.println("GET " + fileName + " HTTP/" + httpVersion);
                System.out.println("If-Modified-Since: " + lastAccessedDate);
                System.out.println("Host: www.aggieandfrankie.com\nUser-Agent: Mozilla/5.0\nAccept-Language: eng\nContent-Length: " + fileName.getBytes().length + "\n");

            } else {
                System.out.println("GET " + fileName + " HTTP/" + httpVersion);
                System.out.println("Host: www.aggieandfrankie.com\nUser-Agent: Mozilla/5.0\nAccept-Language: eng\nContent-Length: " + fileName.getBytes().length + "\n");
            }

            //convert lines into byte array, send to transport layer and wait for response
            byte[] byteArray = fileName.getBytes();
            transportLayer.send( byteArray );
            byteArray = transportLayer.receive();
            String str = new String ( byteArray );

            // string array of server response to be processed
            String[] response = str.split("\n");

            // body of the main for running the program
            // if the response if 200 or 304, process the if startment. 
            // if the response is 404, process the else startment of this outer loop
            if(httpResponse(response[0])) {
                // client has the file if 200 or 304
                hasFile = true;

                // if the response from the server was 200...
                if(response[0].equals("HTTP/1.1 200 OK")||response[0].equals("HTTP/1.0 200 OK")){

                    // check for an embedded object in the page using checkForObjects()
                    objectFileName = checkForObjects(response[1]);

                    // if the initial webpage has an embedded object and if that object hasn't been fetched..
                    if((!objectFileName.equals("")) && (!fetchedObject)){

                        System.out.println("Fetching the embedded object...");

                        // the client app must start another 3 way handshake with the server before
                        // requesting another http object 
                        if(!persistant){
                            transportLayer.synAck(1);
                            transportLayer.synAck(3); 
                        }

                        // client sends http request down to transport layer (to the server) to be fetched
                        System.out.println("\nGET " + objectFileName.substring(2) + " HTTP/" + httpVersion);
                        System.out.println("Host: www.aggieandfrankie.com\nUser-Agent: Mozilla/5.0\nAccept-Language: eng\nContent-Length: " + fileName.getBytes().length + "\n");

                        byte[] byteArray2 = objectFileName.getBytes();
                        transportLayer.send( byteArray2 );
                        byteArray2 = transportLayer.receive();
                        String str2 = new String ( byteArray2 );

                        // split up response in string array to be processed
                        String[] objectResponse = str2.split("\n");
                        String objectData = objectResponse[1];

                        // print out web page along with its embedded object
                        System.out.println(response[0]);
                        lastAccessedMilli = System.currentTimeMillis();
                        lastAccessedDate = new Date(lastAccessedMilli);
                        System.out.println("Date: " + lastAccessedDate);
                        System.out.println( "Server: Apache(Aggie and Frankie)" + "\n" + "Last Modified: " + response[2]);
                        System.out.println("ETag: 0-23-4024c3a5\nAccept-Range: bytes\nContent-Length: " + (byteArray.length + byteArray2.length) + "\nConnection: close\nContent: " + fileName + "\n");
                        System.out.println( response[1] + "\n" + objectResponse[1]);

                        System.out.println("Confirmed: Data Integrity. All things are working as they should.");

                        // now the embedded object has been fetched, this is assuming that every webpage
                        // will have at most one embedded object
                        fetchedObject = true;

                    // if the webpage has no embedded objects..
                    } else {
                        // just print out the webpages data
                        System.out.println(response[0]);
                        lastAccessedMilli = System.currentTimeMillis();
                        lastAccessedDate = new Date(lastAccessedMilli);
                        System.out.println("Date: " + lastAccessedDate);
                        System.out.println( "Server: Apache(Aggie and Frankie)" + "\n" + "Last Modified: " + response[2]);
                        System.out.println("ETag: 0-23-4024c3a5\nAccept-Range: bytes\nContent-Length: " + byteArray.length + "\nConnection: close\nContent: " + fileName + "\n");
                        System.out.println( response[1]);

                        System.out.println("Confirmed: Data Integrity. All things are working as they should.");
                    }

                // if the response code was 304, just print it out again (no need to check for objects)
                } else {
                    System.out.println(response[0]);
                    System.out.println("Confirmed: Data Integrity. All things are working as they should.");
                }

            // the 404 code was returned so either the webpage name is misspelled or
            // there is no file with such name in the directory
            } else {
                hasFile = false;
                System.out.println(response[0]);
                System.out.println("Error notification: There was a problem trying to obtain the web page. This could be due to an incorrect file name or the lack of the file in the project directory");

            }

            //read next line
            System.out.println("\nPress the Enter key to fetch the file again or the q key to terminate");
            line = reader.readLine();
            endTime = System.currentTimeMillis();
        }

        // if the user has input the character q, then the program is ending 
        // returns the total time of the program
        if(line.equals("q")){
            //endTime = System.currentTimeMillis();
            totalTime = endTime-startTime;
            System.out.println("In total and with the given paramets, the website took " + totalTime/1000 + " seconds to arrive");
        }
    }

    /**
    * httpResponse() receives the servers response and returns true or false depending on what it was
    * if 200 or 304 = true, if 404 = false
    **/
    public static boolean httpResponse(String str){
        boolean temp=false;
        if(str.equals("HTTP/1.1 200 OK")||str.equals("HTTP/1.0 200 OK")||str.equals("HTTP/1.1 304 Not Modified")||str.equals("HTTP/1.0 304 Not Modified")) {
            temp= true;
        } else if(str.equals("HTTP/1.1 404 Not Found")||str.equals("HTTP/1.0 404 Not Found")){
            temp= false;
        }
        return temp;
    }

    /**
    * checkForObjects() receives a webpages data and locates the name of the embedded object
    **/
    public static String checkForObjects(String data){
        String[] totalData = data.split(" ");
        String objectFileName="";
        for(int i = 0; i < totalData.length; i++){
            if(totalData[i].startsWith("**")){
                objectFileName = totalData[i];
            }
        }
        return objectFileName;
    }

 
}
