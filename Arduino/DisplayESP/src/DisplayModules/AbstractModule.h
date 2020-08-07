//
// Created by vov4ik on 6/2/20.
//

#ifndef UNTITLED2_SRC_DISPLAYDATAMODULE_H_
#define UNTITLED2_SRC_DISPLAYDATAMODULE_H_
#include <Arduino.h>

class AbstractModule {
 protected:
  uint8_t _moduleRow = 255;
  String _moduleName = "";
 public:
  virtual ~AbstractModule() = 0;
  uint8_t GetModuleRow() const;
  void SetModuleRow(uint8_t module_row);
  String const& GetModuleName() const;
  void SetModuleName(String const& module_name);
  virtual String GetModuleData() = 0;
  virtual String getDisplayDataString() = 0;
};

#endif //UNTITLED2_SRC_DISPLAYDATAMODULE_H_
