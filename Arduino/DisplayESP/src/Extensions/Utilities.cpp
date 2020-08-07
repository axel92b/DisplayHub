//
// Created by vov4ik on 6/7/20.
//
#include "Utilities.h"

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

std::map<String, String> getModulesFromConfiguration(std::vector<String>& splitted_conf) {
    std::map<String, String> modules;
    if (splitted_conf.size() % 2 != 0) {
        for (int i = 0; i < splitted_conf.size() - 1; i += 2) {
            modules.insert(pair<String, String>(splitted_conf[i], splitted_conf[i + 1]));
        }
    } else {
        for (int i = 0; i < splitted_conf.size(); i+=2 ) {
            modules.insert(pair<String, String>(splitted_conf[i], splitted_conf[i + 1]));
        }
    }
    return modules;
}