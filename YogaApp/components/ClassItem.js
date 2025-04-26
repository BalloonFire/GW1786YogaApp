import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';

const ClassItem = ({ classData, onPress }) => (
  <TouchableOpacity onPress={onPress}>
    <View style={styles.container}>
      <Text style={styles.title}>{classData.type} Yoga</Text>
      <Text style={styles.details}>{classData.dayOfWeek} • {classData.formattedTimeRange}</Text>
      <Text style={styles.details}>{classData.formattedDateRange}</Text>
      <Text style={styles.price}>${classData.price.toFixed(2)}</Text>
    </View>
  </TouchableOpacity>
);

const styles = StyleSheet.create({
  container: {
    padding: 15,
    marginBottom: 10,
    backgroundColor: '#fff',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5
  },
  details: {
    fontSize: 14,
    color: '#666',
    marginBottom: 3
  },
  price: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#2a9d8f',
    marginTop: 5
  }
});

export default ClassItem;