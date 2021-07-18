CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: BGSclient
	g++ -o bin/BGSclient bin/connectionHandler.o bin/myMain.o bin/EncoderDecoder.o $(LDFLAGS)

BGSclient: bin/connectionHandler.o bin/myMain.o bin/EncoderDecoder.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/EncoderDecoder.o: src/EncoderDecoder.cpp
	g++ $(CFLAGS) -o bin/EncoderDecoder.o src/EncoderDecoder.cpp

bin/myMain.o: src/myMain.cpp
	g++ $(CFLAGS) -o bin/myMain.o src/myMain.cpp

.PHONY: clean
clean:
	rm -f bin/*
