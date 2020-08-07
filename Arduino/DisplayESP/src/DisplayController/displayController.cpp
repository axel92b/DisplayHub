//
// Created by vov4ik on 6/7/20.
//

#include "Extensions/Utilities.h"
#include "displayController.h"

DisplayController* DisplayController::LoadDisplayModuleConfigurationFromDisk() {
    return nullptr;
}

bool DisplayController::UpdateExistingDisplayConfiguration() {
    return false;
}
bool DisplayController::BuildDisplayModuleConfiguration(String const& configurationJSON) {
    return false;
}
bool DisplayController::SaveDisplayModuleConfiguration(String const& configuration, const char* filename) {

}
void DisplayController::SetHardwareSerial(HardwareSerial* p_serial) {
    hw_s = p_serial;
}