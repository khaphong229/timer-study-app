package com.example.timerstudy.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private long userId;
    private String name;
    private int age;
    private String email;
    private String bio;
    private String profileImageUrl;

    private boolean isLoggedIn;
    private String loginProvider;
    private long lastLoginTime;

    private int totalCoins;
    private int totalStudyMinutes;
    private int totalSessions;
    private int currentStreak;
    private int longestStreak;
    private long lastStudyDate;

    private int selectedBackgroundId;
    private int studyDuration;
    private int breakDuration;
    private boolean soundEnabled;
    private boolean vibratorEnabled;

    // Thêm field accessToken và refreshToken
    private String accessToken;
    private String refreshToken;
    private long tokenExpiresAt; // Timestamp khi token hết hạn

    // Thêm field cho danh sách bạn bè
    private List<String> friendIds;
    private int friendsCount;

    // Constructor
    public User(long userId, String name, int age, String email, String bio, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;

        this.isLoggedIn = false;
        this.loginProvider = "";
        this.lastLoginTime = 0;
        this.totalCoins = 0;
        this.totalStudyMinutes = 0;
        this.totalSessions = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastStudyDate = 0;
        this.selectedBackgroundId = -1;
        this.studyDuration = 25;
        this.breakDuration = 5;
        this.soundEnabled = true;
        this.vibratorEnabled = true;
        this.accessToken = "";
        this.refreshToken = "";
        this.tokenExpiresAt = 0;
        this.friendIds = new ArrayList<>();
        this.friendsCount = 0;
    }

    public User() {
        this.userId = (int) System.currentTimeMillis();
        this.name = "Guest User";
        this.email = "";
        this.profileImageUrl = "";
        this.bio = "";
        this.age = 0;
        this.isLoggedIn = false;
        this.loginProvider = "guest";
        this.totalCoins = 0;
        this.studyDuration = 25;
        this.breakDuration = 5;
        this.soundEnabled = true;
        this.vibratorEnabled = true;
        this.accessToken = "";
        this.refreshToken = "";
        this.tokenExpiresAt = 0;
        this.friendIds = new ArrayList<>();
        this.friendsCount = 0;
    }

    public static User fromFacebookLogin(int facebookId, String name, String email, String profileImageUrl) {
        User user = new User();
        user.userId = facebookId;
        user.name = name;
        user.email = email;
        user.profileImageUrl = profileImageUrl;
        user.isLoggedIn = true;
        user.loginProvider = "facebook";
        user.lastLoginTime = System.currentTimeMillis();
        return user;
    }

    // Getters
    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getLoginProvider() {
        return loginProvider;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public int getTotalCoins() {
        return totalCoins;
    }

    public int getTotalStudyMinutes() {
        return totalStudyMinutes;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public long getLastStudyDate() {
        return lastStudyDate;
    }

    public int getSelectedBackgroundId() {
        return selectedBackgroundId;
    }

    public int getStudyDuration() {
        return studyDuration;
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public boolean isVibratorEnabled() {
        return vibratorEnabled;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(long tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    /**
     * Set tokens với thông tin đầy đủ
     */
    public void setTokens(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        // expiresIn là số giây, convert sang milliseconds và cộng với thời gian hiện
        // tại
        this.tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000);

        // Debug log
        android.util.Log.d("User", "=== SET TOKENS ===");
        android.util.Log.d("User", "Access Token Set: " + (accessToken != null && !accessToken.isEmpty()));
        android.util.Log.d("User", "Access Token Length: " + (accessToken != null ? accessToken.length() : 0));
        android.util.Log.d("User", "Refresh Token Set: " + (refreshToken != null && !refreshToken.isEmpty()));
        android.util.Log.d("User", "Expires In (seconds): " + expiresIn);
        android.util.Log.d("User", "Token Expires At: " + this.tokenExpiresAt);
        android.util.Log.d("User", "Current Time: " + System.currentTimeMillis());
        android.util.Log.d("User",
                "Time Until Expiry (minutes): " + ((this.tokenExpiresAt - System.currentTimeMillis()) / 1000 / 60));
        android.util.Log.d("User", "==================");
    }

    /**
     * Kiểm tra token có hết hạn không
     * Thêm buffer 60 giây để tránh edge case
     */
    public boolean isTokenExpired() {
        if (tokenExpiresAt == 0) {
            android.util.Log.d("User", "Token expired check: tokenExpiresAt is 0");
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long bufferTime = 60 * 1000; // 60 seconds buffer
        boolean expired = currentTime >= (tokenExpiresAt - bufferTime);

        if (expired) {
            android.util.Log.d("User", "Token is EXPIRED");
            android.util.Log.d("User", "Current Time: " + currentTime);
            android.util.Log.d("User", "Expires At: " + tokenExpiresAt);
            android.util.Log.d("User", "Difference (seconds): " + ((tokenExpiresAt - currentTime) / 1000));
        }

        return expired;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
        this.friendsCount = friendIds != null ? friendIds.size() : 0;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    // Setters
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public void setLoginProvider(String loginProvider) {
        this.loginProvider = loginProvider;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setTotalCoins(int totalCoins) {
        this.totalCoins = totalCoins;
    }

    public void setTotalStudyMinutes(int totalStudyMinutes) {
        this.totalStudyMinutes = totalStudyMinutes;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public void setLastStudyDate(long lastStudyDate) {
        this.lastStudyDate = lastStudyDate;
    }

    public void setSelectedBackgroundId(int selectedBackgroundId) {
        this.selectedBackgroundId = selectedBackgroundId;
    }

    public void setStudyDuration(int studyDuration) {
        this.studyDuration = studyDuration;
    }

    public void setBreakDuration(int breakDuration) {
        this.breakDuration = breakDuration;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public void setVibratorEnabled(boolean vibratorEnabled) {
        this.vibratorEnabled = vibratorEnabled;
    }

    // Other methods
    public void addCoins(int coins) {
        this.totalCoins += coins;
    }

    public boolean spendCoins(int coins) {
        if (this.totalCoins >= coins) {
            this.totalCoins -= coins;
            return true;
        }
        return false;
    }

    public void updateStudyStats(int studyMinutes) {
        this.totalStudyMinutes += studyMinutes;
        this.totalSessions++;

        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        long lastStudy = this.lastStudyDate / (1000 * 60 * 60 * 24);

        if (today - lastStudy == 1) {
            this.currentStreak++;
            if (this.currentStreak > this.longestStreak) {
                this.longestStreak = this.currentStreak;
            }
        } else if (today - lastStudy > 1) {
            this.currentStreak = 1;
        }

        this.lastStudyDate = System.currentTimeMillis();
    }

    public boolean isGuest() {
        return !isLoggedIn || loginProvider.equals("guest");
    }

    public void logout() {
        this.isLoggedIn = false;
        this.loginProvider = "guest";
        this.profileImageUrl = "";
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", totalCoins=" + totalCoins +
                ", totalStudyMinutes=" + totalStudyMinutes +
                ", currentStreak=" + currentStreak +
                '}';
    }
}
