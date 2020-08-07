//
// Created by vov4ik on 6/13/20.
//

#ifndef TRANSMITTERPROJECTESP_SRC_TRANSMITTERCONTROLLER_ABSTRACTMODULEUPDATER_CPP_NETWORKMODULEUPDATER_H_
#define TRANSMITTERPROJECTESP_SRC_TRANSMITTERCONTROLLER_ABSTRACTMODULEUPDATER_CPP_NETWORKMODULEUPDATER_H_

#include "AbstractModuleUpdater.h"
class NetworkModuleUpdater : public AbstractModuleUpdater {
  ~NetworkModuleUpdater() = default;
 public:
  NetworkModuleUpdater() = default;
  bool UpdateModule(AbstractModule& module, String module_data) override;
};

#endif //TRANSMITTERPROJECTESP_SRC_TRANSMITTERCONTROLLER_ABSTRACTMODULEUPDATER_CPP_NETWORKMODULEUPDATER_H_
