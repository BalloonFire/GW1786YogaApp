import React, { useState, useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import { getClasses } from '../services/api';
import ClassList from '../components/ClassList';
import SearchBar from '../components/SearchBar';
import LoadingIndicator from '../components/LoadingIndicator';

const HomeScreen = ({ navigation }) => {
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({});

  useEffect(() => {
    fetchClasses();
  }, [filters]);

  const fetchClasses = async () => {
    try {
      setLoading(true);
      const data = await getClasses(filters);
      setClasses(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectClass = (classItem) => {
    navigation.navigate('ClassDetail', { classId: classItem.id });
  };

  if (loading && classes.length === 0) {
    return <LoadingIndicator />;
  }

  return (
    <View style={styles.container}>
      <SearchBar onSearch={setFilters} />
      <ClassList classes={classes} onSelectClass={handleSelectClass} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5'
  }
});

export default HomeScreen;