package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) throws IOException {
			/*
			 * TODO: Crear dis/dos a partir del socket
			 */
		
			
			DataInputStream dis = new DataInputStream(socket.getInputStream()); 
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	
			/*
			 * TODO: Mientras el cliente esté conectado, leer mensajes de socket,
			 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
			 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
			 */
			
				try {
				PeerMessage mensajeCliente =PeerMessage.readMessageFromInputStream(dis);
				int opCode = mensajeCliente.getOpcode(); 
				switch(opCode) {
				case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
					FileInfo[] ficheros = NanoFiles.db.getFiles();
					String subHashcode = mensajeCliente.getHashCode();
					FileInfo[] ficherosEncontrados = FileInfo.lookupHashSubstring(ficheros, subHashcode);
				    
					if (ficherosEncontrados.length != 1) {
						System.err.println("Mas de un fichero encontrado con el fragmento de hash "+ subHashcode);
					}else {
						FileInfo file = ficherosEncontrados[0];
						String hash= file.fileHash;
						String filepath = NanoFiles.db.lookupFilePath(hash);
						File archivo = new File(filepath);
						if(archivo.exists()) {
							PeerMessage mensajeEnviar = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_OK);
							DataInputStream fis = new DataInputStream(new FileInputStream(archivo));
							byte[] archivodata=new byte[(int)archivo.length()];
							fis.readFully(archivodata);
							fis.close();
							mensajeEnviar.setLongitudByte(archivodata.length);
							mensajeEnviar.setData(archivodata);
							mensajeEnviar.writeMessageToOutputStream(dos);
							
						}
					}
					dis.close();
					dos.close();
				break;
				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + opCode);
				}
			} catch(IOException ex) {
				System.out.println("Server exception: " + ex.getMessage());
				ex.printStackTrace();
			}
		
	}
}

