# TeslaSolarMaxSmartCharger

## Installation
copy jar file to Raspberry Pi user's home directory (/home/pi)

Set up the log file by running:
sudo touch /var/log/tesla-solarmax-smart-charger.log
sudo chown pi /var/log/tesla-solarmax-smart-charger.log
chmod 775 /var/log/tesla-solarmax-smart-charger.log

Run the application at startup by adding the following to /etc/rc.local BEFORE "exit 0":
su pi -c "java -jar /home/pi/tesla-solarmax-smart-charger-1.0-SNAPSHOT.jar >> /var/log/tesla-solarmax-smart-charger.log &"
