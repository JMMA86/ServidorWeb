package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public class SolicitudHttp implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public SolicitudHttp(Socket socket) {
        this.socket = socket;
    }

    // Implementa el método run() de la interface Runnable.
    public void run() {
        try {
            proceseSolicitud();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void proceseSolicitud() throws Exception {
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String linea;
        String recurso = "";
        while ((linea = in.readLine()) != null && !linea.isEmpty()) {
            System.out.println(linea);
            // Extrae el nombre del archivo de la línea de solicitud.
            StringTokenizer tokens = new StringTokenizer(linea);
            // El primer token es el método
            String method = tokens.nextToken();
            System.out.println(method);
            if (method.equals("GET")) {
                // El segundo token es el recurso requerido
                recurso = tokens.nextToken();
                System.out.println("METHOD: " + method);
                System.out.println("RECURSO: " + recurso);
                break;
            }
        }

        // Construye un mensaje de respuesta
        String rutaBase = "src/main/resources";
        File file = new File(rutaBase + recurso);
        String lineaDeEstado;
        String lineaHeader;

        if (file.exists() && !file.isDirectory()) {
            lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
            lineaHeader = "Content-Type: " + contentType(recurso) + CRLF;

            enviarString(lineaDeEstado, out);
            enviarString(lineaHeader, out);
            enviarString(CRLF, out);

            try (FileInputStream fis = new FileInputStream(file)) {
                enviarBytes(fis, out);
            }
        } else {
            File file404 = new File(rutaBase + "/404.html");
            if (file404.exists()) {
                lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
                lineaHeader = "Content-Type: text/html" + CRLF;

                enviarString(lineaDeEstado, out);
                enviarString(lineaHeader, out);
                enviarString(CRLF, out);

                try (FileInputStream fis = new FileInputStream(file404)) {
                    enviarBytes(fis, out);
                }
            } else {
                lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
                lineaHeader = "Content-Type: text/html" + CRLF;

                enviarString(lineaDeEstado, out);
                enviarString(lineaHeader, out);
                enviarString(CRLF, out);
                enviarString("<html><body><h1>404 Not Found</h1></body></html>", out);
            }
        }

        // Cierra los streams y el socket.
        out.flush();
        out.close();
        in.close();
    }

    private static String contentType(String nombreArchivo) {
        if (nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")) {
            return "text/html";
        }
        if (nombreArchivo.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if (nombreArchivo.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

    private static void enviarString(String line, OutputStream os) throws Exception {
        os.write(line.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviarBytes(InputStream fis, OutputStream os) throws Exception {
        // Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket.
        byte[] buffer = new byte[2048];
        int bytes;

        // Copia el archivo solicitado hacia el output stream del socket.
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }
}
