import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { WalkieTalkieButton } from '../components/WalkieTalkieButton';
import { sttService } from '../lib/sttService';
import { TTSService } from '../lib/ttsService';
import { P2PService } from '../lib/p2pService';

export default function WalkieTalkieScreen() {
  const [isRecording, setIsRecording] = useState(false);
  const [messages, setMessages] = useState<{sender: string, text: string}[]>([]);
  const [status, setStatus] = useState('Initializing...');

  useEffect(() => {
    const setup = async () => {
      try {
        await P2PService.init();
        await sttService.initModel('model-en-us'); 
        
        P2PService.onReceiveMessage((msg) => {
          setMessages(prev => [...prev, { sender: 'Peer', text: msg }]);
          TTSService.speak(msg);
        });

        setStatus('Ready. Hold button to talk.');
      } catch (e) {
        setStatus('Error initializing services.');
      }
    };
    setup();

    return () => {
      TTSService.stop();
    };
  }, []);

  const handlePressIn = () => {
    setIsRecording(true);
    sttService.startListening((text) => {
      if (text.trim().length > 0) {
        setMessages(prev => [...prev, { sender: 'Me', text }]);
        P2PService.send(text);
      }
    });
  };

  const handlePressOut = () => {
    setIsRecording(false);
    sttService.stopListening();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Semantic Walkie-Talkie</Text>
      <Text style={styles.status}>{status}</Text>
      
      <ScrollView style={styles.chatBox}>
        {messages.map((m, i) => (
          <Text key={i} style={m.sender === 'Me' ? styles.myMsg : styles.peerMsg}>
            {m.sender}: {m.text}
          </Text>
        ))}
      </ScrollView>

      <View style={styles.buttonContainer}>
        <WalkieTalkieButton 
          onPressIn={handlePressIn} 
          onPressOut={handlePressOut} 
          isRecording={isRecording} 
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#f0f4f8' },
  header: { fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 10 },
  status: { fontSize: 16, color: 'gray', textAlign: 'center', marginBottom: 20 },
  chatBox: { flex: 1, backgroundColor: 'white', borderRadius: 10, padding: 10, marginBottom: 20 },
  myMsg: { color: '#3b82f6', marginBottom: 8, textAlign: 'right', fontSize: 16 },
  peerMsg: { color: '#10b981', marginBottom: 8, textAlign: 'left', fontSize: 16 },
  buttonContainer: { alignItems: 'center', marginBottom: 40 }
});
