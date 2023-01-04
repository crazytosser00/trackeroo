package ru.roansa.trackeroo_core.logging.transform

import android.util.Log
import ru.roansa.trackeroo_core.logging.LogEntity

class DebugLevelTransformer : ILogTransformer {

    override fun transform(previousResultString: String, logEntity: LogEntity) =
        "[${logEntity.level.name}] "
}