package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
//import java.net.InetSocketAddress;
import java.net.UnknownHostException;
/*import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;*/
import java.util.Arrays;
/*import java.util.Collections;
import java.util.LinkedList;
import java.util.List;*/

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

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	
	/*
	 * Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_LOGINOK = "loginok";
	private static final String FIELDNAME_SESSIONKEY = "sessionkey";
	private static final String FIELDNAME_USERLIST = "userlist";
	private static final String FIELDNAME_FILELIST = "filelist";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_LOGING_OUT = "logout";
	private static final String FIELDNAME_PUBLISH_RESPONSE = "publishresponse";
	private static final String FIELDNAME_PUBLISH = "publish";
	private static final String FIELDNAME_REGISTEROK = "registerok";
	private static final String FIELDNAME_UNREGISTEROK = "unregisterok";
	private static final String FIELDNAME_FILE_SERVERS = "fileservers";
	private static final String FIELDNAME_REQUEST_IP = "request_ip";

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String nickname;
	private String[] users;
	private String[] filesServer; 
	private FileInfo[] files;
	private int sessionKey;
	private int port;
	private boolean loginok;
	private boolean logout;
	private boolean publishResponse;
	private boolean unregisterOk; 
	private boolean registerOk; 
	private InetAddress iprequest;
	
	//atributos del mensaje con el getUser una lista, aqui poner una lista 

	public DirMessage(String op) {
		operation = op;
		users = new String[0];
		files = new FileInfo[0]; 
	}
	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	

	public String getOperation() {
		return operation;
	}

	public String[] getFilesServer() {
		return filesServer;
	}

	public void setFilesServer(String[] filesServer) {
		this.filesServer = filesServer;
	}

	public boolean isRegisterOk() {
		return registerOk;
	}



	public void setRegisterOk(boolean registerOk) {
		this.registerOk = registerOk;
	}



	public boolean isUnregisterOk() {
		return unregisterOk;
	}



	public void setUnregisterOk(boolean _unregisterOk) {
		unregisterOk = _unregisterOk;
	}



	public void setNickname(String nick) {
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
	
	public String[] getUsers() {
		return users;
	}

	public void setUsers(String[] userlist) {
		this.users = userlist;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLogout() {
		return logout;
	}

	public void setLogout(boolean logout) {
		this.logout = logout;
	}

	public FileInfo[] getFiles() {
		return files;
	}

	public void setFiles(FileInfo[] files) {
		this.files = files;
	}
	
	public String[] getUserServer() {
		return filesServer;
	}
	
	public void setUserServer(String[] userServer) {
		this.filesServer = userServer;
	}

	public boolean isPublishResponse() {
        return publishResponse;
    }

    public void setPublishResponse(boolean publishResponse) {
        this.publishResponse = publishResponse;
    }
    
	public InetAddress getIprequest() {
		return iprequest;
	}


	public void setIprequest(InetAddress iprequest) {
		this.iprequest = iprequest;
	}


	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 * @throws UnknownHostException 
	 */
	public static DirMessage fromString(String message) throws UnknownHostException {
		/*
		 * Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */
		
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
				value = value.substring(1, value.length()-1); 
				m.setUsers(value.split(", ")); 
				break;
			}
			case FIELDNAME_PORT: {
				m.setPort(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_FILELIST:{
			    assert (m.getOperation().equals(DirMessageOps.OPERATION_FILE_LIST));
			    // Divide la cadena de archivos en partes separadas por el delimitador
			    String[] fileInfos = value.split("; ");
			    // Crea un array de FileInfo para almacenar los detalles de los archivos
			    FileInfo[] files = new FileInfo[fileInfos.length];
			    // Para cada cadena de archivo, divide los detalles en partes separadas por ", "
			    for (int i = 0; i < fileInfos.length; i++) {
			        String[] fileInfoParts = fileInfos[i].split(", ");
			        // El primer elemento es el nombre del archivo
			        String name = fileInfoParts[0];
			        // Si hay más elementos, el segundo es el tamaño y el tercero es el hash
			        long size = 0;
			        String hash = "";
			        if (fileInfoParts.length > 1) {
			            // Extrae el tamaño y el hash si están disponibles
			            size = Long.parseLong(fileInfoParts[1].replace(" bytes", ""));
			            hash = fileInfoParts[2];
			        }
			        // Crea un nuevo objeto FileInfo con el nombre y los detalles opcionales
			        files[i] = new FileInfo(hash,name, size, " ");
			    }
			    m.setFiles(files);
			    
			    break; 
			}
			
			case FIELDNAME_PUBLISH: {
			    assert (m.getOperation().equals(DirMessageOps.OPERATION_PUBLISH));
			    m.setFiles(FileInfo.loadFilesFromFolder(value));
			    break;
			}

            case FIELDNAME_PUBLISH_RESPONSE: {
                assert (m.getOperation().equals(DirMessageOps.OPERATION_PUBLISH_RESPONSE));
                m.setPublishResponse(Boolean.parseBoolean(value));
                break;
            }
            
            case FIELDNAME_REGISTEROK:{
            	m.setRegisterOk(Boolean.parseBoolean(value));
            	break;
            }
            case FIELDNAME_UNREGISTEROK: {
                m.setUnregisterOk(Boolean.parseBoolean(value));
                break;
            }
            case FIELDNAME_FILE_SERVERS: {
				// Eliminar los corchetes al principio y al final del string
		        value = value.substring(1, value.length() - 1);
		        // Dividir el string en un array de strings usando la coma como delimitador
		        m.setFilesServer(value.split(", "));
		        break;
			}
            case FIELDNAME_REQUEST_IP: {
            	value = value.substring(1, value.length());
            	m.setIprequest(InetAddress.getByName(value));
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
				if(loginok==true) {
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				}
				sb.append(FIELDNAME_LOGINOK + DELIMITER + loginok + END_LINE);
			}break;
		}
		case DirMessageOps.OPERATION_USER_LIST: {
			if(users.length != 0 && !(users.length <0)) {
				sb.append(FIELDNAME_USERLIST + DELIMITER + Arrays.toString(users) + END_LINE);
				sb.append(FIELDNAME_FILE_SERVERS + DELIMITER + Arrays.toString(filesServer) + END_LINE);
			} else {
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_FGSERVE: {
			sb.append(FIELDNAME_USERLIST + DELIMITER + port + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGIN_OUT: {
			if(nickname != null) {
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE); 
			}else {
				sb.append(FIELDNAME_LOGING_OUT + DELIMITER + logout + END_LINE);
			}
			break;}
		case DirMessageOps.OPERATION_FILE_LIST: {
		    if (files.length > 0) {
		        // Construye una cadena con la representación de los archivos
		        StringBuilder filesString = new StringBuilder();
		        for (FileInfo file : files) {
		            filesString.append(file.fileName).append(", "); // Nombre del archivo
		            filesString.append(file.fileSize).append(" bytes, "); // Tamaño del archivo
		            filesString.append(file.fileHash).append("; "); // Hash del archivo
		        }
		        // Agrega los detalles de los archivos al mensaje
		        sb.append(FIELDNAME_FILELIST + DELIMITER + filesString.toString() + END_LINE);
		    }
		    break;
		}
		
		case DirMessageOps.OPERATION_PUBLISH_RESPONSE: {
			//Le devuelve una respuesta en funcion de si ha sido o no un exito la publicacion de ficheros
            sb.append(FIELDNAME_PUBLISH_RESPONSE+ DELIMITER + publishResponse + END_LINE);
            break;
        }
		
		case DirMessageOps.OPERATION_REGISTER_FILE_SERVER: {
			// Si es un response, la variable puerto no estará inicializada y será 0
			if (port != 0) {
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			} else {
				sb.append(FIELDNAME_REGISTEROK + DELIMITER +  registerOk + END_LINE);
			}
			break;
		}
		
		case DirMessageOps.OPERATION_UNREGISTER_SERVER: {
			// Si es un response, la variable nickname no estará inicializada
			if (nickname != null) {
				//pongo en el buffer nickname y sessionKey
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
				sb.append(FIELDNAME_SESSIONKEY + DELIMITER + sessionKey + END_LINE);
			} else {
				//pongo en el buffer unregisterOk
				sb.append(FIELDNAME_UNREGISTEROK + DELIMITER + unregisterOk + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_REQUEST_IP: {
			// Si es un response, la variable nickname no estará inicializada
			if (nickname != null) {
				//se pone el nickname
				sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			} else {
				//pongo el iprequest y el puerto
				sb.append(FIELDNAME_REQUEST_IP + DELIMITER + iprequest.toString() + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			}
			break;
		}
				
		}
	    sb.append(END_LINE); 
	    return sb.toString();
	}
}
