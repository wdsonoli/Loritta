package net.perfectdreams.loritta.discord

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.common.utils.ConfigUtils
import java.io.File

@Serializable
class LorittaDiscordConfig(
    val token: String,
    val applicationId: Long
)

@OptIn(ExperimentalSerializationApi::class)
fun ConfigUtils.parseDiscordConfig(): LorittaDiscordConfig {
    val file = File("discord.conf")
    if (file.exists().not()) {
        file.createNewFile()
        file.writeBytes(LorittaDiscord::class.java.getResourceAsStream("/discord.conf").readAllBytes())
    }
    return Hocon {}.decodeFromConfig(ConfigFactory.parseFile(File("discord.conf")))
}