package ru.roansa.trackeroo_core.logging.file.transform

import ru.roansa.trackeroo_core.ext.doIfExist
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipTransformer : ILogFileTransformer {

    val archiveName = "log_archive.zip"

    override fun transform(target: File): File {
        //FIXME remove non-null call
        if (target.listFiles() == null) return target
        ZipOutputStream(BufferedOutputStream(FileOutputStream("${target.path}/$archiveName"))).use { out ->
            for (file in target.listFiles()!!) {
                if (file.name.contains(archiveName)) break
                FileInputStream(file).use { fiStream ->
                    val entry = ZipEntry(file.name)
                    out.putNextEntry(entry)
                    val biStream = BufferedInputStream(fiStream)
                    biStream.copyTo(out, 1024)
                }
            }
        }
        val zipFile = File("${target.path}/log_archive.zip")
        zipFile.doIfExist {
            return this
        }
        return target
    }
}