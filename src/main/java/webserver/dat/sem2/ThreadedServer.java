package webserver.dat.sem2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 The purpose of ServerMain is to...

 @author kasper
 */
public class ThreadedServer {

    public static void main( String[] args ) throws Exception {
        picoServer06();
/*
        System.out.println(line+"\n using current thread context loader\n"+line);
        loadResourceWithContextLoader("pages/index.html");
        loadResourceWithContextLoader("/index.html");
        loadResourceWithContextLoader("pages/adding.html");
        loadResourceWithContextLoader("/adding.html");
*/
    }

    /*
    Plain server that just answers what date it is.
    It ignores all path and parameters and really just tell you what date it is
     */


    /*
    This server has exception handling - so if something goes wrong we do not
    have to start it again. (this is a yellow/red thing for now)
     */
    private static void picoServer05() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";

        ExecutorService workingJack = Executors.newFixedThreadPool( 17 );

        while ( true ) { // keep listening (as is normal for a server)
            Socket socket = server.accept();
            workingJack.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MakeResponse(root, socket);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private static void MakeResponse(String root, Socket socket) throws IOException {
        try {
            System.out.println( "-----------------" );
            HttpRequest req = new HttpRequest( socket.getInputStream() );
            String path = root + req.getPath();
            String html = getResource( path );
            String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
            socket.getOutputStream().write( httpResponse.getBytes(StandardCharsets.UTF_8) );
            System.out.println( "<<<<<<<<<<<<<<<<<" );
        } catch ( Exception ex ) {
            String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                    + "UUUUPS: " + ex.getLocalizedMessage();
            socket.getOutputStream().write( httpResponse.getBytes(StandardCharsets.UTF_8) );
        } finally {
            if ( socket != null ) {
                socket.close();
            }
        }
    }

    /*
    This server requires static files to be named ".html" or ".txt". Other path
    names is assumed to be a name of a service.
     */
    private static void picoServer06() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
        int count = 0;

        ExecutorService workingJack = Executors.newFixedThreadPool( 100 );

        while ( true ) { // keep listening (as is normal for a server)
            Socket socket = server.accept();
            workingJack.execute(new Runnable() {
                @Override
                public void run() {
                    Pico06MakeResponse(root, count, socket, workingJack);
                }
            });
        }
    }

    private static void Pico06MakeResponse(String root, int count, Socket socket, ExecutorService workingJack) {
        try {
            System.out.println("---- reqno: " + count + " ----");
            HttpRequest req = new HttpRequest(socket.getInputStream());

            workingJack.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Pico06MakeResponseThreads(root, socket, workingJack, req);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }

    }

    private static void Pico06MakeResponseThreads(String root, Socket socket, ExecutorService workingJack, HttpRequest req) throws IOException {
        try {
            String path = req.getPath();

            switch (getFileType(path)){
                case "html":
                case "txt":
                    workingJack.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pico06MakeFileResponseTask(root, socket, path);
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    });
                    break;
                case "jpg":
                    workingJack.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pico06MakeImageResponseTask(root, socket, path);
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    });
                    break;
                case "path":
                    workingJack.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pico06MakePathResponseTask(root,socket, req, path);
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    });
                    break;
            }
        } catch ( Exception ex ) {
            String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                    + "UUUUPS: " + ex.getLocalizedMessage();
            socket.getOutputStream().write( httpResponse.getBytes(StandardCharsets.UTF_8) );
        }
    }

    private static void pico06MakeFileResponseTask(String root, Socket socket, String path) throws Exception {
        try {
            String httpResponse;

            String html = getResource(root + path);
            System.out.println("Trådnavn: " + Thread.currentThread().getName());

            httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
            socket.getOutputStream().write(httpResponse.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex){
            String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                    + "UUUUPS: " + ex.getLocalizedMessage();
            socket.getOutputStream().write( httpResponse.getBytes(StandardCharsets.UTF_8) );
        } finally {
            if ( socket != null ) {
                socket.close();
            }
        }
    }

    private static void pico06MakePathResponseTask(String root, Socket socket, HttpRequest req, String path) throws IOException {

        try {
            String httpResponse;
            String res = "";
            switch (path) {
                case "/":
                    pico06MakeFileResponseTask(root, socket, "/index.html");
                case "/addournumbers":
                    res = addOurNumbers(req);
                    break;
                case "/calcNumbers":
                    switch (req.getParameter("operation")) {
                        case "Add":
                            res = calcOurNumbers(req, "Add");
                            break;
                        case "Multiply":
                            res = calcOurNumbers(req, "Multiply");
                            break;
                    }
                    break;
                default:
                    res = "Unknown path: " + path;
                    break;
            }
            httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + res;
            socket.getOutputStream().write(httpResponse.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex){
            String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                    + "UUUUPS: " + ex.getLocalizedMessage();
            socket.getOutputStream().write( httpResponse.getBytes(StandardCharsets.UTF_8) );
        } finally {
            if ( socket != null ) {
                socket.close();
            }
        }
    }

    private static void pico06MakeImageResponseTask(String root, Socket socket, String path) throws Exception {
        try {
            byte[] bytecontent = getResourceFileContentsByteFormat(root + path);
            int numOfBytes = bytecontent.length;
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
            outToClient.writeBytes("Content-Type: image/jpeg\r\n");
            outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
            outToClient.writeBytes("\r\n");
            outToClient.write(bytecontent, 0, numOfBytes);
        } catch (Exception ex){
            String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                    + "UUUUPS: " + ex.getLocalizedMessage();
            socket.getOutputStream().write( httpResponse.getBytes(StandardCharsets.UTF_8) );
        } finally {
            if ( socket != null ) {
                socket.close();
            }
        }
    }

    private static String getFileType(String path){
        if (path.endsWith(".html")) return "html";
        if (path.endsWith(".txt")) return "txt";
        if (path.endsWith(".jpg")) return "jpg";
        return "path";
    }

    private static String addOurNumbers( HttpRequest req ) {
        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );
        return generateHTML("result.tmpl",first, second, String.valueOf(fi + si), "+");
    }

    private static String calcOurNumbers(HttpRequest req, String operation) {
        String result = "";
        String operatorChar = "";
        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );

        switch (operation){
            case "Add":
                result = String.valueOf(fi + si);
                operatorChar = "+";
                break;
            case "Multiply":
                result = String.valueOf(fi * si);
                operatorChar = "*";
                break;
        }
        return generateHTML("multiplyresult.tmpl",first, second, result, operatorChar);
    }

    private static String generateHTML(String fileName, String a, String b, String c, String d){
        String res = "";
        try {
            res = getResource(fileName);
            res = res.replace("$0", a);
            res = res.replace("$1", b);
            res = res.replace("$2", c);
            res = res.replace("$3",d);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Templatefilen result.tmpl findes ikke");
        }
        return res;
    }

    private static String getResource(String fileName) {
        StringBuilder val = new StringBuilder();

        try {
            Class cls = Class.forName("webserver.dat.sem2.ThreadedServer");

            // returns the ClassLoader object associated with this Class
            ClassLoader cLoader = cls.getClassLoader();

            // input stream
            try (InputStream i = cLoader.getResourceAsStream(fileName)) {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8))) {

                    // reads each line
                    String l;
                    while ((l = r.readLine()) != null) {
                        val.append(l);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return val.toString();
    }

    private static byte[] getResourceFileContentsByteFormat( String fileName ) throws Exception {
        //Get file from resources folder
        Class cls = Class.forName("webserver.dat.sem2.ThreadedServer");
        ClassLoader cLoader = cls.getClassLoader();

        // input stream
        ByteArrayOutputStream bos;
        try (InputStream i = cLoader.getResourceAsStream(fileName)) {

            //FileInputStream fis = new FileInputStream(i);
            bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            try {
                for (int readNum; (readNum = i.read(buf)) != -1; ) {
                    //Writes to this byte array output stream
                    bos.write(buf, 0, readNum);
                    System.out.println("read " + readNum + " bytes,");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return bos.toByteArray();
    }

}