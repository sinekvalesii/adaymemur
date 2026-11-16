#!/usr/bin/env bash
# Emülatörde uygulamayı açıp ekran görüntüsü alır (CI doğrulaması ve mağaza görselleri için).
set -x
PKG=com.selcukyus.adaymemur
ACT=$PKG/.MainActivity
OUT=ekranlar
mkdir -p $OUT
cek() { sleep "$2"; adb exec-out screencap -p > "$OUT/$1.png"; }
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS || true
adb shell settings put system time_12_24 24
adb shell am start -n $ACT; cek 01-karsilama 10
adb shell am force-stop $PKG
adb shell am start -n $ACT --ez atla true --ei sekme 0; cek 02-ilanlar 18
ID=$(curl -s https://sinekvalesii.github.io/adaymemur/data/ilanlar.json | python3 -c "
import sys,json
d=json.load(sys.stdin)['ilanlar']
c=[i for i in d if i.get('kadro') and i.get('sonBasvuru') and i.get('kpss') and 'lisans' in i.get('egitim',[])]
c=c or [i for i in d if i.get('kadro') and i.get('sonBasvuru')] or d
print(c[0]['id'])")
adb shell am force-stop $PKG
adb shell am start -n $ACT --ez demoPro true --ez demoProfil true --ei sekme 1; cek 03-bana-uygun 15
adb shell am start -n $ACT --es ilanId "$ID"; cek 04-detay 8
adb shell input swipe 540 1700 540 700 400; cek 05-detay-uygunluk 3
adb shell am start -n $ACT --ei sekme 0 --es rota filtre; cek 06-filtre 4
adb shell am start -n $ACT --ei sekme 0 --es rota gundem; cek 07-gundem 4
adb shell am start -n $ACT --ei sekme 3; cek 08-hesap 3
adb shell cmd uimode night yes
adb shell am force-stop $PKG
adb shell am start -n $ACT --ez demoPro true --ei sekme 0; cek 09-koyu-ilanlar 14
adb logcat -d -s AndroidRuntime:E > $OUT/cokme.txt || true
ls -la $OUT
