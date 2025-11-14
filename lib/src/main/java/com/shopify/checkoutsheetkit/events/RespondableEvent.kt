package com.shopify.checkoutsheetkit.events

/**
 * Interface for checkout events that require a deferred response.
 *
 * Events implementing this interface can be stored and responded to at a later time,
 * allowing for asynchronous user interactions like address or payment selection.
 */
public interface RespondableEvent {
    /**
     * Unique identifier for this event. Used to track and respond to events asynchronously.
     */
    public val id: String?
}
