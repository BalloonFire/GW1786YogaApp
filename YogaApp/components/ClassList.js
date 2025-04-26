import React from 'react';
import { FlatList, StyleSheet } from 'react-native';
import ClassItem from './ClassItem';

const ClassList = ({ classes, onSelectClass }) => (
  <FlatList
    data={classes}
    keyExtractor={(item) => item.id}
    renderItem={({ item }) => (
      <ClassItem 
        classData={item} 
        onPress={() => onSelectClass(item)}
      />
    )}
    contentContainerStyle={styles.list}
  />
);

const styles = StyleSheet.create({
  list: {
    padding: 10
  }
});

export default ClassList;