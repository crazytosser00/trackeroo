package ru.roansa.trackeroo_core.logging.transform

import ru.roansa.trackeroo_core.logging.LogEntity

class MessageTransformer : ILogTransformer {

    override fun transform(previousResultString: String, logEntity: LogEntity) = logEntity.run {
        val separator =
            if (tag == null || message == null) ""
            else ": "

        throwable?.localizedMessage ?: "$tag$separator$message"
    }

}