package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {

	private ServerSocket serverSocket = null;
	
	private boolean stopServer = false;
	//private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	private Socket socket;
	private int port; 
	
	
	public int getPort() {
		return port;
	}

	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public NFServer() throws IOException {
	    InetSocketAddress serverSocketAddress = new InetSocketAddress(0);
	    serverSocket = new ServerSocket();
	    //serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
	    serverSocket.setReuseAddress(true);
	    serverSocket.bind(serverSocketAddress);
	    port = serverSocket.getLocalPort(); 
	    System.out.println("* BackgroundServer operando en el puerto " + port);
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
	    socket = null;
	    while (!stopServer) {
	        try {
	            socket = serverSocket.accept();
	            //System.out.println("New client in the port : " + socket.getPort());
	            
	            // Si el servidor está detenido, no deberías aceptar más conexiones
	            if (!stopServer) {
	                NFServerThread hilo = new NFServerThread(socket);
	                hilo.start();
	            } else {
	                // Cerrar el socket si el servidor está detenido
	                socket.close();
	            }
	        } catch (IOException e) {
	            // Manejar la excepción adecuadamente
	            if (!stopServer) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
	/**
	 * TODO: Añadir métodos a esta clase para: 1) Arrancar el servidor en un hilo
	 * nuevo que se ejecutará en segundo plano 2) Detener el servidor (stopserver)
	 * 3) Obtener el puerto de escucha del servidor etc.
	 */


	public void startServer() {
		new Thread(this).start(); 
	}
	
	//Funcion para detener el servidor lanzado en el hilo 
	public void stopserver() {
		 stopServer = true;
	        try {
	        	
	            serverSocket.close();
	            System.out.println("* Servidor detenido.");
	        } catch (IOException e) {
	            System.err.println("* Error al detener el servidor: " + e.getMessage());
	            e.printStackTrace();
	        }
	}
	
	public Socket getSocket() {
		return socket;
	}

}
