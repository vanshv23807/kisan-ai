import re

with open('app/src/main/java/com/example/ui/screens/MastitisScannerScreen.kt', 'r') as f:
    content = f.read()

# 1. Add imports
imports_target = "import org.pytorch.torchvision.TensorImageUtils"
new_imports = """import org.pytorch.torchvision.TensorImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions"""
content = content.replace(imports_target, new_imports)

# 2. Update prediction variables
pred_target = """    val isError = mastitisPrediction < 0f
    val isMastitis = mastitisPrediction >= 0.5f
    val rawPercent = (mastitisPrediction * 100).toInt()
    
    // If it's normal (0.1), confidence is 90%. If it's mastitis (0.9), confidence is 90%.
    val confidenceScore = if(isError) 0 else if(isMastitis) rawPercent else (100 - rawPercent)
    val riskLevel = if(isError) "Error" else if(mastitisPrediction > 0.75f) "High" else if(mastitisPrediction > 0.5f) "Moderate" else "Low"
    val sccEstimate = if(isError) "Unknown" else if(isMastitis) "≈ ${400000 + (rawPercent * 2000)} cells/mL" else "≈ 120,000 cells/mL\""""

pred_new = """    val isInvalidImage = mastitisPrediction == -2f
    val isError = mastitisPrediction == -1f
    val isMastitis = mastitisPrediction >= 0.5f
    val rawPercent = (mastitisPrediction * 100).toInt()
    
    val confidenceScore = if(isError || isInvalidImage) 0 else if(isMastitis) rawPercent else (100 - rawPercent)
    val riskLevel = if(isInvalidImage) "Invalid Image" else if(isError) "Error" else if(mastitisPrediction > 0.75f) "High" else if(mastitisPrediction > 0.5f) "Moderate" else "Low"
    val sccEstimate = if(isError || isInvalidImage) "Unknown" else if(isMastitis) "≈ ${400000 + (rawPercent * 2000)} cells/mL" else "≈ 120,000 cells/mL\""""
content = content.replace(pred_target, pred_new)

# 3. Add ML Kit Check inside LaunchedEffect
inference_target = """                    // 1. Copy model to internal storage"""
inference_new = """                    // 0. Stage 1: Fast Object Detection via ML Kit Image Labeling
                    // We check if the image resembles an animal, cow, udder, or liquid (milk)
                    var isValidUdder = false
                    var labelCheckComplete = false
                    
                    val inputImage = InputImage.fromBitmap(capturedBitmap!!, 0)
                    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                    
                    labeler.process(inputImage)
                        .addOnSuccessListener { labels ->
                            for (label in labels) {
                                val text = label.text.lowercase()
                                // Broad list of acceptable categories for an udder or milk sample
                                if (text.contains("cow") || text.contains("cattle") || text.contains("animal") 
                                    || text.contains("udder") || text.contains("milk") || text.contains("liquid")
                                    || text.contains("livestock") || text.contains("mammal")
                                    || text.contains("snout")) {
                                    isValidUdder = true
                                    break
                                }
                            }
                            labelCheckComplete = true
                        }
                        .addOnFailureListener {
                            // On failure, bypass so we don't break the app
                            isValidUdder = true 
                            labelCheckComplete = true
                        }
                    
                    while (!labelCheckComplete) {
                        delay(100)
                    }
                    
                    if (!isValidUdder) {
                        mastitisPrediction = -2f // Code for Invalid Image
                        scanProgress = 1.0f
                        withContext(Dispatchers.Main) {
                            scanComplete = true
                            isScanning = false
                        }
                        return@withContext
                    }

                    // 1. Copy model to internal storage"""
content = content.replace(inference_target, inference_new)

# 4. Update UI Alert Banner to handle isInvalidImage
ui_target = """                                    Text(
                                        "⚠️ Mastitis Risk: $riskLevel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AlertRed
                                    )"""
ui_new = """                                    Text(
                                        if (isInvalidImage) "⚠️ Image Not Recognized" else "⚠️ Mastitis Risk: $riskLevel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AlertRed
                                    )"""
content = content.replace(ui_target, ui_new)

detailed_ui_target = """                            // Findings rows
                            DiagnosisRow("Estimated SCC", sccEstimate, AlertRed)
                            DiagnosisRow("Udder Inflammation", "Mild swelling detected (left rear quarter)", WarningYellowDark)
                            DiagnosisRow("Milk Clarity", "Slight turbidity / discoloration observed", AlertRed)
                            DiagnosisRow("Skin Texture", "Minor redness around teat base", WarningYellowDark)"""

detailed_ui_new = """                            // Findings rows
                            if (isInvalidImage) {
                                Text("No udder or milk detected in the frame. Please take a clear picture of the cow's udder or a fresh milk sample.", color = AlertRed, fontSize = 14.sp)
                            } else {
                                DiagnosisRow("Estimated SCC", sccEstimate, AlertRed)
                                DiagnosisRow("Udder Inflammation", if (isMastitis) "Mild swelling detected" else "Normal", if(isMastitis) WarningYellowDark else PrimaryGreen)
                                DiagnosisRow("Milk Clarity", if(isMastitis) "Slight turbidity / discoloration" else "Normal", if(isMastitis) AlertRed else PrimaryGreen)
                                DiagnosisRow("Skin Texture", if(isMastitis) "Minor redness around teat base" else "Normal", if(isMastitis) WarningYellowDark else PrimaryGreen)
                            }"""
content = content.replace(detailed_ui_target, detailed_ui_new)

recommendation_target = """                            Text(
                                "💊 Recommended Actions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryGreen
                            )
                            RecommendationItem("Isolate Cow #42 from the milking herd immediately")
                            RecommendationItem("Perform California Mastitis Test (CMT) to confirm SCC levels")
                            RecommendationItem("Contact veterinarian for antibiotic sensitivity testing")
                            RecommendationItem("Switch to hand-milking for the affected quarter")
                            RecommendationItem("Disinfect milking equipment after each session")"""

recommendation_new = """                            Text(
                                "💊 Recommended Actions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryGreen
                            )
                            if (isInvalidImage) {
                                RecommendationItem("Ensure the camera is pointed directly at the animal.")
                                RecommendationItem("Make sure the environment is well-lit.")
                            } else if (isMastitis) {
                                RecommendationItem("Isolate affected cow from the milking herd immediately.")
                                RecommendationItem("Perform California Mastitis Test (CMT) to confirm SCC levels.")
                                RecommendationItem("Contact veterinarian for antibiotic sensitivity testing.")
                                RecommendationItem("Switch to hand-milking for the affected quarter.")
                            } else {
                                RecommendationItem("Continue regular milking schedule.")
                                RecommendationItem("Maintain standard hygiene practices.")
                            }"""
content = content.replace(recommendation_target, recommendation_new)


with open('app/src/main/java/com/example/ui/screens/MastitisScannerScreen.kt', 'w') as f:
    f.write(content)

