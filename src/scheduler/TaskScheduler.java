package scheduler;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.JOptionPane;
import jfxtras.scene.control.agenda.Agenda;

class TaskScheduler 
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public void runScheduler() 
    {
        final Runnable taskScheduler = new Runnable() 
        {
            @Override
            public void run() 
            { 
                appointmentAlert();
            }
        };
        final ScheduledFuture<?> scheduleHandle = scheduler.scheduleAtFixedRate(taskScheduler, 10, 90, SECONDS);
        scheduler.schedule(new Runnable() 
        {
            @Override
            public void run() 
            { 
                scheduleHandle.cancel(true); 
            }
        }, 60 * 60 * 24, SECONDS);
    }
    
    
    public void appointmentAlert()
    {
     
        List<Agenda.Appointment> listOfTodayAgenda=Scheduler.listOfToday();
        for(int i=0;i<listOfTodayAgenda.size();i++)
        {
            String startTime = getTimeString(listOfTodayAgenda.get(i).getStartLocalDateTime(),0);
            String startTimeMinusOne=getTimeString(listOfTodayAgenda.get(i).getStartLocalDateTime(),1);
            String startTimeMinusNine=getTimeString(listOfTodayAgenda.get(i).getStartLocalDateTime(),9);
            String startTimeMinusTen=getTimeString(listOfTodayAgenda.get(i).getStartLocalDateTime(),10);
            String currentTime=getTimeString(LocalDateTime.now(),0);
            if(startTime.equals(currentTime) || startTimeMinusOne.equals(currentTime))
            {
                String musicFileName = "music.mp3";
                Media music = new Media(new File(musicFileName).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(music);
                mediaPlayer.play();
                JOptionPane.showMessageDialog(null,"You have an Event right now\nEvent Summary: "+listOfTodayAgenda.get(i).getSummary() ,listOfTodayAgenda.get(i).getAppointmentGroup().getDescription().toUpperCase() , JOptionPane.INFORMATION_MESSAGE);
            }
            if(startTimeMinusTen.equals(currentTime) || startTimeMinusNine.equals(currentTime))
            {
                JOptionPane.showMessageDialog(null,"You have an upcoming Event.\nEvent Summary: "+listOfTodayAgenda.get(i).getSummary() ,listOfTodayAgenda.get(i).getAppointmentGroup().getDescription().toUpperCase() , JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private String getTimeString(LocalDateTime ldt,long timeToSubtract)
    {
        LocalDateTime dateTime = ldt.minusMinutes(timeToSubtract);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String convertedTime=dateTime.format(formatter);
        return convertedTime;
    }
}
