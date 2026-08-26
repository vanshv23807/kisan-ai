package com.example

import org.junit.Test
import org.pytorch.LiteModuleLoader
import java.io.File

class ModelLoadTest {
    @Test
    fun testLoad() {
        val file = File("src/main/assets/chicken_model_lite.ptl")
        println(file.absolutePath)
        try {
            val module = LiteModuleLoader.load(file.absolutePath)
            println("Loaded successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
