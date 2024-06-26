package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream dis; 
	private DataOutputStream dos; 
	
	public Socket getSocket() {
		return this.socket;
	}
	public DataInputStream getDis() {
		return dis;
	}
	
	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}
	
	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
			serverAddr = fserverAddr;
	        socket = new Socket(serverAddr.getAddress(),serverAddr.getPort());
	        if (socket.isConnected()) {
	            System.out.println("Conexión TCP establecida.");
	            dis = new DataInputStream(socket.getInputStream());
	            dos = new DataOutputStream(socket.getOutputStream());
	        } else {
	            throw new IOException("No se pudo establecer la conexión TCP.");
	        }
	}

	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
	    boolean downloaded = false;
	    try {
	        // Envía mensaje al servidor para solicitar la descarga del archivo
	        PeerMessage msgToServer = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FROM);
	        msgToServer.setHashCode(targetFileHashSubstr);
	        msgToServer.setLongitudByte((int)targetFileHashSubstr.getBytes().length);
	        msgToServer.writeMessageToOutputStream(dos);

	        // Recibe respuesta del servidor
	        PeerMessage msgFromServer = PeerMessage.readMessageFromInputStream(dis);
	        
	        // Verifica si la respuesta indica que se ha iniciado la descarga
	        if (msgFromServer.getOpcode() == PeerMessageOps.OPCODE_DOWNLOAD_OK) {
	            FileOutputStream fos = new FileOutputStream(file);
	            	//aqui data es null y da error
	            if (msgFromServer.getData() != null) {
	                byte[] data = msgFromServer.getData();
	                fos.write(data);
	                fos.close();
	                downloaded = true;
	            } else {
	                System.err.println("* El mensaje del servidor no contiene datos para descargar.");
	                downloaded = false;
	            }
	            // Verifica la integridad del archivo descargado
	            String newHash = FileDigest.computeFileChecksumString(file.getAbsolutePath());
	            if (!newHash.contains(targetFileHashSubstr)) {
	                System.err.println("* El archivo descargado está corrupto.");
	                downloaded = false;
	            }
	        } else {
	            System.err.println("* El servidor no ha iniciado la descarga del archivo.");
	            downloaded = false;
	        }
	    } catch (IOException e) {
	        System.err.println("* Error durante la descarga del archivo: " + e.getMessage());
	        throw e;
	    }

	    return downloaded;
	}
}
