package es.um.redes.nanoFiles.tcp.message;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "ejercicio3";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		byte opcode = 1;
		String hola = "holacomoestasperro";
		byte len = (byte) hola.length();

		PeerMessage msgOut = new PeerMessage(opcode);
		msgOut.setHashCode(hola);
		msgOut.setLongitudByte(len);//System.out.println(msgOut.toString());
		msgOut.writeMessageToOutputStream(fos);
		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		
		
		/*
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 */
		 if (msgOut.getOpcode() != msgIn.getOpcode()) {
		        System.err.println("Opcode does not match!");}
		 else {
			 System.out.println(msgIn.getOpcode()+msgIn.getLongitudByte());
		 }
		
	}

}

