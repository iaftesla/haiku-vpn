package com.haiku.vpn.core

import kotlinx.serialization.json.*

/**
 * Helper class to generate JSON configuration strings for sing-box-core.
 */
object SingBoxConfigGenerator {

    /**
     * Generates a complete JSON configuration for sing-box.
     * Uses TUN inbound to route all traffic on the device through the VLESS Reality proxy.
     */
    fun generate(config: RealityConfig): String {
        return buildJsonObject {
            // Log block
            put("log", buildJsonObject {
                put("level", "info")
                put("timestamp", true)
            })

            // Inbounds block (TUN interface for Android VPN)
            putJsonArray("inbounds") {
                add(buildJsonObject {
                    put("type", "tun")
                    put("tag", "tun-in")
                    put("interface_name", "tun0")
                    put("inet4_address", "172.19.0.1/30")
                    put("mtu", 1400)
                    put("auto_route", true)
                    put("strict_route", true)
                    put("stack", "gvisor") // gvisor stack works best for mobile userspace TCP/IP
                    put("sniff", true)
                })
            }

            // Outbounds block
            putJsonArray("outbounds") {
                // Main Proxy (VLESS Reality)
                add(buildJsonObject {
                    put("type", "vless")
                    put("tag", "proxy")
                    put("server", config.address)
                    put("server_port", config.port)
                    put("uuid", config.uuid)
                    if (config.flow.isNotEmpty()) {
                        put("flow", config.flow)
                    }

                    // TLS & Reality settings
                    put("tls", buildJsonObject {
                        put("enabled", true)
                        put("server_name", config.sni)
                        put("utls", buildJsonObject {
                            put("enabled", true)
                            put("fingerprint", config.fingerprint)
                        })
                        if (config.publicKey.isNotEmpty()) {
                            put("reality", buildJsonObject {
                                put("enabled", true)
                                put("public_key", config.publicKey)
                                put("short_id", config.shortId)
                            })
                        }
                    })
                    
                    put("packet_encoding", "xudp")
                })

                // Direct connection for local bypass / DNS fallback
                add(buildJsonObject {
                    put("type", "direct")
                    put("tag", "direct")
                })

                // DNS outbound
                add(buildJsonObject {
                    put("type", "dns")
                    put("tag", "dns-out")
                })
            }

            // Route rules
            put("route", buildJsonObject {
                putJsonArray("rules") {
                    add(buildJsonObject {
                        putJsonArray("protocol") {
                            add("dns")
                        }
                        put("outbound", "dns-out")
                    })
                    add(buildJsonObject {
                        put("port", 53)
                        put("outbound", "dns-out")
                    })
                }
                put("auto_detect_interface", true)
            })

            // DNS servers
            put("dns", buildJsonObject {
                putJsonArray("servers") {
                    add(buildJsonObject {
                        put("tag", "dns-proxy")
                        put("address", "8.8.8.8")
                        put("detour", "proxy")
                    })
                    add(buildJsonObject {
                        put("tag", "dns-direct")
                        put("address", "1.1.1.1")
                        put("detour", "direct")
                    })
                }
                putJsonArray("rules") {
                    add(buildJsonObject {
                        put("outbound", "direct")
                        put("server", "dns-direct")
                    })
                }
            })
        }.toString()
    }
}
