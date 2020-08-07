//
// Created by vov4ik on 6/7/20.
//
#include "AbstractModule.h"

AbstractModule::~AbstractModule() {

}
uint8_t AbstractModule::GetRow() const {
    return _row;
}
void AbstractModule::SetRow(uint8_t module_row) {
    _row = module_row;
}
String const& AbstractModule::GetModuleName() const {
    return _name;
}
void AbstractModule::SetName(String const& module_name) {
    _name = module_name;
}
