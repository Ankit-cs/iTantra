import * as Speech from 'expo-speech';

export class TTSService {
  static async speak(text: string, language: string = 'en') {
    const isSpeaking = await Speech.isSpeakingAsync();
    if (isSpeaking) {
      await Speech.stop();
    }
    Speech.speak(text, { language });
  }

  static async stop() {
    await Speech.stop();
  }
}
