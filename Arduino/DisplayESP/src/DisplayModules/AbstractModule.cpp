//
// Created by vov4ik on 6/7/20.
//
#include "AbstractModule.h"

AbstractModule::~AbstractModule() {

}
uint8_t AbstractModule::GetModuleRow() const {
    return _moduleRow;
}
void AbstractModule::SetModuleRow(uint8_t module_row) {
    _moduleRow = module_row;
}
String const& AbstractModule::GetModuleName() const {
    return _moduleName;
}
void AbstractModule::SetModuleName(String const& module_name) {
    _moduleName = module_name;
}
