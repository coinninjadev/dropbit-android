package com.coinninja.coinkeeper.util.crypto

import android.net.Uri
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.util.isNotNullOrEmpty
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

@Mockable
class BitcoinUri {
    private var baseUri = Uri.EMPTY
    val isBip70: Boolean
        get() = baseUri.getQueryParameter(BitcoinParameter.BIP70.parameterKey).isNotNullOrEmpty()
    val merchantUri: Uri?
        get() {
            val url = baseUri.getQueryParameter(BitcoinParameter.BIP70.parameterKey)
            return if (url.isNotNullOrEmpty())
                Uri.parse(url)
            else
                null
        }

    val address: String get() = baseUri.authority ?: ""
    val scheme: String get() = baseUri.scheme ?: ""
    val isValidPaymentAddress: Boolean get() = address.isNotEmpty() || isBip70

    val satoshiAmount: Long
        get() =
            BTCCurrency(
                    baseUri.getQueryParameter(BitcoinParameter.AMOUNT.parameterKey) ?: ""
            ).toSatoshis()


    override fun toString(): String {
        return baseUri.toString().replace("://", ":")
    }

    @Mockable
    class Builder @Inject constructor(
            val bitcoinUtil: BitcoinUtil
    ) {
        private var _address: String = ""
        private var _parameters: MutableMap<BitcoinParameter, String> = mutableMapOf()
        private var _scheme = "bitcoin"

        fun setScheme(scheme: String) {
            if (scheme.isNotNullOrEmpty())
                _scheme = scheme
        }

        fun setAmount(btcCurrency: BTCCurrency): Builder {
            addParameter(BitcoinParameter.AMOUNT, btcCurrency.toUriFormattedString())
            return this
        }

        fun setAddress(address: String): Builder {
            if (bitcoinUtil.isValidBTCAddress(address)) {
                _address = address
            }
            return this
        }

        fun removeAmount(): Builder {
            _parameters.remove(BitcoinParameter.AMOUNT)
            return this
        }

        fun addParameter(key: BitcoinParameter, value: String): Builder {
            _parameters[key] = value
            return this
        }

        fun addParameters(parameters: Map<BitcoinParameter, String>): Builder {
            _parameters.clear()
            _parameters = parameters.toMutableMap()
            return this
        }

        fun build(): BitcoinUri {
            val bitcoinUri = BitcoinUri()
            val builder = Uri.Builder().scheme(_scheme).authority(_address)
            _parameters.keys.forEach { parameter ->
                builder.appendQueryParameter(parameter.parameterKey, _parameters[parameter])
            }
            bitcoinUri.baseUri = builder.build()
            return bitcoinUri
        }

        fun build(address: String, parameters: MutableMap<BitcoinParameter, String>): BitcoinUri {
            setAddress(address)
            addParameters(parameters)
            return build()
        }

        fun build(address: String): BitcoinUri {
            setAddress(address)
            return build()
        }

        fun parse(data: String?): BitcoinUri {
            val baseUri = if (data.isNullOrEmpty())
                return build()
            else
                Uri.parse(data)

            if (baseUri.scheme == "bitcoin") {
                val stringUri = StringUri(data)
                setScheme(stringUri.schema)
                setAddress(stringUri.authority)
                addParameters(stringUri.queryMap)
            } else if (baseUri.scheme == "http" || baseUri.scheme == "https") {
                addParameter(BitcoinParameter.BIP70, baseUri.toString())
            } else {
                val text = parseFromText(data)
                if (text.isNotEmpty()) {
                    val stringUri = StringUri(text)
                    setScheme(stringUri.schema)
                    setAddress(stringUri.authority)
                    addParameters(stringUri.queryMap)
                }
            }
            return build()
        }

        internal fun parseFromText(text: String?): String {
            if (text.isNotNullOrEmpty()) {
                val pattern: Pattern = Pattern.compile(DropbitIntents.BITCOIN_URI_PATTERN)
                val matcher: Matcher = pattern.matcher(text)
                return if (matcher.find()) {
                    matcher.group(0)
                } else {
                    ""
                }
            }
            return ""
        }

    }

    internal class StringUri(val uriString: String) {

        enum class LookupStatus {
            NOT_CACHED, CACHED;
        }

        private var schemaStatus: LookupStatus = LookupStatus.NOT_CACHED
        private var schemaSeparatorStatus: LookupStatus = LookupStatus.NOT_CACHED
        private var _schemaSeparator: Int = -1
        private var _scheme: String = ""
        private var authorityStatus: LookupStatus = LookupStatus.NOT_CACHED
        private var _authority: String = ""
        private var querySeparatorStatus: LookupStatus = LookupStatus.NOT_CACHED
        private var _querySeparator: Int = -1
        private var queryMapStatus: LookupStatus = LookupStatus.NOT_CACHED
        private var _queryMap: MutableMap<BitcoinParameter, String> = mutableMapOf()


        private val schemaSeparator: Int
            get() {
                return when (schemaSeparatorStatus) {
                    LookupStatus.CACHED -> {
                        _schemaSeparator
                    }
                    else -> {
                        _schemaSeparator = uriString.indexOf(":")
                        schemaSeparatorStatus = LookupStatus.CACHED
                        _schemaSeparator
                    }

                }
            }
        private val querySeparator: Int
            get() {
                return when (querySeparatorStatus) {
                    LookupStatus.CACHED -> {
                        _querySeparator
                    }
                    else -> {
                        _querySeparator = uriString.indexOf("?")
                        querySeparatorStatus = LookupStatus.CACHED
                        _querySeparator
                    }
                }
            }

        val schema: String
            get() {
                return when (schemaStatus) {
                    LookupStatus.CACHED -> _scheme
                    else -> {
                        if (schemaSeparator > 0)
                            _scheme = uriString.substring(0, schemaSeparator)
                        schemaStatus = LookupStatus.CACHED
                        _scheme
                    }
                }
            }

        val authority: String
            get() {
                return when (authorityStatus) {
                    LookupStatus.CACHED -> _authority
                    else -> {
                        val end = if (querySeparator > 0) querySeparator else uriString.length
                        _authority = uriString.substring(schemaSeparator + 1, end)
                        authorityStatus = LookupStatus.CACHED
                        _authority
                    }
                }

            }

        val queryMap: MutableMap<BitcoinParameter, String>
            get() {
                return when (queryMapStatus) {
                    LookupStatus.CACHED -> _queryMap
                    else -> {
                        if (querySeparator > 0) {
                            val queryString = URLDecoder.decode(uriString.substring(querySeparator + 1, uriString.length), "UTF-8")
                            queryString.split("&").forEach { param ->
                                val parts = param.split("=")
                                BitcoinParameter.from(parts[0])?.let { bitcoinParameter ->
                                    if (parts.size == 2) {
                                        _queryMap[bitcoinParameter] = parts[1]
                                    } else if (parts.size == 3) {
                                        val encodedPart = URLEncoder.encode("=${parts[2]}", "UTF-8")
                                        _queryMap[bitcoinParameter] = "${parts[1]}${encodedPart}"
                                    }
                                }
                            }
                        }
                        queryMapStatus = LookupStatus.CACHED
                        _queryMap
                    }
                }
            }
    }
}