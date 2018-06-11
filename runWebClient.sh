echo REMEMBER TO COMPILE FROM IDE FIRST

jar cvf WebClient/webswing-2.5.4/CollaborativeTextEditor.jar WebClient/out/production/WebClient/CollaborativeTextEditor/*

cd WebClient/webswing-2.5.4

python -mwebbrowser http://localhost:8080/cte/

sh run.sh

cd ..
