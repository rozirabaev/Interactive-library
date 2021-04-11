//
//

#include "KeyboardThread.h"
#include "ConnectionHandler.h"
#include "StompMessageProtocol.h"
class ConnectionHandler;

KeyboardThread::KeyboardThread(ConnectionHandler &hand, StompMessageProtocol &protocol):  handler(&hand),protocol(&protocol){
}


void KeyboardThread::run() {
    while(1) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        const string out = protocol->processKeyboard(line);
        if(out!="") {
            handler->sendFrameAscii(out, '\0');
        }
    }
}

KeyboardThread::KeyboardThread(const KeyboardThread &kt): handler(kt.handler),protocol(kt.protocol) {}
KeyboardThread& KeyboardThread::operator=(const KeyboardThread &kt){
    handler=kt.handler;
    protocol=kt.protocol;
    return  *this;
}
KeyboardThread::~KeyboardThread() {
    delete(handler);
    delete(protocol);
}
