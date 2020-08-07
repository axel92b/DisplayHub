//
// Created by vov4ik on 6/3/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_DISPLAYMODULEFACTORY_H_
#define UNTITLED2_SRC_DISPLAYMODULES_DISPLAYMODULEFACTORY_H_

#include "AbstractModule.h"
#include "newsModule.h"
#include "clockModule.h"
#include "stocksModule.h"
#include "weatherModule.h"

// {"stock_symbol":"inct","name":"Stocks","index":0}
// {"country":"Israel","city":"Haifa","name":"Weather","index":3}
// {"feed":"Walla","name":"News Feed","index":1}
// {"display_format":"HH:mm","format":"24H","name":"Clock","index":2,"region":"Israel"}

class DisplayModuleFactory {
 public:
  DisplayModuleFactory() = default;
  static AbstractModule* createDisplayDataModule(String const& moduleName, String const& args);
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_DISPLAYMODULEFACTORY_H_
