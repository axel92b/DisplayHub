//
// Created by vov4ik on 6/7/20.
//
#include "Utilities.h"
#include <HTTPClient.h>

using namespace std;
//Split escape sequence, must start and end with one char(not included in result)
void split(const String& str, vector<String>& cont, char delim, bool pushNewLineChars, bool skipFirstEntry) {
    vector<char> parts;
    bool foundFirstEntry = !skipFirstEntry;
    for (int i = 1; i < str.length(); i++) {
        if (str[i] != delim) {
            if (pushNewLineChars) {
                if (str[i] == '\r' || str[i] == '\n') {
                    continue;
                }
            }
            parts.push_back(str[i]);
        } else {
            if (foundFirstEntry) {
                parts.push_back('\0');
                String const& temp(parts.data());
                cont.push_back(temp);
                parts.clear();
            } else {
                foundFirstEntry = true;
                parts.clear();
            }
        }
    }
    if (!parts.empty()) {
        parts.push_back('\0');
        String const& temp = String(parts.data());
        cont.push_back(temp);
        parts.clear();
    }
}

String httpGETRequest(const char* serverName) {
    HTTPClient client; // Your IP address with path or Domain name with URL path
    client.begin(serverName);

    // Send HTTP POST request
    int httpResponseCode = client.GET();

    String payload = "{}";

    if (httpResponseCode>0) {
        Serial.print("HTTP Response code: ");
        Serial.println(httpResponseCode);
        payload = client.getString();
    }
    else {
        Serial.print("Error code: ");
        Serial.println(httpResponseCode);
    }
    // Free resources
    client.end();

    return payload;
}

std::map<String, String> getModulesFromConfiguration(std::vector<String>& splitted_conf) {
    std::map<String, String> modules;
    if (splitted_conf.size() % 2 != 0) {
        for (int i = 0; i < splitted_conf.size() - 1; i += 2) {
            Serial.print(splitted_conf[i]);
            if(modules.find(splitted_conf[i]) != modules.end()) {
                modules.insert(pair<String, String>(splitted_conf[i]+String(i), splitted_conf[i + 1]));
            }else{
                modules.insert(pair<String, String>(splitted_conf[i], splitted_conf[i + 1]));

            }
        }
    } else {
        for (int i = 0; i < splitted_conf.size(); i+=2 ) {
            Serial.print(splitted_conf[i]);
            if(modules.find(splitted_conf[i]) != modules.end()) {
                modules.insert(pair<String, String>(splitted_conf[i]+String(i), splitted_conf[i + 1]));
            }else{
                modules.insert(pair<String, String>(splitted_conf[i], splitted_conf[i + 1]));

            };
        }
    }
    return modules;
}