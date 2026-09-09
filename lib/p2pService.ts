import {
  initialize,
  startDiscoveringPeers,
  connect,
  sendMessage,
  receiveMessage
} from 'react-native-wifi-p2p';
import { PermissionsAndroid, Platform } from 'react-native';

export class P2PService {
  static async init() {
    try {
      if (Platform.OS === 'android') {
        await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          PermissionsAndroid.PERMISSIONS.NEARBY_WIFI_DEVICES
        ]);
      }
      await initialize();
      console.log('P2P initialized');
    } catch (e) {
      console.error('P2P initialization failed', e);
    }
  }

  static async discoverPeers() {
    try {
      await startDiscoveringPeers();
    } catch (e) {
      console.error('Failed to discover peers', e);
    }
  }

  static async connectToDevice(deviceAddress: string) {
    try {
      await connect(deviceAddress);
    } catch (e) {
      console.error('Failed to connect to device', e);
    }
  }

  static async send(message: string) {
    try {
      // Send a payload string
      await sendMessage(message);
    } catch (e) {
      console.error('Failed to send message', e);
    }
  }

  static async onReceiveMessage(callback: (message: string) => void) {
    // Listen for incoming semantic payloads
    try {
      while (true) {
        const message = await receiveMessage({ meta: false });
        callback(message);
      }
    } catch (e) {
      console.error('Failed to set up message receiver', e);
    }
  }
}
