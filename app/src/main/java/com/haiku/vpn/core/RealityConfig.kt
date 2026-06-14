package com.haiku.vpn.core

import kotlinx.serialization.Serializable
import java.net.URLDecoder
import java.util.UUID

/**
 * Data class representing a VLESS Reality profile configuration.
 */
@Serializable
data class RealityConfig(
    val name: String,
    val countryCode: String = "jp",
    val address: String,
    val port: Int,
    val uuid: String,
    val flow: String = "",
    val publicKey: String,
    val shortId: String = "",
    val sni: String,
    val fingerprint: String = "chrome",
    val network: String = "tcp",
    val grpcServiceName: String = "",
    val wsPath: String = ""
) {
    companion object {
        /**
         * Parsers a vless:// URI string into a RealityConfig.
         * Example: vless://uuid@host:port?security=reality&pbk=PUBLIC_KEY&sid=SHORT_ID&sni=SNI#Name
         */
        fun parseFromUri(uriString: String): RealityConfig {
            val trimmed = uriString.trim()
            if (!trimmed.startsWith("vless://", ignoreCase = true)) {
                throw IllegalArgumentException("Invalid URI: Must start with vless://")
            }

            // Remove prefix and separate fragment (Node Name)
            val withoutPrefix = trimmed.substring(8)
            val parts = withoutPrefix.split("#", limit = 2)
            val mainPart = parts[0]
            val nodeName = if (parts.size > 1) {
                runCatching { URLDecoder.decode(parts[1], "UTF-8") }.getOrDefault(parts[1])
            } else {
                "Haiku Node"
            }
            val nameParts = nodeName.split("|", limit = 2)
            val finalName = nameParts[0].trim()
            val country = if (nameParts.size > 1) nameParts[1].trim().lowercase() else "jp"

            // Separate userInfo (UUID) and host:port?query
            val atSplit = mainPart.split("@", limit = 2)
            if (atSplit.size < 2) {
                throw IllegalArgumentException("Invalid URI: Missing '@' separator for UUID")
            }
            val uuid = atSplit[0]
            val rest = atSplit[1]

            // Separate host info and query parameters
            val querySplit = rest.split("?", limit = 2)
            val hostPort = querySplit[0]
            val queryStr = if (querySplit.size > 1) querySplit[1] else ""

            // Parse host and port
            val portSplit = hostPort.split(":", limit = 2)
            val address = portSplit[0]
            val port = if (portSplit.size > 1) {
                portSplit[1].toIntOrNull() ?: 443
            } else {
                443
            }

            // Parse Query parameters
            val params = parseQueryString(queryStr)

            val security = params["security"] ?: params["security"] ?: ""
            if (!security.equals("reality", ignoreCase = true)) {
                throw IllegalArgumentException("Unsupported security protocol: $security. ONLY 'reality' is supported.")
            }

            val pbk = params["pbk"] ?: params["publickey"] ?: throw IllegalArgumentException("Missing Reality public key ('pbk')")
            val sid = params["sid"] ?: params["shortid"] ?: ""
            val sni = params["sni"] ?: throw IllegalArgumentException("Missing SNI ('sni')")
            val flow = params["flow"] ?: ""
            val fp = params["fp"] ?: params["fingerprint"] ?: "chrome"
            val network = params["type"] ?: params["network"] ?: "tcp"
            val grpcServiceName = params["servicename"] ?: ""
            val wsPath = params["path"] ?: ""

            // Validate UUID format
            try {
                UUID.fromString(uuid)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid UUID format: $uuid")
            }

            return RealityConfig(
                name = finalName,
                countryCode = country,
                address = address,
                port = port,
                uuid = uuid,
                flow = flow,
                publicKey = pbk,
                shortId = sid,
                sni = sni,
                fingerprint = fp,
                network = network,
                grpcServiceName = grpcServiceName,
                wsPath = wsPath
            )
        }

        private fun parseQueryString(query: String): Map<String, String> {
            if (query.isEmpty()) return emptyMap()
            val result = mutableMapOf<String, String>()
            val pairs = query.split("&")
            for (pair in pairs) {
                val idx = pair.indexOf("=")
                if (idx > 0 && idx < pair.length - 1) {
                    val key = pair.substring(0, idx).lowercase()
                    val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    result[key] = value
                }
            }
            return result
        }
    }
}
