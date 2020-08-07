//
// Created by vov4ik on 6/2/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_STOCKSMODULE_H_
#define UNTITLED2_SRC_DISPLAYMODULES_STOCKSMODULE_H_

#include <Arduino.h>
#include <vector>
#include "AbstractModule.h"

// {"stock_symbol":"inct","name":"Stocks","index":0}

class StocksModule : public AbstractModule {
  ~StocksModule() override = default;
  std::vector<String> _stock_symbols;
  int timeout = 0;
  String display_string;
 public:
  String GetData() override;
 public:
  String getDisplayDataString() override;
 public:
    bool Update(bool forceUpdate) override;

    static StocksModule* Instance(String const& string);
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_STOCKSMODULE_H_
