package com.shuttlebackend.config;

import org.springframework.context.annotation.Configuration;

/**
 * Deprecated: raw WebSocket configuration replaced by STOMP-based broker.
 * Kept as a placeholder (no bean) to avoid classpath errors until old file is removed.
 */
@Configuration
public class WebSocketConfig {
    // intentionally left blank - STOMP broker used instead in WebSocketBrokerConfig
}
