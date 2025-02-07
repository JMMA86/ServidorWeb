package org.example;

import java.net.ServerSocket;
import java.net.Socket;

public final class ServidorWeb {
    public static void main(String[] args) throws Exception {
        // Establece el número de puerto.
        int puerto = 8080;

        // Estableciendo el socket de escucha.
        ServerSocket socket = new ServerSocket(puerto);

        // Muestra un mensaje en la pantalla.
        System.out.println("Servidor Web escuchando en el puerto " + puerto);

        // Procesando las solicitudes HTTP en un ciclo infinito.
        try {
            while (true) {
                // Escuchando las solicitudes de conexión TCP.
                Socket socketConexion = socket.accept();
                
                // Construye un objeto para procesar el mensaje de solicitud HTTP.
                SolicitudHttp solicitud = new SolicitudHttp(socketConexion);
                
                // Crea un nuevo hilo para procesar la solicitud.
                Thread hilo = new Thread(solicitud);
                
                // Inicia el hilo.
                hilo.start();
            }
        } finally {
            // Cierra el socket de escucha.
            socket.close();
        }
    }
}