package android.com.displayhubcompanion.models

class ModuleFireStoreModel (
    var name: String? = null,
    var index: Int? = null
) {
    fun toModuleClass(): Module {
        val module = Module()
        module.index = index
        module.name = name
        return module
    }
}