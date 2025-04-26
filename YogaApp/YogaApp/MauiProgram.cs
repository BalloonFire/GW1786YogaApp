using Firebase.Database;
using Microsoft.Extensions.Logging;
using YogaApp;

public static class MauiProgram
{
    public static MauiApp CreateMauiApp()
    {
        var builder = MauiApp.CreateBuilder();
        builder
            .UseMauiApp<App>()
            .ConfigureFonts(fonts =>
            {
                fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
            });

        builder.Services.AddMauiBlazorWebView();

#if DEBUG
        builder.Services.AddBlazorWebViewDeveloperTools();
        builder.Logging.AddDebug();
#endif

        // Add Firebase service
        builder.Services.AddSingleton<FirebaseClient>(sp =>
        {
            var options = new FirebaseOptions
            {
                AuthTokenAsyncFactory = () => Task.FromResult("AIzaSyC4em7M8E3D4XYWepbvEtS764AOY5YAEAQ") // API key
            };
            return new FirebaseClient(
                "https://yogaadminapp-f8f6a-default-rtdb.firebaseio.com", // Firebase Database URL
                options);
        });

        builder.Services.AddSingleton<YogaCourseService>();
        builder.Services.AddSingleton<YogaScheduleService>();
        builder.Services.AddSingleton<TeacherService>();

        return builder.Build();
    }
}
