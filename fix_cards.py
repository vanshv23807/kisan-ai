import re

with open('app/src/main/java/com/example/ui/screens/LivestockScreen.kt', 'r') as f:
    content = f.read()

# Fix Crop Card
crop_target = """                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {"""
crop_replacement = """                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {"""
content = content.replace(crop_target, crop_replacement)

# Next, inside Crop Card, add weight to Column
# We need to find the specific Column.
# We can just replace all `Column {` with `Column(modifier = Modifier.weight(1f)) {` 
# ONLY IF they come right after `Spacer(modifier = Modifier.width(12.dp))`
content = re.sub(r'Spacer\(modifier = Modifier\.width\(12\.dp\)\)\n\n\s*Column \{', 'Spacer(modifier = Modifier.width(12.dp))\n\n                                Column(modifier = Modifier.weight(1f)) {', content)


with open('app/src/main/java/com/example/ui/screens/LivestockScreen.kt', 'w') as f:
    f.write(content)

