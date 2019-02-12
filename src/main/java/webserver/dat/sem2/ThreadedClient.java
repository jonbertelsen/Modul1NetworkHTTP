package webserver.dat.sem2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedClient {

    private static void getRequest(String hostName, int portNumber, String message, int i) throws IOException {
        System.out.println( "Connect to: " + hostName + ":" + portNumber );
        Socket mySocket = new Socket( hostName, portNumber );
        sendMessagePrintResult( mySocket, message, i );
        mySocket.close();
    }

    public static void main( String[] args ) {
        try {
            picoClient01();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Main terminated");
    }

    private static void picoClient01() throws Exception {
        String hostName = "http://188.166.161.47";
        int portNumber = 8080;
        String message = "GET /index.html HTTP/1.0\r\n\r\n";

        ExecutorService workingJack = Executors.newFixedThreadPool( 50 );

        try {
            // Fetch webpage 100 times:
            for (int i = 0; i < 1000; i++) {
                final int number = i;
                workingJack.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getRequest(hostName, portNumber, message, number);
                        } catch (IOException ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                });
            }
        } catch ( Exception ex ) {
            System.out.println( "Uuups: " + ex.getLocalizedMessage() );
        }
    }

    private static void sendMessagePrintResult( Socket mySocket, String message, int i ) throws IOException {
        PrintWriter out = new PrintWriter( mySocket.getOutputStream(), true );
        BufferedReader in = new BufferedReader(
                new InputStreamReader( mySocket.getInputStream() ) );
        // Send message to server
        out.println( message );
        // print response
        System.out.println("------- Task: " + i + ": " + "------" );
        String line;
        while ( ( line = in.readLine() ) != null ) {
            System.out.println( line );
        }

    }

}
