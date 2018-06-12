#include <string>
#include <fstream>
#include <iostream>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <regex>

using namespace std;

#define DIR_PATH "./Files/"

typedef vector<char> LINE;
typedef unordered_set<int> LISTENERS;
typedef vector<LINE> SHEET;
typedef pair<SHEET, LISTENERS> DOCK;
typedef unordered_map<string, DOCK> DATABASE;

bool add(DATABASE &, string, string);
void printSheet(SHEET);
SHEET bufferFile(string);
vector<string> split(const string&, const string&);

void test()
{
	string file = "kohan.txt";
	LINE line1{ 'b' }, line2{ '2','q' };
	SHEET sheet{line2};
	LISTENERS lis({ 1,2 });

	DOCK dock = make_pair(sheet, lis);

	DATABASE dataBase;
	dataBase.emplace(file, dock);
	//przychodzi info od klienta
	//string info = "1.0.1.1:t";
	string info1 = "1.0.1.3:";

	while(true){
		printSheet(dataBase[file].first);
		add(dataBase, file, info1);
		cin>>info1;
	}

	
}

void updateFile(string fileName, SHEET sheet)
{
	fstream fileStream;
	fileStream.open(DIR_PATH + fileName, ios::out | ios::trunc);
	if (fileStream.good())
	{
		printf("Opening for update went OK \n");
		for (auto i = sheet.begin(); i != sheet.end(); i++)
		{
			for (auto j = (*i).begin(); j != (*i).end(); j++)
			{
				fileStream << *j;
			}
			fileStream << endl;
		}
		fileStream.flush();			/*Forcing buffer to go to harddisc's emory*/
		fileStream.close(); 			/*Close the file when finished*/
		printf("Everything is OK \n");
	}
	else
	{
		printf("SOMETHING IS WRONG\n");
	}
}

bool add(DATABASE &dataBase, string file, string info)
{
	regex re1(":"), re2("[0-9]+"), re3("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[ -~\r\n]*$"), re4("\n");
	int startIndex[2], endIndex[2];
	string data, change;
	smatch tmp;

	if(!regex_match(info,re3)){
		perror("ERROR: Wrong char sequence\n");
		return false;
	}

	regex_search(info, tmp, re1);
	data = tmp.prefix().str();
	change = tmp.suffix().str();

	sregex_iterator next(data.begin(), data.end(), re2);
	startIndex[0] = atoi((*next).str().c_str());
	startIndex[1] = atoi((*(++next)).str().c_str());
	endIndex[0] = atoi((*(++next)).str().c_str());
	endIndex[1] = atoi((*(++next)).str().c_str());

	int linesAmount = endIndex[0] - startIndex[0];

	DOCK &dock = dataBase[file];
	SHEET &sheet = dock.first;

	if (startIndex[0] > sheet.size() || startIndex[0] <= 0){
		perror("ERROR: Wrong row index of first argument\n");
		return false;
	}
	if (startIndex[1] > sheet[startIndex[0]-1].size()){
		perror("ERROR: Wrong column index of first argument\n");
		return false;
	}

	if (change == "")
	{
		if (endIndex[0] > sheet.size() || endIndex[0] <= 0){
			perror("ERROR: Wrong row index of second argument\n");
			return false;
		}
		if (endIndex[1] > sheet[endIndex[0]-1].size()){
			perror("ERROR: Wrong column index of second argument\n");
			return false;
		}
		if (linesAmount == 0)
		{
			LINE &line = sheet[startIndex[0] - 1];
			line.erase(line.begin() + startIndex[1], line.begin() + endIndex[1]);
		}
		else
		{
			LINE &line = sheet[startIndex[0] - 1];
			LINE &lineEnd = sheet[endIndex[0] - 1];

			line.erase(line.begin() + startIndex[1], line.end());
			line.insert(line.end(), lineEnd.begin() + endIndex[1], lineEnd.end());
			sheet.erase(sheet.begin() + startIndex[0], sheet.begin() + endIndex[0]);
		}
	}
	else
	{
		auto vecLines = split(change, "\n");
		if (vecLines.size() - 1 != linesAmount){
			perror("ERROR: lines amount dont match with difference in index\n");
			return false;
		}

		if(sheet.size()==0 && linesAmount==0)
			linesAmount=1;
		vector<LINE> newLines(linesAmount, LINE{});
		sheet.insert(sheet.begin() + startIndex[0], newLines.begin(), newLines.end());

		
		int q = 0;
		int index = startIndex[1];
		for (auto str : vecLines)
		{

			if (q == 0 && linesAmount > 0) {
				LINE &line = sheet[startIndex[0] - 1];
				LINE &lineEnd = sheet[endIndex[0] - 1];
				vector<char> vec(str.begin(), str.end());
				lineEnd.insert(lineEnd.begin(), line.begin() + startIndex[1], line.end());
				line.erase(line.begin() + startIndex[1], line.end());
				line.insert(line.begin() + index, vec.begin(), vec.end());
			}
			else {
				LINE &line = sheet[startIndex[0] - 1 + q];
				vector<char> vec(str.begin(), str.end());
				line.insert(line.begin() + index, vec.begin(), vec.end());
			}
			index = 0;
			q++;
		}
	}
	printSheet(sheet);
	return true;
}

void printSheet(SHEET sheet)
{
	cout << "POCZATEK PLIKU" << endl;
	for (auto i = sheet.begin(); i != sheet.end(); i++)
	{
		for (auto j = (*i).begin(); j != (*i).end(); j++)
		{
			cout << *j;
		}
		cout << endl;
	}
	cout << "KONIEC PLIKU" << endl;

}

SHEET bufferFile(string fileName)
{
	SHEET sheet;
	ifstream file;
	string line;
	file.open((string)DIR_PATH + fileName);
	if (file.is_open())
	{
		printf("bufferFile\n");
		while (getline(file, line))
		{
			vector<char> vec(line.begin(), line.end());
			sheet.emplace_back(vec);
		}
		file.close();
	}
	if(sheet.size()==0)
		sheet.push_back(LINE{});
	printSheet(sheet);
	return sheet;
}

vector<string> split(const string& str, const string& delim)
{
	vector<string> tokens;
	size_t prev = 0, pos = 0;
	do
	{
		pos = str.find(delim, prev);
		if (pos == string::npos) pos = str.length();
		string token = str.substr(prev, pos - prev);
		tokens.push_back(token);
		prev = pos + delim.length();
	} while (pos < str.length() && prev < str.length());
	return tokens;
}
