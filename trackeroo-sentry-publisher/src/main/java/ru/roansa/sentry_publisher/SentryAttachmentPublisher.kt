package ru.roansa.sentry_publisher

import io.sentry.Attachment
import ru.roansa.trackeroo_core.logging.publish.ILogPublisher
import java.io.File

class SentryAttachmentPublisher : ILogPublisher<Attachment> {

    override fun publish(file: File): Attachment {
        return Attachment(file.readBytes(), file.name)
    }

}