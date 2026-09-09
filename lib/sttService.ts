import * as Vosk from 'react-native-vosk';

export class STTService {
  private isLoaded = false;

  constructor() {
  }

  async initModel(modelPath: string) {
    try {
      await Vosk.loadModel(modelPath);
      this.isLoaded = true;
      console.log('Vosk model loaded successfully');
    } catch (e) {
      console.error('Failed to load Vosk model', e);
    }
  }

  async startListening(onResult: (text: string) => void) {
    if (!this.isLoaded) {
      console.warn('Vosk model is not loaded yet');
      return;
    }
    
    // The exact listener API depends on the react-native-vosk version.
    Vosk.onResult((res: any) => {
      if (res) onResult(res.text || res);
    });
    
    try {
      await Vosk.start();
    } catch (e) {
      console.error('Failed to start listening', e);
    }
  }

  async stopListening() {
    if (!this.isLoaded) return;
    Vosk.stop();
  }
}

export const sttService = new STTService();
