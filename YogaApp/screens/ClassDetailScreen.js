import React, { useState, useEffect } from 'react';
import { View, Text, Button, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { getClassDetails, getTeacher, getSchedulesForClass } from '../services/api';

const ClassDetailScreen = ({ route, navigation }) => {
  const { classId } = route.params;
  const [classDetails, setClassDetails] = useState(null);
  const [teacher, setTeacher] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const details = await getClassDetails(classId);
        setClassDetails(details);
        
        const classSchedules = await getSchedulesForClass(classId);
        setSchedules(classSchedules);
        
        if (classSchedules.length > 0 && classSchedules[0].teacherId) {
          const teacherData = await getTeacher(classSchedules[0].teacherId);
          setTeacher(teacherData);
        }
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
  }, [classId]);

  const handleBook = () => {
    navigation.navigate('Booking', { classId });
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" />
        <Text>Loading class details...</Text>
      </View>
    );
  }

  if (!classDetails) {
    return (
      <View style={styles.container}>
        <Text>Class not found</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>{classDetails.type}</Text>
        {teacher && (
          <Text style={styles.subtitle}>with {teacher.name}</Text>
        )}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>When</Text>
        <Text>{classDetails.dayOfWeek} at {classDetails.formattedTimeRange}</Text>
        <Text>Duration: {classDetails.duration} minutes</Text>
        <Text>Date Range: {classDetails.formattedDateRange}</Text>
      </View>

      {schedules.length > 0 && (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Upcoming Sessions</Text>
          {schedules.map(schedule => (
            <View key={schedule.id} style={styles.scheduleItem}>
              <Text>{schedule.formattedDateTime}</Text>
              {schedule.comments && <Text>Notes: {schedule.comments}</Text>}
            </View>
          ))}
        </View>
      )}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>About</Text>
        <Text>{classDetails.description || 'A wonderful yoga class for all levels.'}</Text>
      </View>

      <View style={styles.priceContainer}>
        <Text style={styles.price}>${classDetails.price.toFixed(2)}</Text>
      </View>

      <Button title="Book Now" onPress={handleBook} />
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#fff'
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  header: {
    marginBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    paddingBottom: 20
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold'
  },
  subtitle: {
    fontSize: 16,
    color: '#666'
  },
  section: {
    marginBottom: 20
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5
  },
  scheduleItem: {
    padding: 10,
    backgroundColor: '#f9f9f9',
    borderRadius: 5,
    marginBottom: 10
  },
  priceContainer: {
    marginVertical: 20,
    alignItems: 'center'
  },
  price: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#2a9d8f'
  }
});

export default ClassDetailScreen;