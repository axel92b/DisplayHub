//
// Created by vov4ik on 6/7/20.
//

#ifndef UNTITLED2_SRC_EXTENSIONS_UTILITIES_H_
#define UNTITLED2_SRC_EXTENSIONS_UTILITIES_H_
#include "Arduino.h"
#include <vector>
#include <map>

#define DEBUG_MODE 1

void split(const String& str, std::vector<String>& cont, char delim = ' ', bool pushNewLineChars = false, bool skipFirstEntry = false);

template <typename T>
static void debugPrint(T type,bool noNewLine = false) {
#if DEBUG_MODE == 1
    if (noNewLine) {
        Serial.print(type);
    }else{
        Serial.println(type);
    }
#endif
}

String httpGETRequest(const char* serverName);

///
/// @param splitted_conf
/// @return map of module name -> module json configuration string
std::map<String, String> getModulesFromConfiguration(std::vector<String>& splitted_conf);
#endif //UNTITLED2_SRC_EXTENSIONS_UTILITIES_H_
