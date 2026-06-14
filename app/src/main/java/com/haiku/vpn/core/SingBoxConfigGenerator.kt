package com.haiku.vpn.core

import kotlinx.serialization.json.*

/**
 * Helper class to generate JSON configuration strings for V2Ray/Xray core (libv2ray).
 */
object SingBoxConfigGenerator {

    /**
     * Generates a complete JSON configuration for v2ray/xray core.
     * Uses standard V2Ray v4 format compatible with Xray-core in libv2ray.
     */
    fun generate(config: RealityConfig): String {
        return buildJsonObject {
            // Log block
            put("log", buildJsonObject {
                put("loglevel", "warning")
            })

            // Inbounds block (Standard socks inbound that libv2ray intercepts)
            putJsonArray("inbounds") {
                add(buildJsonObject {
                    put("port", 10808)
                    put("protocol", "socks")
                    put("listen", "127.0.0.1")
                    put("settings", buildJsonObject {
                        put("auth", "noauth")
                        put("udp", true)
                        put("ip", "127.0.0.1")
                    })
                    put("sniffing", buildJsonObject {
                        put("enabled", true)
                        putJsonArray("destOverride") {
                            add("http")
                            add("tls")
                        }
                    })
                })
            }

            // Outbounds block
            putJsonArray("outbounds") {
                // Main Proxy (VLESS Reality / TLS)
                add(buildJsonObject {
                    put("protocol", "vless")
                    put("tag", "proxy")
                    
                    put("settings", buildJsonObject {
                        putJsonArray("vnext") {
                            add(buildJsonObject {
                                put("address", config.address)
                                put("port", config.port)
                                putJsonArray("users") {
                                    add(buildJsonObject {
                                        put("id", config.uuid)
                                        put("encryption", "none")
                                        if (config.flow.isNotEmpty()) {
                                            put("flow", config.flow)
                                        }
                                    })
                                }
                            })
                        }
                    })

                    // Stream settings
                    put("streamSettings", buildJsonObject {
                        val transportNetwork = if (config.network.isNotEmpty()) config.network else "tcp"
                        put("network", transportNetwork)
                        
                        val isReality = config.publicKey.isNotEmpty()
                        val securityType = if (isReality) "reality" else "tls"
                        put("security", securityType)

                        if (isReality) {
                            put("realitySettings", buildJsonObject {
                                put("show", false)
                                put("fingerprint", if (config.fingerprint.isNotEmpty()) config.fingerprint else "chrome")
                                put("serverName", config.sni)
                                put("publicKey", config.publicKey)
                                put("shortId", config.shortId)
                                put("spiderX", "")
                            })
                        } else {
                            put("tlsSettings", buildJsonObject {
                                put("serverName", config.sni)
                                put("allowInsecure", false)
                                put("fingerprint", if (config.fingerprint.isNotEmpty()) config.fingerprint else "chrome")
                            })
                        }

                        // Transport settings
                        if (transportNetwork == "ws") {
                            put("wsSettings", buildJsonObject {
                                put("path", config.wsPath)
                                put("headers", buildJsonObject {
                                    put("Host", config.sni)
                                })
                            })
                        } else if (transportNetwork == "grpc") {
                            put("grpcSettings", buildJsonObject {
                                put("serviceName", config.grpcServiceName)
                                put("multiMode", true)
                            })
                        }
                    })
                })

                // Direct connection outbound
                add(buildJsonObject {
                    put("protocol", "freedom")
                    put("settings", buildJsonObject {})
                    put("tag", "direct")
                })

                // Blocked connection outbound
                add(buildJsonObject {
                    put("protocol", "blackhole")
                    put("settings", buildJsonObject {})
                    put("tag", "blocked")
                })
            }

            // Routing rules
            put("routing", buildJsonObject {
                put("domainStrategy", "IPIfNonMatch")
                putJsonArray("rules") {
                    // Direct bypass for private / local IPs
                    add(buildJsonObject {
                        put("type", "field")
                        put("outboundTag", "direct")
                        putJsonArray("ip") {
                            add("geoip:private")
                        }
                    })
                    // Direct bypass for common local DNS addresses
                    add(buildJsonObject {
                        put("type", "field")
                        put("outboundTag", "direct")
                        putJsonArray("ip") {
                            add("1.1.1.1")
                            add("8.8.8.8")
                        }
                    })
                }
            })

            // DNS settings
            put("dns", buildJsonObject {
                putJsonArray("servers") {
                    add("1.1.1.1")
                    add("8.8.8.8")
                }
            })
        }.toString()
    }
}
