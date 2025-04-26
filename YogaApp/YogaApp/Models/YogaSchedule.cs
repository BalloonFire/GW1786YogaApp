using System.ComponentModel.DataAnnotations;

public class YogaSchedule
{
    public string Id { get; set; } = string.Empty;

    [Required]
    public string CourseId { get; set; } = string.Empty;

    [Required]
    public string TeacherId { get; set; } = string.Empty;

    [Required]
    public DateTime Date { get; set; }

    public string Comments { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    // Navigation properties (optional)
    public YogaCourse? Course { get; set; }
    public Teacher? Teacher { get; set; }

    // Formatted properties for display
    public string FormattedDate => Date.ToString("dddd, MMMM dd, yyyy");
    public string FormattedTime => Date.ToString("h:mm tt");
    public string FormattedDateTime => $"{FormattedDate} at {FormattedTime}";

    // Display info combining course and teacher
    public string DisplayInfo =>
        $"{Course?.Type ?? "Class"} with {Teacher?.Name ?? "Teacher"} - {FormattedTime}";
}
