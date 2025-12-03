/**
 * Copyright 2023 Aon plc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gdssecurity.handlers;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.http.InterceptedRequest;
import burp.api.montoya.proxy.http.ProxyRequestHandler;
import burp.api.montoya.proxy.http.ProxyRequestReceivedAction;
import burp.api.montoya.proxy.http.ProxyRequestToBeSentAction;
import com.gdssecurity.helpers.BTPConstants;

/**
 * Class to handle the highlighting of Blazor traffic in HTTP requests
 */
public class BTPHttpRequestHandler implements ProxyRequestHandler {

    private MontoyaApi _montoya;
    private Logging _logging;

    /**
     * Constructor for the BTPHttpRequestHandler object
     * @param montoyaApi - an instance of the Burp Montoya APIs
     */
    public BTPHttpRequestHandler(MontoyaApi montoyaApi) {
        this._montoya = montoyaApi;
        this._logging = montoyaApi.logging();
    }

    /**
     * Handles the highlighting by listening for matching HTTP requests and auto-highlighting them
     * @param interceptedRequest - An object containing the intercepted HTTP request
     * @return the request object with highlighting applied if applicable, otherwise just let the request go through un-touched
     */
    @Override
    public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
        // Highlight Blazor traffic
        if (interceptedRequest.body().length() != 0 && interceptedRequest.path().contains("_blazor?id")) {
            HighlightColor highlightColor = getHighlightColor();
            interceptedRequest.annotations().setHighlightColor(highlightColor);
        }
        // Highlight Blazor Traffic via SignalR Header
        if (interceptedRequest.hasHeader(BTPConstants.SIGNALR_HEADER)) {
            HighlightColor highlightColor = getHighlightColor();
            interceptedRequest.annotations().setHighlightColor(highlightColor);
        }
        // Highlight Blazor negotiation requests
        if (interceptedRequest.url().contains(BTPConstants.NEGOTIATE_URL)) {
            HighlightColor highlightColor = getHighlightColor();
            interceptedRequest.annotations().setHighlightColor(highlightColor);
        }

        return ProxyRequestReceivedAction.continueWith(interceptedRequest);
    }

    /**
     * Handles the logic for after a request has been processed
     * Just forward along the un-touched request since it was already modified by handleRequestReceived
     * @param interceptedRequest - An object holding the HTTP request right before it is sent
     * @return the un-touched request object
     */
    @Override
    public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
        return ProxyRequestToBeSentAction.continueWith(interceptedRequest);
    }

    /**
     * Gets the user-selected highlight color from preferences, defaults to CYAN if not set
     * @return the HighlightColor to use for highlighting Blazor traffic
     */
    private HighlightColor getHighlightColor() {
        try {
            String savedColor = this._montoya.persistence().preferences().getString("blazor_highlight_color");
            if (savedColor != null) {
                return HighlightColor.valueOf(savedColor);
            }
        } catch (IllegalArgumentException e) {
            this._logging.logToError("[-] Invalid highlight color preference, using default CYAN: " + e.getMessage());
        }
        return HighlightColor.CYAN; // Default fallback
    }
}
