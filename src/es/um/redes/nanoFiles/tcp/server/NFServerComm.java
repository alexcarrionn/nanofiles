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
			PeerMessage mensajeCliente = PeerMessage.readMessageFromInputStream(dis);
			byte opCode = mensajeCliente.getOpcode();

			// de momento solo se puede el download_from
			switch (opCode) {
			case PeerMessageOps.OPCODE_DOWNLOAD_FROM: {
				// cogemos los ficheros disponobles
				FileInfo[] ficheros = NanoFiles.db.getFiles();
				// cogemos el HashCode que queremos
				String subHashcode = mensajeCliente.getHashCode();
				// Ficheros encontrados con ese hashcode
				FileInfo[] ficherosEncontrados = FileInfo.lookupHashSubstring(ficheros, subHashcode);

				if (ficherosEncontrados.length != 1) {
					System.err.println("* Mas de un fichero encontrado con el fragmento de hash " + subHashcode);
				} else {
					//cogemos los ficheros encontrados, el primero
					FileInfo file = ficherosEncontrados[0];
					//cogemos el filePath
					String filePath = file.filePath;
					//Hacemos un nuevo archivo con ese Path que hemos cogido
					File archivo = new File(filePath);
					
					
					PeerMessage mensajeEnviar = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_OK);
					DataInputStream fis = new DataInputStream(new FileInputStream(archivo));
					byte[] archivodata = new byte[(int) archivo.length()];
					fis.readFully(archivodata);
					mensajeEnviar.setLongitudByte(archivodata.length);
					mensajeEnviar.setData(archivodata);
					mensajeEnviar.writeMessageToOutputStream(dos);
					fis.close(); 

				}
				dis.close();
				dos.close();
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + opCode);
			}
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}

	}
}
