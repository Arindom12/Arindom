import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Student {
    List<String> courses;

    public Student(List<String> courses) {
        this.courses = courses;
    }

    public List<String> getCourses() {
        return courses;
    }

    @Override
    public String toString() {
        return "Student{" + "courses=" + courses + '}';
    }
}

public class Main {

    public static void main(String[] args) {
        List<Student> students = readCSVFile("C:\\Users\\bordo\\Downloads\\StudentData.csv");
        System.out.println("Students and their courses:");
        for (Student student : students) {
            System.out.println(student);
        }

        Map<Integer, List<String>> scheduleByDay = generateExamSchedule(students);

        System.out.println("\nExam Schedule:");
        for (Map.Entry<Integer, List<String>> entry : scheduleByDay.entrySet()) {
            System.out.println("Day " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
        }

        writeScheduleToFile(scheduleByDay, "C:\\Users\\bordo\\Downloads\\StudentData.txt");
    }

    public static List<Student> readCSVFile(String filePath) {
        List<Student> students = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<Integer> courseColumnIndices = new ArrayList<>();

            // Read the header row to find "Course Code" columns
            if ((line = br.readLine()) != null) {
                String[] headers = line.split(",");
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].contains("Course Code")) {
                        courseColumnIndices.add(i);
                    }
                }
            }

            // Read the student rows
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<String> courses = new ArrayList<>();
                for (int columnIndex : courseColumnIndices) {
                    if (columnIndex < values.length && !values[columnIndex].isEmpty()) {
                        courses.add(values[columnIndex]);
                    }
                }
                students.add(new Student(courses));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static Map<Integer, List<String>> generateExamSchedule(List<Student> students) {
        Map<String, Set<String>> courseConflicts = new HashMap<>();
        Map<Student, Set<Integer>> studentExamDays = new HashMap<>();

        // Initialize course conflicts map
        for (Student student : students) {
            List<String> courses = student.getCourses();
            for (String course : courses) {
                courseConflicts.putIfAbsent(course, new HashSet<>());
            }
        }

        // Determine course conflicts
        for (Student student : students) {
            List<String> courses = student.getCourses();
            for (int i = 0; i < courses.size(); i++) {
                for (int j = i + 1; j < courses.size(); j++) {
                    courseConflicts.get(courses.get(i)).add(courses.get(j));
                    courseConflicts.get(courses.get(j)).add(courses.get(i));
                }
            }
        }

        // Schedule exams
        Map<String, Integer> examSchedule = new HashMap<>();
        int day = 1;

        while (!courseConflicts.isEmpty()) {
            Set<String> scheduledCourses = new HashSet<>();
            for (String course : courseConflicts.keySet()) {
                if (examSchedule.containsKey(course)) {
                    continue;
                }
                boolean canBeScheduled = true;
                for (String conflict : courseConflicts.get(course)) {
                    if (scheduledCourses.contains(conflict)) {
                        canBeScheduled = false;
                        break;
                    }
                }
                if (canBeScheduled) {
                    boolean studentConflict = false;
                    for (Student student : students) {
                        if (student.getCourses().contains(course)) {
                            if (!studentExamDays.containsKey(student)) {
                                studentExamDays.put(student, new HashSet<>());
                            }
                            if (studentExamDays.get(student).contains(day)) {
                                studentConflict = true;
                                break;
                            }
                        }
                    }
                    if (!studentConflict) {
                        examSchedule.put(course, day);
                        scheduledCourses.add(course);
                        for (Student student : students) {
                            if (student.getCourses().contains(course)) {
                                studentExamDays.get(student).add(day);
                            }
                        }
                    }
                }
            }
            for (String course : scheduledCourses) {
                courseConflicts.remove(course);
            }
            day++;
        }

        // Group schedule by day
        Map<Integer, List<String>> scheduleByDay = new HashMap<>();
        for (Map.Entry<String, Integer> entry : examSchedule.entrySet()) {
            scheduleByDay.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        return scheduleByDay;
    }

    public static void writeScheduleToFile(Map<Integer, List<String>> scheduleByDay, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Map.Entry<Integer, List<String>> entry : scheduleByDay.entrySet()) {
                writer.write("Day " + entry.getKey() + ": " + String.join(", ", entry.getValue()) + "\n");
            }
            System.out.println("\nSchedule has been written to the file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
