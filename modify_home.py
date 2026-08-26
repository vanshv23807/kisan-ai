import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# Remove AI Crop Scanner
content = re.sub(r'[ \t]*// AI Crop Photo Growth & Disease Scanner Block.*?// Smart Bluetooth Cattle Tracker', '// Smart Bluetooth Cattle Tracker', content, flags=re.DOTALL)

# Remove Crop Insurance
content = re.sub(r'[ \t]*// Crop Insurance & Govt Payout Support Block.*?// Crop Growth Tracker Card', '// Crop Growth Tracker Card', content, flags=re.DOTALL)

# Remove Govt Schemes
content = re.sub(r'[ \t]*// Government Schemes & Subsidies Block.*?// Bank Accounts & Credit Card', '// Bank Accounts & Credit Card', content, flags=re.DOTALL)

# Remove Helplines
# The helplines item starts with "// Farmer Emergency & Govt Helpline Card"
# and ends right before the LazyColumn closing brace. 
# After the LazyColumn closing brace, there are dialogs starting with "if (showBankInputDialog)". No wait, those are inside `item`? No, dialogs are in the main Composable scope or inside `item`? In Compose, you can't put `if (showDialog)` inside `LazyColumn` directly unless it's inside `item`.
# Let's check how the file is structured at the end of the LazyColumn.
