package cn.lliiooll.processors

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

@KotlinPoetKspPreview
class UiItemAgentEntryProcessor(
        private val codeGenerator: CodeGenerator,
        private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("io.github.qauxv.base.annotation.UiItemAgentEntry")
                .filter { it is KSClassDeclaration }
                .map { it as KSClassDeclaration }
                .toList()
        if (symbols.isEmpty()) {
            return emptyList()
        }

        logger.info("UiItemAgentEntryProcessor start.")
        val simpleNameMap = HashMap<String, String>(symbols.size)
        val mGetApi = FunSpec.builder("getAnnotatedUiItemAgentEntryList").run {
            addCode(CodeBlock.Builder().run {
                add("return arrayOf(«")
                symbols.forEachIndexed { index, ksClassDeclaration ->
                    if (simpleNameMap.contains(ksClassDeclaration.simpleName.asString())) {
                        logger.error("Duplicate name in UiItemAgentEntry's simpleName: ${ksClassDeclaration.qualifiedName?.asString() ?: "null"}, ${simpleNameMap[ksClassDeclaration.simpleName.asString()]}")
                    } else {
                        simpleNameMap[ksClassDeclaration.simpleName.asString()] =
                                ksClassDeclaration.qualifiedName?.asString() ?: "null"
                    }
                    val isJava = ksClassDeclaration.containingFile?.filePath?.endsWith(".java") == true
                    // logger.warn("Processing >>> $ksClassDeclaration,isJava = $isJava")
                    val typeName = ksClassDeclaration.asStarProjectedType().toTypeName()
                    val format = StringBuilder("\n%T").run {
                        if (isJava) append(".INSTANCE")
                        if (index == symbols.lastIndex) append("\n") else append(",")
                        toString()
                    }
                    add(format, typeName)
                }
                add("»)")
                build()
            })
            build()
        }
        logger.info("UiItemAgentEntryProcessor count = " + symbols.size + ".")
        // @file:JvmName("AnnotatedUiItemAgentEntryList")
        val annotationSpec = AnnotationSpec.builder(JvmName::class).run {
            addMember("%S", "AnnotatedUiItemAgentEntryList")
            build()
        }
        val dependencies = Dependencies(true, *(symbols.map {
            it.containingFile!!
        }.toTypedArray()))
        FileSpec.builder("io.github.qauxv.gen", "AnnotatedUiItemAgentEntryList")
                .addAnnotation(annotationSpec)
                .addFunction(mGetApi)
                .build()
                .writeTo(codeGenerator, dependencies)
        return emptyList()
    }
}

@KotlinPoetKspPreview
class UiItemAgentEntryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return UiItemAgentEntryProcessor(environment.codeGenerator, environment.logger)
    }
}
