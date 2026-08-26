with open('app/src/main/java/com/example/ui/screens/PoultryScannerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import android.media.MediaRecorder\nimport android.media.MediaRecorder", "import android.media.MediaRecorder")

with open('app/src/main/java/com/example/ui/screens/PoultryScannerScreen.kt', 'w') as f:
    f.write(content)

