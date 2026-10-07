key:
	keytool -genkeypair \
		-v \
		-keystore beeponcharge-release.keystore \
		-alias beeponcharge \
		-keyalg RSA \
		-keysize 2048 \
		-validity 25000

release:
	./gradlew renameReleaseApk

ver:
	apksigner verify --verbose app/build/outputs/apk/release/*.apk

debug:
	./gradlew assembleDebug

inst:
	adb install -r app/build/outputs/apk/debug/app-debug.apk

all: debug inst
	beep

log:
	adb logcat -C | grep -i BeepOnCharge