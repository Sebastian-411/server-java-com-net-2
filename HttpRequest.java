import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

final class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {

        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String requestLine = br.readLine();

        System.out.println();
        System.out.println(requestLine);

        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();
        String fileName = tokens.nextToken();

        if (fileName.equals("/")) {
            fileName = "/index.html";
        }

        fileName = "." + fileName;

        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "HTTP/1.0 201 CREATED" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        } else {
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: text/html" + CRLF;
            entityBody = "<HTML>" +
                    "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
                    "<BODY>404 Not Found</BODY></HTML>";
        }

        os.writeBytes(statusLine);

        os.writeBytes(contentTypeLine);

        os.writeBytes(CRLF);

        if (fileExists) {
            sendBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(entityBody);
        }

        os.close();
        br.close();
        socket.close();
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {

        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
