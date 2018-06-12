//g++ -std=c++11 Server.cpp -lpthread ; echo 1 ; ./a.out
#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <deque>
#include <dirent.h>
#include <pthread.h>
#include <sys/stat.h>
#include "DataBase.cpp"
#include <netdb.h>

#define MYPORT 8181    				// port, z którym będą się łączyli użytkownicy
#define BACKLOG 10				// jak dużo możę być oczekujących połączeń w kolejce
#define MAX_LENGTH 4096				// maksymalna wielkosc wiadomosci

struct stat info;
deque<string> FileList;				// lista plików na serwerze
unordered_map<int, int> ChildrenSockets;	// mapa do rozróżniania klientów
DATABASE dataBase;				// baza zawartosci plików
deque<pair<string, string> > messageQueue;	// kolejka wiadomosci zmian
unordered_set<pthread_t> threads;		// odpalone wątki
int root_socket; 				// socket obsługujący tworzenie połączeń
int end_proc = 0; 				// flaga zakończenia

pthread_t thread_id;
struct arg_struct {
	int sock;
	int PORT_num;
};

void *connection_handler(void *);
void *listening(void*);
void termination_handler(int);
void readDocumentNames(const string&, deque<string>&);

int main(int c, char** v)
{
	if (c > 1)
	{
		test();
		return 0;
	}
	struct sockaddr_in my_addr; // informacja o moim adresie
	struct sockaddr_in their_addr; // informacja o adresie osoby łączącej się
	unsigned int sin_size = sizeof(struct sockaddr_in);
	int yes = 1;

	if ((root_socket = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		perror("socket");
		exit(1);
	}

	if (setsockopt(root_socket, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
	{
		perror("setsockopt");
		exit(1);
	}

	my_addr.sin_family = AF_INET; // host byte order
	my_addr.sin_port = htons(MYPORT); // short, network byte order
	my_addr.sin_addr.s_addr = INADDR_ANY; // uzupełnij moim adresem IP
	memset(&(my_addr.sin_zero), '\0', 8); // wyzeruj resztę struktury

	if (bind(root_socket, (struct sockaddr *)&my_addr, sizeof(struct sockaddr)) == -1)
	{
		perror("bind");
		exit(1);
	}

	if (listen(root_socket, BACKLOG) == -1)
	{
		perror("listen");
		exit(1);
	}

	signal(SIGINT, &termination_handler);

    if (stat(DIR_PATH, &info) != 0)
        system((string("mkdir -p ") + DIR_PATH).c_str());
    else if (info.st_mode & S_IFDIR) {}
    else
        cout<<(string("") + DIR_PATH + " is no directory\n").c_str();

	pthread_create(&thread_id, NULL, listening, 0);
	threads.insert(thread_id);
	readDocumentNames(DIR_PATH, FileList);
	int port_generator = 9000;
	cout<<"Server is up\n";
	while (1)
	{ // głowna pętla accept()
		struct arg_struct args;
		if ((args.sock = accept(root_socket, (struct sockaddr *)&their_addr, &sin_size)) == -1)
		{
			if(end_proc)
				break;
			else{
				continue;
			}
		}
		args.PORT_num = port_generator++;
		printf("server: got connection from %s\n", inet_ntoa(their_addr.sin_addr));
		pthread_create(&thread_id, NULL, connection_handler, &args);
		threads.insert(thread_id);
	
		int mother_socket; // nasłuchuj na sock_fd, nowe połaczenia na sock

		int yes = 1;

		if ((mother_socket = socket(AF_INET, SOCK_STREAM, 0)) == -1)
		{
			perror("socket");
			continue;
		}

		if (setsockopt(mother_socket, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
		{
			perror("setsockopt");
			continue;
		}

		struct sockaddr_in addrRemote; // informacja o adresie osoby łączącej się
		memset(&addrRemote, 0, sizeof(struct sockaddr_in));
		addrRemote.sin_port = htons((port_generator - 1));
		addrRemote.sin_family = AF_INET;
		addrRemote.sin_addr.s_addr = htonl(INADDR_ANY);

		int timeout = 10;
		while (connect(mother_socket, (sockaddr*)&addrRemote, sizeof(addrRemote)) == -1)
		{
			sleep(1);
			if (timeout-- == 0){
				close(mother_socket);
				break;
			}
		}
		if (timeout < 0){
			continue;
		}
		ChildrenSockets.emplace(args.sock, mother_socket);
	}
	for(auto thread : threads){
		pthread_join(thread, NULL);
	}
	cout<<"All threads ended\nServer is down\n";
	return 0;
}

void readDocumentNames(const string& dirName, deque<string>& list)
{
	DIR* dirp = opendir(dirName.c_str());
	struct dirent *dp;
	while ((dp = readdir(dirp)) != NULL)
	{
		if (*(dp->d_name) != '.' && *(dp->d_name) != '.' + '.')
			list.push_back(dp->d_name);
	}
	closedir(dirp);
}

void *listening(void*)
{
	pair<string, string> message;
	while (!end_proc)
	{
		if (!messageQueue.empty())
		{
			message = messageQueue.front();
			messageQueue.pop_front();
			auto &listeners = dataBase[message.second].second;
			string info = message.first;
			cout << "Matula:  " << info << "###";
			if (add(dataBase, message.second, info))
				for (auto x : listeners)
					send(ChildrenSockets[x], info.c_str(), strlen(info.c_str()), 0);
			else
				cout<<"IGNORED\n";
			cout<<"Alutam\n";
		}

	}
	cout<<"Listening thread end\n";
}

void termination_handler (int signum)
{
	cout<< "Signal " << signum << " caught..." << endl;
	end_proc = 1;
	close(root_socket);
	for (auto kv : ChildrenSockets){
		send(kv.second, "", 1, 0);
		close(kv.second);
	}
}

void *connection_handler(void* socket_desc)
{
	// to jest proces-dziecko
	struct arg_struct arg = *(struct arg_struct*)socket_desc;
	int sock = arg.sock;
	string fileOpen = "";
	sleep(1);

	if (send(sock, &arg.PORT_num, sizeof(int), 0) == -1)
		perror("send");
	int amount;
	char instr[MAX_LENGTH];

	while (1)
	{
		cout<<"czekam\n";
		if ((amount = recv(sock, instr, sizeof(instr) - 1, 0)) == -1){
			perror("recv");
			break;
		}
		

		if (amount == 0)
			break;

		instr[amount] = '\0';

		printf("%d: %s  \n", amount, instr);
		if (strlen(instr) < 2)
		{
			cout << "zbyt krotka wiadomosc" << endl;
			continue;
		}

		if (instr[0] == 'N')
		{
			int fWrongName = 0;
			for (int i = 0; i < MAX_LENGTH - 1; i++)
				instr[i] = instr[i + 1];
			for (int j = 0; j < FileList.size(); j++)
			{
				if (FileList[j] == ((string)instr).append(".txt"))
				{
					fWrongName = 1;
				}

			}
			if (!fWrongName)
			{
				FileList.push_back(((string)instr).append(".txt"));
				fstream fileStream;
				fileStream.open(DIR_PATH + FileList[FileList.size() - 1], ios::out);
				if (fileStream.good())
				{
					perror("Creation went OK \n");
					fileStream << "\0";
					fileStream.flush();			/*Forcing buffer to go to harddisc's memory*/
					fileStream.close(); 			/*Close the file when finished*/
				}
				else
					perror("Error in file creation\n");
			}
			else
			{
				perror("File with such a name already exists\n");
				fWrongName = 0;
			}

		}
		if (!strcmp(instr, "UP"))
		{
			if (FileList.size() == 0)
				send(sock, "\n", strlen("\n"), 0);
			string temp="";
			for (int j = 0; j < FileList.size(); j++)
			{
				temp=temp+FileList[j]+"\n";
			}
			send(sock, temp.c_str(), strlen(temp.c_str()), 0);
		}
		if (instr[0] == 'G')
		{
			if(fileOpen.size()>0)
				perror("GG");
			string info(instr + 1, strlen(instr) - 1);
			fileOpen = info;
			if (dataBase.count(fileOpen) > 0)
				dataBase[fileOpen].second.insert(sock);
			else
			{
				LISTENERS lis({ sock });
				SHEET sheet = bufferFile(fileOpen);
				DOCK dock = make_pair(sheet, lis);
				dataBase.emplace(fileOpen, dock);
			}
				string sheet;
				for (auto i = dataBase[info].first.begin(); i != dataBase[info].first.end(); i++)
				{
					for (auto j = (*i).begin(); j != (*i).end(); j++)
					{
						sheet=sheet+*j;
					}
					cout<<sheet<<endl;
					send(sock, (sheet+"\n").c_str(), strlen((sheet+"\n").c_str()), 0);
					sheet="";
				}

			send(sock, "\0", sizeof(char), 0);


		}
		if (!strcmp(instr, "UG")) //close file
		{
			dataBase[fileOpen].second.erase(sock);
			cout << dataBase[fileOpen].second.size() << endl;
			if (dataBase[fileOpen].second.size() == 0)
			{
				DOCK &dock = dataBase[fileOpen];
				SHEET &sheet = dock.first;
				updateFile(fileOpen, sheet);
				dataBase.erase(fileOpen);
			}
			fileOpen = "";
		}
		if (instr[0] == 'Z')
		{
			string info(instr + 1, strlen(instr) - 1);
			messageQueue.emplace_back(info, fileOpen);

		}

	}
	if(fileOpen.size()>0)
	{
		dataBase[fileOpen].second.erase(sock);
		if (dataBase[fileOpen].second.size() == 0)
		{
			DOCK &dock = dataBase[fileOpen];
			SHEET &sheet = dock.first;
			updateFile(fileOpen, sheet);
			dataBase.erase(fileOpen);

		}
	}
	ChildrenSockets.erase(sock);
	if(!end_proc)
		threads.erase(pthread_self());
	close(sock);
	cout<<"child process end\n";
}
