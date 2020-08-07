//
// Created by vov4ik on 6/2/20.
//

#ifndef UNTITLED2_SRC_DISPLAYDATAMODULE_H_
#define UNTITLED2_SRC_DISPLAYDATAMODULE_H_
#include <Arduino.h>

class AbstractModule {
 protected:
  uint8_t _row = 255;
  String _name = "";
 public:
  virtual ~AbstractModule() = 0;
  uint8_t GetRow() const;
  void SetRow(uint8_t module_row);
  String const& GetModuleName() const;
  void SetName(String const& module_name);
  virtual String GetData() = 0;
  virtual String getDisplayDataString() = 0;
  virtual bool Update(bool forceUpdate) = 0;
};

#endif //UNTITLED2_SRC_DISPLAYDATAMODULE_H_
