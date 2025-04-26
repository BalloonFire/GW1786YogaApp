import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import HomeScreen from '../screens/HomeScreen';
import ClassDetailScreen from '../screens/ClassDetailScreen';
import BookingScreen from '../screens/BookingScreen';

const Stack = createStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Home">
        <Stack.Screen 
          name="Home" 
          component={HomeScreen} 
          options={{ title: 'Yoga Classes' }}
        />
        <Stack.Screen 
          name="ClassDetail" 
          component={ClassDetailScreen} 
          options={{ title: 'Class Details' }}
        />
        <Stack.Screen 
          name="Booking" 
          component={BookingScreen} 
          options={{ title: 'Book Class' }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}