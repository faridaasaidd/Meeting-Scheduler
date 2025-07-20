import java.text.SimpleDateFormat;
import java.util.*;

class Notifier {
    private String name;

    public Notifier(String name) {
        this.name = name;
    }

    public void notify(String message) {
        System.out.println("Notification to " + name + ": " + message);
    }

    public String getName() {
        return name;
    }
}

class TimeInterval {
    private Calendar startTime;
    private Calendar endTime;

    public TimeInterval(Calendar startTime, Calendar endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

   public boolean clash(TimeInterval interval) {
        return startTime.before(interval.getEndTime()) && endTime.after(interval.getStartTime());
    }

}

class Participant extends Notifier {
    public Participant(String name) {
        super(name);
    }

    public String getName() {
        return super.getName();
    }
}



class Meeting {
    private static int meetingCodeCounter = 1000;

    private int meetingCode;
    private String title;
    private int location;
    private TimeInterval interval;
    private Participant organizer;
    private List<Participant> participants;
    private static List<Meeting> meetings = new ArrayList<>();

    //creating the meeting with no participants
    public Meeting(String title, int location, TimeInterval interval, Participant organizer) {
        this.meetingCode = meetingCodeCounter++;
        this.title = title;
        this.location = location;
        this.interval = interval;
        this.organizer = organizer;
        this.participants = new ArrayList<>();
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }

    public boolean removeParticipant(Participant p) {
        return participants.remove(p);
    }

    public int getMeetingCode() {
        return meetingCode;
    }

    public int getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public Participant getOrganizer() {
        return organizer;
    }

    public static void bookMeeting(Meeting meeting) {
        meetings.add(meeting);
    }

    public static boolean cancelMeeting(int meetingCode) {
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).getMeetingCode() == meetingCode) {
                meetings.remove(i);
                return true;
            }
        }
        return false;  // meeting not found
    }

    public static List<Meeting> getAllMeetings() {
        return new ArrayList<>(meetings);
    }

    public static boolean isAvailable(TimeInterval interval) {
    for (int i = 0; i < meetings.size(); i++) {
        Meeting meeting = meetings.get(i);
        if (meeting.getInterval().clash(interval)) {
            return false;
        }
    }
        return true;
    }

    public static Meeting findMeetingByCode(int code) {
        for (Meeting m : meetings) {
            if (m.getMeetingCode() == code) return m;
        }
        return null;
    }

}

// want to make reminder service always run in the background
class ReminderService {
    private List<Meeting> meetings;

    public ReminderService(List<Meeting> meetings) {
        this.meetings = meetings;
    }

    public void sendReminders() {
        Calendar now = Calendar.getInstance();

        for (Meeting meeting : meetings) {
            Calendar startTime = meeting.getInterval().getStartTime();

            long millisDiff = startTime.getTimeInMillis() - now.getTimeInMillis();
            long hoursDiff = millisDiff / (1000 * 60 * 60); // convert ms to hours

            if (hoursDiff == 24 || hoursDiff == 2) {
                for (Participant p : meeting.getParticipants()) {
                    p.notify("Reminder: Meeting " + meeting.getMeetingCode() + " starts in " + hoursDiff + " hours.");
                }
            }
        }
    }
}

public class Scheduler {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Meeting> allMeetings = Meeting.getAllMeetings();
        ReminderService reminderService = new ReminderService(allMeetings);

        // Start background reminder service ??

        while (true) {
            System.out.println("\n--- Scheduler Menu ---");
            System.out.println("1. Create a new meeting");
            System.out.println("2. Add participant to a meeting");
            System.out.println("3. Remove participant from a meeting");
            System.out.println("4. Cancel a meeting");
            System.out.println("5. View all meetings");
            System.out.println("6. Check availability for a time interval");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: {
                    System.out.print("Enter meeting title: ");
                    String title = scanner.nextLine();

                    System.out.print("Enter location: ");
                    int location = scanner.nextInt();
                    scanner.nextLine();

                    System.out.print("Enter organizer name: ");
                    String organizerName = scanner.nextLine();
                    Participant organizer = new Participant(organizerName);

                    Calendar start = readCalendar(scanner, "start");
                    Calendar end = readCalendar(scanner, "end");

                    TimeInterval interval = new TimeInterval(start, end);
                    if (Meeting.isAvailable(interval)) {
                        Meeting m = new Meeting(title, location, interval, organizer);
                        Meeting.bookMeeting(m);
                        System.out.println("Meeting created with code: " + m.getMeetingCode());
                    } else {
                        System.out.println("Time slot not available.");
                    }
                    break;
                }
                case 2: {
                    System.out.print("Enter meeting code: ");
                    int code = scanner.nextInt();
                    scanner.nextLine();
                    Meeting m = Meeting.findMeetingByCode(code);
                    if (m == null) {
                        System.out.println("Meeting not found.");
                        break;
                    }

                    System.out.print("Enter participant name: ");
                    String name = scanner.nextLine();
                    Participant p = new Participant(name);
                    m.addParticipant(p);
                    System.out.println("Participant added.");
                    break;
                }
                case 3: {
                    System.out.print("Enter meeting code: ");
                    int code = scanner.nextInt();
                    scanner.nextLine();

                    Meeting m = Meeting.findMeetingByCode(code);
                    if (m == null) {
                        System.out.println("Meeting not found.");
                        break;
                    }

                    System.out.print("Enter participant name to remove: ");
                    String name = scanner.nextLine();
                    Participant toRemove = null;
                    for (Participant p : m.getParticipants()) {
                        if (p.getName().equals(name)) {
                            toRemove = p;
                            break;
                        }
                    }

                    if (toRemove != null && m.removeParticipant(toRemove)) {
                        System.out.println("Participant removed.");
                    } else {
                        System.out.println("Participant not found.");
                    }
                    break;
                }
                case 4: {
                    System.out.print("Enter meeting code to cancel: ");
                    int code = scanner.nextInt();
                    scanner.nextLine();
                    if (Meeting.cancelMeeting(code)) {
                        System.out.println("Meeting canceled.");
                    } else {
                        System.out.println("Meeting not found.");
                    }
                    break;
                }
                case 5: {
                    List<Meeting> meetings = Meeting.getAllMeetings();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    if (meetings.isEmpty()) {
                        System.out.println("No meetings scheduled.");
                    } else {
                        for (Meeting m : meetings) {
                        String start = sdf.format(m.getInterval().getStartTime().getTime());
                        String end = sdf.format(m.getInterval().getEndTime().getTime());
                        System.out.println("Code: " + m.getMeetingCode()
                            + ", Title: " + m.getTitle()
                            + ", Organizer: " + m.getOrganizer().getName()
                            + ", Location: " + m.getLocation()
                            + ", Start: " + start
                            + ", End: " + end
                            + ", Participants: " + m.getParticipants().size());
                            
                        }
                    }
                    break;
                }
                case 6: {
                    Calendar start = readCalendar(scanner, "start");
                    Calendar end = readCalendar(scanner, "end");
                    TimeInterval interval = new TimeInterval(start, end);

                    if (Meeting.isAvailable(interval)) {
                        System.out.println("The time slot is available.");
                    } else {
                        System.out.println("The time slot is NOT available.");
                    }
                    break;
                }
                case 7: {
                    System.out.println("Exiting...");
                    return;
                }
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static Calendar readCalendar(Scanner scanner, String label) {
        Calendar c = Calendar.getInstance();
        System.out.println("Enter " + label + " time:");
        System.out.print("Year: ");
        c.set(Calendar.YEAR, scanner.nextInt());
        System.out.print("Month (0-11): ");
        c.set(Calendar.MONTH, scanner.nextInt());
        System.out.print("Day: ");
        c.set(Calendar.DAY_OF_MONTH, scanner.nextInt());
        System.out.print("Hour (0-23): ");
        c.set(Calendar.HOUR_OF_DAY, scanner.nextInt());
        System.out.print("Minute: ");
        c.set(Calendar.MINUTE, scanner.nextInt());
        scanner.nextLine(); // consume newline
        return c;
    }
}

