package org.rushhe.f50client.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.http.buildUrl
import io.ktor.http.encodedPath
import io.ktor.http.headers
import org.koin.core.component.KoinComponent
import org.rushhe.f50client.model.MessageResponse
import org.rushhe.f50client.viewmodel.SettingsViewModel

class F50ClientApi(
    private val client: HttpClient,
    private val settingsViewModel: SettingsViewModel
) : KoinComponent {
    private fun HttpRequestBuilder.setupSMSRequest() {
        url {
            protocol = URLProtocol.HTTP
            host = settingsViewModel.routerAddress.value
            encodedPath = "/goform/goform_get_cmd_process"
        }

        headers {
            header(
                "Referer",
                buildUrl {
                    protocol = URLProtocol.HTTP; host = settingsViewModel.routerAddress.value
                })
        }
    }

    suspend fun fetchSMS() = client.get {
        setupSMSRequest()

        url {
            parameters.append("isTest", "false")
            parameters.append("cmd", "sms_data_total")
            parameters.append("page", "0")
            parameters.append("data_per_page", "5")
            parameters.append("mem_store", "1")
            parameters.append("tags", "1")
            parameters.append("order_by", "order by id desc")
        }
    }.body<MessageResponse>()
}