package ss.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import ss.dependencies.Master;
import ss.dependencies.Slave;
import ss.utils.logging.Logger;
import ss.utils.logging.MessageLevels;

public class ServerThread extends Thread {
	
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private Logger logger;
	
	public ServerThread(DataInputStream dis, DataOutputStream dos)
	{
		this.dis = dis;
		this.dos = dos;
		
		this.logger = new Logger("ServerThread");
	}
	
	public void run()
	{
		try {
			logger.printMessage("Requesting operation from client", MessageLevels.INFO);
			dos.writeUTF("Please provide your operation of choice"); 
			String received = dis.readUTF();
			logger.printMessage("Received operation", MessageLevels.INFO);
			logger.printMessage("Operation: " + received, MessageLevels.DATA);
			
			switch(received.toLowerCase()) {
				case "download":
					Master.download(dis, dos);
					break;
				case "upload master":
					Master.upload(dis, dos);
					break;
				case "upload slave":
					Slave.upload(dis, dos);
					break;
				case "rm master":
					Master.rm(dis, dos);
					break;
				case "rm slave":
					Slave.rm(dis, dos);
					break;
				case "ls":
					Master.ls(dis, dos);
					break;
				default:
					logger.printMessage("Operation invalid. Notifying client", MessageLevels.WARNING);
					dos.writeUTF("Invalid input");
			}
			
			logger.printMessage("Terminating connection with client", MessageLevels.INFO);
			this.dis.close();
			this.dos.close();
			logger.printMessage("Terminated connection", MessageLevels.INFO);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}