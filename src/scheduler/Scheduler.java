package scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.swing.JOptionPane;
import jfxtras.samples.JFXtrasSampleBase;
import jfxtras.scene.control.LocalDateTimeTextField;
import jfxtras.scene.control.agenda.Agenda;
import jfxtras.scene.control.agenda.Agenda.Appointment;
import jfxtras.scene.control.agenda.Agenda.LocalDateTimeRange;
import jfxtras.scene.control.agenda.AgendaSkinSwitcher;
import jfxtras.scene.layout.GridPane;

public class Scheduler extends JFXtrasSampleBase
{
    public static Agenda agenda;
    private List<Event> eventList;
    List<Event> eventListFromFile=getDataFromFile();
    public Scheduler() 
    {
       
       agenda=new Agenda();
       eventList=new ArrayList<>();
        // setup appointment groups
        final Map<String, Agenda.AppointmentGroup> listOfAppointmentGroupMap = new TreeMap<String, Agenda.AppointmentGroup>();
        for (Agenda.AppointmentGroup appointmentGroup : agenda.appointmentGroups()) 
        {
            listOfAppointmentGroupMap.put(appointmentGroup.getDescription(), appointmentGroup);
        }

        // accept new appointments
        agenda.newAppointmentCallbackProperty().set(new Callback<Agenda.LocalDateTimeRange, Agenda.Appointment>()
	{
            @Override
            public Agenda.Appointment call(LocalDateTimeRange dateTimeRange)
            {
		return new Agenda.AppointmentImplLocal()
                    .withStartLocalDateTime( dateTimeRange.getStartLocalDateTime() )
                    .withEndLocalDateTime( dateTimeRange.getEndLocalDateTime() )
                    .withSummary("new")
                    .withDescription("new")
                    .withAppointmentGroup(listOfAppointmentGroupMap.get("group01"));
            }
        });

	// initial set
	LocalDate localDateToday = LocalDate.now();
	LocalDateTime localStartDateTime = localDateToday.atStartOfDay().plusHours(5);
	localStartDateTime = localStartDateTime.minusDays(localStartDateTime.getDayOfWeek().getValue() > 3 && localStartDateTime.getDayOfWeek().getValue() < 7 ? 3 : -1);
		
        //Array to get Saved Appointments        
        Appointment[] appointmentsFromFile=new Appointment[eventListFromFile.size()];
        for(int i=0;i<appointmentsFromFile.length;i++)
        {
            appointmentsFromFile[i]=new Agenda.AppointmentImplLocal()
		.withStartLocalDateTime(eventListFromFile.get(i).getStartTime())
		.withEndLocalDateTime(eventListFromFile.get(i).getEndTime())
		.withSummary(eventListFromFile.get(i).getSummary())
		.withLocation(eventListFromFile.get(i).getLocation())
		.withAppointmentGroup(listOfAppointmentGroupMap.get(eventListFromFile.get(i).getGroup()));
        }
        
        agenda.appointments().addAll(appointmentsFromFile);
		
        // action
	agenda.setActionCallback( (appointment) -> 
        {
            showPopup(agenda, "Action on " + appointment);
            return null;
        });
        
        
        
        TaskScheduler ts=new TaskScheduler();
        ts.runScheduler();        
    }
   
    @Override
    public Node getPanel(Stage stage) 
    {
        return agenda;
    }

    @Override
    public Node getControlPanel() 
    {
        // the result
        GridPane gridPane = new GridPane();
        gridPane.setVgap(2.0);
        gridPane.setHgap(2.0);

        // setup the grid so all the labels will not grow, but the rest will
        ColumnConstraints columnConstraintsAlwaysGrow = new ColumnConstraints();
        columnConstraintsAlwaysGrow.setHgrow(Priority.ALWAYS);
        
        ColumnConstraints columnConstraintsNeverGrow = new ColumnConstraints();
        columnConstraintsNeverGrow.setHgrow(Priority.NEVER);
        
        gridPane.getColumnConstraints().addAll(columnConstraintsNeverGrow, columnConstraintsAlwaysGrow);
        
        int rowIdx = 0;

        // skin
        {
            gridPane.add(new Label("Skin"), new GridPane.C().row(rowIdx).col(0).halignment(HPos.RIGHT));
            AgendaSkinSwitcher agendaSkinSwitcher = new AgendaSkinSwitcher(agenda);
            gridPane.add(agendaSkinSwitcher, new GridPane.C().row(rowIdx).col(1));
        }
        rowIdx++;

        // displayed calendar
        {
            gridPane.add(new Label("Display"), new GridPane.C().row(rowIdx).col(0).halignment(HPos.RIGHT));
            LocalDateTimeTextField localDateTimeTextField = new LocalDateTimeTextField();
            gridPane.add(localDateTimeTextField, new GridPane.C().row(rowIdx).col(1));
            localDateTimeTextField.localDateTimeProperty().bindBidirectional(agenda.displayedLocalDateTime());
        }
        rowIdx++;

        // AllowDragging
        {
            gridPane.add(new Label("Allow Dragging"), new GridPane.C().row(rowIdx).col(0).halignment(HPos.RIGHT));
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(true);
            gridPane.add(checkBox, new GridPane.C().row(rowIdx).col(1));
            agenda.allowDraggingProperty().bind(checkBox.selectedProperty());
        }
        rowIdx++;

        // AllowResize
        {
            gridPane.add(new Label("Allow Resize"), new GridPane.C().row(rowIdx).col(0).halignment(HPos.RIGHT));
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(true);
            gridPane.add(checkBox, new GridPane.C().row(rowIdx).col(1));
            agenda.allowResizeProperty().bind(checkBox.selectedProperty());
        }
        rowIdx++;

        // Locale
        {
            gridPane.add(new Label("Locale"), new GridPane.C().row(rowIdx).col(0).halignment(HPos.RIGHT));
            ObservableList<Locale> locales = FXCollections.observableArrayList(Locale.getAvailableLocales());
            FXCollections.sort(locales,  (o1, o2) -> { return o1.toString().compareTo(o2.toString()); } );
            ComboBox<Locale> comboBox = new ComboBox<>( locales );
            comboBox.converterProperty().set(new StringConverter<Locale>() {
                @Override
                public String toString(Locale locale) {
                    return locale == null ? "-"  : locale.toString();
                }

                @Override
                public Locale fromString(String s) {
                    if ("-".equals(s)) return null;
                    return new Locale(s);
                }
            });
            comboBox.setEditable(true);
            gridPane.add(comboBox, new GridPane.C().row(rowIdx).col(1));
            comboBox.valueProperty().bindBidirectional(agenda.localeProperty());
        }
        rowIdx++;

        // print
        {
            gridPane.add(new Label("Save Agenda"), new GridPane.C().row(rowIdx).col(0).halignment(HPos.RIGHT));
            Button lButton = new Button("Save to File");
            gridPane.add(lButton, new GridPane.C().row(rowIdx).col(1));
            lButton.setOnAction( (event) -> {
            	printAll();
            });
        }
        rowIdx++;
       
        // done
        return gridPane;
    }

    @Override
    public String getJavaDocURL() 
    {
        return "http://jfxtras.org/doc/8.0/jfxtras-agenda/" + Agenda.class.getName().replace(".", "/") + ".html";
    }

    public static void main(String[] args) 
    {
        launch(args);
    }


    /**
     * get the calendar for the first day of the week
     */
    static private Calendar getFirstDayOfWeekCalendar(Locale locale, Calendar c)
    {
        // result
        int firstDayOfWeek = Calendar.getInstance(locale).getFirstDayOfWeek();
        int currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int delta = 0;
        if (firstDayOfWeek <= currentDayOfWeek)
        {
            delta = -currentDayOfWeek + firstDayOfWeek;
        }
        else
        {
            delta = -currentDayOfWeek - (7-firstDayOfWeek);
        }
        c = ((Calendar)c.clone());
        c.add(Calendar.DATE, delta);
        return c;
    }
    
    public void printAll()
    {
        ObservableList<Agenda.Appointment> list=agenda.appointments();
        for(int i=0;i<list.size();i++)
        {
            LocalDateTime startTime=list.get(i).getStartLocalDateTime();
            LocalDateTime endTime=list.get(i).getEndLocalDateTime();
            String groupName=list.get(i).getAppointmentGroup().getDescription();
            String summary=list.get(i).getSummary()==null?"N/A":list.get(i).getSummary();
            String location=list.get(i).getLocation()==null?"N/A":list.get(i).getLocation();
           
            Event event = new Event(startTime,endTime,groupName,summary,location);
            eventList.add(event);
            saveDataToFile();  
        }
        JOptionPane.showMessageDialog(null,"Data Saved Successfully");
    }

    @Override
    public String getSampleName() 
    {
        return this.getClass().getSimpleName();
    }
    
    private void saveDataToFile()
    {
        
        try(FileWriter fw = new FileWriter("files/scheduler.txt",false))
        {
            for(int i=0;i<eventList.size();i++)
                fw.write(eventList.get(i).writeToFile());
        } 
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    private List<Event> getDataFromFile()
    {
        List<Event> listOfEvents=new ArrayList<>();
        File file = new File("files/scheduler.txt");
         try(Scanner myReader = new Scanner(file))
         {
            while (myReader.hasNextLine()) 
            {
                String line = myReader.nextLine();
                String[] data=line.split(",");
                if(data.length==3)
                {
                    String sDate=data[0];
                    sDate=sDate.replace("T","-");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"); 
                    LocalDateTime startTime = LocalDateTime.parse(sDate, formatter);
                    
                    String eDate=data[1];
                    eDate=eDate.replace("T","-");
                    LocalDateTime endTime = LocalDateTime.parse(eDate, formatter);
                    
                    String group=data[2];
                    
                    String summary="N/A";
                    
                    String location="N/A";
                    
                    Event event;
                    event = new Event(startTime,endTime,group,summary,location);
                    listOfEvents.add(event);
                }
                if(data.length==5)
                {
                    String sDate=data[0];
                    sDate=sDate.replace("T","-");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm"); 
                    LocalDateTime startTime = LocalDateTime.parse(sDate, formatter);
                    
                    String eDate=data[1];
                    eDate=eDate.replace("T","-");
                    LocalDateTime endTime = LocalDateTime.parse(eDate, formatter);
                    
                    String group=data[2];
                    
                    String summary=data[3];
                    
                    String location=data[4];
                    Event event;
                    event = new Event(startTime,endTime,group,summary,location);
                    listOfEvents.add(event);
                    
                }
            }
        } 
        catch(FileNotFoundException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
        return listOfEvents;
    }    
    
    public static List<Agenda.Appointment> listOfToday()
    {
        List<Agenda.Appointment> appointmentList=agenda.appointments();
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        List<Agenda.Appointment> todayAgenda=new ArrayList<>();
        // Format LocalDateTime
        String formattedDateTime = currentDateTime.format(formatter);
       
        for(int i=0;i<appointmentList.size();i++)
        {
            String startDate=appointmentList.get(i).getStartLocalDateTime().format(formatter);
            if(startDate.equalsIgnoreCase(formattedDateTime))
            {
                todayAgenda.add(appointmentList.get(i));
            }
        }
        return todayAgenda;
    }
    
}


