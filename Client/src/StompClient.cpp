#include <stdlib.h>
#include <ConnectionHandler.h>
#include <KeyboardThread.h>
#include <thread>

int main (int argc, char *argv[]) {


    ConnectionHandler* hand = new ConnectionHandler();

    StompMessageProtocol* protocol  = new StompMessageProtocol(*hand);
    KeyboardThread  keyThread(*hand, *protocol);
    std::thread th(&KeyboardThread::run,&keyThread);
    while(1){
        if(hand->isConnected()) {
            string ans;
            if (!hand->getFrameAscii(ans, '\0')) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }
            string out = protocol->processFrame(ans);
            if (out != "")
                if (!hand->sendFrameAscii(out,'\0')) {
                    std::cout << "Disconnected. Exiting...\n" << std::endl;
                    break;
                }
        }
    }
    th.join();
    delete(hand);
    delete(protocol);
    }

    //login 132.72.45.172:7777 adssd22 qqq