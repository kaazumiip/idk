package com.example.schedule_application.model;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recommendations {

    public static String generate(List<Task> taskList) {
        Map<String, Integer> categoryCount = new HashMap<>();
        int incompleteTasks = 0;

        long now = System.currentTimeMillis();
        int morningTasks = 0, eveningTasks = 0;

        for (Task task : taskList) {
            // Count by category
            String category = task.getCategory() != null ? task.getCategory().toLowerCase() : "uncategorized";
            int count = categoryCount.containsKey(category) ? categoryCount.get(category) : 0;
            categoryCount.put(category, count + 1);

            // Completion status (if implemented)
            if (!task.isCompleted()) {
                incompleteTasks++;
            }

            // Count based on task time (e.g., "14:30")
            int hour = parseHourFromTime(task.getTime());
            if (hour != -1) {
                if (hour < 12) morningTasks++;
                else if (hour >= 17) eveningTasks++;
            }
        }

        // Recommendation logic
        if (incompleteTasks >= 3) {
            return "You’ve got a few tasks pending. Try finishing one now.";
        }

        if (categoryCount.containsKey("work") && categoryCount.get("work") > 5) {
            return "Been working hard lately — maybe schedule a break or a walk?";
        }

        if (categoryCount.containsKey("relax") && categoryCount.get("relax") < 2) {
            return "Don’t forget to schedule some relaxation!";
        }

        if (morningTasks == 0 && isMorning(now)) {
            return "You’re usually more productive in the morning. Let’s plan something!";
        }

        return "You're doing great! Ready to plan your next task?";
    }

    private static int parseHourFromTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) return -1;
        try {
            String[] parts = timeString.split(":");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    private static boolean isMorning(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= 6 && hour < 12;
    }
}
