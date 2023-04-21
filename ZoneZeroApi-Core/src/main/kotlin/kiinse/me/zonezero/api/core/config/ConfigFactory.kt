package kiinse.me.zonezero.api.core.config

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import java.io.File
import java.io.InputStream

object ConfigFactory {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    var config = getToml("config.toml")

    private fun getToml(fileName: String): TomlParseResult {
        val file = File("data", fileName)
        if (!file.exists()) {
            val inputStream = accessFile(fileName)
            if (inputStream != null) {
                try {
                    FileUtils.copyInputStreamToFile(inputStream, file)
                } catch (e: Exception) {
                    logger.warn("Error on copying file '{}'! Message: {}", file.name, e.message)
                }
            } else {
                logger.warn("File '${fileName}' not found inside jar.")
            }
        }
        val result = Toml.parse(file.inputStream())
        result.errors().forEach { logger.warn(it.message) }
        return result
    }

    fun getFile(fileName: String): File? {
        val file = File("data", fileName)
        if (!file.exists()) {
            val inputStream = accessFile(fileName)
            if (inputStream != null) {
                try {
                    FileUtils.copyInputStreamToFile(inputStream, file)
                } catch (e: Exception) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return file
    }

    private fun accessFile(file: String): InputStream? {
        return ConfigFactory::class.java.getResourceAsStream(file) ?: ConfigFactory::class.java.classLoader.getResourceAsStream(file)
    }

}