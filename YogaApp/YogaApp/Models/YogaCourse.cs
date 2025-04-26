public class YogaCourse
{
    public string Id { get; set; } = string.Empty;
    public string DayOfWeek { get; set; } = string.Empty;
    public string StartTime { get; set; } = string.Empty;
    public string EndTime { get; set; } = string.Empty;
    public int Capacity { get; set; }
    public int Duration { get; set; }
    public decimal Price { get; set; }
    public string Type { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public DateTime StartDate { get; set; }
    public DateTime EndDate { get; set; }

    public string FormattedTime => $"{StartTime} - {EndTime}";
    public string FormattedDateRange => $"{StartDate:d} to {EndDate:d}";
}
