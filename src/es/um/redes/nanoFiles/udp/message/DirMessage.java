package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea
	/*
	private static final String SUCCESS = "succes";
	private static final String FAIL = "fail";
	*/
	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_LOGINOK = "loginok";
	private static final String FIELDNAME_SESSIONKEY = "sessionkey";
	private static final String FIELDNAME_USERLIST = "userlist";
	//private static final String FIELDNAME_FILELIST = "filelist";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_LOGING_OUT = "logout";
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private List<String> users;
	private int sessionKey;
	private int port;
	//private boolean isServer;
	//private String files;
	private boolean loginok;
	private boolean logout;
	//atributos del mensaje con el getUser una lista, aqui poner una lista 



	public boolean isLogout() {
		return logout;
	}

	public void setLogout(boolean logout) {
		this.logout = logout;
	}

	public DirMessage(String op) {
		operation = op;
	}




	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {
	//comprobar que es para un mensaje por ejemplo un login y que no haga nada para otros que no utilicen el nickname
	nickname = nick;
	}

	public String getNickname() {
		return nickname;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}
	public boolean getLoginOk() {
		return loginok;
	}

	public void setLoginOk(boolean loginok) {
		this.loginok = loginok;
	}
	
	public List<String> getUsers() {
		return Collections.unmodifiableList(users);
	}

	public void setUsers(List<String> userlist) {
		this.users = userlist;
	}

	public String getPort(String port) {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}



	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */

		System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;



		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_NICKNAME: {
				assert (m.getOperation().equals(DirMessageOps.OPERATION_LOGIN));
				m.setNickname(value);
				break;
			}
			case FIELDNAME_LOGINOK: {
				assert (m.getOperation().equals(DirMessageOps.OPERATION_LOGIN));
				// Castea la cadena "true" de la linea "loginok:true" y la 
				// convierte a boolean
				m.setLoginOk(Boolean.parseBoolean(value));
				break;
			}
			case FIELDNAME_LOGING_OUT: {
				assert (m.getOperation().equals(DirMessageOps.OPERATION_LOGIN_OUT));
				// Castea la cadena "true" de la linea "loginok:true" y la 
				// convierte a boolean
				m.setLoginOk(Boolean.parseBoolean(value));
				break;
			}
			case FIELDNAME_SESSIONKEY: {
				assert (m.getOperation().equals(DirMessageOps.OPERATION_LOGIN));
				m.setSessionKey(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_USERLIST: {
				assert (m.getOperation().equals(DirMessageOps.OPERATION_USER_LIST));
				String[] usuarios = value.split(String.valueOf(DELIMITER));
				List<String> users = Arrays.asList(usuarios);
				m.setUsers(users);
				break;
			}
			case FIELDNAME_PORT: {
				assert (m.getOperation().equals(DirMessageOps.OPERATION_FGSERVE));
				m.setPort(Integer.parseInt(value));
				break;
			}
			
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}




		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE);
	    // Construimos el campo de operación
	    switch (operation) {
		
		case DirMessageOps.OPERATION_LOGIN: {
			//Si es una request, tendrá el campo de nickname que quiera iniciar sesion 
			if(nickname != null) {
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);}
			else {
				if(loginok ==true) {
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				}
				sb.append(FIELDNAME_LOGINOK + DELIMITER + loginok + END_LINE);
			}break;
		}
		case DirMessageOps.OPERATION_USER_LIST: {
			String userslist = String.join(", ", users); 
			sb.append(FIELDNAME_USERLIST + DELIMITER + userslist + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_FGSERVE: {
			sb.append(FIELDNAME_USERLIST + DELIMITER + port + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGIN_OUT: {
			//Si es una request, tendrá el campo de nickname que quiera iniciar sesion 
			if(nickname != null) {
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE); 
			}
			sb.append(FIELDNAME_LOGING_OUT + DELIMITER + logout + END_LINE);
			}
		break;
			
		}
	    sb.append(END_LINE); // Marcamos el final del mensaje
	    return sb.toString();
	}
}
