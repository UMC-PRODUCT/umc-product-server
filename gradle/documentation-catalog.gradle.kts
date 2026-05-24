import java.io.File
import java.util.Locale

data class DocumentationAnnotation(
    val name: String,
    val text: String,
    val line: Int
)

data class DocumentationMethod(
    val name: String,
    val line: Int,
    val annotations: List<DocumentationAnnotation>
)

data class DocumentationClass(
    val packageName: String,
    val className: String,
    val sourceFile: File,
    val relativePath: String,
    val domain: String,
    val interfaces: List<String>,
    val annotations: List<DocumentationAnnotation>,
    val methods: List<DocumentationMethod>
)

data class OperationDocumentation(
    val apiId: String?,
    val operationId: String?,
    val summary: String,
    val role: String,
    val source: String,
    val line: Int
)

data class ApiDocumentationEntry(
    val sequence: Int,
    val domain: String,
    val apiId: String,
    val endpoint: String,
    val httpMethod: String,
    val role: String,
    val deprecated: Boolean,
    val source: String,
    val line: Int,
    val operationId: String?
)

data class ErrorCodeDocumentationEntry(
    val sequence: Int,
    val domain: String,
    val enumName: String,
    val constantName: String,
    val httpStatus: String,
    val code: String,
    val message: String,
    val source: String,
    val line: Int
)

val documentationSourceRoot = file("src/main/java")
val apiCatalogMarkdownFile = file("docs/guides/API_목록.md")
val errorCodeCatalogMarkdownFile = file("docs/guides/ErrorCode_목록.md")
val apiCatalogJsonFile = file("docs/guides/API_목록.json")
val errorCodeCatalogJsonFile = file("docs/guides/ErrorCode_목록.json")
val staticApiCatalogMarkdownFile = file("src/main/resources/static/docs/catalog/api/catalog.md")
val staticApiCatalogJsonFile = file("src/main/resources/static/docs/catalog/api/catalog.json")
val staticApiCatalogIndexFile = file("src/main/resources/static/docs/catalog/api/index.html")
val staticErrorCodeCatalogMarkdownFile = file("src/main/resources/static/docs/catalog/error/catalog.md")
val staticErrorCodeCatalogJsonFile = file("src/main/resources/static/docs/catalog/error/catalog.json")
val staticErrorCodeCatalogIndexFile = file("src/main/resources/static/docs/catalog/error/index.html")

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

fun String.simpleJavaName(): String = substringAfterLast('.').trim()

fun domainFromPackage(packageName: String): String {
    return packageName
        .removePrefix("com.umc.product.")
        .substringBefore(".")
        .ifBlank { "unknown" }
}

fun normalizeEndpoint(vararg parts: String): String {
    val segments = parts
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { it.trim('/') }
        .filter { it.isNotBlank() }

    return if (segments.isEmpty()) {
        "/"
    } else {
        "/" + segments.joinToString("/")
    }
}

fun extractFirstStringLiteral(text: String): String? {
    val match = Regex("\"((?:\\\\.|[^\"\\\\])*)\"", RegexOption.DOT_MATCHES_ALL).find(text)
    return match?.groupValues?.get(1)?.replace("\\\"", "\"")
}

fun extractStringAttribute(text: String, attributeName: String): String? {
    val match = Regex(
        "$attributeName\\s*=\\s*\"((?:\\\\.|[^\"\\\\])*)\"",
        setOf(RegexOption.DOT_MATCHES_ALL)
    ).find(text)

    return match?.groupValues?.get(1)?.replace("\\\"", "\"")
}

fun extractMappingPath(annotation: DocumentationAnnotation): String {
    val pathValue = extractStringAttribute(annotation.text, "path")
        ?: extractStringAttribute(annotation.text, "value")
        ?: extractFirstStringLiteral(annotation.text)

    return pathValue.orEmpty()
}

fun extractHttpMethod(annotation: DocumentationAnnotation): String {
    return when (annotation.name) {
        "GetMapping" -> "GET"
        "PostMapping" -> "POST"
        "PutMapping" -> "PUT"
        "PatchMapping" -> "PATCH"
        "DeleteMapping" -> "DELETE"
        "RequestMapping" -> Regex("RequestMethod\\.([A-Z]+)")
            .findAll(annotation.text)
            .map { it.groupValues[1] }
            .distinct()
            .joinToString(",")
            .ifBlank { "REQUEST" }

        else -> annotation.name.removeSuffix("Mapping").uppercase(Locale.ROOT)
    }
}

fun parseAnnotation(lines: List<String>, startIndex: Int): Pair<DocumentationAnnotation, Int> {
    val startLine = lines[startIndex]
    val name = Regex("@([A-Za-z0-9_]+)").find(startLine)?.groupValues?.get(1).orEmpty()
    val text = StringBuilder(startLine.trim())

    var index = startIndex
    var balance = startLine.count { it == '(' } - startLine.count { it == ')' }

    while (balance > 0 && index + 1 < lines.size) {
        index += 1
        val nextLine = lines[index]
        text.append('\n').append(nextLine.trim())
        balance += nextLine.count { it == '(' } - nextLine.count { it == ')' }
    }

    return DocumentationAnnotation(name, text.toString(), startIndex + 1) to index
}

fun extractMethodName(line: String): String? {
    val normalized = line.trim()

    if (normalized.startsWith("if ") ||
        normalized.startsWith("for ") ||
        normalized.startsWith("while ") ||
        normalized.startsWith("switch ") ||
        normalized.startsWith("catch ") ||
        normalized.startsWith("return ") ||
        normalized.startsWith("new ")
    ) {
        return null
    }

    return Regex(
        "^(?:public|protected|private)?\\s*(?:static\\s+)?(?:final\\s+)?[A-Za-z0-9_<>,.?\\[\\]\\s]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\("
    ).find(normalized)?.groupValues?.get(1)
}

fun parseDocumentationClass(sourceFile: File): DocumentationClass? {
    val lines = sourceFile.readLines()
    val packageName = lines.firstOrNull { it.trim().startsWith("package ") }
        ?.trim()
        ?.removePrefix("package ")
        ?.removeSuffix(";")
        ?: return null

    val relativePath = sourceFile.relativeTo(project.projectDir).invariantSeparatorsPath
    val domain = domainFromPackage(packageName)

    val methods = mutableListOf<DocumentationMethod>()
    var className: String? = null
    var interfaces = emptyList<String>()
    var classAnnotations = emptyList<DocumentationAnnotation>()
    val pendingAnnotations = mutableListOf<DocumentationAnnotation>()

    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        if (trimmed.startsWith("@")) {
            val (annotation, endIndex) = parseAnnotation(lines, index)
            pendingAnnotations.add(annotation)
            index = endIndex + 1
            continue
        }

        if (className == null) {
            val classMatch = Regex("\\b(?:class|interface|enum)\\s+([A-Za-z_][A-Za-z0-9_]*)").find(trimmed)
            if (classMatch != null) {
                className = classMatch.groupValues[1]
                classAnnotations = pendingAnnotations.toList()
                pendingAnnotations.clear()

                interfaces = Regex("\\bimplements\\s+([^\\{]+)")
                    .find(trimmed)
                    ?.groupValues
                    ?.get(1)
                    ?.split(",")
                    ?.map { it.simpleJavaName() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
            }

            index += 1
            continue
        }

        val methodName = extractMethodName(trimmed)
        if (methodName != null) {
            methods.add(DocumentationMethod(methodName, index + 1, pendingAnnotations.toList()))
            pendingAnnotations.clear()
            index += 1
            continue
        }

        if (trimmed.isNotBlank() && !trimmed.startsWith("//") && trimmed.endsWith(";")) {
            pendingAnnotations.clear()
        }

        index += 1
    }

    return className?.let {
        DocumentationClass(
            packageName = packageName,
            className = it,
            sourceFile = sourceFile,
            relativePath = relativePath,
            domain = domain,
            interfaces = interfaces,
            annotations = classAnnotations,
            methods = methods
        )
    }
}

fun extractOperationDocumentation(method: DocumentationMethod, source: String): OperationDocumentation? {
    val operationAnnotation = method.annotations.firstOrNull { it.name == "Operation" } ?: return null
    val summary = extractStringAttribute(operationAnnotation.text, "summary").orEmpty()
    val operationId = extractStringAttribute(operationAnnotation.text, "operationId")?.takeIf { it.isNotBlank() }

    val summaryMatch = Regex("^\\[([A-Z][A-Z0-9_]*(?:-[A-Z0-9_]+)+)]\\s*(.*)$", RegexOption.DOT_MATCHES_ALL)
        .find(summary)
        ?.takeIf { match -> match.groupValues[1].any { it.isDigit() } }
    val summaryApiId = summaryMatch?.groupValues?.get(1)
    val role = summaryMatch?.groupValues?.get(2)?.trim()?.ifBlank { summary } ?: summary

    return OperationDocumentation(
        apiId = operationId ?: summaryApiId,
        operationId = operationId,
        summary = summary,
        role = role,
        source = source,
        line = operationAnnotation.line
    )
}

fun classMappingPath(clazz: DocumentationClass): String {
    return clazz.annotations.firstOrNull { it.name == "RequestMapping" }?.let(::extractMappingPath).orEmpty()
}

fun isDeprecated(clazz: DocumentationClass, method: DocumentationMethod): Boolean {
    return clazz.annotations.any { it.name == "Deprecated" } ||
        method.annotations.any { it.name == "Deprecated" } ||
        method.annotations.any { it.name == "Operation" && it.text.contains("deprecated = true") }
}

fun buildApiDocumentationEntries(): List<ApiDocumentationEntry> {
    val javaClasses = documentationSourceRoot
        .walkTopDown()
        .filter { it.isFile && it.extension == "java" }
        .mapNotNull(::parseDocumentationClass)
        .toList()

    val operationByInterface = javaClasses
        .filter { it.annotations.none { annotation -> annotation.name == "RestController" } }
        .associate { clazz ->
            clazz.className to clazz.methods.mapNotNull { method ->
                extractOperationDocumentation(method, clazz.relativePath)?.let { method.name to it }
            }.toMap()
        }

    val entries = mutableListOf<ApiDocumentationEntry>()

    javaClasses
        .filter { clazz -> clazz.annotations.any { it.name == "RestController" } }
        .forEach { clazz ->
            val basePath = classMappingPath(clazz)
            clazz.methods.forEach methodLoop@{ method ->
                val mappingAnnotation = method.annotations.firstOrNull { it.name.endsWith("Mapping") } ?: return@methodLoop
                val methodOperation = extractOperationDocumentation(method, clazz.relativePath)
                val interfaceOperation = clazz.interfaces
                    .asSequence()
                    .mapNotNull { operationByInterface[it]?.get(method.name) }
                    .firstOrNull()
                val operation = methodOperation ?: interfaceOperation

                entries.add(
                    ApiDocumentationEntry(
                        sequence = 0,
                        domain = clazz.domain,
                        apiId = operation?.apiId ?: "<미지정>",
                        endpoint = normalizeEndpoint(basePath, extractMappingPath(mappingAnnotation)),
                        httpMethod = extractHttpMethod(mappingAnnotation),
                        role = operation?.role?.ifBlank { "<요약 없음>" } ?: "<요약 없음>",
                        deprecated = isDeprecated(clazz, method),
                        source = operation?.source ?: clazz.relativePath,
                        line = operation?.line ?: method.line,
                        operationId = operation?.operationId
                    )
                )
            }
        }

    return entries
        .sortedWith(compareBy<ApiDocumentationEntry> { it.domain }.thenBy { it.apiId }.thenBy { it.endpoint })
        .mapIndexed { index, entry -> entry.copy(sequence = index + 1) }
}

fun buildApiCatalogMarkdown(entries: List<ApiDocumentationEntry>): String = buildString {
    appendLine("# API Catalog")
    appendLine()
    appendLine("운영 중인 API의 ID, 엔드포인트, 메서드, 역할, deprecated 상태를 도메인별로 정리합니다.")
    appendLine()
    appendLine("> 소스 기준: `@Operation(operationId = \"...\")`를 우선 사용하고, 없으면 `summary`의 `[XXX-000]` prefix를 API ID로 읽습니다. 갱신: `./gradlew generateDocumentationCatalogs`")
    appendLine()

    entries.groupBy { it.domain }.forEach { (domain, domainEntries) ->
        appendLine("## $domain")
        appendLine()
        appendLine("| 순번 | 도메인 | API ID | Endpoint | HTTP Method | 역할 | Deprecated | Source |")
        appendLine("|---:|---|---|---|---|---|:---:|---|")
        domainEntries.forEach { entry ->
            appendLine(
                "| ${entry.sequence} | ${entry.domain.escapeMarkdownCell()} | ${entry.apiId.escapeMarkdownCell()} | `${entry.endpoint.escapeMarkdownCell()}` | ${entry.httpMethod.escapeMarkdownCell()} | ${entry.role.escapeMarkdownCell()} | ${if (entry.deprecated) "O" else "X"} | `${entry.source}:${entry.line}` |"
            )
        }
        appendLine()
    }
}

fun buildApiCatalogJson(entries: List<ApiDocumentationEntry>): String = buildString {
    appendLine("[")
    entries.forEachIndexed { index, entry ->
        appendLine("  {")
        appendLine("    \"sequence\": ${entry.sequence},")
        appendLine("    \"domain\": \"${entry.domain.escapeJson()}\",")
        appendLine("    \"apiId\": \"${entry.apiId.escapeJson()}\",")
        appendLine("    \"endpoint\": \"${entry.endpoint.escapeJson()}\",")
        appendLine("    \"httpMethod\": \"${entry.httpMethod.escapeJson()}\",")
        appendLine("    \"role\": \"${entry.role.escapeJson()}\",")
        appendLine("    \"deprecated\": ${entry.deprecated},")
        appendLine("    \"source\": \"${entry.source.escapeJson()}\",")
        appendLine("    \"line\": ${entry.line},")
        appendLine("    \"operationId\": ${entry.operationId?.let { "\"${it.escapeJson()}\"" } ?: "null"}")
        append("  }")
        appendLine(if (index == entries.lastIndex) "" else ",")
    }
    appendLine("]")
}

fun extractErrorCodeEntries(): List<ErrorCodeDocumentationEntry> {
    val entries = mutableListOf<ErrorCodeDocumentationEntry>()

    documentationSourceRoot
        .walkTopDown()
        .filter { it.isFile && it.name.endsWith("ErrorCode.java") }
        .forEach { sourceFile ->
            val lines = sourceFile.readLines()
            val relativePath = sourceFile.relativeTo(project.projectDir).invariantSeparatorsPath
            val packageName = lines.firstOrNull { it.trim().startsWith("package ") }
                ?.trim()
                ?.removePrefix("package ")
                ?.removeSuffix(";")
                .orEmpty()
            val domain = domainFromPackage(packageName)
            val enumName = Regex("\\benum\\s+([A-Za-z_][A-Za-z0-9_]*)").find(lines.joinToString("\n"))
                ?.groupValues
                ?.get(1)
                ?: sourceFile.nameWithoutExtension

            var inConstants = false
            var chunk = StringBuilder()
            var chunkStartLine = 0
            var balance = 0

            lines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                if (!inConstants && trimmed.startsWith("public enum ")) {
                    inConstants = true
                    return@forEachIndexed
                }

                if (!inConstants || trimmed.isBlank() || trimmed.startsWith("//")) {
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
                        "^([A-Z0-9_]+)\\s*\\(\\s*HttpStatus\\.([A-Z0-9_]+)\\s*,\\s*\"([^\"]+)\"\\s*,\\s*\"((?:\\\\.|[^\"])*)\"",
                        RegexOption.DOT_MATCHES_ALL
                    ).find(constantText)

                    if (match != null) {
                        entries.add(
                            ErrorCodeDocumentationEntry(
                                sequence = 0,
                                domain = domain,
                                enumName = enumName,
                                constantName = match.groupValues[1],
                                httpStatus = match.groupValues[2],
                                code = match.groupValues[3],
                                message = match.groupValues[4].replace("\\\"", "\""),
                                source = relativePath,
                                line = chunkStartLine
                            )
                        )
                    }

                    balance = 0
                }
            }
        }

    return entries
        .sortedWith(compareBy<ErrorCodeDocumentationEntry> { it.domain }.thenBy { it.code }.thenBy { it.constantName })
        .mapIndexed { index, entry -> entry.copy(sequence = index + 1) }
}

fun buildErrorCodeCatalogMarkdown(entries: List<ErrorCodeDocumentationEntry>): String = buildString {
    appendLine("# ErrorCode Catalog")
    appendLine()
    appendLine("서버가 반환하는 ErrorCode를 도메인, HTTP 상태, 코드, 메시지 기준으로 정리합니다.")
    appendLine()
    appendLine("> 소스 기준: 각 도메인의 `*ErrorCode.java` enum을 스캔합니다. 갱신: `./gradlew generateDocumentationCatalogs`")
    appendLine()

    entries.groupBy { it.domain }.forEach { (domain, domainEntries) ->
        appendLine("## $domain")
        appendLine()
        appendLine("| 순번 | 도메인 | Enum | Constant | HTTP Status | Code | Message | Source |")
        appendLine("|---:|---|---|---|---|---|---|---|")
        domainEntries.forEach { entry ->
            appendLine(
                "| ${entry.sequence} | ${entry.domain.escapeMarkdownCell()} | ${entry.enumName.escapeMarkdownCell()} | `${entry.constantName.escapeMarkdownCell()}` | ${entry.httpStatus.escapeMarkdownCell()} | `${entry.code.escapeMarkdownCell()}` | ${entry.message.escapeMarkdownCell()} | `${entry.source}:${entry.line}` |"
            )
        }
        appendLine()
    }
}

fun buildErrorCodeCatalogJson(entries: List<ErrorCodeDocumentationEntry>): String = buildString {
    appendLine("[")
    entries.forEachIndexed { index, entry ->
        appendLine("  {")
        appendLine("    \"sequence\": ${entry.sequence},")
        appendLine("    \"domain\": \"${entry.domain.escapeJson()}\",")
        appendLine("    \"enumName\": \"${entry.enumName.escapeJson()}\",")
        appendLine("    \"constantName\": \"${entry.constantName.escapeJson()}\",")
        appendLine("    \"httpStatus\": \"${entry.httpStatus.escapeJson()}\",")
        appendLine("    \"code\": \"${entry.code.escapeJson()}\",")
        appendLine("    \"message\": \"${entry.message.escapeJson()}\",")
        appendLine("    \"source\": \"${entry.source.escapeJson()}\",")
        appendLine("    \"line\": ${entry.line}")
        append("  }")
        appendLine(if (index == entries.lastIndex) "" else ",")
    }
    appendLine("]")
}

fun buildCatalogIndexHtml(title: String, markdownFileName: String, jsonFileName: String, activePath: String): String {
    val isApiCatalog = activePath == "api"
    val pageLabel = if (isApiCatalog) "API Catalog" else "ErrorCode Catalog"
    val eyebrow = if (isApiCatalog) "Endpoint Inventory" else "Failure Contract"
    val lead = if (isApiCatalog) {
        "API ID, 경로, 메서드, 담당 도메인을 한 화면에서 확인합니다."
    } else {
        "클라이언트와 서버가 공유하는 실패 응답 계약을 빠르게 찾습니다."
    }
    val searchPlaceholder = if (isApiCatalog) {
        "도메인, API ID, endpoint, 역할로 검색"
    } else {
        "도메인, ErrorCode, HTTP status, 메시지로 검색"
    }
    val emptyMessage = if (isApiCatalog) {
        "일치하는 API가 없습니다."
    } else {
        "일치하는 ErrorCode가 없습니다."
    }

    return """
        <!doctype html>
        <html lang="ko">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>$title</title>
            <link href="/umc-logo.svg" rel="icon" type="image/svg+xml">
            <style>
                :root {
                    color-scheme: light;
                    --bg: #f7f8fb;
                    --panel: #ffffff;
                    --panel-soft: #f1f5f9;
                    --border: #d9e1ec;
                    --border-strong: #b9c5d6;
                    --text: #111827;
                    --muted: #64748b;
                    --muted-strong: #475569;
                    --accent: #2563eb;
                    --accent-strong: #1d4ed8;
                    --success: #047857;
                    --danger: #b42318;
                    --warning: #b45309;
                    --code-bg: #edf2f7;
                    --table-head: #eef3f9;
                    --shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
                }

                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    background: var(--bg);
                    color: var(--text);
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    line-height: 1.5;
                }

                header {
                    position: sticky;
                    top: 0;
                    z-index: 20;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    gap: 24px;
                    min-height: 72px;
                    padding: 14px clamp(16px, 4vw, 48px);
                    border-bottom: 1px solid var(--border);
                    background: rgba(255, 255, 255, 0.92);
                    backdrop-filter: blur(14px);
                }

                .brand {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    min-width: 0;
                }

                .brand-mark {
                    display: grid;
                    place-items: center;
                    width: 38px;
                    height: 38px;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background: #fff;
                    box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
                }

                .brand-mark img {
                    width: 26px;
                    height: 26px;
                }

                .brand h1 {
                    margin: 0;
                    font-size: 18px;
                    font-weight: 750;
                    letter-spacing: 0;
                }

                .brand p {
                    margin: 2px 0 0;
                    color: var(--muted);
                    font-size: 13px;
                }

                nav {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    flex-wrap: wrap;
                    justify-content: flex-end;
                }

                nav a {
                    color: var(--muted);
                    border: 1px solid transparent;
                    border-radius: 6px;
                    padding: 8px 11px;
                    text-decoration: none;
                    font-size: 14px;
                    white-space: nowrap;
                }

                nav a:hover,
                nav a.active {
                    color: var(--accent);
                    border-color: #bfdbfe;
                    background: #eff6ff;
                }

                .page {
                    width: min(1440px, calc(100vw - 32px));
                    margin: 0 auto 64px;
                }

                .hero {
                    display: grid;
                    grid-template-columns: minmax(0, 1.45fr) minmax(280px, 0.55fr);
                    gap: 24px;
                    padding: 42px 0 24px;
                    align-items: end;
                }

                .eyebrow {
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                    margin: 0 0 12px;
                    color: var(--accent-strong);
                    font-size: 13px;
                    font-weight: 750;
                    letter-spacing: 0.08em;
                    text-transform: uppercase;
                }

                .eyebrow::before {
                    content: "";
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                    background: var(--success);
                }

                .hero h2 {
                    margin: 0;
                    border: 0;
                    padding: 0;
                    color: var(--text);
                    font-size: clamp(36px, 5vw, 68px);
                    line-height: 1;
                    font-weight: 800;
                    letter-spacing: 0;
                }

                .hero-copy {
                    max-width: 720px;
                    margin: 18px 0 0;
                    color: var(--muted-strong);
                    font-size: 18px;
                }

                .hero-aside {
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background: var(--panel);
                    padding: 18px;
                    box-shadow: var(--shadow);
                }

                .hero-aside strong {
                    display: block;
                    margin-bottom: 6px;
                    font-size: 14px;
                }

                .hero-aside p {
                    margin: 0;
                    color: var(--muted);
                    font-size: 14px;
                }

                .toolbar {
                    position: sticky;
                    top: 72px;
                    z-index: 15;
                    display: grid;
                    grid-template-columns: minmax(260px, 1fr) auto;
                    gap: 14px;
                    align-items: center;
                    margin-bottom: 18px;
                    padding: 14px;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background: rgba(255, 255, 255, 0.94);
                    backdrop-filter: blur(14px);
                    box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
                }

                .search {
                    position: relative;
                    min-width: 0;
                }

                .search input {
                    width: 100%;
                    min-height: 44px;
                    border: 1px solid var(--border-strong);
                    border-radius: 8px;
                    padding: 0 14px 0 42px;
                    background: #fff;
                    color: var(--text);
                    font: inherit;
                    outline: none;
                }

                .search input:focus {
                    border-color: var(--accent);
                    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.14);
                }

                .search::before {
                    content: "";
                    position: absolute;
                    left: 15px;
                    top: 50%;
                    width: 14px;
                    height: 14px;
                    border: 2px solid var(--muted);
                    border-radius: 50%;
                    transform: translateY(-56%);
                }

                .search::after {
                    content: "";
                    position: absolute;
                    left: 28px;
                    top: 27px;
                    width: 8px;
                    height: 2px;
                    border-radius: 999px;
                    background: var(--muted);
                    transform: rotate(45deg);
                }

                .result-count {
                    color: var(--muted);
                    font-size: 14px;
                    white-space: nowrap;
                }

                .stats {
                    display: grid;
                    grid-template-columns: repeat(4, minmax(0, 1fr));
                    gap: 12px;
                    margin-bottom: 18px;
                }

                .stat {
                    min-width: 0;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background: var(--panel);
                    padding: 16px;
                    box-shadow: 0 10px 26px rgba(15, 23, 42, 0.05);
                }

                .stat span {
                    display: block;
                    color: var(--muted);
                    font-size: 13px;
                }

                .stat strong {
                    display: block;
                    margin-top: 8px;
                    font-size: 26px;
                    line-height: 1;
                    letter-spacing: 0;
                }

                main {
                    padding: 24px;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background: var(--panel);
                    box-shadow: var(--shadow);
                    overflow: hidden;
                }

                .loading,
                .error {
                    margin: 0;
                    color: var(--muted);
                    font-size: 15px;
                }

                .error {
                    color: var(--danger);
                }

                .no-results {
                    display: none;
                    margin: 18px 0 0;
                    padding: 14px 16px;
                    border: 1px solid #fed7aa;
                    border-radius: 8px;
                    background: #fff7ed;
                    color: #9a3412;
                    font-size: 14px;
                }

                h1,
                h2,
                h3 {
                    line-height: 1.25;
                    letter-spacing: 0;
                }

                h1 {
                    margin: 0 0 10px;
                    font-size: 28px;
                }

                h2 {
                    display: inline-flex;
                    align-items: center;
                    margin: 32px 0 12px;
                    padding: 7px 10px;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    background: var(--panel-soft);
                    font-size: 15px;
                    font-weight: 750;
                }

                p {
                    margin: 10px 0;
                    color: var(--muted-strong);
                }

                a {
                    color: var(--accent);
                }

                blockquote {
                    margin: 18px 0;
                    padding: 12px 14px;
                    border: 1px solid #bfdbfe;
                    border-left: 4px solid #60a5fa;
                    border-radius: 8px;
                    background: #eff6ff;
                    color: var(--muted);
                }

                code {
                    padding: 2px 5px;
                    border-radius: 4px;
                    background: var(--code-bg);
                    font-family: "SFMono-Regular", Consolas, monospace;
                    font-size: 0.92em;
                }

                .table-wrap {
                    width: 100%;
                    margin: 12px 0 28px;
                    border: 1px solid var(--border);
                    border-radius: 8px;
                    overflow: auto;
                    background: #fff;
                }

                .catalog-table {
                    width: 100%;
                    min-width: 1040px;
                    border-collapse: collapse;
                    margin: 0;
                    font-size: 13px;
                }

                th,
                td {
                    border-bottom: 1px solid var(--border);
                    padding: 10px 12px;
                    text-align: left;
                    vertical-align: middle;
                }

                th {
                    position: sticky;
                    top: 0;
                    z-index: 1;
                    background: var(--table-head);
                    color: var(--muted-strong);
                    font-size: 12px;
                    font-weight: 750;
                    letter-spacing: 0.04em;
                    text-transform: uppercase;
                }

                td {
                    color: var(--text);
                }

                tbody tr:hover td {
                    background: #f8fbff;
                }

                tbody tr:last-child td {
                    border-bottom: 0;
                }

                td:nth-child(1) {
                    color: var(--muted);
                    font-variant-numeric: tabular-nums;
                    width: 64px;
                }

                td:nth-child(3) code,
                td:nth-child(4) code,
                td:nth-child(6) code {
                    color: #0f172a;
                    font-weight: 650;
                }

                .is-hidden {
                    display: none !important;
                }

                @media (max-width: 720px) {
                    header {
                        align-items: flex-start;
                        flex-direction: column;
                        padding: 14px 16px;
                    }

                    .page {
                        width: calc(100vw - 16px);
                    }

                    .hero {
                        grid-template-columns: 1fr;
                        padding: 26px 0 16px;
                    }

                    .hero h2 {
                        font-size: 40px;
                    }

                    .hero-copy {
                        font-size: 16px;
                    }

                    .toolbar {
                        top: 127px;
                        grid-template-columns: 1fr;
                    }

                    .stats {
                        grid-template-columns: repeat(2, minmax(0, 1fr));
                    }

                    nav {
                        justify-content: flex-start;
                    }

                    main {
                        padding: 16px;
                    }
                }

                @media (max-width: 480px) {
                    .stats {
                        grid-template-columns: 1fr;
                    }
                }
            </style>
        </head>
        <body>
            <header>
                <div class="brand">
                    <span class="brand-mark" aria-hidden="true"><img src="/umc-logo.svg" alt=""></span>
                    <div>
                        <h1>UMC PRODUCT Docs</h1>
                        <p>서버 계약을 소스에서 바로 읽어옵니다.</p>
                    </div>
                </div>
                <nav aria-label="문서 이동">
                    <a href="/docs/catalog/api/" class="${if (activePath == "api") "active" else ""}">API</a>
                    <a href="/docs/catalog/error/" class="${if (activePath == "error") "active" else ""}">ErrorCode</a>
                    <a href="$markdownFileName">Markdown</a>
                    <a href="$jsonFileName">JSON</a>
                </nav>
            </header>
            <div class="page">
                <section class="hero" aria-labelledby="catalog-title">
                    <div>
                        <p class="eyebrow">$eyebrow</p>
                        <h2 id="catalog-title">$pageLabel</h2>
                        <p class="hero-copy">$lead</p>
                    </div>
                    <aside class="hero-aside">
                        <strong>자동 생성 문서</strong>
                        <p>소스 변경 후 `./gradlew generateDocumentationCatalogs`로 갱신합니다.</p>
                    </aside>
                </section>
                <section class="stats" id="stats" aria-label="카탈로그 요약"></section>
                <section class="toolbar" aria-label="카탈로그 검색">
                    <label class="search" for="catalog-search">
                        <input id="catalog-search" type="search" placeholder="$searchPlaceholder" autocomplete="off">
                    </label>
                    <span class="result-count" id="result-count"></span>
                </section>
                <main id="content">
                    <p class="loading">카탈로그를 불러오는 중입니다.</p>
                </main>
                <p class="no-results" id="no-results">$emptyMessage</p>
            </div>
            <script src="/webjars/markdown-it/14.1.0/dist/markdown-it.min.js"></script>
            <script>
                const content = document.getElementById("content");
                const stats = document.getElementById("stats");
                const searchInput = document.getElementById("catalog-search");
                const resultCount = document.getElementById("result-count");
                const noResults = document.getElementById("no-results");
                const markdownSource = "$markdownFileName";
                const jsonSource = "$jsonFileName";
                const pageKind = "$activePath";
                const md = window.markdownit({
                    html: false,
                    linkify: true,
                    typographer: false
                });

                function fetchText(path) {
                    return fetch(path, { cache: "no-cache" }).then((response) => {
                        if (!response.ok) {
                            throw new Error("HTTP " + response.status);
                        }
                        return response.text();
                    });
                }

                function fetchJson(path) {
                    return fetch(path, { cache: "no-cache" }).then((response) => {
                        if (!response.ok) {
                            throw new Error("HTTP " + response.status);
                        }
                        return response.json();
                    });
                }

                function uniqueCount(rows, key) {
                    return new Set(rows.map((row) => row[key]).filter(Boolean)).size;
                }

                function renderStats(rows) {
                    const values = pageKind === "api"
                        ? [
                            ["등록된 API", rows.length],
                            ["도메인", uniqueCount(rows, "domain")],
                            ["ID 미지정", rows.filter((row) => row.apiId === "<미지정>").length],
                            ["Deprecated", rows.filter((row) => row.deprecated).length]
                        ]
                        : [
                            ["ErrorCode", rows.length],
                            ["도메인", uniqueCount(rows, "domain")],
                            ["HTTP Status", uniqueCount(rows, "httpStatus")],
                            ["Enum", uniqueCount(rows, "enumName")]
                        ];

                    stats.innerHTML = values.map((item) => (
                        '<article class="stat"><span>' + item[0] + '</span><strong>' + item[1].toLocaleString("ko-KR") + '</strong></article>'
                    )).join("");
                }

                function enhanceTables() {
                    const firstHeading = content.querySelector("h1");
                    if (firstHeading) {
                        firstHeading.remove();
                    }

                    content.querySelectorAll("table").forEach((table) => {
                        table.classList.add("catalog-table");
                        const wrapper = document.createElement("div");
                        wrapper.className = "table-wrap";
                        table.parentNode.insertBefore(wrapper, table);
                        wrapper.appendChild(table);

                        table.querySelectorAll("tbody tr").forEach((row) => {
                            row.dataset.search = row.textContent.toLowerCase();
                        });
                    });
                }

                function updateResultCount(visible, total) {
                    resultCount.textContent = visible.toLocaleString("ko-KR") + " / " + total.toLocaleString("ko-KR") + "개 표시";
                    noResults.style.display = visible === 0 ? "block" : "none";
                }

                function applyFilter() {
                    const query = searchInput.value.trim().toLowerCase();
                    let total = 0;
                    let visible = 0;

                    content.querySelectorAll(".catalog-table tbody tr").forEach((row) => {
                        total += 1;
                        const matched = query === "" || row.dataset.search.includes(query);
                        row.classList.toggle("is-hidden", !matched);
                        if (matched) {
                            visible += 1;
                        }
                    });

                    content.querySelectorAll(".table-wrap").forEach((wrapper) => {
                        const visibleRows = wrapper.querySelectorAll("tbody tr:not(.is-hidden)").length;
                        wrapper.classList.toggle("is-hidden", visibleRows === 0 && query !== "");
                        const heading = wrapper.previousElementSibling;
                        if (heading && heading.tagName === "H2") {
                            heading.classList.toggle("is-hidden", visibleRows === 0 && query !== "");
                        }
                    });

                    updateResultCount(visible, total);
                }

                Promise.all([fetchText(markdownSource), fetchJson(jsonSource)])
                    .then(([markdown, rows]) => {
                        content.innerHTML = md.render(markdown);
                        renderStats(rows);
                        enhanceTables();
                        applyFilter();
                        searchInput.addEventListener("input", applyFilter);
                    })
                    .catch((error) => {
                        content.innerHTML = '<p class="error">카탈로그를 불러오지 못했습니다. ' + error.message + '</p>';
                        stats.innerHTML = "";
                        updateResultCount(0, 0);
                    });
            </script>
        </body>
        </html>
    """.trimIndent() + "\n"
}

fun writeIfChanged(target: File, content: String) {
    target.parentFile.mkdirs()
    if (!target.exists() || target.readText() != content) {
        target.writeText(content)
    }
}

fun collectDocumentationIssues(): List<String> {
    val apiEntries = buildApiDocumentationEntries()
    val errorCodeEntries = extractErrorCodeEntries()
    val issues = mutableListOf<String>()

    apiEntries
        .filter { it.apiId == "<미지정>" }
        .forEach { issues.add("API ID 누락: ${it.httpMethod} ${it.endpoint} (${it.source}:${it.line})") }

    apiEntries
        .filter { it.apiId != "<미지정>" }
        .groupBy { it.apiId }
        .filterValues { it.size > 1 }
        .forEach { (apiId, duplicated) ->
            issues.add("API ID 중복: $apiId -> ${duplicated.joinToString { "${it.httpMethod} ${it.endpoint}" }}")
        }

    errorCodeEntries
        .groupBy { it.code }
        .filterValues { it.size > 1 }
        .forEach { (code, duplicated) ->
            issues.add("ErrorCode 중복: $code -> ${duplicated.joinToString { "${it.enumName}.${it.constantName}" }}")
        }

    return issues
}

tasks.register("generateApiCatalog") {
    group = "documentation"
    description = "컨트롤러와 OpenAPI 애너테이션을 스캔해 API ID 목록 문서를 생성합니다."

    doLast {
        val entries = buildApiDocumentationEntries()
        val markdown = buildApiCatalogMarkdown(entries)
        val json = buildApiCatalogJson(entries)

        writeIfChanged(apiCatalogMarkdownFile, markdown)
        writeIfChanged(apiCatalogJsonFile, json)
        writeIfChanged(staticApiCatalogMarkdownFile, markdown)
        writeIfChanged(staticApiCatalogJsonFile, json)
        writeIfChanged(staticApiCatalogIndexFile, buildCatalogIndexHtml("UMC PRODUCT API Catalog", "catalog.md", "catalog.json", "api"))
        println("[generateApiCatalog] ${entries.size}개 API를 문서화했습니다.")
    }
}

tasks.register("generateErrorCodeCatalog") {
    group = "documentation"
    description = "ErrorCode enum을 스캔해 ErrorCode 목록 문서를 생성합니다."

    doLast {
        val entries = extractErrorCodeEntries()
        val markdown = buildErrorCodeCatalogMarkdown(entries)
        val json = buildErrorCodeCatalogJson(entries)

        writeIfChanged(errorCodeCatalogMarkdownFile, markdown)
        writeIfChanged(errorCodeCatalogJsonFile, json)
        writeIfChanged(staticErrorCodeCatalogMarkdownFile, markdown)
        writeIfChanged(staticErrorCodeCatalogJsonFile, json)
        writeIfChanged(staticErrorCodeCatalogIndexFile, buildCatalogIndexHtml("UMC PRODUCT ErrorCode Catalog", "catalog.md", "catalog.json", "error"))
        println("[generateErrorCodeCatalog] ${entries.size}개 ErrorCode를 문서화했습니다.")
    }
}

tasks.register("generateDocumentationCatalogs") {
    group = "documentation"
    description = "API ID와 ErrorCode 목록 문서를 모두 생성합니다."
    dependsOn("generateApiCatalog", "generateErrorCodeCatalog")
}

tasks.register("checkDocumentationCatalogs") {
    group = "verification"
    description = "API ID와 ErrorCode 자동 생성 문서가 최신 상태인지 검증합니다."

    doLast {
        val apiEntries = buildApiDocumentationEntries()
        val errorCodeEntries = extractErrorCodeEntries()
        val expectedFiles = mapOf(
            apiCatalogMarkdownFile to buildApiCatalogMarkdown(apiEntries),
            apiCatalogJsonFile to buildApiCatalogJson(apiEntries),
            errorCodeCatalogMarkdownFile to buildErrorCodeCatalogMarkdown(errorCodeEntries),
            errorCodeCatalogJsonFile to buildErrorCodeCatalogJson(errorCodeEntries),
            staticApiCatalogMarkdownFile to buildApiCatalogMarkdown(apiEntries),
            staticApiCatalogJsonFile to buildApiCatalogJson(apiEntries),
            staticApiCatalogIndexFile to buildCatalogIndexHtml("UMC PRODUCT API Catalog", "catalog.md", "catalog.json", "api"),
            staticErrorCodeCatalogMarkdownFile to buildErrorCodeCatalogMarkdown(errorCodeEntries),
            staticErrorCodeCatalogJsonFile to buildErrorCodeCatalogJson(errorCodeEntries),
            staticErrorCodeCatalogIndexFile to buildCatalogIndexHtml("UMC PRODUCT ErrorCode Catalog", "catalog.md", "catalog.json", "error")
        )

        val staleFiles = expectedFiles.filter { (file, expected) -> !file.exists() || file.readText() != expected }.keys
        if (staleFiles.isNotEmpty()) {
            throw GradleException(
                "문서 카탈로그가 최신 상태가 아닙니다. ./gradlew generateDocumentationCatalogs 실행 필요: " +
                    staleFiles.joinToString { it.relativeTo(project.projectDir).invariantSeparatorsPath }
            )
        }

        println("[checkDocumentationCatalogs] 문서 카탈로그가 최신 상태입니다.")
    }
}

tasks.register("validateDocumentationCatalogs") {
    group = "verification"
    description = "API ID와 ErrorCode의 누락/중복을 검사합니다. -PstrictDocumentationCatalogs=true 설정 시 실패 처리합니다."

    doLast {
        val issues = collectDocumentationIssues()
        if (issues.isEmpty()) {
            println("[validateDocumentationCatalogs] API ID와 ErrorCode 할당 문제가 없습니다.")
            return@doLast
        }

        issues.forEach { println("[validateDocumentationCatalogs] $it") }
        if (project.findProperty("strictDocumentationCatalogs") == "true") {
            throw GradleException("문서 카탈로그 검증 실패: ${issues.size}건")
        }
    }
}

tasks.register("nextApiId") {
    group = "documentation"
    description = "다음 API ID를 추천합니다. 예: ./gradlew nextApiId -Pprefix=CHALLENGER"

    doLast {
        val prefix = (project.findProperty("prefix") as String?)?.uppercase(Locale.ROOT)
            ?: throw GradleException("-Pprefix=CHALLENGER 형식으로 API ID prefix를 지정해주세요.")
        val width = (project.findProperty("width") as String?)?.toIntOrNull() ?: 3
        val next = buildApiDocumentationEntries()
            .map { it.apiId }
            .mapNotNull { Regex("^${Regex.escape(prefix)}-(\\d+)$").find(it)?.groupValues?.get(1)?.toIntOrNull() }
            .maxOrNull()
            ?.plus(1)
            ?: 1

        println("$prefix-${next.toString().padStart(width, '0')}")
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
