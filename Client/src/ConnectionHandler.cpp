#include <ConnectionHandler.h>
#include <mutex>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
class  ConnectionHandler;

ConnectionHandler::ConnectionHandler():mutex(), host_("0"),port_(0),io_service_(), socket_(io_service_),
                                        myName(""),
                                        connected(false),
                                        borrowedBooks(unordered_map<string,string>()),
                                        inventory(unordered_map<string,vector<string>>()),
                                        subscriptionsByTopic(unordered_map<string,int>()),
                                        subscriptionsById(unordered_map<int,string>()),
                                        receipts(unordered_map<int,pair<string,bool>>()),
                                        wishList(vector<string>()){}

ConnectionHandler::~ConnectionHandler() {
    borrowedBooks.clear();
    inventory.clear();
    subscriptionsById.clear();
    subscriptionsByTopic.clear();
    receipts.clear();
    wishList.clear();
    close();
}

 
bool ConnectionHandler::connect() {
    cout << "Starting connect to "
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Could not connect to  server " <<  std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    if(!isConnected())
        return false;
    size_t tmp = 0;
	boost::system::error_code error;
    try {

        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
		if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    if(!isConnected())
        return false;
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}



bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do{
            if(!getBytes(&ch, 1))
            {
                return false;
            }
            if(ch!='\0')
                frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::sendFrameAscii(const std::string& frame,char delimiter) {
    std::lock_guard<std::mutex> lock(mutex);
    bool result=sendBytes(frame.c_str(),frame.length());
    if(!result) return false;
    return sendBytes(&delimiter,1);

}
 
// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

string ConnectionHandler::getName() {
    return myName;
  }



  void ConnectionHandler::setName(string name) {
      myName=name;
  }
  void ConnectionHandler::addAndRemoveInventory(string name, string topic,int action) {
      std::lock_guard<std::mutex> lock(mutex);
      if(action==0) {
          if (inventory.find(topic) != inventory.end()) {
              inventory.at(topic).push_back(name);
              return;
          }
          vector<string> newVector;
          newVector.push_back(name);
          inventory.insert(pair<string, vector<string>>(topic, newVector));
      }
      if(action==1) {
          std::vector<string>::iterator it = std::find(inventory.at(topic).begin(), inventory.at(topic).end(), name);
          inventory.at(topic).erase(it);
      }
  }

  void ConnectionHandler::addAndRemoveToWish(string name,int action) {
      std::lock_guard<std::mutex> lock(mutex);
      if(action==0)
        wishList.push_back(name);
      if(action==1){
          std::vector<string>::iterator it = std::find(wishList.begin(), wishList.end(), name);
          wishList.erase(it);
      }
  }

  string ConnectionHandler::getLender(string book) {
      return  borrowedBooks.at(book);
  }

  void ConnectionHandler::addAndRemoveBorrowed(string book,string name, int action) {
      std::lock_guard<std::mutex> lock(mutex);
      if(action==0)
          borrowedBooks.insert(pair<string,string>(book,name));
      if(action==1){
          borrowedBooks.erase(book);
      }
  }

  void ConnectionHandler::addToSubscription(string topic, int id) {
      subscriptionsByTopic.insert(pair<string,int>(topic,id));
      subscriptionsById.insert(pair<int,string>(id,topic));

  }
  void ConnectionHandler::removeSubscription(string topic) {
      int id = subscriptionsByTopic.at(topic);
      subscriptionsByTopic.erase(topic);
      subscriptionsById.erase(id);
  }
  int ConnectionHandler::getSubId(string topic) {
    if (subscriptionsByTopic.count(topic)==0)
        return -1;
    return subscriptionsByTopic.at(topic);
  }
  bool ConnectionHandler::hasBook(string book,string topic) {
      if (inventory.find(topic) != inventory.end()) {
          vector<string> v = inventory.at(topic);
          for(string myBook:v){
          if(book==myBook)
          return true;
          }
      }
      return false;
  }

  vector<string> ConnectionHandler::getBooks(string topic) {
      vector<string> books = vector<string>();
      if(inventory.count(topic)!=0) {
          for (string book:inventory.at(topic))
              books.push_back(book);
      }
      return books;
  }

  bool ConnectionHandler::wished(string book) {
      for(string myBook:wishList) {
          if (myBook == book)
              return true;
      }
      return false;
  }

  void ConnectionHandler::connecting() {
      connected=true;
  }
  bool ConnectionHandler::isConnected() {
      return connected;
  }
  void ConnectionHandler::setHost(string host) {
    this->host_ = host;
}

void ConnectionHandler::setPort(short port) {
    this->port_ = port;
}

void ConnectionHandler::addToReceipts(int recId, string topic, bool action) {
    receipts.insert(pair<int, pair<string,bool>>(recId,pair<string, bool >(topic,action)));
}
unordered_map<int,pair<string,bool >> ConnectionHandler::getREceipts() {
    return receipts;
}

bool ConnectionHandler::isBorrowed(string book) {
    return (borrowedBooks.count(book)==1);
}
void ConnectionHandler::disconnecting() {
    connected= false;
    socket_.close();
    this->borrowedBooks.clear();
    this->wishList.clear();
    this->subscriptionsById.clear();
    this->subscriptionsByTopic.clear();
    this->inventory.clear();
}



