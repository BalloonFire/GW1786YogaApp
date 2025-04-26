import React, { useState } from 'react';
import { 
  View, 
  Text, 
  TextInput,
  Button,
  StyleSheet, 
  TouchableOpacity, 
  Modal,
  TouchableWithoutFeedback, // Add this import
  Pressable // Add this import
} from 'react-native';
import { Picker } from '@react-native-picker/picker';

const timeCategories = [
  { label: 'Any Time', value: '' },
  { label: 'Morning (6AM - 12PM)', value: 'morning' },
  { label: 'Afternoon (12PM - 3PM)', value: 'afternoon' },
  { label: 'Evening (3PM - 6AM)', value: 'evening' }
];

const SearchBar = ({ onSearch }) => {
  const [day, setDay] = useState('');
  const [timeCategory, setTimeCategory] = useState('');
  const [showTimePicker, setShowTimePicker] = useState(false);

  const handleSearch = () => {
    const filters = {};
    if (day) filters.dayOfWeek = day;
    if (timeCategory) filters.timeOfDay = timeCategory;
    onSearch(filters);
  };

  const closeModal = () => {
    setShowTimePicker(false);
  };

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.input}
        placeholder="Day (e.g., Monday)"
        value={day}
        onChangeText={setDay}
      />
      
      <TouchableOpacity 
        style={styles.timeInput}
        onPress={() => setShowTimePicker(true)}
      >
        <Text style={styles.timeInputText}>
          {timeCategory ? 
            timeCategories.find(t => t.value === timeCategory)?.label : 
            'Select Time of Day'}
        </Text>
      </TouchableOpacity>

      <Modal
        visible={showTimePicker}
        transparent={true}
        animationType="slide"
        onRequestClose={closeModal}
      >
        <TouchableWithoutFeedback onPress={closeModal}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        
        <View style={styles.modalContainer}>
          <Pressable 
            style={styles.pickerContainer}
            onPress={(e) => e.stopPropagation()} // Prevent click-through to overlay
          >
            <Picker
              selectedValue={timeCategory}
              onValueChange={(itemValue) => {
                setTimeCategory(itemValue);
                closeModal();
              }}
            >
              {timeCategories.map((time) => (
                <Picker.Item 
                  key={time.value} 
                  label={time.label} 
                  value={time.value} 
                />
              ))}
            </Picker>
          </Pressable>
        </View>
      </Modal>

      <Button title="Search" onPress={handleSearch} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 10,
    backgroundColor: '#f5f5f5'
  },
  input: {
    height: 40,
    borderColor: '#ddd',
    borderWidth: 1,
    marginBottom: 10,
    padding: 8,
    borderRadius: 4,
    backgroundColor: '#fff'
  },
  timeInput: {
    height: 40,
    borderColor: '#ddd',
    borderWidth: 1,
    marginBottom: 10,
    padding: 8,
    borderRadius: 4,
    backgroundColor: '#fff',
    justifyContent: 'center'
  },
  timeInputText: {
    color: '#000'
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)'
  },
  modalContainer: {
    position: 'absolute',
    bottom: 0,
    width: '100%'
  },
  pickerContainer: {
    backgroundColor: 'white',
    padding: 20,
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10
  }
});

export default SearchBar;