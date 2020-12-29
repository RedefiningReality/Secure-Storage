package ss.main;

import java.io.IOException;

import ss.server.Server;
import ss.utils.logging.MessageLevels;

public class StorageServer {

	public static void main(String[] args) throws IOException {
		Server server = new Server(true, MessageLevels.DATA);
		server.start();
	}

}