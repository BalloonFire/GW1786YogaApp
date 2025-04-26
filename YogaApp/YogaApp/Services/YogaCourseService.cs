using Firebase.Database;
using Firebase.Database.Query;

public class YogaCourseService
{
    private readonly FirebaseClient _firebase;
    private const string BasePath = "yogaCourses";

    public YogaCourseService(FirebaseClient firebase)
    {
        _firebase = firebase;
    }

    public async Task<List<YogaCourse>> GetAllCourses()
    {
        var courses = await _firebase
            .Child(BasePath)
            .OnceAsync<YogaCourse>();

        return courses.Select(c =>
        {
            c.Object.Id = c.Key;
            return c.Object;
        }).ToList();
    }

    public async Task<YogaCourse?> GetCourseById(string id)
    {
        var course = await _firebase
            .Child(BasePath)
            .Child(id)
            .OnceSingleAsync<YogaCourse>();

        if (course != null)
        {
            course.Id = id;
            return course;
        }

        return null;
    }

    public async Task<List<YogaCourse>> SearchCourses(string? day = null, string? time = null, string? type = null)
    {
        var allCourses = await GetAllCourses();
        var query = allCourses.AsQueryable();

        if (!string.IsNullOrEmpty(day))
            query = query.Where(c => c.DayOfWeek.Equals(day, StringComparison.OrdinalIgnoreCase));

        if (!string.IsNullOrEmpty(time))
            query = query.Where(c => c.StartTime.StartsWith(time, StringComparison.OrdinalIgnoreCase));

        if (!string.IsNullOrEmpty(type))
            query = query.Where(c => c.Type.Equals(type, StringComparison.OrdinalIgnoreCase));

        return query.ToList();
    }
}
