package net.x320.xmel.app

class RecordField(val id: String, val name: String?, val isStandAlone: Boolean) {
    var usage = 0
        private set

    fun addUsage(): Int {
        return ++usage
    }
}