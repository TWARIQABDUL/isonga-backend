package com.isonga.api.services;

import com.isonga.api.models.Activity;
import com.isonga.api.repositories.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<Activity> getUserActivities(String userIdNumber) {
        return activityRepository.findByUserIdNumber(userIdNumber);
    }

    public Activity saveActivity(Activity activity) {
        return activityRepository.save(activity);
    }
}
