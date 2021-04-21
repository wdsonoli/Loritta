package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.SAMExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object SAMCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.sam"

    override fun declaration() = command(listOf("sam"), CommandCategory.IMAGES) {
        description = LocaleKeyData("commands.command.sam.description")
        executor = SAMExecutor
    }
}