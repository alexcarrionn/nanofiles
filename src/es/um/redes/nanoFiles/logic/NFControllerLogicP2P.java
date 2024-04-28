package es.um.redes.nanoFiles.logic;

import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
//import java.util.Random;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.tcp.server.NFServerSimple;
//import es.um.redes.nanoFiles.tcp.server.NFServerSimple;

public class NFControllerLogicP2P {
	/*
	 * Para bgserve, se necesita un atributo NFServer que actuará como
	 * servidor de ficheros en segundo plano de este peer
	 */
	
	NFServerSimple serversimple; 
	NFServer server;  
	
	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para arrancar un servidor de ficheros en primer plano.
	 * @throws IOException 
	 * 
	 */
	protected void foregroundServeFiles() {
		try {
			//Para hacer el fgserve lo hemos hecho a traves de NFSever Simple 
			serversimple = new NFServerSimple(); 
			//runeamos el serversimple
			serversimple.run();
		} catch (IOException e) {
			System.err.println("* Excepcion de entrada/salida " + e.getMessage());
			e.printStackTrace();
		} 
		
		/*
		 * Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		
		

	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean backgroundServeFiles() {
		//para el bgserve lo primero que hacemos es comprobar que solo haya uno en ejecucion 
		if(server == null) {
			try {
				//una vez que hemos visto que es null, lo creamos
				server = new NFServer(); 
			} catch (IOException e) {
				System.err.println("* Excepcion de entrada/salida " + e.getMessage());
				e.printStackTrace();
			}
			//y lo runeamos a partir de hilos
			server.startServer(); 
			return true;  
		}
		else {
			System.err.println("* Ya hay un servidor activo");
		}

		return false;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param fserverAddr    La dirección del servidor al que se conectará
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	protected boolean downloadFileFromSingleServer(InetSocketAddress fserverAddr, String targetFileHash,
	        String localFileName) throws UnknownHostException, IOException {
	    boolean result = false;
	    //comprobamos que haya algun servidor
	    if (fserverAddr == null) {
	        System.err.println("* Cannot start download - No server address provided");
	        return false;
	    }
	    String folderPath = "nf-shared";
	    File downloadFile = new File(folderPath, localFileName);

	    // Verificar si ya existe un archivo con el mismo nombre
	    if (downloadFile.exists()) {
	        System.err.println("* File with the same name already exists: " + localFileName);
	        return false;
	    }

	    NFConnector connector = new NFConnector(fserverAddr);
	        // Intentar descargar el archivo
	        if (connector.downloadFile(targetFileHash, downloadFile)) {
	            result = true;
	            System.out.println("* File downloaded successfully: " + localFileName);
	        } else {
	            System.err.println("* Failed to download file: " + localFileName);
	        }
	    return result;
	}


	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList La lista de direcciones de los servidores a los que
	 *                          se conectará
	 * @param targetFileHash    Hash completo del fichero a descargar
	 * @param localFileName     Nombre con el que se guardará el fichero descargado
	 */
	public boolean downloadFileFromMultipleServers(LinkedList<InetSocketAddress> serverAddressList,
			String targetFileHash, String localFileName) {
		boolean downloaded = false;

		if (serverAddressList == null) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con cada
		 * servidor de ficheros, y usarlo para descargar un trozo (chunk) del fichero
		 * mediante su método "downloadFileChunk". Se debe comprobar previamente si ya
		 * existe un fichero con el mismo nombre en esta máquina, en cuyo caso se
		 * informa y no se realiza la descarga. Si todo va bien, imprimir mensaje
		 * informando de que se ha completado la descarga.
		 */
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */



		return downloaded;
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros en
	 * segundo plano
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	public int getServerPort() {
		int port = 0;
		/*
		 * Devuelve el puerto de escucha de nuestro servidor de ficheros en
		 * segundo plano
		 */
		port = server.getPort();  
		return port;
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	public void stopBackgroundFileServer() {
		/*
		 * Envia una señal para detener nuestro servidor de ficheros en segundo plano
		 */
		
		server.stopserver();
		server = null; 
	}

}
