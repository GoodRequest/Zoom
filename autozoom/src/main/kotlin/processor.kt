package main

import org.jetbrains.annotations.Nullable
import zoom.Zoom
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class ZoomProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {

        roundEnv.getElementsAnnotatedWith(Zoom::class.java).forEach { element ->
            val className = element.simpleName.toString()
            val zoomName  = element.getAnnotation(Zoom::class.java).name.takeIf(String::isNotBlank) ?: "${className}Zoom"

            val fields    = element.enclosedElements.filter { it.kind == ElementKind.FIELD }
            val folder    = File(processingEnv.options["kapt.kotlin.generated"]!!).apply { this.mkdirs() }
            val file      = File(folder, "$zoomName.kt")

            file.writeText("package ${element.enclosingElement}\n")
            file.appendText("import zoom.*")
            file.appendText("\n\n")
            file.appendText("object $zoomName\n")

            // lens or optional for all fields
            fields.forEach { field ->
                val isNullable = field.getAnnotation(Nullable::class.java) != null
                val opticType  = if(isNullable) "nullableLens" else "lens"
                file.appendText("val $zoomName.$field get() = $opticType(" +
                    "getter = $className::$field, " +
                    "setter = { a, b -> a.copy($field = b) })\n")
            }

            // lens composition
            fields.forEach { field ->
                file.appendText("val <A> Lens<A, $className>.$field get() = compose($zoomName.$field) \n")
                file.appendText("val <A> Lens<A, $className?>.$field get() = compose($zoomName.$field) \n")
            }

            // optional composition
            fields.forEach { field ->
                file.appendText("val <A> Optional<A, $className>.$field get() = compose($zoomName.$field) \n")
            }
        }
        return true
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
    override fun getSupportedAnnotationTypes(): Set<String> = setOf(Zoom::class.java.canonicalName)
}