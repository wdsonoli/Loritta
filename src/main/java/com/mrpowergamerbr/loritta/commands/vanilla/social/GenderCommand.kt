package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Gender
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import org.jetbrains.exposed.sql.transactions.transaction

class GenderCommand : AbstractCommand("gender", listOf("gênero", "genero"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["GENDER_Description"]
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val embed = EmbedBuilder()
				.setTitle(locale["GENDER_WhatAreYou"])
				.setDescription(locale["GENDER_WhyShouldYouSelect"])
				.build()


		val message = context.sendMessage(embed)

		message.addReaction("male:384048518853296128").queue()
		message.addReaction("female:384048518337265665").queue()

		message.onReactionAddByAuthor(context) {
			message.delete().queue()
			if (it.reactionEmote.id == "384048518853296128") {
				transaction(Databases.loritta) {
					context.lorittaUser.profile.options.gender = Gender.MALE
				}

				context.reply(
						LoriReply(
								locale["GENDER_SuccessfullyChanged"],
								"\uD83C\uDF89"
						)
				)
			}
			if (it.reactionEmote.id == "384048518337265665") {
				transaction(Databases.loritta) {
					context.lorittaUser.profile.options.gender = Gender.FEMALE
				}

				context.reply(
						LoriReply(
								locale["GENDER_SuccessfullyChanged"],
								"\uD83C\uDF89"
						)
				)
			}
		}
	}
}