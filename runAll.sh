gnome-terminal -x sh -c "sh runServer.sh; bash"
gnome-terminal -x sh -c "sh runWebClient.sh; bash"
gnome-terminal -x sh -c "sleep 15 ; sh runDesktopClient.sh; bash"

