package com.gdssecurity.editors;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.WebSocketMessage;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorMode;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedWebSocketMessageEditor;
import com.gdssecurity.MessageModel.GenericMessage;
import com.gdssecurity.helpers.BTPConstants;
import com.gdssecurity.helpers.BlazorHelper;
import org.json.JSONArray;
import org.json.JSONException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BTPWebSocketEditor implements ExtensionProvidedWebSocketMessageEditor {

    private MontoyaApi _montoya;
    private WebSocketMessage webSocketMessage;
    private RawEditor editor;
    private BlazorHelper blazorHelper;
    private Logging logging;

    public BTPWebSocketEditor(MontoyaApi api, EditorMode editorMode) {
        this._montoya = api;
        this.editor = this._montoya.userInterface().createRawEditor();
        this.blazorHelper = new BlazorHelper(this._montoya);
        this.logging = this._montoya.logging();
    }

    /**
     * @return The current message set in the editor as an instance of {@link ByteArray}
     */
    @Override
    public ByteArray getMessage() {
        byte[] body;
        if (this.editor.isModified()) {
            body = this.editor.getContents().getBytes();
        } else {
            body = this.webSocketMessage.payload().getBytes();
        }
        if (body == null || body.length == 0) {
            this.logging.logToError("[-] getMessage: The selected editor body is empty/null.");
            return this.webSocketMessage.payload(); // Return original payload instead of null
        }
        JSONArray messages;
        byte[] newBody;
        try {
            messages = new JSONArray(new String(body,StandardCharsets.UTF_8));
            newBody = this.blazorHelper.blazorPack(messages);
        } catch (JSONException e) {
            this.logging.logToError("[-] getMessage - JSONException while parsing JSON array: " + e.getMessage());
            // Return error message as ByteArray instead of null
            String errorMsg = "ERROR: Invalid JSON format - " + e.getMessage();
            return ByteArray.byteArray(errorMsg.getBytes());
        } catch (Exception e) {
            this.logging.logToError("[-] getMessage - Unexpected exception while getting the request: " + e.getMessage());
            String errorMsg = "ERROR: Unexpected error - " + e.getMessage();
            return ByteArray.byteArray(errorMsg.getBytes());
        }
        return ByteArray.byteArray(newBody);
    }

    /**
     * Sets the provided {@link WebSocketMessage} within the editor component.
     *
     * @param message The message to set in the editor.
     */
    @Override
    public void setMessage(WebSocketMessage message) {
        this.webSocketMessage = message;
        byte[] body = webSocketMessage.payload().getBytes();
        ArrayList<GenericMessage> messages = this.blazorHelper.blazorUnpack(body);
        if (messages == null) {
            this.logging.logToError("[-] setMessage - blazorUnpack returned null");
            String errorMsg = "ERROR: Failed to deserialize Blazor data - blazorUnpack returned null";
            this.editor.setContents(ByteArray.byteArray(errorMsg));
            return;
        }
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        try {
            String jsonStrMessages = this.blazorHelper.messageArrayToString(messages);
            outstream.write(jsonStrMessages.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            this.logging.logToError("[-] setMessage - IOException while writing bytes to buffer: " + e.getMessage());
            String errorMsg = "ERROR: IOException while converting Blazor to JSON - " + e.getMessage();
            this.editor.setContents(ByteArray.byteArray(errorMsg));
            return;
        } catch (JSONException e) {
            this.logging.logToError("[-] setMessage - JSONException while parsing JSON array: " + e.getMessage());
            String errorMsg = "ERROR: Failed to deserialize Blazor data - " + e.getMessage();
            this.editor.setContents(ByteArray.byteArray(errorMsg));
            return;
        } catch (Exception e) {
            this.logging.logToError("[-] setMessage - Unexpected exception: " + e.getMessage());
            String errorMsg = "ERROR: Failed to deserialize Blazor data - " + e.getMessage();
            this.editor.setContents(ByteArray.byteArray(errorMsg));
            return;
        }
        this.editor.setContents(ByteArray.byteArray(outstream.toByteArray()));
    }

    /**
     * A check to determine if the Web Socket editor is enabled for a specific {@link WebSocketMessage} message
     *
     * @param message The {@link WebSocketMessage} to check.
     * @return True if the Web Socket message editor is enabled for the provided message.
     */
    @Override
    public boolean isEnabledFor(WebSocketMessage message) {
        if(message == null)
            return false;

        HttpRequest upgradeRequest;
        // Burp Suite bug workaround:
        try{
            upgradeRequest = message.upgradeRequest();
            if(upgradeRequest.url()!=null){
                if (!upgradeRequest.isInScope()) {
                    return false;
                }

                if (!upgradeRequest.url().contains(BTPConstants.BLAZOR_URL)) {
                    if (!upgradeRequest.hasHeader(BTPConstants.SIGNALR_HEADER)) {
                        return false;
                    }
                }
            }
        }catch(Exception e){
            // let's pretend it is all nice and dandy!
            this._montoya.logging().logToError("Ignored error in getting the upgradeRequest(): " + e.getMessage());
        }

        // Response during negotiation containing "{anything}\x1e", not valid blazor and BTP tab shouldn't be enabled
        if (message.payload() != null && message.payload().toString().startsWith("{") && message.payload().toString().endsWith("}\u001E")) {
            return false;
        }

        if(message.payload() != null && !message.payload().toString().isEmpty())
            return true;
        else
            return false;

    }

    /**
     * @return The caption located in the message editor tab header.
     */
    @Override
    public String caption() {
        return BTPConstants.CAPTION;
    }

    /**
     * @return The component that is rendered within the message editor tab.
     */
    @Override
    public Component uiComponent() {
        return this.editor.uiComponent();
    }

    /**
     * The method should return {@code null} if no data has been selected.
     *
     * @return The data that is currently selected by the user.
     */
    @Override
    public Selection selectedData() {
        return this.editor.selection().get();
    }

    /**
     * @return True if the user has modified the current message within the editor.
     */
    @Override
    public boolean isModified() {
        return this.editor.isModified();
    }
}
