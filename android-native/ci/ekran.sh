#!/usr/bin/env bash
# Emülatörde uygulamayı açıp ekran görüntüsü alır (yalnızca CI doğrulaması için).
set -x
PKG=com.selcukyus.adaymemur
ACT=$PKG/.MainActivity
OUT=ekranlar
mkdir -p $OUT
cek() { sleep "$2"; adb exec-out screencap -p > "$OUT/$1.png"; }
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS || true
adb shell am start -n $ACT; cek 01-karsilama 10
adb shell am force-stop $PKG
adb shell am start -n $ACT --ez atla true --ei sekme 0; cek 02-ilanlar 18
adb shell am start -n $ACT --ei sekme 1; cek 03-bana-uygun 4
adb shell am start -n $ACT --ei sekme 2; cek 04-kaydedilen 3
adb shell am start -n $ACT --ei sekme 3; cek 05-hesap 3
ID=$(curl -s https://sinekvalesii.github.io/adaymemur/data/ilanlar.json | python3 -c "import sys,json;d=json.load(sys.stdin)['ilanlar'];print(next((i['id'] for i in d if i.get('kadro') and i.get('sonBasvuru')), d[0]['id']))")
adb shell am start -n $ACT --es ilanId "$ID"; cek 06-detay 8
adb shell cmd uimode night yes
adb shell am force-stop $PKG
adb shell am start -n $ACT --ei sekme 0; cek 07-koyu-ilanlar 12
adb logcat -d -s AndroidRuntime:E > $OUT/cokme.txt || true
ls -la $OUT
