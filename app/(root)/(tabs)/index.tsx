import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomeScreen() {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Walkie-Talkie</Text>
        <Text style={styles.subtitle}>Hold to speak. Text is sent offline.</Text>
      </View>

      <View style={styles.logContainer}>
        <Text style={styles.logText}>System: Ready to transmit...</Text>
      </View>

      <View style={styles.buttonContainer}>
        <TouchableOpacity 
          style={styles.pttButton}
          onPressIn={() => console.log("Started recording...")}
          onPressOut={() => console.log("Stopped recording, converting to text...")}
        >
          <Text style={styles.pttText}>HOLD TO SPEAK</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.pttButton, { marginTop: 20, backgroundColor: '#3b82f6', height: 60, width: 250, borderRadius: 10 }]}
          onPress={() => {
            const { router } = require('expo-router');
            router.push('/walkie-talkie');
          }}
        >
          <Text style={styles.pttText}>Go to Functional Walkie-Talkie</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#0B0E14",
    padding: 20,
  },
  header: {
    alignItems: "center",
    marginTop: 20,
    marginBottom: 40,
  },
  title: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#FFFFFF",
  },
  subtitle: {
    fontSize: 14,
    color: "#8A8D96",
    marginTop: 8,
  },
  logContainer: {
    flex: 1,
    backgroundColor: "#1A1D26",
    borderRadius: 12,
    padding: 16,
    marginBottom: 40,
  },
  logText: {
    color: "#4A9EFF",
    fontSize: 14,
  },
  buttonContainer: {
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 40,
  },
  pttButton: {
    width: 200,
    height: 200,
    borderRadius: 100,
    backgroundColor: "#FF6B4A",
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#FF6B4A",
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.5,
    shadowRadius: 20,
    elevation: 10,
  },
  pttText: {
    color: "#FFFFFF",
    fontSize: 18,
    fontWeight: "bold",
  }
});
