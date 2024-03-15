package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
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
	private static final int MAX_MSG_SIZE_BYTES = 32;


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
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */

	private HashMap<String, InetSocketAddress> clientsAddresses;


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
		messageDiscardProbability = corruptionProbability;
		/*
		 * TODO: (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP
		 * ligado al puerto especificado por el argumento directoryPort en la máquina
		 * local,
		 */
		
		this.socket = new DatagramSocket(DIRECTORY_PORT);
		System.out.println("Server listening on socket addresss " + socket.getLocalSocketAddress());
		
		/*
		 * TODO: (Boletín UDP) Inicializar el resto de atributos de esta clase
		 * (estructuras de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		nicks = new HashMap<>(); 
		sessionKeys = new HashMap<>(); 
		

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
		 * TODO: (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		byte[] receptionBuffer = new byte[MAX_MSG_SIZE_BYTES];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);




		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// TODO: (Boletín UDP) Recibimos a través del socket un datagrama
			socket.receive(packetFromClient);
			// TODO: (Boletín UDP) Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = packetFromClient.getLength();
			// TODO: (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
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
				/*
				 * TODO: (Boletín UDP) Construir una cadena a partir de los datos recibidos en
				 * el buffer de recepción
				 */
				messageFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());


				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ messageFromClient + "\"");
					/*
					 * TODO: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					/*if (messageFromClient.equals("login")) {
						int  NUM = random.nextInt(10000);
						String data = "loginok&"+NUM;
						byte[] sendData =  data.getBytes();
						DatagramPacket packetLoginOk = new DatagramPacket ( sendData, sendData.length, clientAddr);
						socket.send(packetLoginOk);
						System.out.println(
								"Sending datagram with message \"" + new String(sendData) + "\"");
					}
					else {
						System.err.println("La cadena recibida no es \"login\"");
					}


				} else {
				// Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					/*
					 * double rand = Math.random(); if (rand < messageDiscardProbability) {
					 * System.err.println("Directory DISCARDED datagram from " + clientAddr);
					 * continue; }
					 */
					
					/*String user = new String(messageFromClient).split("&")[1];
					
					//Comprobar que mensaje tiene cadena login y usuario esta formado al menos por un caracter
					if(new String(messageFromClient).split("&")[0].equals("login") && user.matches("[a-zA-Z0-9_]+")) {
						System.out.println("User " + user + " is trying to log into directory...");
						
						if(!nicks.containsKey(user)) {
							int sessionKey = random.nextInt(1000);
							System.out.println("User "+ user + "succesfully logged into directory with session key " + sessionKey);
							nicks.put(user, sessionKey);
							
							byte[] sendData = ("loginok&" + sessionKey).getBytes();
							DatagramPacket packetLoginOk = new DatagramPacket(sendData, sendData.length, clientAddr);
							socket.send(packetLoginOk);
						
							System.out.println("Sending message to client: \"" + new String(sendData) + "\"");
					
						}else {
							byte[] sendData = ("login_failed:-1").getBytes();
							DatagramPacket packetLoginFailed = new DatagramPacket(sendData, sendData.length, clientAddr);
							socket.send(packetLoginFailed);
						}
					}else {
						System.err.println("Expected to receive \"login&user\"");
						
					}
					
					if (messageFromClient.equals("login&alumno")) {
						int  NUM = random.nextInt(10000);
						String data = "loginok&"+NUM;
						byte[] sendData =  data.getBytes();
						DatagramPacket packetLoginOk = new DatagramPacket ( sendData, sendData.length, clientAddr);
						socket.send(packetLoginOk);
						System.out.println(
								"Sending datagram with message \"" + new String(sendData) + "\"");
					}
					else {
						System.err.println("La cadena recibida no es \"login\"");
					}
					/*
					 * TODO: Construir String partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					//System.out.println("La cadena recibida es \""+ messageFromClient +"\"");
				}else {
					DirMessage op = DirMessage.fromString(messageFromClient);
					/*
					 * TODO: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					DirMessage response = buildResponseFromRequest(op, clientAddr);
					
					/*
					 * TODO: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */
					String dirMessage =  response.toString();
					byte[] sendData = dirMessage .getBytes();
					DatagramPacket packetToClient = new DatagramPacket (sendData, sendData.length, clientAddr);
					
					socket.send(packetToClient);
					System.out.println("Sending message to client \"" + new String(sendData) + "\"");
				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * TODO: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();
		DirMessage response = null;




		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();

			/*
			 * TODO: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
			 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
			 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(10000)
			 * para generar la session key
			 */
			/*
			 * TODO: Construimos un mensaje de respuesta que indique el éxito/fracaso del
			 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
			 * resultado del método.
			 */
			if(!nicks.containsKey(username)) {
				int sessionKey = random.nextInt(10000);
				nicks.put(username, sessionKey); 
				sessionKeys.put(sessionKey, username); 
				response= new DirMessage(DirMessageOps.OPERATION_LOGIN );
				response.setSessionKey(sessionKey); 
				response.setLoginOk(true); 
			}
			else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGIN ); 
				response.setLoginOk(false);
			}
			
			/*
			 * TODO: Imprimimos por pantalla el resultado de procesar la petición recibida
			 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
			 * servidor
			 */
			/*System.out.println("Response to the operation login sent to: "+clientAddr);
			System.out.println("The message is: "+ response.toString());
			return response; */
			break; 
			 
		}


		case DirMessageOps.OPERATION_LOGIN_OUT:{
			String username = msg.getNickname();
			int sessionKey = nicks.get(username);
			nicks.remove(username);
			sessionKeys.remove(sessionKey);
			
			response = new DirMessage(DirMessageOps.OPERATION_LOGIN_OUT);
			response.setLogout(true);
			
			break; 
		}
		
		case DirMessageOps.OPERATION_USER_LIST:{ 
			response = new DirMessage(DirMessageOps.OPERATION_USER_LIST);
			String[] keysArray = nicks.keySet().toArray(new String[0]); 
			response.setUsers(keysArray); 
			break; 
		}
			

		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
		}
		return response;

	}
}
