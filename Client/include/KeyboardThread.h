//
//

#ifndef BOOST_ECHO_CLIENT_KEYBOARDTHREAD_H
#define BOOST_ECHO_CLIENT_KEYBOARDTHREAD_H


#include <mutex>
#include "ConnectionHandler.h"
#include "StompMessageProtocol.h"

class KeyboardThread {
private: ConnectionHandler* handler;
    StompMessageProtocol* protocol;
public:
    KeyboardThread(ConnectionHandler& hand,StompMessageProtocol& protocol);
    void run();
    KeyboardThread(const KeyboardThread& kt);
    KeyboardThread & operator=(const KeyboardThread &kt);
    ~KeyboardThread();


};


#endif //BOOST_ECHO_CLIENT_KEYBOARDTHREAD_H
