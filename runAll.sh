gnome-terminal -x sh -c "sh runServer.sh; bash"
gnome-terminal -x sh -c "sleep 5 ; sh runDesktopClient.sh; bash"
gnome-terminal -x sh -c "sleep 6 ; sh runWebClient.sh; bash"
