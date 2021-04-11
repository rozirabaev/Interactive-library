//
// Created by kseniia@wincs.cs.bgu.ac.il on 10/01/2020.
//

#ifndef BOOST_ECHO_CLIENT_STOMPMESSAGEPROTOCOL_H
#define BOOST_ECHO_CLIENT_STOMPMESSAGEPROTOCOL_H

#include <string>
#include "ConnectionHandler.h"
class  ConnectionHandler;

using namespace std;
//using std::string;

class StompMessageProtocol {
private:
    int disconnectRec;
    int receiptCounter;
    int subId;
    ConnectionHandler* hand;


    string connected();
    string login(vector<string> msg);
    string join(vector<string> msg);
    string addBook(vector<string> msg);
    string borrow(vector<string> msg);
    string returnBook(vector<string> msg);
    string status(vector<string> msg);
    string exit(vector<string> msg);
    string logout(vector<string> msg);
    string sendFrame(string msg, string topic);
    void receipt (vector<string> tokens);
    void error(vector<string> tokens);
    string message(vector<string> msg);
    string findHeader (string head,vector <string> msg);

public:StompMessageProtocol(ConnectionHandler &hand);
    string processFrame(string msg);
    string processKeyboard(string msg);

};


#endif //BOOST_ECHO_CLIENT_STOMPMESSAGEPROTOCOL_H
