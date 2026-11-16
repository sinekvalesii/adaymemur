/**
 * Uygulama kapalıyken gelen FCM mesajları için başsız (headless) işleyici.
 * Bildirim mesajlarını sistem gösterir; burada ek iş yapılmaz, yalnızca uyarı önlenir.
 */
import { getApp } from '@react-native-firebase/app';
import { getMessaging, setBackgroundMessageHandler } from '@react-native-firebase/messaging';

try {
  getApp();
  setBackgroundMessageHandler(getMessaging(), async () => {});
} catch {
  // Firebase yapılandırılmamış derleme
}
