//
// Created by vov4ik on 6/2/20.
//

#ifndef UNTITLED2_SRC_DISPLAYMODULES_NEWSMODULE_H_
#define UNTITLED2_SRC_DISPLAYMODULES_NEWSMODULE_H_

#include <Arduino.h>
#include "AbstractModule.h"

// {"feed":"Walla","name":"News Feed","index":1}

class NewsModule : public AbstractModule{
  String _source = "";
  String _news = "";
public:
    bool Update(bool forceUpdate) override;

private:
  ~NewsModule() override = default;
 public:
  static NewsModule* Instance(String const& string);
  String GetData() override;
  String getDisplayDataString() override;
};

#endif //UNTITLED2_SRC_DISPLAYMODULES_NEWSMODULE_H_
