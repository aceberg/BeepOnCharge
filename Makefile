debug:
	./gradlew assembleDebug

inst:
	adb install -r app/build/outputs/apk/debug/app-debug.apk

all: debug inst
	beep

log:
	adb logcat -C | grep -i BeepOnCharge