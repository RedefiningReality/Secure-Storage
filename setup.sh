#!/bin/bash
if [ $(/usr/bin/id -u) -ne 0 ]; then
    echo "Please run this script as root (sudo ./setup.sh [client/server] [name])"
    exit
fi

if [ "$#" -ne 2 ] || [[ $1 != "client" ] && [ $1 != "server" ]]; then
	echo "Usage: sudo ./setup.sh [client/server] [name]"
	echo ""
	echo "Options:"
	echo "[client/server]   client => web server"
	echo "                  server => storage server"
	echo "[name]            machine name"
	exit
fi

echo "Installing Java 8 Runtime Environment and git"
apt update
apt install openjdk-8-jre git -y

echo "Cloning secure storage GitHub repository"
git clone https://github.com/TheCatLover/Secure-Storage.git /opt/secure_storage

echo "Setting up gen-keys command"
echo "#!/bin/bash" > /usr/bin/gen-keys
echo "cd /opt/secure_storage" >> /usr/bin/gen-keys
echo "java -jar /opt/secure_storage/keys.jar \$1 \$2 \$3 \$4" >> /usr/bin/gen-keys
chmod 755 /usr/bin/gen-keys

if [ $1 = "client" ]; then
	echo "Setting up client configuration file"
	mv /opt/secure_storage/configuration/client.properties /opt/secure_storage/config.properties
	
	echo "Setting up securestore command"
	echo "#!/bin/bash" > /usr/bin/securestore
	echo "cd /opt/secure_storage" >> /usr/bin/gen-keys
	echo "java -jar /opt/secure_storage/client.jar \$1 \$2 \$3 \$4" >> /usr/bin/securestore
	chmod 755 /usr/bin/securestore
	
	echo "Removing unnecessary files"
	rm /opt/secure_storage/server.jar
	rm -r /opt/secure_storage/configuration
	
	echo "Generating keys and saving in /opt/secure_storage/keys"
	mkdir /opt/secure_storage/keys
	gen-keys /opt/secure_storage/keys $2
else
	echo "Setting up server configuration file"
	mv /opt/secure_storage/configuration/server.properties /opt/secure_storage/config.properties
	
	echo "Setting up securestore command"
	echo "#!/bin/bash" > /usr/bin/securestore
	echo "cd /opt/secure_storage" >> /usr/bin/gen-keys
	echo "java -jar /opt/secure_storage/server.jar" >> /usr/bin/securestore
	chmod 755 /usr/bin/securestore
	
	echo "Removing unnecessary files"
	rm /opt/secure_storage/client.jar
	rm -r /opt/secure_storage/configuration
	
	echo "Creating securestore system service"
	echo "[Unit]" > /etc/systemd/system/securestore.service
	echo "Description=Secure storage service" >> /etc/systemd/system/securestore.service
	echo "" >> /etc/systemd/system/securestore.service
	echo "[Service]" >> /etc/systemd/system/securestore.service
	echo "ExecStart=/usr/bin/securestore" >> /etc/systemd/system/securestore.service
	echo "" >> /etc/systemd/system/securestore.service
	echo "[Install]" >> /etc/systemd/system/securestore.service
	echo "WantedBy=multi-user.target" >> /etc/systemd/system/securestore.service
	systemctl daemon-reload
	
	echo "Setting service to start at boot"
	systemctl enable securestore.service
	
	echo "Starting service"
	systemctl start securestore.service
	
	echo "Generating keys and saving in /opt/secure_storage/keys"
	mkdir /opt/secure_storage/keys
	gen-keys /opt/secure_storage/keys $2 1
fi

echo ""
echo "Installation completed"
echo "Be sure to copy over public keys from other machines to /opt/secure_storage/keys"
echo "If this is a storage server and another has already been created, also replace aes.key and aes.txt with those in other server"
echo "Edit configurations in /opt/secure_storage/config.properties"
echo "If this is a storage server, be sure to restart the server with systemctl restart securestore after editing the configurations file"
echo "If this is the web server, connect to the storage servers with the securestore command (securestore -h for more info)"
