#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
                                           
#include <string>
#include <mutex>
#include <iostream>
#include <boost/asio.hpp>
#include <unordered_map>
#include "StompMessageProtocol.h"

using boost::asio::ip::tcp;
using namespace std;

class ConnectionHandler {
private:
    std::mutex mutex;
    std::string host_;
    short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
	string myName;
	bool connected;
	unordered_map <string, string> borrowedBooks;
    unordered_map <string, vector<string>> inventory;
    unordered_map <string, int> subscriptionsByTopic;
    unordered_map <int, string> subscriptionsById;
    unordered_map <int,pair<string, bool>> receipts; // true=subscribe, false==exit
    vector<string> wishList;



public:
    ConnectionHandler();
    virtual ~ConnectionHandler();
 
    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);

    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame, char delimiter);
 
    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame,char delimiter);
	
    // Close down the connection properly.
    void close();

    string getName();
    void setName(string name);
    void addAndRemoveInventory(string name, string topic,int action);//0--add, 1-remove
    void addAndRemoveToWish(string name,int action);//0--add, 1-remove
    string getLender (string book);
    void addAndRemoveBorrowed (string book,string name, int action);//0--add, 1-remove
    void addToSubscription(string topic,int id);
    void removeSubscription(string topic);
    int getSubId(string topic);
    bool hasBook(string book,string topic);
    vector<string> getBooks(string topic);
    bool wished(string book);
    void connecting();
    void disconnecting();
    bool isConnected();

    void  setHost(string host);
    void setPort(short port);
    void addToReceipts (int recId, string topic, bool action);
    unordered_map<int,pair<string,bool >> getREceipts ();
    string gettop (int id);
    bool isBorrowed(string book);




    }; //class ConnectionHandler
 
#endif
