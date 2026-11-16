/// <reference types="node" />
import fs from 'node:fs';
import path from 'node:path';

import type { ConfigContext, ExpoConfig } from 'expo/config';

/**
 * Aday Memur — Expo yapılandırması.
 * google-services.json (Firebase) varsa bildirim, hesap ve forum derlemeye dahil edilir.
 * AdMob uygulama kimliği verilmezse Google'ın resmî TEST kimliği kullanılır (reklam birimleri boşken reklam çıkmaz).
 */
const GOOGLE_SERVICES = './google-services.json';
const firebaseVar = fs.existsSync(path.join(__dirname, GOOGLE_SERVICES));
const ADMOB_TEST_APP_ID = 'ca-app-pub-3940256099942544~3347511713';
const admobAppId = process.env.ADMOB_ANDROID_APP_ID?.trim() || ADMOB_TEST_APP_ID;

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: 'Aday Memur',
  slug: 'aday-memur',
  owner: process.env.EXPO_OWNER || undefined,
  version: '1.0.0',
  orientation: 'portrait',
  scheme: 'adaymemur',
  userInterfaceStyle: 'automatic',
  icon: './assets/brand/icon.png',
  backgroundColor: '#F4F6F9',
  android: {
    package: 'com.selcukyus.adaymemur',
    versionCode: Number(process.env.ANDROID_VERSION_CODE || 1),
    adaptiveIcon: {
      foregroundImage: './assets/brand/adaptive-foreground.png',
      backgroundColor: '#1B1E23',
      monochromeImage: './assets/brand/adaptive-monochrome.png',
    },
    predictiveBackGestureEnabled: false,
    ...(firebaseVar ? { googleServicesFile: GOOGLE_SERVICES } : {}),
    permissions: ['android.permission.POST_NOTIFICATIONS'],
    blockedPermissions: [
      'android.permission.READ_CALENDAR',
      'android.permission.WRITE_CALENDAR',
      'android.permission.RECORD_AUDIO',
      'android.permission.READ_EXTERNAL_STORAGE',
      'android.permission.WRITE_EXTERNAL_STORAGE',
      'android.permission.SYSTEM_ALERT_WINDOW',
    ],
  },
  ios: { bundleIdentifier: 'com.selcukyus.adaymemur', supportsTablet: false },
  web: { favicon: './assets/brand/favicon.png', bundler: 'metro' },
  plugins: [
    'expo-router',
    [
      'expo-splash-screen',
      {
        image: './assets/brand/splash-mark.png',
        imageWidth: 96,
        backgroundColor: '#F4F6F9',
        dark: { image: './assets/brand/splash-mark.png', backgroundColor: '#0F1115' },
      },
    ],
    'expo-image',
    'expo-web-browser',
    [
      'expo-build-properties',
      {
        android: {
          minSdkVersion: 24,
        },
      },
    ],
    ['react-native-google-mobile-ads', { androidAppId: admobAppId, iosAppId: 'ca-app-pub-3940256099942544~1458002511', delayAppMeasurementInit: true }],
    ...(firebaseVar
      ? ([
          '@react-native-firebase/app',
          '@react-native-firebase/auth',
          [
            '@react-native-firebase/messaging',
            { android: { notificationIcon: './assets/brand/notification-icon.png', notificationColor: '#F1D23A' } },
          ],
        ] as ExpoConfig['plugins'] & object[])
      : []),
    './plugins/withReleaseSigning',
  ],
  experiments: { typedRoutes: true },
  extra: { firebase: firebaseVar },
});
