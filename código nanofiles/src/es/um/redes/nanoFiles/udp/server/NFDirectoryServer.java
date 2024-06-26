package es.um.redes.nanoFiles.udp.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;


public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;
	//longitud del buffer
	private static final int MAX_MSG_SIZE_BYTES = 128;


	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks;
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys;
	/*
	 * Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	private HashMap<String, InetSocketAddress> users;
	private HashMap<String, InetSocketAddress> fileServers;
	
	
	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		setMessageDiscardProbability(corruptionProbability);
		/*
		 * Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */
		
		this.socket = new DatagramSocket(DIRECTORY_PORT);
		System.out.println("Server listening on socket addresss " + socket.getLocalSocketAddress());
		
		/*
		 * Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		nicks = new HashMap<>(); 
		sessionKeys = new HashMap<>();
		users = new HashMap<>(); 
		fileServers = new HashMap<>(); 
		
		if (NanoFiles.testMode) {
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		/*
		 * Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		byte[] receptionBuffer = new byte[MAX_MSG_SIZE_BYTES];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);




		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// Recibimos a través del socket un datagrama
			socket.receive(packetFromClient);
			//Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = packetFromClient.getLength();
			// Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del
			// datagrama recibido
			clientAddr = (InetSocketAddress) packetFromClient.getSocketAddress();




			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String messageFromClient = null;
				
				messageFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());


				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");

				}else {
					DirMessage op = DirMessage.fromString(messageFromClient);
					/*
					 * Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					DirMessage response = buildResponseFromRequest(op, clientAddr);
					
					/*
					 * Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					String dirMessage =  response.toString();
					byte[] sendData = dirMessage .getBytes();
					DatagramPacket packetToClient = new DatagramPacket (sendData, sendData.length, clientAddr);
					
					socket.send(packetToClient);
					System.out.println("Sending message to client " + new String(sendData));
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		
		String operation = msg.getOperation();
		DirMessage response = null;
		
		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();

			if(!nicks.containsKey(username)) {
				int sessionKey = random.nextInt(10000);
				nicks.put(username, sessionKey);
				users.put(username, clientAddr); 
				sessionKeys.put(sessionKey, username); 
				response= new DirMessage(DirMessageOps.OPERATION_LOGIN );
				response.setSessionKey(sessionKey); 
				response.setLoginOk(true); 
			}
			else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGIN ); 
				response.setLoginOk(false);
			}
			break; 
			 
		}


		case DirMessageOps.OPERATION_LOGIN_OUT:{
			String username = msg.getNickname();
			int sessionKey = nicks.get(username);
			nicks.remove(username);
			sessionKeys.remove(sessionKey);
			users.remove(username); 
			
			response = new DirMessage(DirMessageOps.OPERATION_LOGIN_OUT);
			response.setLogout(true);
			
			break; 
		}
		
		case DirMessageOps.OPERATION_USER_LIST:{ 
			response = new DirMessage(DirMessageOps.OPERATION_USER_LIST);
			String[] keysArray = nicks.keySet().toArray(new String[0]); 
			String[] filesServer = fileServers.keySet().toArray(new String[0]);
			response.setUsers(keysArray);
			response.setFilesServer(filesServer); 
			break; 
		}
		case DirMessageOps.OPERATION_FILE_LIST: {
		    response = new DirMessage(DirMessageOps.OPERATION_FILE_LIST);
		    String sharedFolderPath = new File(NanoFiles.sharedDirname).getAbsolutePath();
		    File sharedFolder = new File(sharedFolderPath);
		    FileInfo[] files = null;
		    if (sharedFolder.exists() && sharedFolder.isDirectory()) {
		        files = FileInfo.loadFilesFromFolder(sharedFolderPath);
		        response.setFiles(files);
		    }
		    break;
		}

		case DirMessageOps.OPERATION_PUBLISH: {
		    // Obtener la ruta completa de la carpeta compartida
		    String sharedFolderPath = NanoFiles.sharedDirname; 
		    // Obtener la lista de archivos en la carpeta compartida en nuestro caso nf-shared
		    File sharedFolder = new File(sharedFolderPath);
		    FileInfo[] files = null;
		    if (sharedFolder.exists() && sharedFolder.isDirectory()) {
		        files = FileInfo.loadFilesFromFolder(sharedFolderPath);
		    } else {
		        System.err.println("* La carpeta compartida no existe o no es una carpeta válida.");
		    }

		    if (files != null && files.length > 0) {
		        System.out.println("* Archivos publicados:");
		        for (FileInfo file : files) {
		            System.out.println("*- " + file);
		        }
		    } else {
		        System.out.println("* No se han encontrado archivos para publicar en la carpeta compartida.");
		    }

		    // Construir un mensaje de respuesta indicando si la publicación fue exitosa
		    response = new DirMessage(DirMessageOps.OPERATION_PUBLISH_RESPONSE);
		    // Aquí lo que se hace es poner la respuesta del Publish a true en el caso en el que encuentre algún fichero compatible
		    response.setPublishResponse(files != null && files.length > 0); 
		    break;
		}
		
		case DirMessageOps.OPERATION_REGISTER_FILE_SERVER:{
			System.out.println("* Solicitud de Servidores de ficheros enviada por "+ clientAddr);
			response = new DirMessage(DirMessageOps.OPERATION_REGISTER_FILE_SERVER);
			
			String username = msg.getNickname();
			int port = msg.getPort();
			InetSocketAddress server = new InetSocketAddress(clientAddr.getAddress(), port);
			if(!fileServers.containsKey(username)) {
				fileServers.put(username, server); 
				response.setRegisterOk(true);
			}
			break; 
			
		}
		
		case DirMessageOps.OPERATION_UNREGISTER_SERVER:{
			System.out.println("* Solicitud de cerrar servidor de ficheros enviada por: " + clientAddr);
			response = new DirMessage(DirMessageOps.OPERATION_UNREGISTER_SERVER); 
			String username = msg.getNickname(); 
			if(fileServers.containsKey(username)) {
				fileServers.remove(username); 
				response.setUnregisterOk(true);
			}
			break; 
		}
		
		case DirMessageOps.OPERATION_REQUEST_IP:{
			System.out.println("* solicitud de Ip");
			response = new DirMessage(DirMessageOps.OPERATION_REQUEST_IP); 
			String username = msg.getNickname();

			InetSocketAddress address = fileServers.getOrDefault(username, null);
			if(address != null) {
				response.setIprequest(address.getAddress());
				response.setPort(address.getPort());
			}
			break; 
		}
		
		

        
		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
		}
		return response;

	}

	public double getMessageDiscardProbability() {
		return messageDiscardProbability;
	}

	public void setMessageDiscardProbability(double messageDiscardProbability) {
		this.messageDiscardProbability = messageDiscardProbability;
	}
}
