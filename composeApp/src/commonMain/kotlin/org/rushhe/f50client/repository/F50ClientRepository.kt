package org.rushhe.f50client.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.rushhe.f50client.di.F50ClientWrapper
import org.rushhe.f50client.model.Message
import org.rushhe.f50client.remote.F50ClientApi
import org.rushhe.f50client.viewmodel.SMSListUiState

interface F50ClientRepositoryInterface {
    fun fetchSMSAsFlow(): Flow<List<Message>>
    suspend fun fetchSMS(): List<Message>
    suspend fun fetchAndStoreSMS()
}

class F50ClientRepository(
    private val f50ClientApi: F50ClientApi,
    private val f50ClientDatabase: F50ClientWrapper,
) : F50ClientRepositoryInterface {
    private val coroutineScope: CoroutineScope = MainScope()
    private val f50ClientQueries = f50ClientDatabase.instance?.f50ClientQueries

    private val _networkState = MutableSharedFlow<SMSListUiState>()
    val networkState: SharedFlow<SMSListUiState> = _networkState

    init {
        coroutineScope.launch {
            fetchAndStoreSMS()
        }
    }

    override fun fetchSMSAsFlow(): Flow<List<Message>> {
        // the main reason we need to do this check is that sqldelight isn't currently
        // setup for javascript client
        return f50ClientQueries?.selectAll(
            mapper = { id, content, date, draftGroupId, number, tag ->
                Message(
                    id = id,
                    content = content,
                    date = date,
                    draftGroupId = draftGroupId ?: "",
                    number = number,
                    tag = tag
                )
            }
        )?.asFlow()?.mapToList(Dispatchers.Default) ?: flowOf(emptyList())
    }

    // Used by web and apple clients atm
    override suspend fun fetchSMS(): List<Message> = f50ClientApi.fetchSMS().messages

    override suspend fun fetchAndStoreSMS() {
        _networkState.emit(SMSListUiState.Loading)

        try {
            val result = f50ClientApi.fetchSMS()

            // this is very basic implementation for now that removes all existing rows
            // in db and then inserts results from api request
            // using "transaction" accelerate the batch of queries, especially inserting
            f50ClientQueries?.transaction {
                f50ClientQueries.deleteAll()
                result.messages.forEach {
                    f50ClientQueries.insertItem(
                        it.id,
                        it.content,
                        it.date,
                        it.draftGroupId,
                        it.number,
                        it.tag,
                    )
                }
            }

            _networkState.emit(SMSListUiState.Success(result.messages))
        } catch (e: Exception) {
            _networkState.emit(SMSListUiState.Error("Error: ${e.message}"))
        }
    }
}