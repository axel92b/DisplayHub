//
// Created by vov4ik on 6/2/20.
//

#include "stocksModule.h"
#include "Extensions/Utilities.h"
#include <ArduinoJson.h>


//Creating module Stocks
//Module args {"stock_symbol":"intl,ibm","name":"Stocks","index":2}
StocksModule *StocksModule::Instance(String const &string) {
    auto * sm = new StocksModule();
    StaticJsonDocument<500> doc;
    deserializeJson(doc, string);
    sm->_row = doc["index"].as<uint8_t>();
    sm->SetName("Stocks");
    sm->SetName(doc["name"].as<String>());
    auto stocks_string = "^" + doc["stock_symbol"].as<String>();
    split(stocks_string, sm->_stock_symbols, ',');
    return sm;
}

String StocksModule::getDisplayDataString() {
    return display_string;
}

String StocksModule::GetData() {
    return String();
}

//?request=ibm_intc
//{"stocks":[{"name":"ibm","price":"117.2600"},{"name":"intc","price":"57.5000"}]}
bool StocksModule::Update(bool forceUpdate) {
    if (!timeout || forceUpdate) {
        timeout = 60 * 10;
        String server = "http://us-central1-displayhubcompanion.cloudfunctions.net/getStocks?request=";
        for (int j = 0; j < _stock_symbols.size(); ++j) {
            server += _stock_symbols[j];
            if (j != _stock_symbols.size() - 1) {
                server += "_";
            }
        }
        Serial.println(server);
        StaticJsonDocument<500> doc;
        auto data = httpGETRequest(server.c_str());
        if (data.equals("{}")) {
            Serial.println("Get request failed!");
            timeout = 5;
            return false;
        }
        DeserializationError error = deserializeJson(doc, data);

        // Test if parsing succeeds.
        if (error) {
            Serial.print(F("deserializeJson() failed: "));
            Serial.println(error.c_str());
            timeout = 5;
            return false;
        }

        // Fetch values.
        //
        // Most of the time, you can rely on the implicit casts.
        // In other case, you can do doc["time"].as<long>();
        // {"state":"Israel","city":"Haifa","temp":"23.33","description":"clear sky","humidity":75,"wind":4.01}

        int stocks_symbols_count = doc["stocks"].size();
        display_string = "";
        String separator = "";
        for (int i = 0; i < stocks_symbols_count; i++) {
            display_string +=
                    separator + doc["stocks"][i]["name"].as<String>() + ": " + doc["stocks"][i]["price"].as<String>() +
                    "$";
            separator = " ";
        }
        return true;
    } else {
        Serial.println(timeout);
        timeout--;
    }
    return false;
}
