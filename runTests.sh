gnome-terminal -x sh -c "sh runServer.sh; bash"
gnome-terminal -x sh -c "
rm Server/Files/123.txt Server/Files/test1.txt;
touch Server/Files/123.txt;
cd DesktopClient;
sleep 15;
python testClient.py;
bash"
