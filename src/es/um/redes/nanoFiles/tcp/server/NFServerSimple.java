package es.um.redes.nanoFiles.tcp.server;

//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
//import java.net.SocketTimeoutException;

public class NFServerSimple {

	//private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	//private static final String STOP_SERVER_COMMAND = "fgstop";
	private static final int PORT = 10000;
	private ServerSocket serverSocket = null;
	private int port = PORT; 

	public NFServerSimple() throws IOException {
	    boolean success = false;
	    while (!success) {
	        try {
	            InetSocketAddress socketAddress = new InetSocketAddress(port);
	            serverSocket = new ServerSocket(); 
	            //serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
	            serverSocket.bind(socketAddress);
	            // Si llega aqui sin que salte excepcion significa que ha creado con exito el
	            // servidor
	            success = true;
	        } catch (BindException e) {
	            // Excepcion que salta si ya esta el puerto ocupado
	            System.err.println("Puerto número: " + port + " ocupado. Intentando con el puerto " + (port + 1));
	            port++;
	        }
	    }
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación a menos que se implemente la funcionalidad de
	 * detectar el comando STOP_SERVER_COMMAND (opcional)
	 * @throws IOException 
	 * 
	 */
	public void run() throws IOException {
		/*
		 *Comprobar que el socket servidor está creado y ligado
		 */
		while (true) {
			try {
				/*
				 *Comprobar que el socket servidor está creado y ligado
				 */
				System.out.println("* Servidor en el puerto : "
						+ serverSocket.getLocalPort());
				/*
				 * Usar el socket servidor para esperar conexiones de otros peers que
				 * soliciten descargar ficheros
				 */
				Socket socket = serverSocket.accept();
				/*
				 * Al establecerse la conexión con un peer, la comunicación con dicho
				 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
				 * hay que pasarle el socket devuelto por accept
				 */
				System.out.println(
						"\nNew client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());
				NFServerComm.serveFilesToClient(socket);
			} catch (IOException ex) {
				System.out.println("Server exception: " + ex.getMessage());
				ex.printStackTrace();
			}

		}
	}
}
