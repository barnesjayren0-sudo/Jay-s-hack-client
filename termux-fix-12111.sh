#!/data/data/com.termux/files/usr/bin/bash
# Apply 1.21.11 compile fixes in-place on Termux
set -e
cd ~/Jay-s-hack-client || exit 1

echo "[1/5] selectedSlot -> get/setSelectedSlot"
find src -name '*.java' -print0 | xargs -0 sed -i \
  -e 's/\.getInventory()\.selectedSlot/.getInventory().getSelectedSlot()/g'
find src -name '*.java' -print0 | xargs -0 perl -i -pe \
  's/\.getInventory\(\)\.getSelectedSlot\(\)\s*=\s*([^;]+);/.getInventory().setSelectedSlot($1);/g'

echo "[2/5] getPos() -> getX/Y/Z Vec3d"
find src -name '*.java' -print0 | xargs -0 sed -i \
  -e 's/\([a-zA-Z0-9_]*\)\.getPos() /new net.minecraft.util.math.Vec3d(\1.getX(), \1.getY(), \1.getZ()) /g' \
  -e 's/\([a-zA-Z0-9_]*\)\.getPos())/new net.minecraft.util.math.Vec3d(\1.getX(), \1.getY(), \1.getZ()))/g' \
  -e 's/\([a-zA-Z0-9_]*\)\.getPos()\./new net.minecraft.util.math.Vec3d(\1.getX(), \1.getY(), \1.getZ())./g'

sed -i 's/ChunkPos cp = new net.minecraft.util.math.Vec3d(chunk.getX(), chunk.getY(), chunk.getZ());/ChunkPos cp = chunk.getPos();/g' \
  src/main/java/com/jay/hackclient/module/modules/BaseFinder.java \
  src/main/java/com/jay/hackclient/module/modules/BuildFinder.java 2>/dev/null || true

echo "[3/5] remove private setTag overrides"
find src -name '*.java' -print0 | xargs -0 perl -i -0pe \
  's/private void setTag\(String t\) \{\s*try \{.*?catch \(Throwable ignored\) \{\}\s*\}//sg'

echo "[4/5] ClickGui Matrix3x2fStack API"
sed -i \
  -e 's/getMatrices()\.push()/getMatrices().pushMatrix()/g' \
  -e 's/getMatrices()\.pop()/getMatrices().popMatrix()/g' \
  -e 's/getMatrices()\.translate(cx, cy, 0)/getMatrices().translate(cx, cy)/g' \
  -e 's/getMatrices()\.translate(-cx, -cy, 0)/getMatrices().translate(-cx, -cy)/g' \
  -e 's/getMatrices()\.scale(\([^,]*\), \([^,]*\), [^)]*)/getMatrices().scale(\1, \2)/g' \
  src/main/java/com/jay/hackclient/gui/ClickGuiScreen.java

echo "[5/5] fallDistance cast"
find src -name '*.java' -print0 | xargs -0 sed -i \
  -e 's/float \([a-zA-Z0-9_]*\) = \([a-zA-Z0-9_]*\)\.fallDistance;/float \1 = (float) \2.fallDistance;/g'

echo "Done. Rebuild:"
echo "  gradle clean build --no-daemon"
