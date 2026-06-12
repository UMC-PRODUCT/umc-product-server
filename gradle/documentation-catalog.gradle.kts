import java.io.File
import java.util.Locale

data class ErrorCodeSpecMetadata(
    val description: String?,
    val clientAction: String?,
    val retryable: Boolean?,
    val severity: String?,
    val deprecated: Boolean,
    val replacementCode: String?,
    val owners: List<String>,
    val tags: List<String>
)

data class ErrorCodeSource(
    val enumName: String,
    val file: String,
    val line: Int
)

data class ErrorCodeDocumentationEntry(
    val sequence: Int,
    val domain: String,
    val code: String,
    val name: String,
    val httpStatus: Int,
    val httpStatusName: String,
    val message: String,
    val description: String?,
    val clientAction: String?,
    val retryable: Boolean?,
    val severity: String?,
    val deprecated: Boolean,
    val replacementCode: String?,
    val owners: List<String>,
    val tags: List<String>,
    val source: ErrorCodeSource
)

val documentationSourceRoot = file("src/main/java")
val errorCodeCatalogMarkdownFile = file("docs/guides/ErrorCode_목록.md")
val errorCodeCatalogJsonFile = file("docs/guides/ErrorCode_목록.json")
val errorCodeCatalogSchemaFile = file("docs/guides/ErrorCode_목록.schema.json")
val staticErrorCodeCatalogMarkdownFile = file("src/main/resources/static/docs/catalog/error/catalog.md")
val staticErrorCodeCatalogJsonFile = file("src/main/resources/static/docs/catalog/error/catalog.json")
val staticErrorCodeCatalogSchemaFile = file("src/main/resources/static/docs/catalog/error/catalog.schema.json")
val staticErrorCodeCatalogIndexFile = file("src/main/resources/static/docs/catalog/error/index.html")
val apiCatalogFilesToRemove = listOf(
    file("docs/guides/API_목록.md"),
    file("docs/guides/API_목록.json"),
    file("src/main/resources/static/docs/catalog/api/catalog.md"),
    file("src/main/resources/static/docs/catalog/api/catalog.json"),
    file("src/main/resources/static/docs/catalog/api/index.html")
)

val errorCodeCatalogServiceName = "umc-product-server"
val errorCodeCatalogSchemaVersion = 1

fun String.escapeMarkdownCell(): String = replace("|", "\\|").replace("\n", "<br>")

fun String.escapeJson(): String = buildString {
    this@escapeJson.forEach { ch ->
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
}

fun String.escapeHtml(): String = buildString {
    this@escapeHtml.forEach { ch ->
        when (ch) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&#39;")
            else -> append(ch)
        }
    }
}

fun unescapeJavaString(value: String): String = buildString {
    var index = 0
    while (index < value.length) {
        val ch = value[index]
        if (ch == '\\' && index + 1 < value.length) {
            when (val next = value[index + 1]) {
                '"' -> append('"')
                '\\' -> append('\\')
                'n' -> append('\n')
                'r' -> append('\r')
                't' -> append('\t')
                else -> append(next)
            }
            index += 2
        } else {
            append(ch)
            index += 1
        }
    }
}

fun domainFromPackage(packageName: String): String {
    return packageName
        .removePrefix("com.umc.product.")
        .substringBefore(".")
        .ifBlank { "unknown" }
}

fun httpStatusCode(statusName: String): Int = mapOf(
    "CONTINUE" to 100,
    "SWITCHING_PROTOCOLS" to 101,
    "PROCESSING" to 102,
    "EARLY_HINTS" to 103,
    "OK" to 200,
    "CREATED" to 201,
    "ACCEPTED" to 202,
    "NON_AUTHORITATIVE_INFORMATION" to 203,
    "NO_CONTENT" to 204,
    "RESET_CONTENT" to 205,
    "PARTIAL_CONTENT" to 206,
    "MULTI_STATUS" to 207,
    "ALREADY_REPORTED" to 208,
    "IM_USED" to 226,
    "MULTIPLE_CHOICES" to 300,
    "MOVED_PERMANENTLY" to 301,
    "FOUND" to 302,
    "SEE_OTHER" to 303,
    "NOT_MODIFIED" to 304,
    "TEMPORARY_REDIRECT" to 307,
    "PERMANENT_REDIRECT" to 308,
    "BAD_REQUEST" to 400,
    "UNAUTHORIZED" to 401,
    "PAYMENT_REQUIRED" to 402,
    "FORBIDDEN" to 403,
    "NOT_FOUND" to 404,
    "METHOD_NOT_ALLOWED" to 405,
    "NOT_ACCEPTABLE" to 406,
    "PROXY_AUTHENTICATION_REQUIRED" to 407,
    "REQUEST_TIMEOUT" to 408,
    "CONFLICT" to 409,
    "GONE" to 410,
    "LENGTH_REQUIRED" to 411,
    "PRECONDITION_FAILED" to 412,
    "PAYLOAD_TOO_LARGE" to 413,
    "URI_TOO_LONG" to 414,
    "UNSUPPORTED_MEDIA_TYPE" to 415,
    "REQUESTED_RANGE_NOT_SATISFIABLE" to 416,
    "EXPECTATION_FAILED" to 417,
    "I_AM_A_TEAPOT" to 418,
    "UNPROCESSABLE_ENTITY" to 422,
    "LOCKED" to 423,
    "FAILED_DEPENDENCY" to 424,
    "TOO_EARLY" to 425,
    "UPGRADE_REQUIRED" to 426,
    "PRECONDITION_REQUIRED" to 428,
    "TOO_MANY_REQUESTS" to 429,
    "REQUEST_HEADER_FIELDS_TOO_LARGE" to 431,
    "UNAVAILABLE_FOR_LEGAL_REASONS" to 451,
    "INTERNAL_SERVER_ERROR" to 500,
    "NOT_IMPLEMENTED" to 501,
    "BAD_GATEWAY" to 502,
    "SERVICE_UNAVAILABLE" to 503,
    "GATEWAY_TIMEOUT" to 504,
    "HTTP_VERSION_NOT_SUPPORTED" to 505,
    "VARIANT_ALSO_NEGOTIATES" to 506,
    "INSUFFICIENT_STORAGE" to 507,
    "LOOP_DETECTED" to 508,
    "BANDWIDTH_LIMIT_EXCEEDED" to 509,
    "NOT_EXTENDED" to 510,
    "NETWORK_AUTHENTICATION_REQUIRED" to 511
)[statusName] ?: 0

fun parseStringAttribute(text: String, attributeName: String): String? {
    val match = Regex(
        "\\b${Regex.escape(attributeName)}\\s*=\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
        setOf(RegexOption.DOT_MATCHES_ALL)
    ).find(text)

    return match
        ?.groupValues
        ?.get(1)
        ?.let(::unescapeJavaString)
        ?.takeUnless { it.isBlank() }
}

fun parseBooleanAttribute(text: String, attributeName: String): Boolean? {
    val match = Regex("\\b${Regex.escape(attributeName)}\\s*=\\s*(true|false)").find(text)
    return match?.groupValues?.get(1)?.toBooleanStrict()
}

fun parseSeverityAttribute(text: String): String? {
    val match = Regex("\\bseverity\\s*=\\s*(?:ErrorCodeSeverity\\.)?([A-Z_]+)").find(text)
    val value = match?.groupValues?.get(1)
    return value?.takeUnless { it == "UNSPECIFIED" }
}

fun parseStringArrayAttribute(text: String, attributeName: String): List<String> {
    val arrayMatch = Regex(
        "\\b${Regex.escape(attributeName)}\\s*=\\s*\\{([^}]*)}",
        setOf(RegexOption.DOT_MATCHES_ALL)
    ).find(text)

    if (arrayMatch != null) {
        return Regex("\"((?:\\\\.|[^\"\\\\])*)\"")
            .findAll(arrayMatch.groupValues[1])
            .map { unescapeJavaString(it.groupValues[1]) }
            .filter { it.isNotBlank() }
            .toList()
    }

    val singleMatch = Regex(
        "\\b${Regex.escape(attributeName)}\\s*=\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
        setOf(RegexOption.DOT_MATCHES_ALL)
    ).find(text)

    return singleMatch
        ?.let { listOf(unescapeJavaString(it.groupValues[1])) }
        ?.filter { it.isNotBlank() }
        ?: emptyList()
}

fun extractErrorCodeSpecMetadata(annotations: List<String>): ErrorCodeSpecMetadata {
    val annotation = annotations.firstOrNull { it.contains("ErrorCodeSpec") }
        ?: return ErrorCodeSpecMetadata(
            description = null,
            clientAction = null,
            retryable = null,
            severity = null,
            deprecated = false,
            replacementCode = null,
            owners = emptyList(),
            tags = emptyList()
        )

    return ErrorCodeSpecMetadata(
        description = parseStringAttribute(annotation, "description"),
        clientAction = parseStringAttribute(annotation, "clientAction"),
        retryable = parseBooleanAttribute(annotation, "retryable"),
        severity = parseSeverityAttribute(annotation),
        deprecated = parseBooleanAttribute(annotation, "deprecated") ?: false,
        replacementCode = parseStringAttribute(annotation, "replacementCode"),
        owners = parseStringArrayAttribute(annotation, "owners"),
        tags = parseStringArrayAttribute(annotation, "tags")
    )
}

fun appendJsonStringOrNull(builder: StringBuilder, property: String, value: String?, trailingComma: Boolean = true) {
    builder.append("      \"$property\": ")
    if (value == null) {
        builder.append("null")
    } else {
        builder.append("\"").append(value.escapeJson()).append("\"")
    }
    builder.appendLine(if (trailingComma) "," else "")
}

fun appendJsonBooleanOrNull(builder: StringBuilder, property: String, value: Boolean?, trailingComma: Boolean = true) {
    builder.append("      \"$property\": ")
    builder.append(value?.toString() ?: "null")
    builder.appendLine(if (trailingComma) "," else "")
}

fun appendJsonStringArray(builder: StringBuilder, property: String, values: List<String>, trailingComma: Boolean = true) {
    builder.append("      \"$property\": [")
    builder.append(values.joinToString(", ") { "\"${it.escapeJson()}\"" })
    builder.append("]")
    builder.appendLine(if (trailingComma) "," else "")
}

fun extractErrorCodeEntries(): List<ErrorCodeDocumentationEntry> {
    val entries = mutableListOf<ErrorCodeDocumentationEntry>()

    documentationSourceRoot
        .walkTopDown()
        .filter { it.isFile && it.name.endsWith("ErrorCode.java") }
        .forEach { sourceFile ->
            val lines = sourceFile.readLines()
            val sourceText = lines.joinToString("\n")
            val relativePath = sourceFile.relativeTo(project.projectDir).invariantSeparatorsPath
            val packageName = lines.firstOrNull { it.trim().startsWith("package ") }
                ?.trim()
                ?.removePrefix("package ")
                ?.removeSuffix(";")
                .orEmpty()
            val domain = domainFromPackage(packageName)
            val enumName = Regex("\\benum\\s+([A-Za-z_][A-Za-z0-9_]*)")
                .find(sourceText)
                ?.groupValues
                ?.get(1)
                ?: sourceFile.nameWithoutExtension

            var inConstants = false
            var chunk = StringBuilder()
            var chunkStartLine = 0
            var balance = 0
            var pendingAnnotations = mutableListOf<String>()
            var annotationChunk = StringBuilder()
            var annotationBalance = 0

            lines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                if (!inConstants && Regex("\\benum\\s+$enumName\\b").containsMatchIn(trimmed)) {
                    inConstants = true
                    return@forEachIndexed
                }

                if (!inConstants || trimmed.isBlank() || trimmed.startsWith("//")) {
                    return@forEachIndexed
                }

                if (chunk.isEmpty() && (trimmed.startsWith("@") || annotationChunk.isNotEmpty())) {
                    annotationChunk.append(trimmed).append(' ')
                    annotationBalance += trimmed.count { it == '(' } - trimmed.count { it == ')' }
                    if (annotationBalance <= 0) {
                        pendingAnnotations.add(annotationChunk.toString().trim())
                        annotationChunk = StringBuilder()
                        annotationBalance = 0
                    }
                    return@forEachIndexed
                }

                if (trimmed == ";") {
                    inConstants = false
                    return@forEachIndexed
                }

                if (chunk.isEmpty()) {
                    chunkStartLine = index + 1
                }

                chunk.append(trimmed).append(' ')
                balance += trimmed.count { it == '(' } - trimmed.count { it == ')' }

                if (balance == 0 && (trimmed.endsWith(",") || trimmed.endsWith(";"))) {
                    val constantText = chunk.toString().trim().removeSuffix(",").removeSuffix(";")
                    chunk = StringBuilder()

                    val match = Regex(
                        "^([A-Z0-9_]+)\\s*\\(\\s*HttpStatus\\.([A-Z0-9_]+)\\s*,\\s*\"([^\"]+)\"\\s*,\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
                        RegexOption.DOT_MATCHES_ALL
                    ).find(constantText)

                    if (match != null) {
                        val metadata = extractErrorCodeSpecMetadata(pendingAnnotations)
                        val httpStatusName = match.groupValues[2]
                        entries.add(
                            ErrorCodeDocumentationEntry(
                                sequence = 0,
                                domain = domain,
                                code = match.groupValues[3],
                                name = match.groupValues[1],
                                httpStatus = httpStatusCode(httpStatusName),
                                httpStatusName = httpStatusName,
                                message = unescapeJavaString(match.groupValues[4]),
                                description = metadata.description,
                                clientAction = metadata.clientAction,
                                retryable = metadata.retryable,
                                severity = metadata.severity,
                                deprecated = metadata.deprecated,
                                replacementCode = metadata.replacementCode,
                                owners = metadata.owners,
                                tags = metadata.tags,
                                source = ErrorCodeSource(
                                    enumName = enumName,
                                    file = relativePath,
                                    line = chunkStartLine
                                )
                            )
                        )
                    }

                    pendingAnnotations = mutableListOf()
                    balance = 0
                }
            }
        }

    return entries
        .sortedWith(compareBy<ErrorCodeDocumentationEntry> { it.domain }.thenBy { it.code }.thenBy { it.name })
        .mapIndexed { index, entry -> entry.copy(sequence = index + 1) }
}

fun buildErrorCodeCatalogMarkdown(entries: List<ErrorCodeDocumentationEntry>): String = buildString {
    appendLine("# ErrorCode Catalog")
    appendLine()
    appendLine("서버가 반환하는 ErrorCode를 독립 규격(v1)으로 정리합니다.")
    appendLine()
    appendLine("> 소스 기준: 각 도메인의 `*ErrorCode.java` enum과 선택적 `@ErrorCodeSpec` 메타데이터를 스캔합니다. 갱신: `./gradlew generateDocumentationCatalogs`")
    appendLine()

    entries.groupBy { it.domain }.forEach { (domain, domainEntries) ->
        appendLine("## $domain")
        appendLine()
        appendLine("| 순번 | 도메인 | Code | Name | HTTP Status | Message | Client Action | Retryable | Severity | Deprecated | Owners | Tags | Source |")
        appendLine("|---:|---|---|---|---|---|---|---|---|---|---|---|---|")
        domainEntries.forEach { entry ->
            appendLine(
                "| ${entry.sequence} | ${entry.domain.escapeMarkdownCell()} | `${entry.code.escapeMarkdownCell()}` | `${entry.name.escapeMarkdownCell()}` | ${entry.httpStatus} ${entry.httpStatusName.escapeMarkdownCell()} | ${entry.message.escapeMarkdownCell()} | ${(entry.clientAction ?: "").escapeMarkdownCell()} | ${entry.retryable?.toString() ?: ""} | ${entry.severity ?: ""} | ${entry.deprecated} | ${entry.owners.joinToString(", ").escapeMarkdownCell()} | ${entry.tags.joinToString(", ").escapeMarkdownCell()} | `${entry.source.file}:${entry.source.line}` |"
            )
        }
        appendLine()
    }
}

fun buildErrorCodeCatalogJson(entries: List<ErrorCodeDocumentationEntry>): String = buildString {
    appendLine("{")
    appendLine("  \"schemaVersion\": $errorCodeCatalogSchemaVersion,")
    appendLine("  \"service\": \"$errorCodeCatalogServiceName\",")
    appendLine("  \"generatedAt\": null,")
    appendLine("  \"totalCount\": ${entries.size},")
    appendLine("  \"items\": [")
    entries.forEachIndexed { index, entry ->
        appendLine("    {")
        appendLine("      \"sequence\": ${entry.sequence},")
        appendLine("      \"domain\": \"${entry.domain.escapeJson()}\",")
        appendLine("      \"code\": \"${entry.code.escapeJson()}\",")
        appendLine("      \"name\": \"${entry.name.escapeJson()}\",")
        appendLine("      \"httpStatus\": ${entry.httpStatus},")
        appendLine("      \"httpStatusName\": \"${entry.httpStatusName.escapeJson()}\",")
        appendLine("      \"message\": \"${entry.message.escapeJson()}\",")
        appendJsonStringOrNull(this, "description", entry.description)
        appendJsonStringOrNull(this, "clientAction", entry.clientAction)
        appendJsonBooleanOrNull(this, "retryable", entry.retryable)
        appendJsonStringOrNull(this, "severity", entry.severity)
        appendLine("      \"deprecated\": ${entry.deprecated},")
        appendJsonStringOrNull(this, "replacementCode", entry.replacementCode)
        appendJsonStringArray(this, "owners", entry.owners)
        appendJsonStringArray(this, "tags", entry.tags)
        appendLine("      \"source\": {")
        appendLine("        \"enumName\": \"${entry.source.enumName.escapeJson()}\",")
        appendLine("        \"file\": \"${entry.source.file.escapeJson()}\",")
        appendLine("        \"line\": ${entry.source.line}")
        appendLine("      }")
        append("    }")
        appendLine(if (index == entries.lastIndex) "" else ",")
    }
    appendLine("  ]")
    appendLine("}")
}

fun buildErrorCodeCatalogSchemaJson(): String = """
{
  "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
  "${'$'}id": "https://umc-product.dev/schemas/error-code-catalog.v1.schema.json",
  "title": "ErrorCodeCatalog",
  "type": "object",
  "additionalProperties": false,
  "required": ["schemaVersion", "service", "generatedAt", "totalCount", "items"],
  "properties": {
    "schemaVersion": { "const": 1 },
    "service": { "const": "umc-product-server" },
    "generatedAt": { "type": ["string", "null"], "format": "date-time" },
    "totalCount": { "type": "integer", "minimum": 0 },
    "items": {
      "type": "array",
      "items": { "${'$'}ref": "#/${'$'}defs/ErrorCodeCatalogItem" }
    }
  },
  "${'$'}defs": {
    "ErrorCodeCatalogItem": {
      "type": "object",
      "additionalProperties": false,
      "required": [
        "sequence",
        "domain",
        "code",
        "name",
        "httpStatus",
        "httpStatusName",
        "message",
        "description",
        "clientAction",
        "retryable",
        "severity",
        "deprecated",
        "replacementCode",
        "owners",
        "tags",
        "source"
      ],
      "properties": {
        "sequence": { "type": "integer", "minimum": 1 },
        "domain": { "type": "string", "minLength": 1 },
        "code": { "type": "string", "minLength": 1 },
        "name": { "type": "string", "minLength": 1 },
        "httpStatus": { "type": "integer", "minimum": 100, "maximum": 599 },
        "httpStatusName": { "type": "string", "minLength": 1 },
        "message": { "type": "string", "minLength": 1 },
        "description": { "type": ["string", "null"] },
        "clientAction": { "type": ["string", "null"] },
        "retryable": { "type": ["boolean", "null"] },
        "severity": { "enum": ["INFO", "WARNING", "ERROR", "CRITICAL", null] },
        "deprecated": { "type": "boolean" },
        "replacementCode": { "type": ["string", "null"] },
        "owners": {
          "type": "array",
          "items": { "type": "string" }
        },
        "tags": {
          "type": "array",
          "items": { "type": "string" }
        },
        "source": {
          "type": "object",
          "additionalProperties": false,
          "required": ["enumName", "file", "line"],
          "properties": {
            "enumName": { "type": "string", "minLength": 1 },
            "file": { "type": "string", "minLength": 1 },
            "line": { "type": "integer", "minimum": 1 }
          }
        }
      }
    }
  }
}
""".trimIndent() + "\n"

fun buildCatalogIndexHtml(entries: List<ErrorCodeDocumentationEntry>): String {
    val domainOptions = entries.map { it.domain }.distinct().joinToString("\n") {
        """                    <option value="${it.escapeHtml()}">${it.escapeHtml()}</option>"""
    }
    val statusOptions = entries.map { "${it.httpStatus} ${it.httpStatusName}" }.distinct().joinToString("\n") {
        """                    <option value="${it.escapeHtml()}">${it.escapeHtml()}</option>"""
    }

    return """
        <!doctype html>
        <html lang="ko">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>UMC PRODUCT ErrorCode Catalog</title>
            <style>
                :root {
                    color-scheme: light;
                    font-family: Inter, Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    color: #16201f;
                    background: #f6f8f7;
                }
                * { box-sizing: border-box; }
                body { margin: 0; }
                header {
                    border-bottom: 1px solid #dde6e3;
                    background: #ffffff;
                    padding: 28px 32px;
                }
                main {
                    margin: 0 auto;
                    max-width: 1440px;
                    padding: 24px 32px 48px;
                }
                h1 {
                    margin: 0 0 8px;
                    font-size: 28px;
                    line-height: 1.25;
                    letter-spacing: 0;
                }
                p { margin: 0; color: #52605d; }
                .toolbar {
                    display: grid;
                    grid-template-columns: minmax(240px, 1fr) repeat(2, minmax(180px, 240px));
                    gap: 12px;
                    margin-bottom: 18px;
                }
                input, select {
                    width: 100%;
                    height: 42px;
                    border: 1px solid #cdd8d5;
                    border-radius: 8px;
                    background: #ffffff;
                    color: #16201f;
                    padding: 0 12px;
                    font: inherit;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    background: #ffffff;
                    border: 1px solid #dde6e3;
                }
                th, td {
                    border-bottom: 1px solid #e7eeeb;
                    padding: 12px 14px;
                    text-align: left;
                    vertical-align: top;
                    font-size: 14px;
                    line-height: 1.45;
                }
                th {
                    position: sticky;
                    top: 0;
                    background: #eef5f2;
                    color: #31403d;
                    font-weight: 700;
                }
                code {
                    font-family: "SFMono-Regular", Consolas, monospace;
                    font-size: 13px;
                }
                .muted { color: #687773; }
                .badge {
                    display: inline-block;
                    border: 1px solid #cdd8d5;
                    border-radius: 999px;
                    padding: 2px 8px;
                    margin: 0 4px 4px 0;
                    color: #31403d;
                    background: #f6f8f7;
                    font-size: 12px;
                }
                @media (max-width: 900px) {
                    header, main { padding-left: 16px; padding-right: 16px; }
                    .toolbar { grid-template-columns: 1fr; }
                    .table-wrap { overflow-x: auto; }
                    table { min-width: 1100px; }
                }
            </style>
        </head>
        <body>
            <header>
                <h1>ErrorCode Catalog</h1>
                <p>Generated from server ErrorCode enums. Manifest: <code>catalog.json</code>, schema: <code>catalog.schema.json</code></p>
            </header>
            <main>
                <div class="toolbar">
                    <input id="search" type="search" placeholder="code, name, message, action, owner, tag, source">
                    <select id="domain">
                        <option value="">All domains</option>
$domainOptions
                    </select>
                    <select id="status">
                        <option value="">All statuses</option>
$statusOptions
                    </select>
                </div>
                <p id="summary" class="muted"></p>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Code</th>
                                <th>Domain</th>
                                <th>Status</th>
                                <th>Message</th>
                                <th>Client Action</th>
                                <th>Retryable</th>
                                <th>Severity</th>
                                <th>Tags</th>
                                <th>Source</th>
                            </tr>
                        </thead>
                        <tbody id="rows"></tbody>
                    </table>
                </div>
            </main>
            <script>
                const manifest = ${buildErrorCodeCatalogJson(entries)};
                const rows = document.getElementById("rows");
                const search = document.getElementById("search");
                const domain = document.getElementById("domain");
                const status = document.getElementById("status");
                const summary = document.getElementById("summary");

                function text(item) {
                    return [
                        item.code,
                        item.name,
                        item.message,
                        item.clientAction,
                        item.source.enumName,
                        item.source.file,
                        String(item.source.line),
                        ...item.tags,
                        ...item.owners
                    ].filter(Boolean).join(" ").toLowerCase();
                }

                function badge(value) {
                    return "<span class=\"badge\">" + escapeHtml(value) + "</span>";
                }

                function escapeHtml(value) {
                    return String(value ?? "")
                        .replaceAll("&", "&amp;")
                        .replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;")
                        .replaceAll("\"", "&quot;")
                        .replaceAll("'", "&#39;");
                }

                function render() {
                    const keyword = search.value.trim().toLowerCase();
                    const selectedDomain = domain.value;
                    const selectedStatus = status.value;
                    const filtered = manifest.items.filter((item) => {
                        const itemStatus = item.httpStatus + " " + item.httpStatusName;
                        return (!keyword || text(item).includes(keyword))
                            && (!selectedDomain || item.domain === selectedDomain)
                            && (!selectedStatus || itemStatus === selectedStatus);
                    });

                    summary.textContent = filtered.length + " / " + manifest.totalCount + " error codes";
                    rows.innerHTML = filtered.map((item) => {
                        const tags = item.tags.length > 0 ? item.tags.map(badge).join("") : "<span class=\"muted\">-</span>";
                        return "<tr>"
                            + "<td><code>" + escapeHtml(item.code) + "</code><br><span class=\"muted\">" + escapeHtml(item.name) + "</span></td>"
                            + "<td>" + escapeHtml(item.domain) + "</td>"
                            + "<td>" + item.httpStatus + " " + escapeHtml(item.httpStatusName) + "</td>"
                            + "<td>" + escapeHtml(item.message) + "</td>"
                            + "<td>" + escapeHtml(item.clientAction ?? "-") + "</td>"
                            + "<td>" + escapeHtml(item.retryable ?? "-") + "</td>"
                            + "<td>" + escapeHtml(item.severity ?? "-") + "</td>"
                            + "<td>" + tags + "</td>"
                            + "<td><code>" + escapeHtml(item.source.enumName) + "</code><br><span class=\"muted\">" + escapeHtml(item.source.file) + ":" + item.source.line + "</span></td>"
                            + "</tr>";
                    }).join("");
                }

                search.addEventListener("input", render);
                domain.addEventListener("change", render);
                status.addEventListener("change", render);
                render();
            </script>
        </body>
        </html>
    """.trimIndent() + "\n"
}

fun writeIfChanged(file: File, content: String) {
    file.parentFile.mkdirs()
    if (!file.exists() || file.readText() != content) {
        file.writeText(content)
    }
}

fun collectDocumentationIssues(): List<String> {
    val entries = extractErrorCodeEntries()
    val issues = mutableListOf<String>()

    entries
        .filter { it.code.isBlank() }
        .forEach { issues.add("[ErrorCode] ${it.source.file}:${it.source.line} code is blank") }

    entries
        .filter { it.httpStatus !in 100..599 }
        .forEach { issues.add("[ErrorCode] ${it.source.file}:${it.source.line} unknown HttpStatus ${it.httpStatusName}") }

    entries
        .groupBy { it.code }
        .filter { (code, duplicates) -> code.isNotBlank() && duplicates.size > 1 }
        .forEach { (code, duplicates) ->
            issues.add(
                "[ErrorCode] 중복 code $code: " +
                    duplicates.joinToString { "${it.source.file}:${it.source.line}" }
            )
        }

    entries
        .filter { it.severity != null && it.severity !in setOf("INFO", "WARNING", "ERROR", "CRITICAL") }
        .forEach { issues.add("[ErrorCode] ${it.source.file}:${it.source.line} invalid severity ${it.severity}") }

    return issues
}

tasks.register("generateErrorCodeCatalog") {
    group = "documentation"
    description = "ErrorCode 카탈로그 문서와 v1 JSON manifest/schema를 생성합니다."

    doLast {
        val entries = extractErrorCodeEntries()
        val markdown = buildErrorCodeCatalogMarkdown(entries)
        val json = buildErrorCodeCatalogJson(entries)
        val schema = buildErrorCodeCatalogSchemaJson()

        writeIfChanged(errorCodeCatalogMarkdownFile, markdown)
        writeIfChanged(errorCodeCatalogJsonFile, json)
        writeIfChanged(errorCodeCatalogSchemaFile, schema)
        writeIfChanged(staticErrorCodeCatalogMarkdownFile, markdown)
        writeIfChanged(staticErrorCodeCatalogJsonFile, json)
        writeIfChanged(staticErrorCodeCatalogSchemaFile, schema)
        writeIfChanged(staticErrorCodeCatalogIndexFile, buildCatalogIndexHtml(entries))
        println("[generateErrorCodeCatalog] ${entries.size}개 ErrorCode를 문서화했습니다.")
    }
}

tasks.register("removeApiCatalogArtifacts") {
    group = "documentation"
    description = "삭제된 커스텀 API 카탈로그 산출물을 제거합니다. Scalar/OpenAPI 문서는 유지합니다."

    doLast {
        apiCatalogFilesToRemove.filter { it.exists() }.forEach { it.delete() }
        file("src/main/resources/static/docs/catalog/api")
            .takeIf { it.exists() && it.listFiles().isNullOrEmpty() }
            ?.delete()
    }
}

tasks.register("generateDocumentationCatalogs") {
    group = "documentation"
    description = "ErrorCode 카탈로그 문서와 v1 JSON manifest/schema를 생성합니다."
    dependsOn("removeApiCatalogArtifacts", "generateErrorCodeCatalog")
}

tasks.register("checkDocumentationCatalogs") {
    group = "verification"
    description = "ErrorCode 자동 생성 문서가 최신 상태인지 검증합니다."

    doLast {
        val entries = extractErrorCodeEntries()
        val markdown = buildErrorCodeCatalogMarkdown(entries)
        val json = buildErrorCodeCatalogJson(entries)
        val schema = buildErrorCodeCatalogSchemaJson()
        val expectedFiles = mapOf(
            errorCodeCatalogMarkdownFile to markdown,
            errorCodeCatalogJsonFile to json,
            errorCodeCatalogSchemaFile to schema,
            staticErrorCodeCatalogMarkdownFile to markdown,
            staticErrorCodeCatalogJsonFile to json,
            staticErrorCodeCatalogSchemaFile to schema,
            staticErrorCodeCatalogIndexFile to buildCatalogIndexHtml(entries)
        )

        val staleFiles = expectedFiles.filter { (file, expected) -> !file.exists() || file.readText() != expected }.keys
        val legacyApiFiles = apiCatalogFilesToRemove.filter { it.exists() }
        if (staleFiles.isNotEmpty() || legacyApiFiles.isNotEmpty()) {
            throw GradleException(
                "ErrorCode 카탈로그가 최신 상태가 아닙니다. ./gradlew generateDocumentationCatalogs 실행 필요: " +
                    (staleFiles + legacyApiFiles).joinToString { it.relativeTo(project.projectDir).invariantSeparatorsPath }
            )
        }

        println("[checkDocumentationCatalogs] ErrorCode 카탈로그가 최신 상태입니다.")
    }
}

tasks.register("validateDocumentationCatalogs") {
    group = "verification"
    description = "ErrorCode의 누락/중복과 v1 규격 위반을 검사합니다. -PstrictDocumentationCatalogs=true 설정 시 실패 처리합니다."

    doLast {
        val issues = collectDocumentationIssues()
        if (issues.isEmpty()) {
            println("[validateDocumentationCatalogs] ErrorCode 카탈로그 문제가 없습니다.")
            return@doLast
        }

        issues.forEach { println("[validateDocumentationCatalogs] $it") }
        if (project.findProperty("strictDocumentationCatalogs") == "true") {
            throw GradleException("ErrorCode 카탈로그 검증 실패: ${issues.size}건")
        }
    }
}

tasks.register("nextErrorCode") {
    group = "documentation"
    description = "다음 ErrorCode를 추천합니다. 예: ./gradlew nextErrorCode -Pprefix=CHALLENGER"

    doLast {
        val prefix = (project.findProperty("prefix") as String?)?.uppercase(Locale.ROOT)
            ?: throw GradleException("-Pprefix=CHALLENGER 형식으로 ErrorCode prefix를 지정해주세요.")
        val width = (project.findProperty("width") as String?)?.toIntOrNull() ?: 4
        val next = extractErrorCodeEntries()
            .map { it.code }
            .mapNotNull { Regex("^${Regex.escape(prefix)}-(\\d+)$").find(it)?.groupValues?.get(1)?.toIntOrNull() }
            .maxOrNull()
            ?.plus(1)
            ?: 1

        println("$prefix-${next.toString().padStart(width, '0')}")
    }
}

tasks.named("check") {
    dependsOn("checkDocumentationCatalogs")
}
