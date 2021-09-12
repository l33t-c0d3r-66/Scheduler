package scheduler;

import java.time.LocalDateTime;

public class Event 
{
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String summary;
    private String group;
    private String location;
    
    public Event(LocalDateTime startTime,LocalDateTime endTime,String summary,String group,String location)
    {
        this.startTime=startTime;
        this.endTime=endTime;
        this.summary=summary;
        this.group=group;
        this.location=location;
    }

    public LocalDateTime getStartTime() 
    {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) 
    {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() 
    {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) 
    {
        this.endTime = endTime;
    }

    public String getSummary() 
    {
        return summary;
    }

    public void setSummary(String summary) 
    {
        this.summary = summary;
    }

    public String getGroup() 
    {
        return group;
    }

    public void setGroup(String group) 
    {
        this.group = group;
    }

    public String getLocation() 
    {
        return location;
    }

    public void setLocation(String location) 
    {
        this.location = location;
    }
    
    public String writeToFile()
    {
        return startTime+","+endTime+","+summary+","+group+","+location+"\n";
    }
    
}
