package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
//import java.io.EOFException;
//import java.io.File;
import java.io.IOException;
/*import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;*/

//import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {

	private byte opcode;
	private byte[] data = null;
	private String hashCode = "";
	private int longitudByte = 0;

	/*
	 * Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	
	public PeerMessage(byte op) {
		opcode = op; 
	}

	/*
	 * Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */

	
	public byte getOpcode() {
		return opcode;
	}

	public byte[] getData() {
		return data;
	}

	public String getHashCode() {
		return hashCode;
	}

	public int getLongitudByte() {
		return longitudByte;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}

	public void setLongitudByte(int longitudByte) {
		this.longitudByte = longitudByte;
	}

	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		
		byte opcode = dis.readByte();
		PeerMessage message = new PeerMessage(opcode);
		
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD_FROM:
			//cambia el campo longitud
			int longitud = dis.readInt();
			message.setLongitudByte(longitud);
			//lee la longitud y la pasa a array de bytes
			byte[] hash = new byte[message.getLongitudByte()];
			//lee el hash entero
			dis.readFully(hash);
			message.setHashCode(new String(hash, "UTF-8"));
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD_OK:
			message.setLongitudByte(dis.readInt());
			byte[] file= new byte[message.getLongitudByte()];
			dis.readFully(file);
			message.setData(file); 
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD_FAIL:
			//da un mensaje de error
			System.err.println("Fichero no encontrado");
			break;
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_DOWNLOAD_FROM:
			dos.writeInt(hashCode.getBytes().length);
			dos.writeBytes(hashCode);
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD_OK:
			dos.writeInt(longitudByte);
			dos.write(data);
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD_FAIL:
			System.err.println("Download fail");
			break;
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

}
