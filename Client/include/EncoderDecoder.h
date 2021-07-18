//
// Created by roizu@wincs.cs.bgu.ac.il on 12/31/18.
//
#include <string>
#include "../include/connectionHandler.h"

#ifndef BOOST_ECHO_CLIENT_ENCODERDECODER_H
#define BOOST_ECHO_CLIENT_ENCODERDECODER_H


class EncoderDecoder {
private:
    std::string host;
    short port;
public:
    EncoderDecoder(std::string host, short port);
    static void Encode(ConnectionHandler *connectionHandler); // char is byte in c++
    static void shortToBytes(short num, char* bytesArr);
    static void Decode(ConnectionHandler *connectionHandler);
    static short bytesToShort(char* bytesArr);
    void run(ConnectionHandler *connectionHandler);
};


#endif //BOOST_ECHO_CLIENT_ENCODERDECODER_H
