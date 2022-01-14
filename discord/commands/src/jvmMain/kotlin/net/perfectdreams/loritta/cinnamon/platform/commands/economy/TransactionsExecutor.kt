package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.BOUGHT_SHARES
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.SOLD_SHARES
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.TransactionsCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.components.selectMenu
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.math.ceil
import kotlin.time.ExperimentalTime

class TransactionsExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TransactionsExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", TransactionsCommand.I18N_PREFIX.Options.User.Text)
                .register()

            val page = optionalInteger("page", TransactionsCommand.I18N_PREFIX.Options.Page.Text)
                .register()
        }

        override val options = Options

        private const val TRANSACTIONS_PER_PAGE = 10

        @OptIn(ExperimentalTime::class)
        suspend fun createMessage(
            loritta: LorittaCinnamon,
            i18nContext: I18nContext,
            data: TransactionListData
        ): MessageBuilder.() -> (Unit) {
            val transactions = loritta.services.sonhos.getUserTransactions(
                data.viewingTransactionsOfUserId,
                data.transactionTypeFilter,
                TRANSACTIONS_PER_PAGE,
                (data.page * TRANSACTIONS_PER_PAGE)
            )

            val totalTransactions = loritta.services.sonhos.getUserTotalTransactions(
                data.viewingTransactionsOfUserId,
                data.transactionTypeFilter
            )

            val totalPages = ceil((totalTransactions / TRANSACTIONS_PER_PAGE.toDouble())).toLong()

            val isSelf = data.viewingTransactionsOfUserId.value == data.userId.value

            val cachedUserInfo = if (!isSelf)
                loritta.getCachedUserInfo(data.viewingTransactionsOfUserId)
            else null

            return {
                content = i18nContext.get(TransactionsCommand.I18N_PREFIX.NotAllTransactionsAreHere)

                if (data.page >= totalPages && totalPages != 0L) {
                    // ===[ EASTER EGG: USER INPUT TOO MANY PAGES ]===
                    embed {
                        title = i18nContext.get(TransactionsCommand.I18N_PREFIX.UnknownPage.Title)

                        description = i18nContext.get(TransactionsCommand.I18N_PREFIX.UnknownPage.Description)
                            .joinToString("\n")

                        color = LorittaColors.LorittaRed.toKordColor()

                        // TODO: Host this somewhere else
                        image = "https://cdn.discordapp.com/attachments/513405772911345664/930945637841788958/fon_final_v3_sticker_small.png"
                    }

                    actionRow {
                        interactiveButton(
                            ButtonStyle.Primary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = totalPages - 1
                                )
                            )
                        ) {
                            label = i18nContext.get(TransactionsCommand.I18N_PREFIX.UnknownPage.GoToTheLastPage)
                            loriEmoji = Emotes.LoriSob
                        }
                    }
                } else {
                    embed {
                        if (totalPages != 0L) {
                            // ===[ NORMAL TRANSACTION VIEW ]===
                            title = buildString {
                                if (isSelf)
                                    append(i18nContext.get(TransactionsCommand.I18N_PREFIX.YourTransactions))
                                else append(i18nContext.get(TransactionsCommand.I18N_PREFIX.UserTransactions("${cachedUserInfo?.name}#${cachedUserInfo?.discriminator}")))

                                append(" — ")

                                append(i18nContext.get(TransactionsCommand.I18N_PREFIX.Page(data.page + 1)))
                            }

                            color = LorittaColors.LorittaAqua.toKordColor()

                            description = buildString {
                                for (transaction in transactions) {
                                    append("[<t:${transaction.timestamp.epochSeconds}:d> <t:${transaction.timestamp.epochSeconds}:t> | <t:${transaction.timestamp.epochSeconds}:R>]")
                                    append(" ")
                                    when (transaction) {
                                        is BrokerSonhosTransaction -> {
                                            when (transaction.action) {
                                                BOUGHT_SHARES -> append(
                                                    "\uD83D\uDCB8 ${
                                                        i18nContext.get(
                                                            TransactionsCommand.I18N_PREFIX.Types.HomeBroker.BoughtShares(
                                                                transaction.stockQuantity,
                                                                transaction.ticker,
                                                                transaction.sonhos
                                                            )
                                                        )
                                                    }"
                                                )
                                                SOLD_SHARES -> append(
                                                    "\uD83D\uDCB5 ${
                                                        i18nContext.get(
                                                            TransactionsCommand.I18N_PREFIX.Types.HomeBroker.SoldShares(
                                                                transaction.stockQuantity,
                                                                transaction.ticker,
                                                                transaction.sonhos
                                                            )
                                                        )
                                                    }"
                                                )
                                            }
                                        }
                                        // This should never happen because we do a left join with a "isNotNull" check
                                        is UnknownSonhosTransaction -> {
                                            append("${Emotes.LoriShrug} Transação Desconhecida (Bug?)")
                                        }
                                    }
                                    append("\n")
                                }
                            }

                            footer(i18nContext.get(TransactionsCommand.I18N_PREFIX.TransactionsQuantity(totalTransactions)))
                        } else if (totalPages == 0L) {
                            // ===[ NO MATCHING TRANSACTIONS VIEW ]===
                            title = buildString {
                                if (isSelf)
                                    append(i18nContext.get(TransactionsCommand.I18N_PREFIX.YourTransactions))
                                else append(i18nContext.get(TransactionsCommand.I18N_PREFIX.UserTransactions("${cachedUserInfo?.name}#${cachedUserInfo?.discriminator}")))
                            }

                            color = LorittaColors.LorittaRed.toKordColor()

                            description = i18nContext.get(TransactionsCommand.I18N_PREFIX.NoTransactionsFunnyMessages).random()

                            image = "${loritta.config.website}/assets/img/blog/lori_calca.gif"
                        }
                    }

                    val addLeftButton = data.page != 0L && totalTransactions != 0L
                    val addRightButton = totalPages > (data.page + 1) && totalTransactions != 0L

                    actionRow {
                        if (addLeftButton) {
                            interactiveButton(
                                ButtonStyle.Primary,
                                ChangeTransactionPageButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    data.copy(
                                        page = data.page - 1
                                    )
                                )
                            ) {
                                loriEmoji = Emotes.ChevronLeft
                            }
                        } else {
                            interactiveButton(
                                ButtonStyle.Primary,
                                ChangeTransactionPageButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    data.copy(
                                        page = data.page - 1
                                    )
                                )
                            ) {
                                loriEmoji = Emotes.ChevronLeft
                                disabled = true
                            }
                        }

                        if (addRightButton) {
                            interactiveButton(
                                ButtonStyle.Primary,
                                ChangeTransactionPageButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    data.copy(
                                        page = data.page + 1
                                    )
                                )
                            ) {
                                loriEmoji = Emotes.ChevronRight
                            }
                        } else {
                            interactiveButton(
                                ButtonStyle.Primary,
                                ChangeTransactionPageButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    data.copy(
                                        page = data.page + 1
                                    )
                                )
                            ) {
                                loriEmoji = Emotes.ChevronRight
                                disabled = true
                            }
                        }
                    }

                    actionRow {
                        selectMenu(
                            ChangeTransactionFilterSelectMenuExecutor,
                            ComponentDataUtils.encode(data)
                        ) {
                            val transactionTypes = TransactionType.values()
                            this.allowedValues = 1..(25.coerceAtMost(transactionTypes.size))

                            for (transactionType in transactionTypes) {
                                option(i18nContext.get(transactionType.title), transactionType.name) {
                                    description = i18nContext.get(transactionType.description)
                                    loriEmoji = transactionType.emote
                                    default = transactionType in data.transactionTypeFilter
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val userId = UserId(args[Options.user]?.id?.value ?: context.user.id.value)
        val page = ((args[Options.page] ?: 1L) - 1)
            .coerceAtLeast(0)

        val builtMessage = createMessage(
            context.loritta,
            context.i18nContext,
            TransactionListData(
                context.user.id,
                userId,
                page,
                TransactionType.values().toList()
            )
        )

        context.sendMessage {
            apply(builtMessage)
        }
    }
}