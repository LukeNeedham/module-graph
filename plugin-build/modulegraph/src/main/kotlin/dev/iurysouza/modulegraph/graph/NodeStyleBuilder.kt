package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.focusColor

internal object NodeStyleBuilder {
    /**
     * Generates Mermaid code to customize the styling of nodes based on their focus state and plugin type.
     *
     * @param digraphModel The list of digraph models containing source and target nodes.
     * @param options Configuration options including theme settings and whether to set styles by plugin type.
     * @return A `MermaidCode` containing the Mermaid syntax for highlighting the nodes.
     *
     * Example output of generated Mermaid code:
     * ```mermaid
     * classDef java fill:#C3E88D,stroke:#fff,stroke-width:2px,color:#fff;
     * class gama java;
     *
     * classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
     * class alpha focus;
     * ```
     *Where "alpha" would be one of the focused nodes.
     */
    fun build(digraphModel: List<DigraphModel>, options: GraphOptions): MermaidCode {
        val distinctNodes = digraphModel
            .flatMap { listOf(it.source, it.target) }
            .distinctBy { it.fullName }

        val pluginTypeStyling = applyStylingByPluginType(distinctNodes, options)
        val focusedNodesStyling = highlightFocusedNodes(distinctNodes, options)
        return MermaidCode(
            buildString {
                append(pluginTypeStyling)
                if (pluginTypeStyling.isNotBlank()) {
                    lineBreak()
                }
                append(focusedNodesStyling.value)
            },
        )
    }

    private fun applyStylingByPluginType(
        nodeList: List<ModuleNode>,
        options: GraphOptions,
    ): String {
        return if (options.setStyleByModuleType) {
            """
                |
                |${
                nodeList
                    .distinctBy { it.type }
                    .joinToString("\n") {
                        defineStyleClass(it.pluginClass(), it.type.color)
                    }
            }
            """.trimMargin() + """
                |
                |${nodeList.joinToString("\n") { "class ${it.fullName} ${it.pluginClass()}" }}
            """.trimMargin()
        } else {
            ""
        }
    }

    private fun defineStyleClass(className: String, color: String) =
        """classDef $className fill:$color,stroke:#fff,stroke-width:2px,color:#fff;"""

    private fun ModuleNode.pluginClass(): String = when (type) {
        is ModuleType.ReactNativeLibrary -> "react-native"
        else -> type.id.split(".").takeLast(2).joinToString("-")
    }

    private fun highlightFocusedNodes(
        nodeList: List<ModuleNode>,
        options: GraphOptions,
    ): MermaidCode = MermaidCode(
        if (options.focusedNodesRegex == null) {
            ""
        } else {
            """
               |
               |${defineStyleClass(FOCUS_CLASS_NAME, options.theme.focusColor())}
               |${nodeList.filter { it.isFocused }.joinToString("\n") { "class ${it.fullName} $FOCUS_CLASS_NAME" }}
            """.trimMargin()
        },
    )

    private const val FOCUS_CLASS_NAME = "focus"
}
