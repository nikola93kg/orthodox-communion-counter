#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

echo "=== Building Orthodox Communion Counter APK ==="

BUILD_TOOLS="/Users/apple/Library/Android/sdk/build-tools/35.0.0"
PLATFORM="/Users/apple/Library/Android/sdk/platforms/android-33/android.jar"
AAPT2="$BUILD_TOOLS/aapt2"
D8="$BUILD_TOOLS/d8"
ZIPALIGN="$BUILD_TOOLS/zipalign"
APKSIGNER="$BUILD_TOOLS/apksigner"
KEYSTORE="/tmp/debug.keystore"

# 1. Clean & prepare build directories
rm -rf build
mkdir -p build/gen build/classes build/dex

# 2. Ensure keystore exists
if [ ! -f "$KEYSTORE" ]; then
    echo "Creating debug keystore..."
    keytool -genkey -v -keystore "$KEYSTORE" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
fi

# 3. Compile Android resources
echo "[1/6] Compiling resources with aapt2..."
"$AAPT2" compile --dir res -o build/compiled_res.zip

# 4. Link resources & generate R.java
echo "[2/6] Linking resources and generating R.java..."
"$AAPT2" link -I "$PLATFORM" \
    --manifest AndroidManifest.xml \
    -o build/base.apk \
    --java build/gen \
    --auto-add-overlay \
    build/compiled_res.zip

# 5. Compile Java sources
echo "[3/6] Compiling Java source files..."
javac --release 8 -encoding UTF-8 -cp "$PLATFORM" -d build/classes \
    build/gen/org/orthodox/communioncounter/R.java \
    src/org/orthodox/communioncounter/model/LiturgyRecord.java \
    src/org/orthodox/communioncounter/db/LiturgyDbHelper.java \
    src/org/orthodox/communioncounter/LiturgyAdapter.java \
    src/org/orthodox/communioncounter/MainActivity.java \
    src/org/orthodox/communioncounter/ChronicleActivity.java

# 6. Convert bytecode to Dalvik DEX
echo "[4/6] Creating Dalvik bytecode with d8..."
CLASS_FILES=$(find build/classes -name "*.class")
"$D8" --release --min-api 21 --lib "$PLATFORM" --output build/dex/ $CLASS_FILES

# 7. Package DEX into APK
echo "[5/6] Packaging and aligning APK..."
cd build/dex
zip -u ../base.apk classes.dex
cd "$DIR"

"$ZIPALIGN" -f -p 4 build/base.apk build/aligned.apk

# 8. Sign APK
echo "[6/6] Signing APK..."
"$APKSIGNER" sign --ks "$KEYSTORE" --ks-pass pass:android --key-pass pass:android --out build/CommunionCounter.apk build/aligned.apk

echo "=== Build Successful! ==="
ls -lh build/CommunionCounter.apk

# 9. Deploy to tablet if connected
if adb devices | grep -q "411f811418a560e7.*device"; then
    echo "=== Deploying to Galaxy Note 10.1 tablet ==="
    adb install -r build/CommunionCounter.apk
    echo "=== Launching Communion Counter on tablet ==="
    adb shell am start -n org.orthodox.communioncounter/.MainActivity
fi
