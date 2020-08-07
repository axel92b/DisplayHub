//
// Created by vov4ik on 6/13/20.
//

#ifndef TRANSMITTERPROJECTESP_SRC_TRANSMITTERCONTROLLER_ABSTRACTMODULEUPDATER_H_
#define TRANSMITTERPROJECTESP_SRC_TRANSMITTERCONTROLLER_ABSTRACTMODULEUPDATER_H_

#include <DisplayModules/AbstractModule.h>
class AbstractModuleUpdater {
 public:
    AbstractModuleUpdater() = default;
    virtual bool UpdateModule(AbstractModule& module, String module_data = "") = 0;
    virtual ~AbstractModuleUpdater() = 0;
};

#endif //TRANSMITTERPROJECTESP_SRC_TRANSMITTERCONTROLLER_ABSTRACTMODULEUPDATER_H_
