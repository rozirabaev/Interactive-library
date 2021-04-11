CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system
# All Targets
all: Client
	g++ -pthread -o  bin/Client.o bin/ConnectionHandler.o  bin/KeyboardThread.o bin/StompClient.o bin/StompMessageProtocol.o $(LDFLAGS) 
# Tool invocations
# Executable "StompClient" depends on the files StompClient.o
Client: bin/ConnectionHandler.o  bin/KeyboardThread.o bin/StompClient.o bin/StompMessageProtocol.o 


# Depends on the source and header files
bin/ConnectionHandler.o : src/ConnectionHandler.cpp
	g++ -pthread $(CFLAGS) -o  bin/ConnectionHandler.o src/ConnectionHandler.cpp

# Depends on the source and header files
bin/KeyboardThread.o: src/KeyboardThread.cpp
	g++ -pthread $(CFLAGS) -o  bin/KeyboardThread.o src/KeyboardThread.cpp

# Depends on the source and header files
bin/StompClient.o: src/StompClient.cpp
	g++ -pthread $(CFLAGS) -o  bin/StompClient.o src/StompClient.cpp

# Depends on the source and header files
bin/StompMessageProtocol.o: src/StompMessageProtocol.cpp
	g++ -pthread $(CFLAGS) -o  bin/StompMessageProtocol.o src/StompMessageProtocol.cpp

#Clean the build directory
clean: 
	rm -f bin/*

