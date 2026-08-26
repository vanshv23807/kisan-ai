with open('app/src/main/java/com/example/ui/screens/PoultryScannerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.litongjava.jlibrosa.JLibrosa", "import com.jlibrosa.audio.JLibrosa")

# Remove extra MediaRecorder import
content = content.replace("import android.media.MediaRecorder\nimport android.media.MediaRecorder", "import android.media.MediaRecorder")

# For array iteration, let's make it explicit to avoid any type inference issues
content = content.replace("for (row in melSpec)", "for (row: FloatArray in melSpec)")
content = content.replace("for (v in row)", "for (v: Float in row)")

with open('app/src/main/java/com/example/ui/screens/PoultryScannerScreen.kt', 'w') as f:
    f.write(content)

