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

    val summaryMatch = Regex("^\\[([^\\]]+)]\\s*(.*)$", RegexOption.DOT_MATCHES_ALL).find(summary)
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
    appendLine("# UMC PRODUCT API 목록")
    appendLine()
    appendLine("UMC PRODUCT 서버에서 제공하는 모든 API의 ID, 엔드포인트, HTTP 메서드, 역할, deprecated 여부를 정리한 표입니다.")
    appendLine()
    appendLine("> 본 문서는 `./gradlew generateApiCatalog` 또는 `./gradlew generateDocumentationCatalogs`로 자동 생성합니다. API ID는 `@Operation(operationId = \"...\")`를 우선 사용하고, 없으면 `summary`의 `[XXX-000]` prefix를 사용합니다.")
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
    appendLine("# UMC PRODUCT ErrorCode 목록")
    appendLine()
    appendLine("UMC PRODUCT 서버에서 사용하는 `BaseCode` 기반 ErrorCode enum을 정리한 표입니다.")
    appendLine()
    appendLine("> 본 문서는 `./gradlew generateErrorCodeCatalog` 또는 `./gradlew generateDocumentationCatalogs`로 자동 생성합니다. ErrorCode의 원본은 각 `*ErrorCode.java` enum입니다.")
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
        writeIfChanged(apiCatalogMarkdownFile, buildApiCatalogMarkdown(entries))
        writeIfChanged(apiCatalogJsonFile, buildApiCatalogJson(entries))
        println("[generateApiCatalog] ${entries.size}개 API를 문서화했습니다.")
    }
}

tasks.register("generateErrorCodeCatalog") {
    group = "documentation"
    description = "ErrorCode enum을 스캔해 ErrorCode 목록 문서를 생성합니다."

    doLast {
        val entries = extractErrorCodeEntries()
        writeIfChanged(errorCodeCatalogMarkdownFile, buildErrorCodeCatalogMarkdown(entries))
        writeIfChanged(errorCodeCatalogJsonFile, buildErrorCodeCatalogJson(entries))
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
            errorCodeCatalogJsonFile to buildErrorCodeCatalogJson(errorCodeEntries)
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
