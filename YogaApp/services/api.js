import { initializeApp } from 'firebase/app';
import { getDatabase, ref, get, query, orderByChild, equalTo } from 'firebase/database';

// Your Firebase config (replace with your actual config)
const firebaseConfig = {
  apiKey: "api_key_here",
  authDomain: "https://yogaadminapp-f8f6a-default-rtdb.firebaseio.com",
  projectId: "yogaadminapp-f8f6a",
  storageBucket: "yogaadminapp-f8f6a.firebasestorage.app",
  messagingSenderId: "576354427928",
  appId: "1:576354427928:android:b2276b52a85238fd224f6d"
};

const app = initializeApp(firebaseConfig);
const db = getDatabase(app);

// Convert Firebase Date objects to JavaScript Date
const convertFirebaseDate = (fbDate) => {
  if (!fbDate) return null;
  return new Date(fbDate.time);
};

// Add this helper function to determine time category
const getTimeCategory = (timeString) => {
    if (!timeString) return '';
    
    try {
      const [hoursStr, minutesStr] = timeString.split(':');
      const hours = parseInt(hoursStr, 10);
      const minutes = parseInt(minutesStr || '0', 10);
      const totalMinutes = hours * 60 + minutes;
      
      // Morning: 6:00 AM (360) to 11:59 AM (719)
      if (totalMinutes >= 360 && totalMinutes < 720) return 'morning';
      // Afternoon: 12:00 PM (720) to 2:59 PM (899)
      if (totalMinutes >= 720 && totalMinutes < 900) return 'afternoon';
      // Evening: 3:00 PM (900) to 5:59 AM (359)
      return 'evening';
    } catch (error) {
      console.error('Error parsing time:', error);
      return '';
    }
  };

  export const getClasses = async (filters = {}) => {
    try {
      const dbRef = ref(db, 'yoga_courses');
      let snapshot = await get(dbRef);
      
      if (!snapshot.exists()) return [];
  
      const classesObject = snapshot.val();
      let classes = [];
  
      Object.keys(classesObject).forEach(key => {
        if (key !== 'null' && classesObject[key]) {
          const course = classesObject[key];
          
          // Apply day filter
          let include = true;
          if (filters.dayOfWeek && course.dayOfWeek !== filters.dayOfWeek) {
            include = false;
          }
          
          // Apply time category filter
          if (include && filters.timeOfDay) {
            const startCategory = getTimeCategory(course.startTime);
            const endCategory = getTimeCategory(course.endTime);
            
            // Include if either start or end falls in the selected category
            if (startCategory !== filters.timeOfDay && endCategory !== filters.timeOfDay) {
              include = false;
            }
          }
          
          if (include) {
            classes.push({
              id: course.id,
              type: course.type,
              dayOfWeek: course.dayOfWeek,
              startTime: course.startTime,
              endTime: course.endTime,
              duration: course.duration,
              price: course.price,
              capacity: course.capacity,
              description: course.description,
              startDate: convertFirebaseDate(course.startDate),
              endDate: convertFirebaseDate(course.endDate),
              formattedTimeRange: course.formattedTimeRange
            });
          }
        }
      });
  
      return classes;
    } catch (error) {
      console.error("Error getting classes:", error);
      throw error;
    }
  };
  

export const getClassDetails = async (classId) => {
  try {
    const dbRef = ref(db, 'yoga_courses');
    const snapshot = await get(dbRef);
    
    if (!snapshot.exists()) throw new Error("Class not found");

    const classesObject = snapshot.val();
    for (const key in classesObject) {
      if (classesObject[key] && classesObject[key].id === classId) {
        const course = classesObject[key];
        return {
          id: course.id,
          type: course.type,
          dayOfWeek: course.dayOfWeek,
          startTime: course.startTime,
          endTime: course.endTime,
          duration: course.duration,
          price: course.price,
          capacity: course.capacity,
          description: course.description,
          startDate: convertFirebaseDate(course.startDate),
          endDate: convertFirebaseDate(course.endDate),
          formattedTimeRange: course.formattedTimeRange,
          formattedDateRange: course.formattedDateRange
        };
      }
    }
    throw new Error("Class not found");
  } catch (error) {
    console.error("Error getting class details:", error);
    throw error;
  }
};

export const getTeacher = async (teacherId) => {
  try {
    const dbRef = ref(db, 'yoga_teachers');
    const snapshot = await get(dbRef);
    
    if (!snapshot.exists()) return null;

    const teachersObject = snapshot.val();
    for (const key in teachersObject) {
      if (teachersObject[key] && teachersObject[key].id === teacherId) {
        return teachersObject[key];
      }
    }
    return null;
  } catch (error) {
    console.error("Error getting teacher:", error);
    throw error;
  }
};

export const getSchedulesForClass = async (classId) => {
  try {
    const dbRef = ref(db, 'yoga_schedules');
    const snapshot = await get(dbRef);
    
    if (!snapshot.exists()) return [];

    const schedulesObject = snapshot.val();
    const schedules = [];

    Object.keys(schedulesObject).forEach(key => {
      if (key !== 'null' && schedulesObject[key] && schedulesObject[key].courseId === classId) {
        const schedule = schedulesObject[key];
        schedules.push({
          id: schedule.id,
          courseId: schedule.courseId,
          teacherId: schedule.teacherId,
          date: convertFirebaseDate(schedule.date),
          comments: schedule.comments,
          formattedDate: schedule.formattedDate,
          formattedDateTime: schedule.formattedDateTime
        });
      }
    });

    return schedules;
  } catch (error) {
    console.error("Error getting schedules:", error);
    throw error;
  }
};

export const bookClass = async (classId, userData) => {
  try {
    // In a real implementation, you would write to your database here
    console.log(`Booking class ${classId} for user`, userData);
    return { success: true, bookingId: `temp-${Date.now()}` };
  } catch (error) {
    console.error("Error booking class:", error);
    throw error;
  }
};