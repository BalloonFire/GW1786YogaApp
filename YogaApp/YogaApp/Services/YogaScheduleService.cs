using Firebase.Database;
using Firebase.Database.Query;

public class YogaScheduleService
{
    private readonly FirebaseClient _firebase;
    private const string BasePath = "yogaSchedules";

    public YogaScheduleService(FirebaseClient firebase)
    {
        _firebase = firebase;
    }

    public async Task<List<YogaSchedule>> GetSchedulesByDateRange(DateTime start, DateTime end)
    {
        var schedules = await _firebase
            .Child(BasePath)
            .OrderBy("Date")
            .StartAt(start.ToString("o"))
            .EndAt(end.ToString("o"))
            .OnceAsync<YogaSchedule>();

        return schedules.Select(s =>
        {
            s.Object.Id = s.Key;
            return s.Object;
        }).ToList();
    }

    public async Task<List<YogaSchedule>> GetSchedulesByTeacher(string teacherId)
    {
        var schedules = await _firebase
            .Child(BasePath)
            .OrderBy("TeacherId")
            .EqualTo(teacherId)
            .OnceAsync<YogaSchedule>();

        return schedules.Select(s =>
        {
            s.Object.Id = s.Key;
            return s.Object;
        }).ToList();
    }
}
