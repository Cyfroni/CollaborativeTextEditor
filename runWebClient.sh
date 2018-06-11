jar cvf CollaborativeTextEditor.jar WebClient/out/production/WebClient/CollaborativeTextEditor/*

mv CollaborativeTextEditor.jar webswing-2.5.4

cd webswing-2.5.4

python -mwebbrowser http://localhost:8080

sh run.sh

cd ..
