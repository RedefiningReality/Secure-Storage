# Secure-Storage
Command line utility to securely transfer files from web server to two storage servers (master and slave) and securely store files on storage servers using AES-256 encryption  

The network is intended to be comprised of three servers:
- two storage servers (not connected to internet)
- one web server (connected to internet)  

All machines should be running a Linux/Unix Operating System. The jar files were additionally tested on Windows, but the setup script is targeted towards Linux.
In the event of failure of one storage server, the other will perform all the same tasks without any issues  

### Communication

Commands are sent in the following manner:
1. The web server sends the command to one of the storage servers, which acts as the master
2. The master storage server performs the command and sends the result back to the web server
3. For upload and remove commands, the master storage server forwards the command to the other storage server, which acts as the slave
4. The slave storage server performs the command forwarded to it by the master storage server and confirms successful execution  

### Encryption

Files are transferred using an original encrypted protocol that consists of the following steps:
1. Creating a new randomly-generated AES-256 symmetric key and IV
2. Sending the AES-256 symmetric key encrypted with the recipient's public key
3. Sending a hash of the AES-256 symmetric key encrypted with the sender's private key
4. Sending the AES-256 IV in the same way the symmetric key was sent
5. Sending the file encrypted with AES-256 symmetric encryption  

As well as being transferred using AES encryption, files are stored on storage servers in full using AES-256 encryption, and the original plaintext file is deleted after encryption.
All RSA keys as well as the keys for the AES-256 encryption of files stored on the storage server are located under ```/opt/secure_storage/keys```  

### Additional Features

Both the server and the client are configurable by editing the ```/opt/secure_storage/config.properties``` file. This configuration file contains the names of all the private/public keys, the base storage and temporary directory paths, and the IPs and ports of other machines to connect to. Both the server and the client additionally log all steps of every transfer to the ```/opt/secure_storage/securestore.log``` file to ease debugging in the event of an error.
Operations supported on the web server are list files, upload file, and download file  

### Setup

The [setup.sh](/setup.sh) script can be used to download the contents of this repository and set them up for both the server (storage servers) and client (web server) side.
It creates the service ```securestore``` on the storage servers that acts as a server, either receiving commands from the other storage server or receiving commands from the web server and forwarding them to the other storage server (if relevant).
It creates the command ```securestore``` on the web server that acts as a client and connects to the master storage server.  

Try ```sudo ./setup.sh -h``` for more info
