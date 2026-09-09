import React from 'react';
import { Pressable, Text, StyleSheet } from 'react-native';

interface Props {
  onPressIn: () => void;
  onPressOut: () => void;
  isRecording: boolean;
}

export function WalkieTalkieButton({ onPressIn, onPressOut, isRecording }: Props) {
  return (
    <Pressable
      onPressIn={onPressIn}
      onPressOut={onPressOut}
      style={({ pressed }) => [
        styles.button,
        pressed || isRecording ? styles.buttonPressed : styles.buttonNormal
      ]}
    >
      <Text style={styles.text}>
        {isRecording ? "Recording..." : "Push to Talk"}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    width: 200,
    height: 200,
    borderRadius: 100,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
  },
  buttonNormal: {
    backgroundColor: '#3b82f6',
  },
  buttonPressed: {
    backgroundColor: '#ef4444',
  },
  text: {
    color: 'white',
    fontSize: 20,
    fontWeight: 'bold',
  }
});
