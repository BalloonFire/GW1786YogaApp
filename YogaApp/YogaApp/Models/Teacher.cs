using System.ComponentModel.DataAnnotations;
using System.Net.Mail;

public class Teacher
{
    public string Id { get; set; } = string.Empty;

    [Required(ErrorMessage = "Teacher name is required")]
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } = string.Empty;

    [Required]
    [EmailAddress(ErrorMessage = "Invalid email address")]
    public string Email { get; set; } = string.Empty;

    public string ProfilePictureUrl { get; set; } = string.Empty;
    public string Bio { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public List<string> Specializations { get; set; } = new();

    // Formatted properties
    public string Initials =>
        string.Join("", Name.Split(' ').Select(n => n.FirstOrDefault()));

    public string DisplayName => $"{Name} ({Specializations.FirstOrDefault() ?? "Yoga"})";

    // Validation method
    public bool IsValidEmail()
    {
        try
        {
            var addr = new MailAddress(Email);
            return addr.Address == Email;
        }
        catch
        {
            return false;
        }
    }
}
