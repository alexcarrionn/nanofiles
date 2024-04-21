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
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	
	

	public NFServer() throws IOException {
		/*
		 * TODO: Crear un socket servidor y ligarlo a cualquier puerto disponible
		 */
		InetSocketAddress serverSocketAddress = new InetSocketAddress(0); //le ponemos un 0 para que elija el primero disponible
		//para saber el puerto en el que esta escuchando lo ponemos por pantalla 
		System.out.println("NFSever puesto en el puerto " + serverSocketAddress.getPort());
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddress);
		//serverSocket.setSoTimeout(SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS);
		serverSocket.setReuseAddress(true);
		
	}

	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		/*
		 * TODO: Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */
		Socket socket = null; 
		while(true) {
			try {
				socket = serverSocket.accept();
				System.out.println("New client in the port : " + obtenerPuertoEscucha(socket));
				NFServerComm.serveFilesToClient(socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		/*
		 * TODO: (Opcional) Crear un hilo nuevo de la clase NFServerThread, que llevará
		 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
		 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
		 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
		 * cliente conectado, no podremos tener más de un cliente conectado a este
		 * servidor.
		 */
			
			
		Socket socket2 = null;
		while(true) {
			try {
				socket2 = serverSocket.accept();
				System.out.println("New client in the port : " + socket.getPort());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(socket2 != null) {
				NFServerThread hilo = new NFServerThread(socket2);
				hilo.start(); 
			}
			else {
				break; 
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
	
	@SuppressWarnings("deprecation")
	public void stopserver() {
		new Thread(this).stop();
	}
	
	public int obtenerPuertoEscucha(Socket serverSocketAddress) {
		return serverSocketAddress.getPort(); 
	}

}
