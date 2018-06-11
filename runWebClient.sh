cd WebClient/out/production/WebClient

jar cvf CollaborativeTextEditor.jar .

mv CollaborativeTextEditor.jar ../../../webswing-2.5.4

cd ../../../webswing-2.5.4

cp CollaborativeTextEditor.jar CollaborativeTextEditor2.jar

python -mwebbrowser http://localhost:8080/

sh run.sh

cd ..
