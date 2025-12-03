# Changelog

All notable changes to the Blazor Traffic Processor (BTP) extension will be documented in this file.

## [1.2] - 2025-09-01

### Added
- **Enhanced Editor Syntax Highlighting**: Updated HTTP editors to use appropriate Burp Montoya API editors.
- **HTTP Request Editor**: Now uses `HttpRequestEditor` for proper HTTP syntax highlighting
- **HTTP Response Editor**: Now uses `HttpResponseEditor` for proper HTTP response formatting
- **Customizable Highlight Colors**: Added highlight color selection dropdown in the BTP main tab
  - Users can now choose from 9 different highlight colors (CYAN, BLUE, GREEN, YELLOW, ORANGE, RED, PINK, MAGENTA, GRAY)
  - Color preference is automatically saved and restored
  - Default color remains CYAN for backward compatibility
- **Updated Build instructions**: For easier local building without installing further tools.
- **Updated Gradle Build File**: Made some improvements, updated libraries, and removed other not needed anymore from the build.gradle file.

### Fixed
- **BTP Tab JSON Syntax Highlighting**: Fixed issue where JSON responses in BTP tab were not properly colored
  - Now sets `Content-Type: application/json` header using `withUpdatedHeader()` method
  - Enables proper JSON syntax highlighting and coloring in the BTP response editor tab
- **Improved Error Handling**: Fixed critical issue where editors returned `null` on JSON parsing errors
  - **BTPHttpRequestEditor**: Now returns original request with error message instead of `null`
  - **BTPHttpResponseEditor**: Enhanced error messages with specific error details
  - **BTPWebSocketEditor**: Now returns error message as ByteArray instead of `null`
  - Prevents Burp crashes and provides clear error feedback to users
  - All error messages are logged and displayed in the editor for debugging
- **Null Pointer Exception Fix**: Fixed null pointer exception when processing malformed Blazor data
  - **BTPHttpRequestEditor**: Added null check for `blazorUnpack()` result before processing messages
  - **BlazorHelper.messageArrayToString**: Added null check for individual messages before calling `toJsonString()`
  - **BlazorHelper.blazorUnpack**: Improved error handling when `initializeMessage()` returns null
  - Now handles malformed Blazor data (e.g., unterminated strings) gracefully with error messages
  - Prevents extension crashes and provides meaningful error feedback instead of null pointer exceptions
- **Scope Check Error Fix**: Fixed `IllegalArgumentException` when checking if URL is in scope with empty scope or invalid URLs
  - **BTPHttpRequestEditor.isEnabledFor()**: Added comprehensive validation before scope checking
    - Validates scope object is not null before use
    - Validates URL is not null or blank before passing to `isInScope()`
    - Wraps `isInScope()` call in try-catch to handle `IllegalArgumentException`
    - Stores URL in variable to avoid multiple method calls
  - **BTPHttpResponseEditor.isEnabledFor()**: Applied same validation improvements
  - Prevents crashes when no URLs are added to Burp's scope or when URL is invalid/blank
  - Editors gracefully return `false` instead of throwing exceptions, preventing extension errors

### Technical Improvements
- **BTPHttpRequestEditor**: 
  - Changed from `RawEditor` to `HttpRequestEditor`
  - Updated method calls: `getContents()` → `getRequest()`, `setContents()` → `setRequest()`
  - Simplified body extraction logic
  - Updated response content-type header in BTP request view to match JSON body
- **BTPHttpResponseEditor**:
  - Changed from `RawEditor` to `HttpResponseEditor`
  - Updated method calls: `setContents()` → `setResponse()`
  - Improved error handling with proper HTTP response objects
  - Updated response content-type header in BTP response view to match JSON body
- **BTPWebSocketEditor**:
  - Kept as `RawEditor` since WebSocket messages don't have HTTP structure
  - Uses `getContents()` and `setContents()` methods for raw message handling
  - Maintains compatibility with WebSocket message format
- **BTPView (Main Tab)**:
  - Added new options panel with highlight color selection
  - Integrated highlight color preference with existing WebSocket preference
  - Improved UI layout with better organization of controls
- **BTPHttpRequestHandler & BTPHttpResponseHandler**:
  - Added `getHighlightColor()` method to retrieve user preference
  - Replaced hardcoded `HighlightColor.CYAN` with dynamic color selection
  - Added error handling for invalid color preferences

## [1.1] - Previous Release

### Initial Features
- Blazor SignalR traffic processing and conversion
- HTTP request/response editors for Blazor traffic
- WebSocket message editor for Blazor WebSocket traffic
- Main BTP tab for ad-hoc conversions
- Context menu integration
- Automatic WebSocket to HTTP downgrade handling

---

## Notes

- All changes maintain backward compatibility
- Extension continues to work with existing Blazor applications
- No changes to core Blazor processing logic
- Performance improvements through better API usage
- Highlight color preference is automatically saved and restored between sessions
- WebSocket editor maintains RawEditor for compatibility with WebSocket message format
