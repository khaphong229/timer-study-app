# Setup Instructions

## Required Configuration Files

This project requires two configuration files that contain sensitive credentials. These files are **NOT** included in the repository for security reasons.

### 1. Firebase Configuration (`google-services.json`)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project or create a new one
3. Go to Project Settings > General
4. Under "Your apps", download `google-services.json`
5. Place the file at: `app/google-services.json`

**Template**: See `app/google-services.json.example` for the structure.

### 2. Facebook SDK Configuration (`local.properties`)

1. Open `local.properties` in the project root
2. Add your Facebook credentials:

```properties
# Facebook SDK Configuration
facebook.app_id=YOUR_FACEBOOK_APP_ID
facebook.client_token=YOUR_FACEBOOK_CLIENT_TOKEN
```

**Get your credentials from**: [Facebook Developers](https://developers.facebook.com/apps/)

**Template**: See `local.properties.example` for the full structure.

## Build the Project

After setting up both configuration files:

1. Open the project in Android Studio
2. Sync Gradle files
3. Build > Rebuild Project
4. Run the app

## Security Notes

- **NEVER** commit `local.properties` or `google-services.json` to version control
- Both files are already in `.gitignore`
- Always use the `.example` template files as reference
