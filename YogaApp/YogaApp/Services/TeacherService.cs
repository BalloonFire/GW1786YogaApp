using Firebase.Database;
using Firebase.Database.Query;

public class TeacherService
{
    private readonly FirebaseClient _firebase;
    private const string BasePath = "teachers";

    public TeacherService(FirebaseClient firebase)
    {
        _firebase = firebase;
    }

    public async Task<List<Teacher>> GetAllTeachers()
    {
        var teachers = await _firebase
            .Child(BasePath)
            .OrderByKey()
            .OnceAsync<Teacher>();

        return teachers.Select(t =>
        {
            t.Object.Id = t.Key;
            return t.Object;
        }).ToList();
    }

    public async Task<Teacher?> GetTeacherById(string id)
    {
        var teacher = await _firebase
            .Child(BasePath)
            .Child(id)
            .OnceSingleAsync<Teacher>();

        if (teacher != null)
        {
            teacher.Id = id;
            return teacher;
        }

        return null; // safer than returning null when method says non-null!
    }
}
