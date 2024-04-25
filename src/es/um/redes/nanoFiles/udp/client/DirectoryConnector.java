package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;
	
	public final static int MAX_MSG_SIZE_BYTES = 32;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;
	private String username; 
	private FileInfo[] ficheros; 
	
	public DirectoryConnector(String address) throws IOException {

		InetAddress serverIp = InetAddress.getByName(address);
		this.directoryAddress = new InetSocketAddress(serverIp, DIRECTORY_PORT);
		socket = new DatagramSocket();
		System.out.println("Created UDP socket at local addresss " + socket.getLocalSocketAddress());


	}




	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * @throws IOException 
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) throws IOException {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}

		DatagramPacket packetToServer = new DatagramPacket(requestData, requestData.length, directoryAddress);
		socket.send(packetToServer);
		System.out.println("Sending message to server: " + new String(requestData) );
		
		DatagramPacket packetFromServer = new DatagramPacket(responseData,responseData.length);
		
		socket.setSoTimeout(TIMEOUT);
		
		for(int i = 0; i< MAX_NUMBER_OF_ATTEMPTS; i++) {
			try {
				socket.receive(packetFromServer);
				response = Arrays.copyOfRange(responseData, 0, packetFromServer.getLength());
				SocketAddress responseAddr = packetFromServer.getSocketAddress();
				System.out.println("Datagram received from server at addr " + responseAddr);
				break; 
		}catch (SocketTimeoutException e) {
			System.out.println("socket.receive() reachered TIMEOUT. " + (MAX_NUMBER_OF_ATTEMPTS-i) + " attemps remaining.");
			if( i== MAX_NUMBER_OF_ATTEMPTS) {
				System.err.println("the timeout is complete ");
				System.exit(-1); }
		}
			socket.send(packetToServer); 
			System.out.println("resending message to server");
		}

		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 * @throws IOException 
	 */
	public boolean testSendAndReceive() throws IOException {
		/*
		 * TODO: Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe
		 * enviar un datagrama con la cadena "login" y comprobar que la respuesta
		 * recibida es "loginok". En tal caso, devuelve verdadero, falso si la respuesta
		 * no contiene los datos esperados.
		 */
		
		boolean success = false;
		String dato = "login"; 
		String loginok = "loginok"; 
		byte[] mandarDato = dato.getBytes();
		if (Arrays.equals(sendAndReceiveDatagrams(mandarDato), loginok.getBytes())) {
			success = true;
		}
		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}
	
	public String getUsername() {
		return username;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 * @throws IOException 
	 */
	public boolean logIntoDirectory(String nickname) throws IOException {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		
		username = nickname;
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOGIN);
		m.setNickname(nickname);
		String messageLogin = m.toString();
		
		byte[] sendData = messageLogin.getBytes();
		
		byte[] response = sendAndReceiveDatagrams(sendData);
		DirMessage r = DirMessage.fromString(new String(response));
		
		boolean loginok = r.getLoginOk();
		int num = r.getSessionKey();
		
		if (loginok && num <= 10000) {
            System.out.println("SessionKey: " + num);
            sessionKey = num;
            success = true;
            return success;
        }
		
		System.err.println("Usuario ya registrado");
		return success;
	}
		
	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 * @throws IOException 
	 */
	public String[] getUserList() throws IOException {
		String[] userlist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_USER_LIST); 
		String messageUserList = m.toString(); 
		byte[] sendData = messageUserList.getBytes(); 
		byte[] response = sendAndReceiveDatagrams(sendData); 
		DirMessage r = DirMessage.fromString(new String(response));
		userlist = r.getUsers(); 
		return userlist;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 * @throws IOException 
	 */
	public boolean logoutFromDirectory() throws IOException {
		DirMessage m = new DirMessage(DirMessageOps.OPERATION_LOGIN_OUT);
		m.setNickname(this.getUsername());
		String messageLogOut = m.toString();
		byte[] sendData = messageLogOut.getBytes();
		
		byte[] response = sendAndReceiveDatagrams(sendData);
		DirMessage r = DirMessage.fromString(new String(response));
		
		boolean logout = r.isLogout();
		logout=true; 
		if (logout) {
			return true;
        }
		
		return false;
	}
		 

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
	    boolean success = false;
	    try {
	        // Crear un mensaje de solicitud de registro de servidor
	        DirMessage registerMessage = new DirMessage(DirMessageOps.OPERATION_PORT_REGISTER);
	        registerMessage.setServerPort(serverPort);
	        registerMessage.setSessionKey(sessionKey); // Añadir la clave de sesión para identificación
	        registerMessage.setNickname(username);
	        // Convertir el mensaje a una cadena de bytes
	        byte[] requestData = registerMessage.toString().getBytes();
	        // Enviar el mensaje al directorio y recibir la respuesta
	        byte[] responseData = sendAndReceiveDatagrams(requestData);
	        
	        // Convertir la respuesta a un objeto DirMessage
	        DirMessage responseMessage = DirMessage.fromString(new String(responseData));
	        int port = responseMessage.getServerPort(); 
	        // Verificar si la operación fue exitosa en la respuesta recibida
	        if(port != 0) {
	        	success = true;
	        }
	        
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return success;
	}


	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 * @throws IOException 
	 */
	public boolean publishLocalFiles(FileInfo[] files) throws IOException {
	    boolean success = false;
	    try {
	        // Crear un mensaje para publicar archivos
	        DirMessage message = new DirMessage(DirMessageOps.OPERATION_PUBLISH);
	        message.setFiles(files);
	        String messageString = message.toString();
	        
	        // Convertir el mensaje a bytes y enviarlo al servidor de directorio
	        byte[] sendData = messageString.getBytes();
	        byte[] responseData = sendAndReceiveDatagrams(sendData);
	        
	        // Interpretar la respuesta del servidor
	        if (responseData != null) {
	            DirMessage response = DirMessage.fromString(new String(responseData));
	            if (response != null && response.getOperation().equals(DirMessageOps.OPERATION_PUBLISH_RESPONSE)) {
	                // Verificar cualquier otro campo en la respuesta según sea necesario
	                // Establecer success en true si la publicación fue exitosa
	                success = true;
	            } else {
	                System.err.println("Error: Respuesta inesperada del servidor de directorio.");
	            }
	        } else {
	            // No se recibió respuesta del servidor
	            System.err.println("Error: No se recibió respuesta del servidor.");
	        }
	    } catch (IOException e) {
	        // Manejar cualquier excepción de E/S que pueda ocurrir durante la comunicación
	        System.err.println("Error de E/S durante la publicación de archivos: " + e.getMessage());
	        throw e; // Relanzar la excepción para que el llamador también pueda manejarla si es necesario
	    }
	    return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 * @throws IOException 
	 */
	public FileInfo[] getFileList() {
	    FileInfo[] filelist = null;
	    try {
	        DirMessage m = new DirMessage(DirMessageOps.OPERATION_FILE_LIST);
	        String messageFileList = m.toString();
	        byte[] sendData = messageFileList.getBytes();
	        byte[] response = sendAndReceiveDatagrams(sendData);
	        DirMessage r = DirMessage.fromString(new String(response));
	        filelist = r.getFiles();
	    } catch (IOException e) {
	        // Maneja la excepción de comunicación con el servidor
	        e.printStackTrace();
	    }
	    return filelist;
	}


	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public String[] getServerNicknamesSharingThisFile(String fileHash) {
		String[] nicklist = null;
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar



		return nicklist;
	}




}
