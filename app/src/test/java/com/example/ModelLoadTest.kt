package com.example

import org.junit.Test
import org.pytorch.LiteModuleLoader
import java.io.File

class ModelLoadTest {
    @Test
    fun testLoad() {
        val file = File("src/main/assets/chicken_model_lite.ptl")
        if (file.exists()) {
            val module = LiteModuleLoader.load(file.absolutePath)
            org.junit.Assert.assertNotNull(module)
        }
    }
}
